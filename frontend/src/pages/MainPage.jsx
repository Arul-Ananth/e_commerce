import React from "react";
import Body from "../components/Body.jsx";

function MainPage({ drawerOpen, toggleDrawer }) {
    const [selectedCategory, setSelectedCategory] = React.useState("All");

    return (
        <Body
            drawerOpen={drawerOpen}
            toggleDrawer={toggleDrawer}
            selectedCategory={selectedCategory}
            setSelectedCategory={setSelectedCategory}
        />
    );
}

export default MainPage;
