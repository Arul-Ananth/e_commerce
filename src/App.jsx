import React from 'react';
import { BrowserRouter as Router, Routes, Route } from  'react-router-dom';

// Import your page components

import SignUp from './components/Sing-up.jsx';
import Login from './components/Login';


import MainPage from "./pages/MainPage.jsx";

function App() {
    return (
        <Router>
            <Routes>
                {/* Homepage */}
                <Route path="/" element={<MainPage />} />

                {/* Signup and Login */}
                <Route path="/signup" element={<SignUp />} />
                <Route path="/login" element={<Login />} />

                {/* Public pages that don’t require auth */}

            </Routes>
        </Router>
    );
}

export default App;
