// components/ProductGrid.js

import React from 'react';
import { Box } from '@mui/material';
import ProductCard from './ProductCard.jsx';

const ProductGrid = ({ products }) => (
  <Box
    sx={{
      display: 'grid',
      gap: 3, // spacing between cards
      gridTemplateColumns: {
        xs: 'repeat(1, minmax(0, 1fr))', // phones
        sm: 'repeat(2, minmax(0, 1fr))', // small tablets
        md: 'repeat(3, minmax(0, 1fr))', // tablets
        lg: 'repeat(4, minmax(0, 1fr))', // desktops
        xl: 'repeat(5, minmax(0, 1fr))', // wide screens (max 5 columns)
      },
      width: '100%', // fill horizontally
    }}
  >
    {products.map((product) => (
      <ProductCard key={product.id} product={product} />
    ))}
  </Box>
);

export default ProductGrid;
