import React, { useEffect, useMemo, useState } from "react";
import {
    Box,
    Typography,
    Paper,
    Grid,
    Button,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Stack,
    Alert,
    CircularProgress
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import { fetchProducts, deleteProduct } from "../../api/ApiService";

export default function AdminDashboard() {
    const navigate = useNavigate();
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const load = async () => {
            setLoading(true);
            setError("");
            try {
                const data = await fetchProducts("All");
                setProducts(Array.isArray(data) ? data : []);
            } catch (err) {
                console.error("Failed to load products", err);
                setError("Failed to load products.");
            } finally {
                setLoading(false);
            }
        };

        load();
    }, []);

    const handleRemove = async (id) => {
        if (!window.confirm("Remove this product? This cannot be undone.")) return;
        try {
            await deleteProduct(id);
            setProducts((prev) => prev.filter((p) => p.id !== id));
        } catch (err) {
            console.error("Failed to delete product", err);
            alert("Failed to remove product.");
        }
    };

    const rows = useMemo(() => {
        return products.map((p) => {
            const raw = p.description || "";
            const shortDescription = raw.length > 140 ? `${raw.slice(0, 140)}...` : raw;
            return {
                id: p.id,
                name: p.name || "Untitled",
                price: p.price ?? "-",
                shortDescription
            };
        });
    }, [products]);

    return (
        <Box sx={{ display: "flex", justifyContent: "center", p: 4 }}>
            <Paper elevation={3} sx={{ p: 4, width: "100%", maxWidth: 1100 }}>
                <Typography variant="h4" gutterBottom sx={{ fontWeight: "bold" }}>
                    Admin Dashboard
                </Typography>
                <Typography variant="body1" sx={{ mb: 3 }}>
                    Manage products, users, and staff access.
                </Typography>

                <Grid container spacing={2}>
                    <Grid item xs={12} sm={6} md={4}>
                        <Button
                            fullWidth
                            variant="contained"
                            onClick={() => navigate("/admin/add-product")}
                        >
                            Add Product
                        </Button>
                    </Grid>
                    <Grid item xs={12} sm={6} md={4}>
                        <Button
                            fullWidth
                            variant="contained"
                            onClick={() => navigate("/admin/add-manager")}
                        >
                            Add Manager
                        </Button>
                    </Grid>
                    <Grid item xs={12} sm={6} md={4}>
                        <Button
                            fullWidth
                            variant="contained"
                            onClick={() => navigate("/admin/users")}
                        >
                            Manage Users
                        </Button>
                    </Grid>
                </Grid>

                <Box sx={{ mt: 4 }}>
                    <Typography variant="h5" gutterBottom>
                        Product List
                    </Typography>

                    {loading && (
                        <Stack alignItems="center" sx={{ py: 4 }}>
                            <CircularProgress />
                        </Stack>
                    )}
                    {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                    {!loading && !error && rows.length === 0 && (
                        <Alert severity="info">No products found.</Alert>
                    )}

                    {!loading && !error && rows.length > 0 && (
                        <TableContainer component={Paper} variant="outlined">
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell>Name</TableCell>
                                        <TableCell>Price</TableCell>
                                        <TableCell>Short Description</TableCell>
                                        <TableCell align="right">Actions</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {rows.map((row) => (
                                        <TableRow key={row.id}>
                                            <TableCell>{row.name}</TableCell>
                                            <TableCell>ƒ,1{row.price}</TableCell>
                                            <TableCell>{row.shortDescription}</TableCell>
                                            <TableCell align="right">
                                                <Button
                                                    color="error"
                                                    variant="outlined"
                                                    onClick={() => handleRemove(row.id)}
                                                >
                                                    Remove
                                                </Button>
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    )}
                </Box>
            </Paper>
        </Box>
    );
}
