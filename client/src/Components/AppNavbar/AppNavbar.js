import {Container, Nav, Navbar, NavDropdown} from "react-bootstrap";
import { useNavigate } from "react-router-dom";


function AppNavbar(){
    const navigate = useNavigate();
    return <>
        <Navbar bg="secondary" expand="lg">
            <Container>
                <Navbar.Brand href="/" onClick={(e)=>{e.preventDefault(); navigate("/");}}>TICKETING</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="me-auto">
                        <Nav.Link href="/" onClick={(e)=>{e.preventDefault(); navigate("/");}}>Home</Nav.Link>
                        <NavDropdown title="Products" id="basic-nav-dropdown">
                            <NavDropdown.Item href="/products" onClick={(e)=>{e.preventDefault(); navigate("/products");}}>Get all products</NavDropdown.Item>
                            <NavDropdown.Item href="/productid" onClick={(e)=>{e.preventDefault(); navigate("/productid");}}>Get product by ID</NavDropdown.Item>
                        </NavDropdown>
                        <NavDropdown title="Profile" id="basic-nav-dropdown">
                            <NavDropdown.Item href="/userinfo" onClick={(e)=>{e.preventDefault(); navigate("/userinfo");}}>Get profile info</NavDropdown.Item>
                            <NavDropdown.Item href="/usercreate" onClick={(e)=>{e.preventDefault(); navigate("/usercreate");}}>Create profile</NavDropdown.Item>
                            <NavDropdown.Item href="/usercreate" onClick={(e)=>{e.preventDefault(); navigate("/userupdate");}}>Update profile</NavDropdown.Item>
                        </NavDropdown>
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    </>
}

export default AppNavbar;