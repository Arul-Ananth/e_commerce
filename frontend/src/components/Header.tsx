import React from "react";
import { AppBar, Toolbar, Typography, Button, IconButton, Badge, Box } from "@mui/material";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import MenuIcon from "@mui/icons-material/Menu";
import { useNavigate } from "react-router-dom";
import { useCart } from "../global_component/CartContext";
import { useAuth } from "../global_component/AuthContext";

export default function Header({ onMenuClick }) {
    const navigate = useNavigate();
    const { cartCount } = useCart();
    const { isAuthenticated, logout, user } = useAuth(); // Destructure 'user'

    const handleLoginLogout = () => {
        if (isAuthenticated) {
            logout();
            navigate("/");
        } else {
            navigate("/login");
        }
    };

    return (
        <AppBar position="static">
            <Toolbar>
                <IconButton color="inherit" edge="start" sx={{ mr: 1 }} onClick={onMenuClick}>
                    <MenuIcon />
                </IconButton>

                <Typography
                    variant="h6"
                    sx={{ flexGrow: 1, cursor: "pointer" }}
                    onClick={() => navigate("/")}
                >
                    E-Commerce
                </Typography>

                <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>

                    {/* Show Hello Username if logged in */}
                    {isAuthenticated && user && (
                        <Typography variant="body2" sx={{ mr: 1, display: { xs: 'none', sm: 'block' } }}>
                            Hello, {user.username || user.email}
                        </Typography>
                    )}

                    <IconButton color="inherit" onClick={() => navigate("/cart")}>
                        <Badge badgeContent={cartCount} color="error">
                            <ShoppingCartIcon />
                        </Badge>
                    </IconButton>

                    <Button color="inherit" onClick={handleLoginLogout}>
                        {isAuthenticated ? "Logout" : "Login"}
                    </Button>
                </Box>
            </Toolbar>
        </AppBar>
    );
}