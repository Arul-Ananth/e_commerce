// components/ProductGrid.js

import React from 'react';
import { Grid, Card } from '@mui/material';
import ProductCard from './ProductCard.jsx';

const ProductGrid = ({ products }) => (
    <Grid container spacing={3}>
        {products.map((product) => (
            <Grid item xs={12} sm={6} md={4} lg={3} key={product.id}>
                <Card>
                    <ProductCard product={product} />
                </Card>
            </Grid>
        ))}
    </Grid>
);

export default ProductGrid;
