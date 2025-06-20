import React from 'react';
import Header from './components/Header.jsx';
import Body from './components/Body.jsx';
import {AppBar, CssBaseline, Toolbar, Typography} from "@mui/material";
import ProductGrid from "./components/ProductGrid.jsx";

function App() {
    const [drawerOpen, setDrawerOpen] = React.useState(false);

    const toggleDrawer = () => {
        setDrawerOpen(prev => !prev);
    };

    return (
        <>
            <Header toggleDrawer={toggleDrawer} />
            <Body drawerOpen={drawerOpen} toggleDrawer={toggleDrawer} />        </>
    );
}


export default App;
