import { Nav, Navbar } from "react-bootstrap";
import NavbarLink from "./NavbarLink";
import NavbarButton from "./NavbarButton";

function AppNavbar(props) {
  const selected = props.selected;
  const logout = props.logout;
  return (
    <>
      <>
        <Navbar
          className="fixed-top"
          style={{
            backgroundColor: "#FDE0BE",
            padding: 0,
            boxShadow: "0px 10px 20px -10px rgba(0,0,0,0.8)",
            justifyContent: "center",
          }}
        >
          {/*<Navbar.Brand href="/" onClick={(e)=>{e.preventDefault(); navigate("/");}}>TICKETING</Navbar.Brand>*/}

          <Navbar.Toggle aria-controls="basic-navbar-nav" />
          <Navbar.Collapse
            id="basic-navbar-nav"
            style={{ justifyContent: "center" }}
          >
            <Nav>
              <NavbarLink
                href={"/"}
                text={"HOME"}
                selected={selected === "home"}
              />
              {props.loggedIn && (
                <>
                  {props.user !== null && props.user.role !== "VENDOR" && (
                    <NavbarLink
                      href={"/tickets"}
                      text={"TICKETS"}
                      selected={selected === "tickets"}
                    />
                  )}
                  {props.user !== null && props.user.role !== "VENDOR" && (
                    <NavbarLink
                      href={"/profileinfo"}
                      text={"PROFILE"}
                      selected={selected === "profile"}
                    />
                  )}
                  {props.user !== null && props.user.role === "MANAGER" && (
                    <>
                      <NavbarLink
                        href={"/tickethistory"}
                        text={"HISTORY"}
                        selected={selected === "tickethistory"}
                      />
                      <NavbarLink
                        href={"/products"}
                        text={"PRODUCTS"}
                        selected={selected === "products"}
                      />
                      <NavbarLink
                        href={"/brands"}
                        text={"BRANDS"}
                        selected={selected === "brands"}
                      />
                      <NavbarLink
                        href={"/userscreate"}
                        text={"USERS"}
                        selected={selected === "userscreate"}
                      />
                    </>
                  )}
                  {props.user !== null && props.user.role === "CLIENT" && (
                    <>
                      <NavbarLink
                        href={"/products"}
                        text={"PRODUCTS"}
                        selected={selected === "products"}
                      />
                    </>
                  )}
                </>
              )}
              {/*<NavbarLink href={"/"} text={"CONTACTS"}  selected={selected==="contacts"}/>*/}
              <NavbarLink
                href={"/aboutus"}
                text={"ABOUT US"}
                selected={selected === "aboutus"}
              />
            </Nav>
          </Navbar.Collapse>
          {props.loggedIn && (
            <Nav style={{ right: "20px", position: "absolute" }}>
              <NavbarButton
                onClick={() => {
                  logout();
                }}
                href={"/"}
                text={"LOGOUT"}
              />
            </Nav>
          )}
        </Navbar>
      </>
    </>
  );
}

export default AppNavbar;
