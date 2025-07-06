import React, { useState } from 'react';
import {
    Box,
    Button,
    TextField,
    Typography,
    Paper,
} from '@mui/material';

function SignUp() {
    const [formData, setFormData] = useState({
        email: '',
        password: '',
        confirmPassword: '',
        phone: '',
        captcha: '',
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        // TODO: Add validation, captcha check, and submit logic
        console.log('Submitted:', formData);
    };

    return (
        <Box
            sx={{
                height: '100vh',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                bgcolor: '#f4f6f8',
            }}
        >
            <Paper elevation={3} sx={{ p: 4, width: 350 }}>
                <Typography variant="h5" align="center" gutterBottom>
                    Sign Up
                </Typography>
                <form onSubmit={handleSubmit}>
                    <TextField
                        label="Email"
                        name="email"
                        type="email"
                        fullWidth
                        margin="normal"
                        required
                        value={formData.email}
                        onChange={handleChange}
                    />
                    <TextField
                        label="Password"
                        name="password"
                        type="password"
                        fullWidth
                        margin="normal"
                        required
                        value={formData.password}
                        onChange={handleChange}
                    />
                    <TextField
                        label="Confirm Password"
                        name="confirmPassword"
                        type="password"
                        fullWidth
                        margin="normal"
                        required
                        value={formData.confirmPassword}
                        onChange={handleChange}
                    />
                    <TextField
                        label="Phone Number"
                        name="phone"
                        type="tel"
                        fullWidth
                        margin="normal"
                        required
                        value={formData.phone}
                        onChange={handleChange}
                    />
                    <TextField
                        label="Captcha"
                        name="captcha"
                        fullWidth
                        margin="normal"
                        required
                        value={formData.captcha}
                        onChange={handleChange}
                    />
                    {/* Replace with real captcha image or plugin if needed */}
                    <Typography variant="caption" sx={{ color: 'gray' }}>
                        Enter "1234" for demo
                    </Typography>

                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        sx={{ mt: 2 }}
                    >
                        Sign Up
                    </Button>
                </form>
            </Paper>
        </Box>
    );
}

export default SignUp;
