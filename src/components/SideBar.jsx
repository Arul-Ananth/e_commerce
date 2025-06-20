import {Drawer, Typography} from "@mui/material";
import React from "react";

function SideBar(){
    return(
        /*
        1)a 3-dot icon
        2)on clicking a 3-dot icon it must open
        3) on opening it must display the relevant product categories fetched from the backend
        4) it must then display a checkbox to eliminate the particular product from displaying


         */
        <Drawer variant="permanent" anchor="left" open={true} >
            <div>
                <h1>Menu</h1>
                <ul>
                    <li>1</li>
                    <li>About</li>
                    <li>3</li>
                </ul>
            </div>
        </Drawer>
    );
}
export default SideBar;