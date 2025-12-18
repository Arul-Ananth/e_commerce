import axios from "axios";

// Update to match your Spring Boot "api/v1" standard
const API_BASE = 'http://localhost:8080/api/v1';
const API_BASE_AUTH = 'http://localhost:8080/auth';

// ---- Axios Instances ----
const api = axios.create({
    baseURL: API_BASE,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json'
    }
});

const authApi = axios.create({
    baseURL: API_BASE_AUTH,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json'
    }
});

// ---- 1. Public Data Fetchers (Using native fetch for simplicity in loaders) ----

export async function fetchCategories() {
    try {
        const res = await fetch(`${API_BASE}/categories`);
        if (!res.ok) throw new Error("Failed to fetch categories");
        const data = await res.json();
        // Return "All" plus the list from backend
        return ['All', ...data];
    } catch (err) {
        console.error('Error fetching categories:', err);
        return ['All']; // Fallback
    }
}

export async function fetchProducts(category) {
    try {
        // New Endpoint Logic
        const url = category === 'All' || !category
            ? `${API_BASE}/products`
            : `${API_BASE}/products/category/${category}`;

        const res = await fetch(url);
        if (!res.ok) throw new Error("Failed to fetch products");
        return await res.json();
    } catch (err) {
        console.error('Error fetching products:', err);
        return [];
    }
}

export async function fetchProduct(id) {
    try {
        const res = await fetch(`${API_BASE}/products/${id}`);
        if (!res.ok) throw new Error("Failed to fetch product");
        return await res.json();
    } catch (err) {
        console.error('Error fetching product:', err);
        return null;
    }
}

export async function fetchReviews(productId) {
    try {
        // New Nested Endpoint: /products/{id}/reviews
        const res = await fetch(`${API_BASE}/products/${productId}/reviews`);
        if (!res.ok) throw new Error("Failed to fetch reviews");
        return await res.json();
    } catch (err) {
        console.error('Error fetching reviews:', err);
        return [];
    }
}

// ---- 2. Auth Services ----

async function login({ email, password }) {
    const { data } = await authApi.post("/login", { email, password });
    return data;
}

async function signup({ email, password, username }) {
    const { data } = await authApi.post("/signup", { email, password, username });
    return data;
}

// Token Management
function setAuthToken(token) {
    if (token) {
        api.defaults.headers.common.Authorization = `Bearer ${token}`;
        authApi.defaults.headers.common.Authorization = `Bearer ${token}`;
    } else {
        delete api.defaults.headers.common.Authorization;
        delete authApi.defaults.headers.common.Authorization;
    }
}

function clearAuthToken() {
    delete api.defaults.headers.common.Authorization;
    delete authApi.defaults.headers.common.Authorization;
}

// ---- 3. Cart Services (Protected) ----

async function getCart() {
    // Endpoint: GET /api/v1/cart
    const { data } = await api.get("/cart");
    return data.items ?? [];
}

export async function addOrUpdateCartItem(productId, quantity) {
    // Endpoint: POST /api/v1/cart/items
    const { data } = await api.post("/cart/items", { productId, quantity });
    return data;
}

async function updateCartItem(productId, quantity) {
    // Endpoint: PATCH /api/v1/cart/items/{id}
    const { data } = await api.patch(`/cart/items/${productId}`, { quantity });
    return data;
}

async function removeCartItem(productId) {
    // Endpoint: DELETE /api/v1/cart/items/{id}
    const { data } = await api.delete(`/cart/items/${productId}`);
    return data;
}

async function clearCart() {
    // Endpoint: DELETE /api/v1/cart
    const { data } = await api.delete("/cart");
    return data;
}

async function startCheckout() {
    // Endpoint: POST /api/v1/checkout (Make sure to create this Controller in backend!)
    const { data } = await api.post("/checkout", {});
    return data;
}

// ---- 4. Admin Services (Protected: Role=ADMIN) ----

export async function addProduct(productData) {
    // Endpoint: POST /api/v1/products
    const { data } = await api.post("/products", productData);
    return data;
}

export async function updateProduct(id, productData) {
    // Endpoint: PUT /api/v1/products/{id}
    const { data } = await api.put(`/products/${id}`, productData);
    return data;
}

export async function deleteProduct(id) {
    // Endpoint: DELETE /api/v1/products/{id}
    const { data } = await api.delete(`/products/${id}`);
    return data;
}

// ---- 5. Review Services (User) ----

export async function addReview(productId, reviewData) {
    // Endpoint: POST /api/v1/products/{id}/reviews
    // reviewData should be { rating: 5, comment: "..." }
    const { data } = await api.post(`/products/${productId}/reviews`, reviewData);
    return data;
}



export async function getAllUsers() {
    const { data } = await api.get("/users");
    return data;
}

export async function flagUser(userId) {
    const { data } = await api.patch(`/users/${userId}/flag`);
    return data;
}

export async function unflagUser(userId) {
    const { data } = await api.patch(`/users/${userId}/unflag`);
    return data;
}

export async function deleteUser(userId) {
    await api.delete(`/users/${userId}`);
}

export async function registerManager(managerData) {
    // managerData = { email, password, username }
    const { data } = await api.post("/users/managers", managerData);
    return data;
}

// ... existing imports


export async function uploadImage(file) {
    const formData = new FormData();
    formData.append("file", file);

    const { data } = await api.post("/images/upload", formData, {
        headers: {
            "Content-Type": "multipart/form-data",
        },
    });
    return data.url; // Returns the http://localhost:8080/images/xyz.jpg string
}



// ---- Export Default ----
export default {
    api,
    authApi,
    setAuthToken,
    clearAuthToken,
    login,
    signup,
    getCart,
    addOrUpdateCartItem,
    updateCartItem,
    removeCartItem,
    clearCart,
    startCheckout,
    // Exporting these again for convenience
    fetchCategories,
    fetchProducts,
    fetchProduct,
    fetchReviews,
    addProduct,
    updateProduct,
    deleteProduct,
    addReview,
    getAllUsers,
    flagUser,
    unflagUser,
    deleteUser,
    registerManager,
    uploadImage,

};