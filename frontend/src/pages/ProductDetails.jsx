import React, { useEffect, useState } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import {
    Box,
    Typography,
    Button,
    Grid,
    Card,
    CardMedia,
    Rating,
    Stack,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogContentText,
    DialogActions,
    // NEW IMPORTS FOR RADIO BUTTONS
    Radio,
    RadioGroup,
    FormControlLabel,
    FormControl,
    FormLabel
} from "@mui/material";
import AddShoppingCartIcon from "@mui/icons-material/AddShoppingCart";
import FlashOnIcon from "@mui/icons-material/FlashOn";
import DeleteIcon from "@mui/icons-material/Delete";

import { fetchProduct, fetchReviews, deleteProduct } from "../api/ApiService.jsx";
import { useCart } from "../global_component/CartContext";
import { useAuth } from "../global_component/AuthContext";

function ProductDetails() {
    const { id } = useParams();
    const navigate = useNavigate();
    const location = useLocation();

    const [product, setProduct] = useState(null);
    const [reviews, setReviews] = useState([]);

    // NEW: State for selected discount
    const [selectedDiscount, setSelectedDiscount] = useState(null);

    // Auth & Cart
    const { addToCart } = useCart();
    const { isAuthenticated, user } = useAuth();

    // Check if Admin
    const isAdmin = user?.roles?.includes('ROLE_ADMIN');

    // Delete Modal State
    const [openDelete, setOpenDelete] = useState(false);

    useEffect(() => {
        fetchProduct(id).then((data) => setProduct(data));
        fetchReviews(id).then((data) => setReviews(data));
    }, [id]);

    if (!product) return <p>Loading product...</p>;


    const handleAddToCart = async () => {
        try {
            await addToCart(product, 1);

            // --- FRONTEND ONLY FIX: Save Discount to Local Storage ---
            if (selectedDiscount) {
                // Get existing discounts map or create new one
                const savedDiscounts = JSON.parse(localStorage.getItem("cart_discounts") || "{}");

                // Save this discount mapped by Product ID
                savedDiscounts[product.id] = selectedDiscount;

                // Write back to storage
                localStorage.setItem("cart_discounts", JSON.stringify(savedDiscounts));
            }
            // ---------------------------------------------------------

        } catch (e) {
            console.error("Error adding to cart:", e);
        }
    };

    const handleBuyNow = async () => {
        if (!isAuthenticated) {
            const redirectTarget = encodeURIComponent("/checkout");
            navigate(`/login?redirect=${redirectTarget}`, { state: { from: location } });
            return;
        }
        await handleAddToCart();
        navigate("/checkout");
    };

    // --- Delete Logic ---
    const handleDeleteClick = () => {
        setOpenDelete(true);
    };

    const handleConfirmDelete = async () => {
        try {
            await deleteProduct(id);
            setOpenDelete(false);
            navigate("/", { replace: true });
        } catch (err) {
            console.error("Failed to delete product", err);
            alert("Failed to delete product. It might be in a user's cart.");
        }
    };

    const handleCancelDelete = () => {
        setOpenDelete(false);
    };

    // NEW: Handle Radio Change
    const handleDiscountChange = (event) => {
        const discountId = parseInt(event.target.value);
        if (isNaN(discountId)) {
            setSelectedDiscount(null); // Case for "No Discount"
        } else {
            const discount = product.discounts.find(d => d.id === discountId);
            setSelectedDiscount(discount);
        }
    };

    // NEW: Calculate Price based on selection
    const calculatePrice = () => {
        if (!selectedDiscount) return product.price;
        const discountAmount = (product.price * selectedDiscount.percentage) / 100;
        return (product.price - discountAmount).toFixed(2);
    };

    return (
        <Box sx={{ p: 4 }}>
            <Typography variant="h4" gutterBottom>
                {product.name}
            </Typography>

            {/* --- MODIFIED DISCOUNT SECTION WITH RADIO BUTTONS --- */}
            {product.discounts && product.discounts.length > 0 && (
                <Box sx={{ mt: 2 }}>
                    <FormControl component="fieldset" fullWidth>
                        <FormLabel component="legend" sx={{ mb: 1, color: 'text.secondary' }}>
                            Select an Offer:
                        </FormLabel>
                        <RadioGroup
                            name="discount-group"
                            value={selectedDiscount ? selectedDiscount.id : "none"}
                            onChange={handleDiscountChange}
                        >
                            {/* Option 1: No Discount */}
                            <FormControlLabel
                                value="none"
                                control={<Radio color="primary" />}
                                label="Original Price (No Discount)"
                                sx={{ mb: 1 }}
                            />

                            {/* Option 2..N: Dynamic Discounts */}
                            {product.discounts.map((discount, i) => (
                                <FormControlLabel
                                    key={i}
                                    value={discount.id}
                                    sx={{ width: '100%', ml: 0, mb: 1, alignItems: 'flex-start' }} // Align nicely
                                    control={<Radio color="success" sx={{ mt: 1 }} />} // Green radio for visibility
                                    label={
                                        // YOUR ORIGINAL STYLE BOX PRESERVED HERE
                                        <Box sx={{
                                            p: 1.5,
                                            width: '100%',
                                            border: '1px dashed #2e7d32',
                                            borderRadius: 1,
                                            bgcolor: 'rgba(46, 125, 50, 0.05)',
                                            display: 'flex',
                                            justifyContent: 'space-between',
                                            alignItems: 'center',
                                            ml: 1 // Add margin left to separate from radio
                                        }}>
                                            <Box>
                                                <Typography variant="body2" fontWeight="bold" color="success.main">
                                                    {discount.description || "Special Discount"}: {discount.percentage}% Off
                                                </Typography>
                                                <Typography variant="caption" color="text.secondary">
                                                    Valid from {discount.startDate} {discount.endDate ? `to ${discount.endDate}` : ""}
                                                </Typography>
                                            </Box>
                                        </Box>
                                    }
                                />
                            ))}
                        </RadioGroup>
                    </FormControl>
                </Box>
            )}

            <Grid container spacing={2} sx={{ mt: 2 }}>
                {/* Left: Images */}
                <Grid item xs={12} md={6}>
                    <Grid container spacing={1}>
                        {product.images && product.images.map((img, index) => (
                            <Grid item xs={6} key={index}>
                                <Card sx={{ height: '100%' }}>
                                    <CardMedia
                                        component="img"
                                        image={img}
                                        alt={`Product image ${index}`}
                                        sx={{ height: 200, objectFit: "cover" }}
                                    />
                                </Card>
                            </Grid>
                        ))}
                    </Grid>
                </Grid>

                {/* Right: Info */}
                <Grid item xs={12} md={6}>
                    <Typography variant="body1" paragraph>
                        {product.description}
                    </Typography>
                    <Typography variant="subtitle1" color="text.secondary">
                        Category: {product.category}
                    </Typography>

                    {/* MODIFIED PRICE DISPLAY */}
                    <Box sx={{ mt: 1, mb: 2 }}>
                        {selectedDiscount ? (
                            <Box>
                                <Typography variant="h6" sx={{ textDecoration: 'line-through', color: 'text.secondary' }}>
                                    ₹{product.price}
                                </Typography>
                                <Typography variant="h4" color="success.main" fontWeight="bold">
                                    ₹{calculatePrice()}
                                </Typography>
                            </Box>
                        ) : (
                            <Typography variant="h5">
                                Price: ₹{product.price}
                            </Typography>
                        )}
                    </Box>

                    <Stack spacing={2} sx={{ mt: 2, maxWidth: 400 }}>
                        {/* User Buttons */}
                        <Stack direction="row" spacing={2}>
                            <Button
                                variant="contained"
                                color="primary"
                                startIcon={<AddShoppingCartIcon />}
                                onClick={handleAddToCart}
                                sx={{ flexGrow: 1 }}
                            >
                                Add to Cart
                            </Button>
                            <Button
                                variant="contained"
                                color="warning"
                                startIcon={<FlashOnIcon />}
                                onClick={handleBuyNow}
                                sx={{ flexGrow: 1 }}
                            >
                                Buy Now
                            </Button>
                        </Stack>

                        {/* Admin Only Delete Button */}
                        {isAdmin && (
                            <Button
                                variant="outlined"
                                color="error"
                                startIcon={<DeleteIcon />}
                                onClick={handleDeleteClick}
                                sx={{ mt: 2 }}
                            >
                                Delete Product (Admin)
                            </Button>
                        )}
                    </Stack>
                </Grid>
            </Grid>

            {/* Reviews Section */}
            <Box sx={{ mt: 5 }}>
                <Typography variant="h5" gutterBottom>
                    Customer Reviews
                </Typography>
                {reviews.length === 0 ? (
                    <Typography color="text.secondary">No reviews yet.</Typography>
                ) : (
                    reviews.map((review, index) => (
                        <Card key={index} sx={{ mt: 2, p: 2 }}>
                            <Stack direction="row" spacing={1} alignItems="center" mb={1}>
                                <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                                    {review.user}
                                </Typography>
                                <Rating value={review.rating} readOnly size="small" />
                            </Stack>
                            <Typography variant="body2">{review.comment}</Typography>
                        </Card>
                    ))
                )}
            </Box>

            {/* Delete Confirmation Dialog */}
            <Dialog
                open={openDelete}
                onClose={handleCancelDelete}
                aria-labelledby="alert-dialog-title"
                aria-describedby="alert-dialog-description"
            >
                <DialogTitle id="alert-dialog-title">
                    {"Delete this product?"}
                </DialogTitle>
                <DialogContent>
                    <DialogContentText id="alert-dialog-description">
                        Are you sure you want to permanently delete <strong>{product.name}</strong>?
                        This action cannot be undone.
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCancelDelete}>Cancel</Button>
                    <Button onClick={handleConfirmDelete} color="error" autoFocus>
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
}

export default ProductDetails;