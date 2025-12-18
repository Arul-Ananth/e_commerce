import React, { useState } from "react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { useAuth } from "../global_component/AuthContext";
import ApiService from "../api/ApiService";
import {
    Box,
    Button,
    TextField,
    Typography,
    Paper,
    Alert,
    Container
} from "@mui/material";

export default function Login() {
    const navigate = useNavigate();
    const location = useLocation();
    const { login } = useAuth();

    const [formData, setFormData] = useState({ email: "", password: "" });
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    // Get the page the user was trying to visit (or default to home)
    const from = new URLSearchParams(location.search).get("redirect") || "/";

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setLoading(true);

        try {
            console.log("Attempting login with:", formData.email);

            // 1. Call API
            const response = await ApiService.login(formData);
            console.log("API Response:", response);

            // 2. Validate Response
            if (!response.token || !response.user) throw new Error("Invalid server response: Missing token or user data.");


            // 3. Update Auth Context
            // The backend returns { token: "...", user: {...} }
            login(response.token, response.user);

            // 4. Redirect
            // Decode the redirect target to handle special characters
            const redirectPath = decodeURIComponent(from);
            navigate(redirectPath, { replace: true });

        } catch (err) {
            console.error("Login Error:", err);
            // Check for specific backend error message or fallback
            const msg = err.response?.data?.message || "Invalid email or password";
            setError(msg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container component="main" maxWidth="xs">
            <Box
                sx={{
                    marginTop: 8,
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                }}
            >
                <Paper elevation={3} sx={{ p: 4, width: '100%' }}>
                    <Typography component="h1" variant="h5" align="center" gutterBottom>
                        Sign In
                    </Typography>

                    {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

                    <form onSubmit={handleSubmit}>
                        <TextField
                            margin="normal"
                            required
                            fullWidth
                            id="email"
                            label="Email Address"
                            name="email"
                            autoComplete="email"
                            autoFocus
                            value={formData.email}
                            onChange={handleChange}
                        />
                        <TextField
                            margin="normal"
                            required
                            fullWidth
                            name="password"
                            label="Password"
                            type="password"
                            id="password"
                            autoComplete="current-password"
                            value={formData.password}
                            onChange={handleChange}
                        />

                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            sx={{ mt: 3, mb: 2 }}
                            disabled={loading}
                        >
                            {loading ? "Signing in..." : "Sign In"}
                        </Button>

                        <Box display="flex" justifyContent="center">
                            <Link to="/signup" style={{ textDecoration: 'none', color: '#1976d2' }}>
                                {"Don't have an account? Sign Up"}
                            </Link>
                        </Box>
                    </form>
                </Paper>
            </Box>
        </Container>
    );
}