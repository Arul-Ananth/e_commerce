import React, { createContext, useContext, useState, useEffect } from "react";
import ApiService from "../api/ApiService";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const initializeAuth = () => {
            const token = localStorage.getItem("token");
            const storedUser = localStorage.getItem("user");

            // Check if token exists AND storedUser is valid (not "undefined" string)
            if (token && storedUser && storedUser !== "undefined") {
                try {
                    const parsedUser = JSON.parse(storedUser);
                    ApiService.setAuthToken(token);
                    setIsAuthenticated(true);
                    setUser(parsedUser);
                } catch (error) {
                    console.error("Corrupted auth data found, clearing...", error);
                    // Auto-fix the issue by clearing bad data
                    localStorage.removeItem("token");
                    localStorage.removeItem("user");
                    ApiService.clearAuthToken();
                }
            } else {
                // If data is missing or explicitly "undefined", ensure we are clean
                ApiService.clearAuthToken();
            }
            setLoading(false);
        };

        initializeAuth();
    }, []);

    const login = (token, userData) => {
        localStorage.setItem("token", token);
        // Safety check: ensure userData is not undefined before saving
        if (userData) {
            localStorage.setItem("user", JSON.stringify(userData));
            ApiService.setAuthToken(token);
            setIsAuthenticated(true);
            setUser(userData);
        } else {
            console.error("Login failed: User data is undefined");
        }
    };

    const logout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        ApiService.clearAuthToken();
        setIsAuthenticated(false);
        setUser(null);
    };

    if (loading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', marginTop: '50px' }}>
                Loading Application...
            </div>
        );
    }

    return (
        <AuthContext.Provider value={{ isAuthenticated, user, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);