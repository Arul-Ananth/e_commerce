import { createTheme, ThemeProvider, CssBaseline } from "@mui/material";
import type { PropsWithChildren } from "react";

export const theme = createTheme({
    palette: {
        mode: "dark",
        primary: { main: "#1976d2" },
        background: {
            default: "#121212",
            paper: "#1e1e1e",
        },
    },
});

export function AppThemeProvider({ children }: PropsWithChildren) {
    return (
        <ThemeProvider theme={theme}>
            <CssBaseline />
            {children}
        </ThemeProvider>
    );
}

export default theme;
