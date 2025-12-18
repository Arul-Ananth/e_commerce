import React, { useState } from 'react';
import {
    Box, TextField, Button, Typography, Paper, Grid, MenuItem, Alert, Stack,
    CircularProgress, InputAdornment, IconButton
} from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import PercentIcon from '@mui/icons-material/Percent';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import RemoveCircleIcon from '@mui/icons-material/RemoveCircle';
import { addProduct, uploadImage } from '../../api/ApiService';
import { useNavigate } from 'react-router-dom';

const CATEGORIES = ["Electronics", "Fashion", "Home", "Beauty", "Books", "Toys"];

function AddProduct() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [uploading, setUploading] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState(false);

    const [formData, setFormData] = useState({
        name: "", description: "", price: "", category: "", imageUrl: ""
    });

    // New State for Multiple Discounts
    const [discounts, setDiscounts] = useState([
        { description: "", percentage: "", startDate: "", endDate: "" }
    ]);

    // Handle standard inputs
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    // Handle dynamic discount inputs
    const handleDiscountChange = (index, e) => {
        const { name, value } = e.target;
        const newDiscounts = [...discounts];
        newDiscounts[index][name] = value;
        setDiscounts(newDiscounts);
    };

    // Add a new empty row
    const addDiscountRow = () => {
        setDiscounts([...discounts, { description: "", percentage: "", startDate: "", endDate: "" }]);
    };

    // Remove a row
    const removeDiscountRow = (index) => {
        const newDiscounts = [...discounts];
        newDiscounts.splice(index, 1);
        setDiscounts(newDiscounts);
    };

    const handleFileChange = async (e) => {
        const file = e.target.files[0];
        if (!file) return;
        setUploading(true);
        try {
            const url = await uploadImage(file);
            setFormData(prev => ({ ...prev, imageUrl: url }));
        } catch (err) { setError("Image upload failed"); }
        finally { setUploading(false); }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true); setError(""); setSuccess(false);

        // Filter out empty rows (where percentage is missing)
        const validDiscounts = discounts.filter(d => d.percentage && d.startDate);

        try {
            const payload = {
                name: formData.name,
                description: formData.description,
                category: formData.category,
                price: parseFloat(formData.price),
                images: [formData.imageUrl],
                // Send the array of discounts
                discounts: validDiscounts.map(d => ({
                    description: d.description,
                    percentage: parseFloat(d.percentage),
                    startDate: d.startDate,
                    endDate: d.endDate || null
                }))
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
                <Typography variant="h4" gutterBottom>Add New Product</Typography>
                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                {success && <Alert severity="success" sx={{ mb: 2 }}>Product added successfully!</Alert>}

                <form onSubmit={handleSubmit}>
                    <Grid container spacing={3}>
                        <Grid item xs={12} sm={6}><TextField fullWidth label="Name" name="name" value={formData.name} onChange={handleChange} required /></Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField fullWidth select label="Category" name="category" value={formData.category} onChange={handleChange} required>
                                {CATEGORIES.map(opt => <MenuItem key={opt} value={opt}>{opt}</MenuItem>)}
                            </TextField>
                        </Grid>
                        <Grid item xs={12} sm={6}><TextField fullWidth label="Price" name="price" type="number" value={formData.price} onChange={handleChange} required /></Grid>

                        {/* --- DYNAMIC DISCOUNTS SECTION --- */}
                        <Grid item xs={12}>
                            <Typography variant="h6" gutterBottom>
                                Available Discounts
                                <Button startIcon={<AddCircleIcon />} onClick={addDiscountRow} sx={{ ml: 2, textTransform: 'none' }}>
                                    Add Another Offer
                                </Button>
                            </Typography>

                            {discounts.map((discount, index) => (
                                <Paper key={index} variant="outlined" sx={{ p: 2, mb: 2, bgcolor: '#f8f9fa' }}>
                                    <Grid container spacing={2} alignItems="center">
                                        <Grid item xs={12} sm={3}>
                                            <TextField fullWidth label="Description (e.g. Xmas)" name="description" value={discount.description} onChange={(e) => handleDiscountChange(index, e)} size="small" />
                                        </Grid>
                                        <Grid item xs={6} sm={3}>
                                            <TextField
                                                fullWidth label="%" name="percentage" type="number"
                                                value={discount.percentage} onChange={(e) => handleDiscountChange(index, e)} size="small"
                                                InputProps={{ endAdornment: <PercentIcon fontSize="small"/> }}
                                            />
                                        </Grid>
                                        <Grid item xs={6} sm={3}>
                                            <TextField fullWidth type="date" label="Start" name="startDate" value={discount.startDate} onChange={(e) => handleDiscountChange(index, e)} size="small" InputLabelProps={{ shrink: true }} />
                                        </Grid>
                                        <Grid item xs={10} sm={2}>
                                            <TextField fullWidth type="date" label="End" name="endDate" value={discount.endDate} onChange={(e) => handleDiscountChange(index, e)} size="small" InputLabelProps={{ shrink: true }} />
                                        </Grid>
                                        <Grid item xs={2} sm={1}>
                                            <IconButton color="error" onClick={() => removeDiscountRow(index)}><RemoveCircleIcon /></IconButton>
                                        </Grid>
                                    </Grid>
                                </Paper>
                            ))}
                        </Grid>
                        {/* --------------------------------- */}

                        <Grid item xs={12}>
                            <Button component="label" startIcon={<CloudUploadIcon />} variant="outlined">
                                Upload Image <input type="file" hidden onChange={handleFileChange} />
                            </Button>
                            <TextField fullWidth margin="dense" size="small" value={formData.imageUrl} InputProps={{ readOnly: true }} placeholder="Image URL will appear here" />
                        </Grid>
                        <Grid item xs={12}><TextField fullWidth multiline rows={3} label="Description" name="description" value={formData.description} onChange={handleChange} required /></Grid>

                        <Grid item xs={12} sx={{ textAlign: 'right' }}>
                            <Button type="submit" variant="contained" size="large" disabled={loading}>Publish Product</Button>
                        </Grid>
                    </Grid>
                </form>
            </Paper>
        </Box>
    );
}
export default AddProduct;