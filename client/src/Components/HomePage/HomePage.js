import {  useNavigate } from "react-router-dom";
import "./HomePage.css";
import AppNavbar from "../AppNavbar/AppNavbar";
import { useEffect } from "react";
import NavigationButton from "../Common/NavigationButton";
import NavigationLink from "../Common/NavigationLink";
import WarrantyGenerate from "../Products/WarrantyGenerate/WarrantyGenerate";
import DashboardPage from "../Admin/DashboardPage/DashboardPage";

function HomePage(props) {
  const navigate = useNavigate();
  const loggedIn = props.loggedIn;
  const user = props.user;
  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  return (
    <>
      <AppNavbar
        user={props.user}
        logout={props.logout}
        loggedIn={loggedIn}
        selected={"home"}
      />
      <div className="CenteredButton" style={{ marginTop: "50px" }}>
        <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>
          TICKETING SUPPORT
        </h1>
        <hr
          style={{
            color: "white",
            width: "25%",
            alignSelf: "center",
            marginLeft: "auto",
            marginRight: "auto",
            marginBottom: "2px",
            marginTop: "2px",
          }}
        />
        <h5 style={{ color: "#EEEEEE" }}>simple and easy ticketing</h5>
      </div>

      {loggedIn && user !== null && user.role === "CLIENT" && (
        <>
          <div className="CenteredButton" style={{ marginTop: "50px" }}>
            <h5 style={{ color: "#EEEEEE" }}>
              Nice to see you,{" "}
              <span style={{ color: "#FDE0BE" }}>{user.name}</span>!
            </h5>
            <h6 style={{ color: "#EEEEEE" }}>
              <i>We are glad to offer all the support you need.</i>
            </h6>
            <h6 style={{ color: "#EEEEEE" }}>
              <i>Contact us and we'll take care about everything else.</i>
            </h6>
          </div>
          <div className="CenteredButton" style={{ marginTop: "70px" }}>
            <NavigationButton
              text={"Register a product"}
              onClick={(e) => {
                e.preventDefault();
                navigate("/productregister");
              }}
            />
          </div>
          <div className="CenteredButton" style={{ marginTop: "25px" }}>
            <NavigationButton
              text={"Open a ticket"}
              onClick={(e) => {
                e.preventDefault();
                navigate("/newticket");
              }}
            />
          </div>
        </>
      )}

      {loggedIn && user !== null && user.role === "EXPERT" && (
        <>
          <div className="CenteredButton" style={{ marginTop: "50px" }}>
            <h5 style={{ color: "#EEEEEE" }}>
              Welcome, <span style={{ color: "#FDE0BE" }}>{user.name}</span>.
            </h5>
            <h6 style={{ color: "#EEEEEE" }}>
              <i>
                This is the place where you can consult the tickets you are
                responsible of.
              </i>
            </h6>
            <h6 style={{ color: "#EEEEEE" }}>
              <i>Helping customers has never been so easy.</i>
            </h6>
          </div>
          <div className="CenteredButton" style={{ marginTop: "70px" }}>
            <NavigationButton
              text={"View assigned tickets"}
              onClick={(e) => {
                e.preventDefault();
                navigate("/tickets");
              }}
            />
          </div>
        </>
      )}

      {loggedIn && user !== null && user.role === "VENDOR" && (
        <>
          <WarrantyGenerate />
          {/*<div className="CenteredButton" style={{marginTop:"70px"}}>
                    <NavigationButton text={"View assigned tickets"} onClick={(e) => { e.preventDefault(); navigate("/tickets") }}/>
                </div>*/}
        </>
      )}

      {loggedIn && user !== null && user.role === "MANAGER" && (
        <>
          <DashboardPage />
        </>
      )}

      {!loggedIn && (
        <>
          <div className="CenteredButton" style={{ marginTop: "70px" }}>
            <NavigationButton
              text={"Login"}
              onClick={(e) => {
                e.preventDefault();
                navigate("/login");
              }}
            />
            <div
              style={{ fontSize: "12px", color: "#EEEEEE", marginTop: "5px" }}
            >
              <span>Don't have an account?</span>{" "}
              <NavigationLink href={"/signup"} text={"Sign up"} />
            </div>
          </div>
        </>
      )}

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
    </>
  );
}

export default HomePage;
