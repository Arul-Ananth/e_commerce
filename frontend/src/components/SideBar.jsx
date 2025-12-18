// components/Sidebar.js

import React from 'react';
import {
    Drawer,
    List,
    ListItem,
    ListItemText,
    Divider,
    IconButton,
    Toolbar,
    useMediaQuery,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { useTheme } from '@mui/material/styles';

const drawerWidth = 240;

const Sidebar = ({ drawerOpen, toggleDrawer, categories, selectedCategory, setSelectedCategory }) => {
    const theme = useTheme();
    const isSmall = useMediaQuery(theme.breakpoints.down('md'));

    const handleClose = () => toggleDrawer(false);

    return (
        <Drawer
            variant={isSmall ? 'temporary' : 'persistent'}
            anchor="left"
            open={drawerOpen}
            onClose={handleClose} // enables backdrop/ESC close on small screens
            ModalProps={{ keepMounted: true }}
            sx={{
                width: drawerOpen ? drawerWidth : 0,
                flexShrink: 0,
                '& .MuiDrawer-paper': {
                    width: drawerWidth,
                    boxSizing: 'border-box',
                    transition: 'transform 225ms ease, width 0.3s ease',
                    overflowX: 'hidden',
                },
            }}
        >
            <Toolbar sx={{ justifyContent: 'flex-end' }}>
                <IconButton aria-label="close sidebar" onClick={handleClose}>
                    <CloseIcon />
                </IconButton>
            </Toolbar>
            <Divider />
            <List>
                <ListItem button onClick={() => { setSelectedCategory('All'); if (isSmall) handleClose(); }} selected={selectedCategory === 'All'}>
                    <ListItemText primary="All Categories" />
                </ListItem>
                <Divider />
                {categories.map((category, index) => (
                    <ListItem
                        button
                        key={index}
                        onClick={() => { setSelectedCategory(category); if (isSmall) handleClose(); }}
                        selected={selectedCategory === category}
                    >
                        <ListItemText primary={category} />
                    </ListItem>
                ))}
                {/* Optional explicit close action for larger screens */}
                {!isSmall && (
                    <>
                        <Divider />
                        <ListItem button onClick={handleClose}>
                            <ListItemText primary="Close" />
                        </ListItem>
                    </>
                )}
            </List>
        </Drawer>
    );
};

export default Sidebar;