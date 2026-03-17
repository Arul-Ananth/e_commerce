import { useState } from "react";
import type { PropsWithChildren, ReactElement } from "react";
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from "react-router-dom";
import Header from "./components/Header";
import MainPage from "./pages/MainPage";
import SignUp from "./pages/SignUp";
import ProductDetails from "./pages/ProductDetails";
import Buy from "./pages/Buy";
import Login from "./pages/Login";
import AddProduct from "./pages/admin/AddProduct";
import AddManager from "./pages/admin/AddManager";
import ManageUsers from "./pages/admin/ManageUsers";
import AdminDashboard from "./pages/admin/AdminDashboard";
import ManagerDashboard from "./pages/admin/ManagerDashboard";
import AdminRoute from "./global_component/AdminRoute";
import ManagerRoute from "./global_component/ManagerRoute";
import { AuthProvider, useAuth } from "./global_component/AuthContext";
import { CartProvider } from "./global_component/CartContext";
import { buildLoginRedirectPath } from "./global_component/authUtils";

function ProtectedRoute({ children }: PropsWithChildren): ReactElement {
    const { isAuthenticated } = useAuth();
    const location = useLocation();

    if (!isAuthenticated) {
        const redirectTarget = `${location.pathname}${location.search}`;
        return <Navigate to={buildLoginRedirectPath(redirectTarget)} replace />;
    }

    return <>{children}</>;
}

export default function App() {
    const [drawerOpen, setDrawerOpen] = useState(false);

    const toggleDrawer = (nextState?: boolean) => {
        setDrawerOpen((prev) => (typeof nextState === "boolean" ? nextState : !prev));
    };

    return (
        <AuthProvider>
            <CartProvider>
                <Router>
                    <Header onMenuClick={toggleDrawer} />

                    <Routes>
                        <Route path="/" element={<MainPage drawerOpen={drawerOpen} toggleDrawer={toggleDrawer} />} />
                        <Route path="/product/:id" element={<ProductDetails />} />
                        <Route path="/login" element={<Login />} />
                        <Route path="/signup" element={<SignUp />} />

                        <Route element={<AdminRoute />}>
                            <Route path="/admin/dashboard" element={<AdminDashboard />} />
                            <Route path="/admin/add-product" element={<AddProduct />} />
                            <Route path="/admin/add-manager" element={<AddManager />} />
                            <Route path="/admin/users" element={<ManageUsers />} />
                        </Route>

                        <Route element={<ManagerRoute />}>
                            <Route path="/manager/dashboard" element={<ManagerDashboard />} />
                            <Route path="/manager/add-product" element={<AddProduct />} />
                            <Route path="/manager/users" element={<ManageUsers />} />
                        </Route>

                        <Route
                            path="/checkout"
                            element={
                                <ProtectedRoute>
                                    <Buy />
                                </ProtectedRoute>
                            }
                        />

                        <Route path="*" element={<Navigate to="/" replace />} />
                    </Routes>
                </Router>
            </CartProvider>
        </AuthProvider>
    );
}
