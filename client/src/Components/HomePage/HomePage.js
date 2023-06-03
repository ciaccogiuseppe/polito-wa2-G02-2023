import { Button } from "react-bootstrap";
import {Link, useNavigate} from "react-router-dom";
import "./HomePage.css";
import AppNavbar from "../AppNavbar/AppNavbar";
import {useState} from "react";
import NavigationButton from "../Common/NavigationButton";

function HomePage(props) {
    const navigate = useNavigate();
    const loggedIn = props.loggedIn
    const [linkColor, setLinkColor] = useState("#FDE0BE")

    return <>
        <div style={{
            position: 'absolute',
            backgroundColor: '#537188',
            width: '100%',
            height: '100%'
        }}>
            <AppNavbar loggedIn={loggedIn}/>

            <div className="CenteredButton" style={{marginTop:"50px"}}>
                <h1 style={{color:"#EEEEEE"}}>TICKETING SUPPORT</h1>
                <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>
                <h5 style={{color:"#EEEEEE"}}>simple and easy ticketing</h5>
            </div>

            {loggedIn &&
            <>
                <div className="CenteredButton" style={{marginTop:"70px"}}>
                    <NavigationButton text={"Open a ticket"} onClick={(e) => { e.preventDefault(); navigate("/") }}/>
                </div>
            </>
            }

            {!loggedIn &&
                <>
                    <div className="CenteredButton" style={{marginTop:"70px"}}>
                        <NavigationButton text={"Login"} onClick={(e) => { e.preventDefault(); navigate("/login") }}/>
                        <div style={{fontSize:"12px", color:"#EEEEEE", marginTop:"5px" }}>
                            <span>Don't have an account?</span> <Link
                            style={{color:linkColor}}
                            onMouseOver={()=>setLinkColor("#c79e4d")}
                            onMouseLeave={()=>setLinkColor("#FDE0BE")}>Sign up</Link>
                        </div>
                    </div>
                </>
            }

            {/*<div className="CenteredButton">
                <NavigationButton text={"Login"} onClick={(e) => { e.preventDefault(); navigate("/") }}/>
            </div>*/}

            {/*<div className="CenteredButton" style={{marginTop:"50px"}}>
                <NavigationButton text={"Get all products"} onClick={(e) => { e.preventDefault(); navigate("/products") }}/>
            </div>
            <div className="CenteredButton">
                <NavigationButton text={"Get product by ID"} onClick={(e) => { e.preventDefault(); navigate("/productid") }}/>
            </div>
            <div className="CenteredButton">
                <NavigationButton text={"Get profile by mail"} onClick={(e) => { e.preventDefault(); navigate("/userInfo") }}/>
            </div>
            <div className="CenteredButton">
                <NavigationButton text={"Create new profile"} onClick={(e) => { e.preventDefault(); navigate("/usercreate") }}/>
            </div>
            <div className="CenteredButton">
                <NavigationButton text={"Edit profile"} onClick={(e) => { e.preventDefault(); navigate("/userupdate") }}/>
            </div>*/}

            {/*<hr style={{color:"white", width:"90%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginTop:"100px"}}/>

            <div className="CenteredButton">
            <div className="text-white">This client is intended to test server APIs, therefore only few client-side validation checks are implemented</div>
            </div>*/}
        </div>
    </>
}

export default HomePage;