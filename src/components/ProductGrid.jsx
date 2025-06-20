import React, { useEffect, useState } from 'react';
import { Grid, Card, CardMedia, CardContent, Typography, Box } from '@mui/material';
import { fetchAllProducts } from '../api/productService';

function ProductGrid() {
    const [products, setProducts] = useState([]);

    useEffect(() => {
        fetchAllProducts().then(setProducts).catch(console.error);
    }, []);

    return (
        <Box sx={{ p: 4 }}>
            <Grid container spacing={3}>
                {products.map(product => (
                    <Grid item xs={12} sm={6} md={4} lg={3} key={product.id}>
                        <Card>
                            <CardMedia
                                component="img"
                                height="140"
                                image={product.imageUrl}
                                alt={product.name}
                            />
                            <CardContent>
                                <Typography variant="h6">{product.name}</Typography>
                                <Typography variant="body2">{product.description}</Typography>
                                <Typography variant="caption" color="text.secondary">
                                    Category: {product.category}
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        </Box>
    );
}

export default ProductGrid;
