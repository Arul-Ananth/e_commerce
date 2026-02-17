import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';

const ManagerRoute = () => {
    const { isAuthenticated, user } = useAuth();
    if (!isAuthenticated) return <Navigate to="/login" replace />;


    const isManager = user?.roles?.includes('ROLE_MANAGER');
    const isAdmin = user?.roles?.includes('ROLE_ADMIN');

    if (!isManager && !isAdmin) return <Navigate to="/" replace />;

    return <Outlet />;
};

export default ManagerRoute;