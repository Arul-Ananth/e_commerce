import React from 'react';
import { BrowserRouter as Router, Routes, Route } from  'react-router-dom';
import {CartProvider} from "./global_component/CartContext.jsx";

// Import your page components

import SignUp from './components/Sing-up.jsx';
import Login from './components/Login';


import MainPage from "./pages/MainPage.jsx";
import ProductDetails from "./pages/ProductDetails.jsx";


function App() {
    return (
        <Router>
            <Routes>
                {/* Homepage */}
                <Route path="/" element={<MainPage />} />
                <Route path="/product/:id" element={<ProductDetails />} />

                {/* Signup and Login */}
                <Route path="/signup" element={<SignUp />} />
                <Route path="/login" element={<Login />} />

                {/* Public pages that don’t require auth */}


            </Routes>
        </Router>
        
    );
}

export default App;
