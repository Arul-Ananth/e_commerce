import React, { useEffect, useState } from 'react';
import { Box, Toolbar } from '@mui/material'; // 👈 Import Toolbar
import Sidebar from './Sidebar';
import Header from './Header';
import ProductGrid from './ProductGrid';
import { fetchCategories, fetchProducts } from '../api/ApiService';

const drawerWidth = 240;

function Body({ drawerOpen, toggleDrawer }) {
    const [categories, setCategories] = useState([]);
    const [selectedCategory, setSelectedCategory] = useState('All');
    const [products, setProducts] = useState([]);

    useEffect(() => {
        fetchCategories().then(setCategories);
    }, []);

    useEffect(() => {
        fetchProducts(selectedCategory).then(setProducts);
    }, [selectedCategory]);

    return (
        <Box sx={{ display: 'flex' }}>
            <Sidebar
                drawerOpen={drawerOpen}
                toggleDrawer={toggleDrawer}   // pass toggler down
                categories={categories}
                selectedCategory={selectedCategory}
                setSelectedCategory={setSelectedCategory}
            />

            <Box
                component="main"
                sx={{
                    flexGrow: 1,
                    p: 3,
                    width: drawerOpen ? `calc(100% - ${drawerWidth}px)` : '100%',
                    transition: 'width 0.3s ease',
                }}
            >
                <Toolbar /> {/* 👈 This pushes content below the fixed AppBar */}
                <ProductGrid products={products} />
            </Box>
        </Box>
    );
}

export default Body;
