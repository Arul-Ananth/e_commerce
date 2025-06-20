import React, { useState } from 'react';
import {
    AppBar,
    Toolbar,
    Typography,
    IconButton,
    Box,
    InputBase,
    Button,
    Dialog,
    DialogTitle,
    DialogContent,
    TextField,
    DialogActions,
    Tooltip
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import StorefrontIcon from '@mui/icons-material/Storefront';
import MenuIcon from '@mui/icons-material/Menu';

function HeaderTest({ onToggleSidebar }) {
    const [open, setOpen] = useState(false);

    const handleLoginClick = () => setOpen(true);
    const handleClose = () => setOpen(false);

    const redirectHome = () => {
        window.location.href = '/';
    };

    return (
        <>
            <AppBar position="static">
                <Toolbar sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                        {/* Sidebar Toggle Button */}
                        <Tooltip title="Toggle Sidebar">
                            <IconButton color="inherit" onClick={onToggleSidebar} edge="start" sx={{ mr: 2 }}>
                                <MenuIcon />
                            </IconButton>
                        </Tooltip>

                        {/* Logo and Site Name */}
                        <Box
                            sx={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}
                            onClick={redirectHome}
                        >
                            <StorefrontIcon sx={{ mr: 1 }} />
                            <Typography variant="h6" noWrap>
                                ShopEasy
                            </Typography>
                        </Box>
                    </Box>

                    {/* Search Bar */}
                    <Box
                        sx={{
                            display: 'flex',
                            alignItems: 'center',
                            backgroundColor: 'white',
                            borderRadius: 1,
                            px: 1,
                            width: '40%',
                        }}
                    >
                        <SearchIcon color="action" />
                        <InputBase placeholder="Search…" sx={{ ml: 1, flex: 1 }} />
                    </Box>

                    {/* Login Button */}
                    <Button color="inherit" onClick={handleLoginClick}>
                        Sign Up / Login
                    </Button>
                </Toolbar>
            </AppBar>

            {/* Login Dialog */}
            <Dialog open={open} onClose={handleClose}>
                <DialogTitle>Login or Sign Up</DialogTitle>
                <DialogContent>
                    <TextField autoFocus margin="dense" label="Email" fullWidth variant="standard" />
                    <TextField margin="dense" label="Password" type="password" fullWidth variant="standard" />
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose}>Cancel</Button>
                    <Button onClick={handleClose}>Submit</Button>
                </DialogActions>
            </Dialog>
        </>
    );
}

export default HeaderTest;
