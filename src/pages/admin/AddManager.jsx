import React, { useState } from 'react';
import { Box, TextField, Button, Typography, Paper, Alert } from '@mui/material';
import { registerManager } from '../../api/ApiService';

export default function AddManager() {
    const [formData, setFormData] = useState({ email: '', password: '', username: '' });
    const [msg, setMsg] = useState(null);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await registerManager(formData);
            setMsg({ type: 'success', text: 'Manager account created successfully!' });
            setFormData({ email: '', password: '', username: '' });
        } catch {
            // FIXED: Removed unused 'err'
            setMsg({ type: 'error', text: 'Failed to create manager.' });
        }
    };

    return (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <Paper sx={{ p: 4, width: 400 }}>
                <Typography variant="h5" mb={2}>Create Manager Account</Typography>
                {msg && <Alert severity={msg.type} sx={{ mb: 2 }}>{msg.text}</Alert>}
                <form onSubmit={handleSubmit}>
                    <TextField
                        fullWidth label="Username" margin="normal"
                        value={formData.username}
                        onChange={e => setFormData({...formData, username: e.target.value})}
                    />
                    <TextField
                        fullWidth label="Email" margin="normal"
                        value={formData.email}
                        onChange={e => setFormData({...formData, email: e.target.value})}
                    />
                    <TextField
                        fullWidth label="Password" type="password" margin="normal"
                        value={formData.password}
                        onChange={e => setFormData({...formData, password: e.target.value})}
                    />
                    <Button type="submit" variant="contained" fullWidth sx={{ mt: 2 }}>
                        Create Manager
                    </Button>
                </form>
            </Paper>
        </Box>
    );
}