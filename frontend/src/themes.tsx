// src/themes.jsx
import React from 'react';
import { createTheme, ThemeProvider, CssBaseline } from '@mui/material';

export const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: { main: '#1976d2' },
    background: {
      default: '#121212',
      paper: '#1e1e1e',
    },
  },
});

export function AppThemeProvider({ children }) {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      {children}
    </ThemeProvider>
  );
}

export default theme;
