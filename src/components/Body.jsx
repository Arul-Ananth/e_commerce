import React, { useEffect, useState } from 'react';
import 'react-lazy-load-image-component/src/effects/blur.css';
import {
    Box,
    Drawer,
    List,
    ListItem,
    ListItemText,
    Divider,
    Grid,
    Card,
    CardMedia,
    CardContent,
    Typography
} from '@mui/material';

import { LazyLoadImage } from 'react-lazy-load-image-component';
import ProductCard from "./ProductCard.jsx";

// Width when drawer is expanded
const drawerWidth = 240;

function Body({ drawerOpen, toggleDrawer, selectedCategory, setSelectedCategory }) {
    const [categories, setCategories] = useState([]);
    const [products, setProducts] = useState([]);

    // 🔁 Fetch categories from backend at component mount
    useEffect(() => {
        fetch('http://localhost:8080/api/products/categories')
            .then(res => res.json())
            .then(data => setCategories(['All', ...data])) // Add "All" for show-all option
            .catch(err => console.error('Error fetching categories:', err));
    }, []);

    // 🔁 Fetch products when selectedCategory changes
    useEffect(() => {
        const url =
            selectedCategory === 'All'
                ? 'http://localhost:8080/api/products'
                : `http://localhost:8080/api/products/category/${selectedCategory}`;

        fetch(url)
            .then(res => res.json())
            .then(data => setProducts(data))
            .catch(err => console.error('Error fetching products:', err));
    }, [selectedCategory]);

    return (
        <Box sx={{ display: 'flex' }}>
            {/* Sidebar */}
            <Drawer
                variant="persistent"
                anchor="left"
                open={drawerOpen}
                sx={{
                    width: drawerOpen ? drawerWidth : 0,
                    flexShrink: 0,
                    '& .MuiDrawer-paper': {
                        width: drawerWidth,
                        boxSizing: 'border-box',
                        transition: 'width 0.3s ease',
                        overflowX: 'hidden',
                    },
                }}
            >
                <Divider />
                <List>
                    {/* "All" button at the top */}
                    <ListItem
                        button
                        onClick={() => setSelectedCategory('All')}
                        selected={selectedCategory === 'All'}
                    >
                        <ListItemText primary="All" />
                    </ListItem>

                    {/* Dynamically render remaining categories */}
                    {categories.map((category, index) => (
                        <ListItem
                            button
                            key={index}
                            onClick={() => setSelectedCategory(category)}
                            selected={selectedCategory === category}
                        >
                            <ListItemText primary={category} />
                        </ListItem>
                    ))}
                </List>
            </Drawer>

            {/* Main content area */}
            <Box
                component="main"
                sx={{
                    flexGrow: 1,
                    p: 3,
                    width: drawerOpen ? `calc(100% - ${drawerWidth}px)` : '100%',
                    transition: 'width 0.3s ease',
                }}
            >
                <Grid container spacing={3}>
                    {/* Render product cards */}
                    
                    {products.map((product) => (
                        <Grid item xs={12} sm={6} md={4} lg={3} key={product.id}>
                            <Card>

                                <ProductCard product={product} />
                            </Card>
                        </Grid>
                    ))}

                </Grid>
            </Box>
        </Box>
    );
}

export default Body;
