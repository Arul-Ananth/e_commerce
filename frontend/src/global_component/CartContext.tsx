import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import type { PropsWithChildren } from "react";
import axios from "axios";
import ApiService from "../api/ApiService";
import { useAuth } from "./AuthContext";
import type { CartItem, Product } from "../types/models";

interface CartContextValue {
    cartItems: CartItem[];
    cartCount: number;
    loading: boolean;
    addToCart: (product: Product, qty?: number, discountId?: number | null) => Promise<void>;
    updateQuantity: (productId: number, quantity: number) => Promise<void>;
    removeFromCart: (productId: number) => Promise<void>;
    clear: () => Promise<void>;
    reload: () => Promise<void>;
}

const CartContext = createContext<CartContextValue | undefined>(undefined);

function toOptimisticCartItem(product: Product, qty: number): CartItem {
    return {
        id: product.id,
        productId: product.id,
        title: product.name,
        imageUrl: product.images?.[0],
        quantity: qty,
        price: Number(product.price) || 0,
    };
}

export function CartProvider({ children }: PropsWithChildren) {
    const { isAuthenticated, logout } = useAuth();
    const [cartItems, setCartItems] = useState<CartItem[]>([]);
    const [loading, setLoading] = useState(false);

    const loadCart = useCallback(async (): Promise<void> => {
        if (!isAuthenticated) {
            setCartItems([]);
            return;
        }

        setLoading(true);
        try {
            const items = await ApiService.getCart();
            setCartItems(items);
        } catch (err) {
            if (axios.isAxiosError(err) && (err.response?.status === 401 || err.response?.status === 403)) {
                logout();
                setCartItems([]);
                return;
            }
            console.error("Failed to load cart:", err);
        } finally {
            setLoading(false);
        }
    }, [isAuthenticated, logout]);

    useEffect(() => {
        loadCart().catch((e: unknown) => console.error("Cart init error:", e));
    }, [loadCart]);

    const addToCart = useCallback(
        async (product: Product, qty = 1, discountId: number | null = null): Promise<void> => {
            setCartItems((prev) => {
                const idx = prev.findIndex((p) => p.id === product.id);
                if (idx === -1) {
                    return [...prev, toOptimisticCartItem(product, qty)];
                }

                const updated = [...prev];
                updated[idx] = { ...updated[idx], quantity: updated[idx].quantity + qty };
                return updated;
            });

            try {
                const response = await ApiService.addOrUpdateCartItem(product.id, qty, discountId);
                setCartItems(response?.items ?? []);
            } catch (e) {
                console.error("Add to cart failed", e);
                await loadCart();
                throw e;
            }
        },
        [loadCart],
    );

    const updateQuantity = useCallback(
        async (productId: number, quantity: number): Promise<void> => {
            setCartItems((prev) => prev.map((p) => (p.id === productId ? { ...p, quantity } : p)));
            try {
                const response = await ApiService.updateCartItem(productId, quantity);
                setCartItems(response?.items ?? []);
            } catch (e) {
                console.error("Update quantity failed", e);
                await loadCart();
                throw e;
            }
        },
        [loadCart],
    );

    const removeFromCart = useCallback(
        async (productId: number): Promise<void> => {
            const prevSnapshot = cartItems;
            setCartItems((prev) => prev.filter((p) => p.id !== productId));
            try {
                const response = await ApiService.removeCartItem(productId);
                setCartItems(response?.items ?? []);
            } catch (e) {
                console.error("Remove item failed", e);
                setCartItems(prevSnapshot);
                throw e;
            }
        },
        [cartItems],
    );

    const clear = useCallback(async (): Promise<void> => {
        const prevSnapshot = cartItems;
        setCartItems([]);
        try {
            const response = await ApiService.clearCart();
            setCartItems(response?.items ?? []);
        } catch (e) {
            console.error("Clear cart failed", e);
            setCartItems(prevSnapshot);
            throw e;
        }
    }, [cartItems]);

    const cartCount = useMemo(
        () => cartItems.reduce((acc, item) => acc + (item.quantity || 0), 0),
        [cartItems],
    );

    const value = useMemo<CartContextValue>(
        () => ({
            cartItems,
            cartCount,
            loading,
            addToCart,
            updateQuantity,
            removeFromCart,
            clear,
            reload: loadCart,
        }),
        [cartItems, cartCount, loading, addToCart, updateQuantity, removeFromCart, clear, loadCart],
    );

    return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart(): CartContextValue {
    const context = useContext(CartContext);
    if (!context) {
        throw new Error("useCart must be used within CartProvider");
    }
    return context;
}
