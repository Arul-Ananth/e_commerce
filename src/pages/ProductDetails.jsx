import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
    Box,
    Typography,
    Button,
    Grid,
    Card,
    CardMedia,
    CardContent,
    Rating,
} from '@mui/material';
import {fetchProduct, fetchReviews} from "../api/ApiService.jsx";

function ProductDetails() {
    const { id } = useParams();
    const [product, setProduct] = useState(null);
    const [reviews, setReviews] = useState([]);

    useEffect(() => {
        fetchProduct(id).then(data => setProduct(data));

        fetchReviews(id).then(data => setReviews(data));
    }, [id]);

    if (!product) return <p>Loading product...</p>;

    return (
        <Box sx={{ p: 4 }}>
            <Typography variant="h4" gutterBottom>{product.name}</Typography>
            <Grid container spacing={2}>
                {/* Image Gallery */}
                <Grid item xs={12} md={6}>
                    <Grid container spacing={1}>
                        {product.images.map((img, index) => (
                            <Grid item xs={6} key={index}>
                                <Card>
                                    <CardMedia
                                        component="img"
                                        image={img}
                                        alt={`Product image ${index}`}
                                        sx={{ height: 200, objectFit: 'cover' }}
                                    />
                                </Card>
                            </Grid>
                        ))}
                    </Grid>
                </Grid>

                {/* Product Info */}
                <Grid item xs={12} md={6}>
                    <Typography variant="body1" paragraph>{product.description}</Typography>
                    <Typography variant="subtitle1">Category: {product.category}</Typography>
                    <Typography variant="h6">Price: ₹{product.price}</Typography>
                    <Button variant="contained" sx={{ mt: 2 }}>
                        Add to Cart
                    </Button>
                </Grid>
            </Grid>

            {/* Reviews */}
            <Box sx={{ mt: 5 }}>
                <Typography variant="h5" gutterBottom>Customer Reviews</Typography>
                {reviews.length === 0 ? (
                    <Typography>No reviews yet.</Typography>
                ) : (
                    reviews.map((review, index) => (
                        <Card key={index} sx={{ mt: 2, p: 2 }}>
                            <Typography><strong>{review.user}</strong></Typography>
                            <Rating value={review.rating} readOnly />
                            <Typography>{review.comment}</Typography>
                        </Card>
                    ))
                )}
            </Box>
        </Box>
    );
}

export default ProductDetails;
