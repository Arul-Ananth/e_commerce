// src/components/ProductCard.jsx
import React from 'react';
import { Card, CardContent, Typography, Grid } from '@mui/material';
import { LazyLoadImage } from 'react-lazy-load-image-component';
import { Link } from 'react-router-dom';

function ProductCard({ product }) {
    return (
        <Grid item xs={12} sm={6} md={4} lg={3}>
            {/* Wrap the card in a Link to /product/:id */}
            <Link to={`/product/${product.id}`} style={{ textDecoration: 'none' }}>
                <Card>
                    <LazyLoadImage
                        alt={product.name}
                        src={product.images[0]}
                        effect="blur"
                        width="100%"
                        height="140px"
                        style={{ objectFit: 'cover' }}
                    />
                    <CardContent>
                        <Typography variant="h6">{product.name}</Typography>
                        <Typography variant="body2">{product.description}</Typography>
                        <Typography variant="caption" color="text.secondary">
                            Category: {product.category}
                        </Typography>
                    </CardContent>
                </Card>
            </Link>
        </Grid>
    );
}

export default ProductCard;
