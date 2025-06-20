import React from 'react';
import {
    AppBar, Box, CssBaseline, Divider, Drawer,
    IconButton, List, ListItem, ListItemButton,
    ListItemIcon, ListItemText, Toolbar, Typography, Grid, Card, CardMedia, CardContent
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import InboxIcon from '@mui/icons-material/MoveToInbox';

const drawerWidth = 240;

function Body({ window, drawerOpen, toggleDrawer }) {

    const [mobileOpen, setMobileOpen] = React.useState(false);

    const handleDrawerToggle = () => {
        setMobileOpen(!mobileOpen);
    };

    const categories = ['All', 'Electronics', 'Clothing', 'Books', 'Accessories'];
    const [selectedCategory, setSelectedCategory] = React.useState('All');

    const products = [
        { id: 1, name: 'Headphones', category: 'Electronics', image: 'https://via.placeholder.com/150' },
        { id: 2, name: 'T-Shirt', category: 'Clothing', image: 'https://via.placeholder.com/150' },
        { id: 3, name: 'Book: React', category: 'Books', image: 'https://via.placeholder.com/150' },
        { id: 4, name: 'Camera', category: 'Electronics', image: 'https://via.placeholder.com/150' },
        { id: 5, name: 'Sunglasses', category: 'Accessories', image: 'https://via.placeholder.com/150' },
        { id: 6, name: 'Jacket', category: 'Clothing', image: 'https://via.placeholder.com/150' },
        { id: 7, name: 'Power Bank', category: 'Electronics', image: 'https://via.placeholder.com/150' },
        { id: 8, name: 'Notebook', category: 'Books', image: 'https://via.placeholder.com/150' },
        { id: 9, name: 'Backpack', category: 'Accessories', image: 'https://via.placeholder.com/150' },
    ];


    const filteredProducts = selectedCategory === 'All'
        ? products
        : products.filter(product => product.category === selectedCategory);


    const drawer = (
        <div>
            <Toolbar />
            <Divider />
            <List>
                {categories.map((text) => (
                    <ListItem key={text} disablePadding>
                        <ListItemButton onClick={() => setSelectedCategory(text)}>
                            <ListItemIcon>
                                <InboxIcon />
                            </ListItemIcon>
                            <ListItemText primary={text} />
                        </ListItemButton>
                    </ListItem>
                ))}
            </List>
        </div>
    );

    const container = window !== undefined ? () => window().document.body : undefined;

    return (
        <Box sx={{ display: 'flex' }}>
            <CssBaseline />

            {/* Drawer */}
            <Box component="nav" sx={{ width: { sm: 240 }, flexShrink: { sm: 0 } }}>
                <Drawer
                    variant="temporary"
                    open={drawerOpen}
                    onClose={toggleDrawer}
                    ModalProps={{ keepMounted: true }}
                    sx={{
                        display: { xs: 'block', sm: 'none' },
                        '& .MuiDrawer-paper': { width: 240 },
                    }}
                >
                    {drawer}
                </Drawer>
                <Drawer
                    variant="permanent"
                    open
                    sx={{
                        display: { xs: 'none', sm: 'block' },
                        '& .MuiDrawer-paper': { width: 240 },
                    }}
                >
                    {drawer}
                </Drawer>
            </Box>

            {/* Main content (product grid, etc.) */}
            <Box component="main" sx={{ flexGrow: 1, p: 3, width: { sm: `calc(100% - 240px)` } }}>
                <Toolbar />
                <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
                    <Toolbar />
                    <Grid container spacing={2}>
                        {products.map((product) => (
                            <Grid item xs={12} sm={6} md={4} key={product.id}>
                                <Card>
                                    <CardMedia
                                        component="img"
                                        height="140"
                                        image={product.image}
                                        alt={product.name}
                                    />
                                    <CardContent>
                                        <Typography variant="h6">{product.name}</Typography>
                                        <Typography variant="body2" color="text.secondary">
                                            Category: {product.category}
                                        </Typography>
                                    </CardContent>
                                </Card>
                            </Grid>
                        ))}
                    </Grid>
                </Box>
            </Box>
        </Box>
    );


}

export default Body;
