import React, { useMemo, useState } from "react";
import {
    Box,
    Button,
    Card,
    CardContent,
    Divider,
    IconButton,
    List,
    ListItem,
    ListItemAvatar,
    Avatar,
    ListItemText,
    Stack,
    Typography,
    Alert,
    CircularProgress,
    Toolbar,
    Container,
    Chip // Added Chip for the badge
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import RemoveIcon from "@mui/icons-material/Remove";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import LocalOfferIcon from '@mui/icons-material/LocalOffer'; // Added Icon
import { useNavigate } from "react-router-dom";
import { useCart } from "../global_component/CartContext.jsx";
import { useAuth } from "../global_component/AuthContext.jsx";
import ApiService from "../api/ApiService.jsx";

const currency = new Intl.NumberFormat(undefined, {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 2,
});

export default function Buy() {
    const navigate = useNavigate();
    const { cartItems, updateQuantity, removeFromCart, clear, loading } = useCart();
    const { isAuthenticated } = useAuth();

    const [placing, setPlacing] = useState(false);
    const [error, setError] = useState("");

    // Helper: Calculate Price for a single item (backend already applies discounts)
    const getItemPrice = (item) => Number(item.finalPrice ?? item.price ?? 0);

    // Update Subtotal to use Discounted Prices
    const subtotal = useMemo(
        () =>
            cartItems.reduce((sum, item) => {
                const finalPrice = getItemPrice(item); // Use helper
                const qty = Number(item.quantity) || 0;
                return sum + finalPrice * qty;
            }, 0),
        [cartItems]
    );

    // Calculate Savings for display
    const totalSavings = useMemo(
        () =>
            cartItems.reduce((sum, item) => {
                const originalPrice = Number(item.price) || 0;
                const finalPrice = getItemPrice(item);
                const qty = Number(item.quantity) || 0;
                return sum + (originalPrice - finalPrice) * qty;
            }, 0),
        [cartItems]
    );

    const handlePlaceOrder = async () => {
        setError("");

        if (!isAuthenticated) {
            navigate(`/login?redirect=${encodeURIComponent("/checkout")}`);
            return;
        }

        if (!cartItems.length) {
            setError("Your cart is empty.");
            return;
        }

        setPlacing(true);
        try {
            const res = await ApiService.startCheckout();

            if (res?.redirectUrl) {
                window.location.href = res.redirectUrl;
                return;
            }

            await clear();
            navigate("/", { replace: true });
        } catch (e) {
            setError("Failed to start checkout. Please try again.");
        } finally {
            setPlacing(false);
        }
    };

    const handleQtyDecrease = (id, qty) => {
        const next = Math.max(1, (qty || 1) - 1);
        updateQuantity(id, next);
    };

    const handleQtyIncrease = (id, qty) => {
        const next = (qty || 0) + 1;
        updateQuantity(id, next);
    };

    if (loading) {
        return (
            <Box sx={{ p: 3, display: "flex", alignItems: "center", justifyContent: "center" }}>
                <CircularProgress />
            </Box>
        );
    }

    if (!cartItems.length) {
        return (
            <Box sx={{ p: 3 }}>
                <Typography variant="h5" sx={{ mb: 1 }}>
                    Your cart is empty
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    Add items to your cart and proceed to checkout.
                </Typography>
                <Button variant="contained" onClick={() => navigate("/")}>
                    Continue Shopping
                </Button>
            </Box>
        );
    }

    return (
        <Box sx={{ minHeight: "100vh", display: "flex", flexDirection: "column", bgcolor: "background.default", width: "100vw" }}>
            <Toolbar />
            <Container maxWidth={false} sx={{ flex: 1, py: 2, px: { xs: 2, sm: 3 } }}>
                <Typography variant="h5" sx={{ mb: 2 }}>
                    Checkout
                </Typography>

                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

                <Stack direction={{ xs: "column", md: "row" }} spacing={2} sx={{ width: "100%" }}>

                    {/* Left: Items */}
                    <Card sx={{ flex: 2, minWidth: 0 }}>
                        <CardContent>
                            <Typography variant="h6" sx={{ mb: 1.5 }}>
                                Items ({cartItems.length})
                            </Typography>
                            <Divider sx={{ mb: 1.5 }} />

                            <List disablePadding>
                                {cartItems.map((item) => {
                                    // Calculate Display Values
                                    const productDiscount = item.productDiscount;
                                    const finalPrice = getItemPrice(item);
                                    const hasDiscount = Number(item.totalDiscountPercentage || 0) > 0;

                                    return (
                                        <React.Fragment key={item.id}>
                                            <ListItem
                                                secondaryAction={
                                                    <Stack direction="row" alignItems="center" spacing={0.5}>
                                                        <IconButton size="small" onClick={() => handleQtyDecrease(item.id, item.quantity)}>
                                                            <RemoveIcon fontSize="small" />
                                                        </IconButton>
                                                        <Typography variant="body2" sx={{ width: 28, textAlign: "center" }}>
                                                            {item.quantity}
                                                        </Typography>
                                                        <IconButton size="small" onClick={() => handleQtyIncrease(item.id, item.quantity)}>
                                                            <AddIcon fontSize="small" />
                                                        </IconButton>
                                                        <IconButton color="error" size="small" onClick={() => removeFromCart(item.id)}>
                                                            <DeleteOutlineIcon fontSize="small" />
                                                        </IconButton>
                                                    </Stack>
                                                }
                                            >
                                                <ListItemAvatar>
                                                    <Avatar variant="rounded" src={item.imageUrl} alt={item.title} sx={{ width: 56, height: 56 }} />
                                                </ListItemAvatar>
                                                <ListItemText
                                                    primary={
                                                        <Typography variant="subtitle1" noWrap title={item.title}>
                                                            {item.title}
                                                        </Typography>
                                                    }
                                                    secondary={
                                                        <Box sx={{ mt: 0.5 }}>
                                                            {/* Price Display Logic */}
                                                            <Stack direction="row" alignItems="center" spacing={1}>
                                                            {hasDiscount && (
                                                                <Typography variant="caption" sx={{ textDecoration: 'line-through', color: 'text.secondary' }}>
                                                                    {currency.format(Number(item.price))}
                                                                </Typography>
                                                            )}
                                                            <Typography variant="body2" fontWeight={hasDiscount ? "bold" : "normal"} color={hasDiscount ? "success.main" : "text.primary"}>
                                                                    {currency.format(finalPrice)} x {item.quantity}
                                                                </Typography>
                                                            </Stack>

                                                            {/* Green Discount Badge */}
                                                            {hasDiscount && (
                                                                <Stack direction="row" spacing={0.5} sx={{ mt: 0.5, flexWrap: "wrap" }}>
                                                                    {productDiscount && (
                                                                        <Chip
                                                                            icon={<LocalOfferIcon fontSize="small" style={{ color: 'inherit' }} />}
                                                                            label={`${productDiscount.description || 'Deal'} (-${productDiscount.percentage}%)`}
                                                                            size="small"
                                                                            color="success"
                                                                            variant="outlined"
                                                                            sx={{ height: 20, fontSize: '0.7rem' }}
                                                                        />
                                                                    )}
                                                                    {Number(item.userDiscountPercentage || 0) > 0 && (
                                                                        <Chip
                                                                            label={`User (-${item.userDiscountPercentage}%)`}
                                                                            size="small"
                                                                            color="success"
                                                                            variant="outlined"
                                                                            sx={{ height: 20, fontSize: '0.7rem' }}
                                                                        />
                                                                    )}
                                                                    {Number(item.employeeDiscountPercentage || 0) > 0 && (
                                                                        <Chip
                                                                            label={`Employee (-${item.employeeDiscountPercentage}%)`}
                                                                            size="small"
                                                                            color="success"
                                                                            variant="outlined"
                                                                            sx={{ height: 20, fontSize: '0.7rem' }}
                                                                        />
                                                                    )}
                                                                </Stack>
                                                            )}
                                                        </Box>
                                                    }
                                                />
                                            </ListItem>
                                            <Divider component="li" />
                                        </React.Fragment>
                                    );
                                })}
                            </List>
                        </CardContent>
                    </Card>

                    {/* Right: Summary */}
                    <Card sx={{ flex: 1, minWidth: 280, height: 'fit-content' }}>
                        <CardContent>
                            <Typography variant="h6" sx={{ mb: 1 }}>
                                Order Summary
                            </Typography>
                            <Divider sx={{ mb: 2 }} />

                            <Stack spacing={1.2}>
                                <Row label="Subtotal" value={currency.format(subtotal)} />

                                {totalSavings > 0 && (
                                    <Row
                                        label={<Typography color="success.main" variant="body2">Total Savings</Typography>}
                                        value={<Typography color="success.main" variant="body2">-{currency.format(totalSavings)}</Typography>}
                                    />
                                )}

                                <Row label="Shipping" value="Free" />
                                <Divider />
                                <Row
                                    label={<Typography variant="subtitle1">Total</Typography>}
                                    value={<Typography variant="subtitle1" color="primary">{currency.format(subtotal)}</Typography>}
                                />
                            </Stack>

                            <Button
                                variant="contained"
                                color="primary"
                                fullWidth
                                sx={{ mt: 2 }}
                                onClick={handlePlaceOrder}
                                disabled={placing || !cartItems.length}
                                startIcon={placing ? <CircularProgress size={18} color="inherit" /> : null}
                            >
                                {placing ? "Processing..." : "Place Order"}
                            </Button>

                            <Button variant="text" fullWidth sx={{ mt: 1 }} onClick={() => navigate("/")}>
                                Continue Shopping
                            </Button>
                        </CardContent>
                    </Card>
                </Stack>
            </Container>
        </Box>
    );
}

function Row({ label, value }) {
    return (
        <Stack direction="row" alignItems="center" justifyContent="space-between">
            {typeof label === "string" || typeof label === "number" ? (
                <Typography variant="body2" color="text.secondary">{label}</Typography>
            ) : (label)}

            {typeof value === "string" || typeof value === "number" ? (
                <Typography variant="body2">{value}</Typography>
            ) : (value)}
        </Stack>
    );
}
