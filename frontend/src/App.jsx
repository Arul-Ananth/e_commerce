import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from "react-router-dom";
import Header from "./components/Header";
import MainPage from "./pages/MainPage";

import SignUp from "./pages/SignUp.jsx";
import ProductDetails from "./pages/ProductDetails";
import Buy from "./pages/Buy.jsx";
import Login from "./pages/Login.jsx";

// --- Admin & Manager Components ---
import AddProduct from "./pages/admin/AddProduct";
import AddManager from "./pages/admin/AddManager";
import ManageUsers from "./pages/admin/ManageUsers";

// --- Route Wrappers ---
import AdminRoute from "./global_component/AdminRoute";
import ManagerRoute from "./global_component/ManagerRoute";

// --- Contexts ---
import { AuthProvider, useAuth } from "./global_component/AuthContext";
import { CartProvider } from "./global_component/CartContext";

// Existing ProtectedRoute for normal logged-in users (like Checkout)
function ProtectedRoute({ children }) {
    const { isAuthenticated } = useAuth();
    const location = useLocation();
    if (!isAuthenticated) {
        const redirect = encodeURIComponent(location.pathname + location.search);
        return <Navigate to={`/login?redirect=${redirect}`} replace />;
    }
    return children;
}

export default function App() {
    const [drawerOpen, setDrawerOpen] = React.useState(false);
    const toggleDrawer = () => setDrawerOpen((p) => !p);

    return (
        <AuthProvider>
            <CartProvider>
                <Router>
                    <Header onMenuClick={toggleDrawer} />

                    <Routes>
                        {/* --- Public Routes --- */}
                        <Route path="/" element={<MainPage drawerOpen={drawerOpen} toggleDrawer={toggleDrawer} />} />
                        <Route path="/product/:id" element={<ProductDetails />} />
                        <Route path="/login" element={<Login />} />
                        <Route path="/signup" element={<SignUp />} />

                        {/* --- ADMIN ONLY ROUTES --- */}
                        {/* Only ROLE_ADMIN can access these */}
                        <Route element={<AdminRoute />}>
                            <Route path="/admin/add-product" element={<AddProduct />} />
                            <Route path="/admin/add-manager" element={<AddManager />} />
                            <Route path="/admin/users" element={<ManageUsers />} />
                        </Route>

                        {/* --- MANAGER ROUTES --- */}
                        {/* ROLE_MANAGER (and usually ADMIN) can access these */}
                        <Route element={<ManagerRoute />}>
                            {/* Managers reuse the same Add Product page */}
                            <Route path="/manager/add-product" element={<AddProduct />} />
                            {/* Managers reuse the same User Management page (but with fewer buttons) */}
                            <Route path="/manager/users" element={<ManageUsers />} />
                        </Route>

                        {/* --- Protected User Routes --- */}
                        <Route
                            path="/checkout"
                            element={
                                <ProtectedRoute>
                                    <Buy />
                                </ProtectedRoute>
                            }
                        />

                        {/* --- Catch All --- */}
                        <Route path="*" element={<Navigate to="/" replace />} />
                    </Routes>
                </Router>
            </CartProvider>
        </AuthProvider>
    );
}