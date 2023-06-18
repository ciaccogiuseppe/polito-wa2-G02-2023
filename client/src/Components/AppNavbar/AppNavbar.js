import {Container, Nav, Navbar, NavDropdown} from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import {useState} from "react";
import NavbarLink from "./NavbarLink";
import NavbarButton from "./NavbarButton";


function AppNavbar(props){
    const navigate = useNavigate();
    const selected = props.selected
    const logout = props.logout
    return <>
        <>
            <Navbar className="fixed-top" style={{backgroundColor:"#FDE0BE", padding:0, boxShadow:"0px 10px 20px -10px rgba(0,0,0,0.8)", justifyContent:"center"}}>
                {/*<Navbar.Brand href="/" onClick={(e)=>{e.preventDefault(); navigate("/");}}>TICKETING</Navbar.Brand>*/}

                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav" style={{justifyContent:"center"}}>
                    <Nav>
                        <NavbarLink href={"/"} text={"HOME"} selected={selected==="home"}/>
                        {props.loggedIn && <>
                            <NavbarLink href={"/tickets"} text={"TICKETS"}  selected={selected==="tickets"}/>
                            <NavbarLink href={"/profileinfo"} text={"PROFILE"}  selected={selected==="profile"}/>
                            {(props.user !== null && props.user.role === "MANAGER") &&
                                <>
                                    <NavbarLink href={"/tickethistory"} text={"HISTORY"}  selected={selected==="tickethistory"}/>
                                    <NavbarLink href={"/products"} text={"PRODUCTS"}  selected={selected==="products"}/>
                                    <NavbarLink href={"/brands"} text={"BRANDS"}  selected={selected==="brands"}/>
                                    <NavbarLink href={"/expertcreate"} text={"CREATE EXPERT"}  selected={selected==="expertcreate"}/>
                                </>
                            }
                        </>}
                        {/*<NavbarLink href={"/"} text={"CONTACTS"}  selected={selected==="contacts"}/>*/}
                        <NavbarLink href={"/aboutus"} text={"ABOUT US"}  selected={selected==="aboutus"}/>
                        {/*<NavDropdown
                        style={{backgroundColor:color2, height:"100%", fontWeight:"bold", color:textColor2}}
                        title="PRODUCTS"
                        onMouseOver={()=> {setColor2("#bf730f"); setTextColor2("#666666")}}
                        onMouseLeave={()=> {setColor2("#CBB279"); setTextColor2("#222222")}}
                        id="basic-nav-dropdown">
                        <NavDropdown.Item href="/products" onClick={(e)=>{e.preventDefault(); navigate("/products");}}>Get all products</NavDropdown.Item>
                        <NavDropdown.Item href="/productid" onClick={(e)=>{e.preventDefault(); navigate("/productid");}}>Get product by ID</NavDropdown.Item>
                    </NavDropdown>
                    <NavDropdown title="Profile" id="basic-nav-dropdown">
                        <NavDropdown.Item href="/userinfo" onClick={(e)=>{e.preventDefault(); navigate("/userinfo");}}>Get profile info</NavDropdown.Item>
                        <NavDropdown.Item href="/usercreate" onClick={(e)=>{e.preventDefault(); navigate("/usercreate");}}>Create profile</NavDropdown.Item>
                        <NavDropdown.Item href="/usercreate" onClick={(e)=>{e.preventDefault(); navigate("/userupdate");}}>Update profile</NavDropdown.Item>
                    </NavDropdown>*/}
                    </Nav>


                </Navbar.Collapse>
                {props.loggedIn && <Nav style={{right:"20px", position:"absolute"}}>
                    <NavbarButton onClick={() => {logout()}} href={"/"} text={"LOGOUT"}/>
                </Nav>}
            </Navbar>
        </>


    </>
}

export default AppNavbar;