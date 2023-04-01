import {Container, Nav, Navbar, NavDropdown} from "react-bootstrap";

function AppNavbar(){
    return <>
        <Navbar bg="secondary" expand="lg">
            <Container>
                <Navbar.Brand href="/">TICKETING</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="me-auto">
                        <Nav.Link href="/">Home</Nav.Link>
                        <Nav.Link href="/products">Products</Nav.Link>
                        <NavDropdown title="User" id="basic-nav-dropdown">
                            <NavDropdown.Item href="/userinfo">Get User info</NavDropdown.Item>
                            <NavDropdown.Item href="/createuser">
                                Create User
                            </NavDropdown.Item>
                        </NavDropdown>
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    </>
}

export default AppNavbar;