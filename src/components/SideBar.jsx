// components/Sidebar.js

import React from 'react';
import {
    Drawer,
    List,
    ListItem,
    ListItemText,
    Divider
} from '@mui/material';

const drawerWidth = 240;

const Sidebar = ({ drawerOpen, categories, selectedCategory, setSelectedCategory }) => (
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
);

export default Sidebar;
