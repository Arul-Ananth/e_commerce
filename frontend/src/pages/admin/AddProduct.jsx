import React, { useState } from 'react';
import {
    Box, TextField, Button, Typography, Paper, Grid, MenuItem, Alert, Stack, CircularProgress, RadioGroup,
    FormControlLabel
} from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import { addProduct, uploadImage } from '../../api/ApiService'; // Import uploadImage
import { useNavigate } from 'react-router-dom';

const CATEGORIES = ["Electronics", "Fashion", "Home", "Beauty", "Books", "Toys"];

function AddProduct() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [uploading, setUploading] = useState(false); // New state for image upload
    const [error, setError] = useState("");
    const [success, setSuccess] = useState(false);

    const [formData, setFormData] = useState({
        name: "",
        description: "",
        price: "",
        category: "",
        imageUrl: "",
        discountPercentage: 0,
        discountStartDate: "",
        discountEndDate: "",
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    // New: Handle File Selection
    const handleFileChange = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        setUploading(true);
        setError("");

        try {
            const url = await uploadImage(file);
            // Auto-fill the imageUrl field with the response from backend
            setFormData(prev => ({ ...prev, imageUrl: url }));
        } catch (err) {
            console.error(err);
            setError("Failed to upload image.");
        } finally {
            setUploading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError("");
        setSuccess(false);

        try {
            const hasDiscount =
                formData.discountPercentage !== "" &&
                formData.discountStartDate !== "";
            const discounts = hasDiscount
                ? [{
                    description: "Admin Discount",
                    percentage: parseFloat(formData.discountPercentage),
                    startDate: formData.discountStartDate,
                    endDate: formData.discountEndDate || null
                }]
                : [];

            const payload = {
                name: formData.name,
                description: formData.description,
                category: formData.category,
                price: parseFloat(formData.price),
                images: [formData.imageUrl],
                discounts
            };

            await addProduct(payload);
            setSuccess(true);
            setTimeout(() => navigate('/'), 2000);

        } catch (err) {
            console.error(err);
            setError("Failed to add product.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <Paper elevation={3} sx={{ p: 4, width: '100%', maxWidth: 800 }}>
                <Typography variant="h4" gutterBottom sx={{ mb: 4, fontWeight: 'bold' }}>
                    {/* Admin Dashboard: Add Product */}
                    Add New Product
                </Typography>

                {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}
                {success && <Alert severity="success" sx={{ mb: 3 }}>Product added successfully!</Alert>}

                <form onSubmit={handleSubmit}>
                    <Grid container spacing={3}>
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <TextField
                                fullWidth label="Product Name" name="name"
                                value={formData.name} onChange={handleChange} required
                            />
                        </Grid>

                        <Grid size={{ xs: 12, sm: 6 }}>
                            <TextField
                                fullWidth select label="Category" name="category"
                                value={formData.category} onChange={handleChange} required
                            >
                                {CATEGORIES.map((option) => (
                                    <MenuItem key={option} value={option}>{option}</MenuItem>
                                ))}
                            </TextField>
                        </Grid>

                        <Grid size={{ xs: 12, sm: 6 }}>
                            <TextField
                                fullWidth label="Price (₹)" name="price" type="number"
                                value={formData.price} onChange={handleChange} required
                            />
                        </Grid>

                        {/* UPDATED: Image Upload Section */}
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <Stack direction="row" spacing={2} alignItems="center">
                                <Button
                                    variant="outlined"
                                    component="label"
                                    startIcon={<CloudUploadIcon />}
                                    disabled={uploading}
                                >
                                    {uploading ? "Uploading..." : "Upload Image"}
                                    <input
                                        type="file"
                                        hidden
                                        accept="image/*"
                                        onChange={handleFileChange}
                                    />
                                </Button>
                                {uploading && <CircularProgress size={24} />}
                            </Stack>

                            {/* Preview Field (Read Only) */}
                            <TextField
                                fullWidth
                                margin="dense"
                                label="Image URL (Auto-filled)"
                                name="imageUrl"
                                value={formData.imageUrl}
                                InputProps={{ readOnly: true }}
                                helperText="Upload an image to generate this URL automatically"
                            />
                        </Grid>
                        {/*Discount Session*/}
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <TextField
                                fullWidth label="Discount Percentage" name="discountPercentage" type="number"
                                value={formData.discountPercentage} onChange={handleChange} required
                                >

                            </TextField>

                        </Grid>
                        <Grid size={{ xs: 12, sm: 6 }}>
                            <TextField
                                fullWidth label="Discount Start Date" name="discountStartDate" type="date"
                                value={formData.discountStartDate} onChange={handleChange} required
                            />
                            <TextField
                                fullWidth label="Discount End Date" name="discountEndDate" type="date"
                                value={formData.discountEndDate} onChange={handleChange} required
                            />

                        </Grid>



                        <Grid size={{ xs: 12 }}>
                            <TextField
                                fullWidth multiline rows={4} label="Description" name="description"
                                value={formData.description} onChange={handleChange} required
                            />
                        </Grid>

                        <Grid size={{ xs: 12 }}>
                            <Stack direction="row" spacing={2} justifyContent="flex-end">
                                <Button variant="outlined" onClick={() => navigate('/')} disabled={loading}>
                                    Cancel
                                </Button>
                                <Button type="submit" variant="contained" size="large" disabled={loading || uploading || !formData.imageUrl}>
                                    {loading ? "Publishing..." : "Publish Product"}
                                </Button>
                            </Stack>
                        </Grid>
                    </Grid>
                </form>
            </Paper>
        </Box>
    );
}

export default AddProduct;
