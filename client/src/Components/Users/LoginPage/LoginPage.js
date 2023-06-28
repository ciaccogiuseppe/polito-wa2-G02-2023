import AppNavbar from "../../AppNavbar/AppNavbar";
import React, { useEffect, useState } from "react";
import { Form, Spinner } from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import "./LoginPage.css";
import { useNavigate } from "react-router-dom";
import NavigationLink from "../../Common/NavigationLink";
import { loginAPI } from "../../../API/Auth";
import ErrorMessage from "../../Common/ErrorMessage";
import EyeButton from "../../Common/EyeButton";

function ErrorLink(props) {
  const navigate = useNavigate();
  const [color, setColor] = useState("#ffe6e6");
  return (
    <span
      style={{ color: color, textDecoration: "underline", cursor: "pointer" }}
      onClick={(e) => {
        e.preventDefault();
        navigate(props.navigateTo);
      }}
      onMouseOver={() => setColor("#9f3232")}
      onMouseLeave={() => setColor("#ffe6e6")}
    >
      {props.text}
    </span>
  );
}

function LoginPage(props) {
  const loggedIn = props.loggedIn;
  const setLoggedIn = props.setLoggedIn;
  const [errorMessage, setErrorMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  function login() {
    setLoading(true);
    loginAPI({ username: email, password: password })
      .then((response) => {
        setLoggedIn(true);
        setLoading(false);
        navigate("/");
      })
      .catch((err) => {
        setLoading(false);
        setErrorMessage(err);
      });
  }

  useEffect(() => {
    window.scrollTo(0, 0);
    setErrorMessage("");
  }, []);

  return (
    <>
      <AppNavbar user={props.user} loggedIn={loggedIn} logout={props.logout} />
      <div className="CenteredButton" style={{ marginTop: "50px" }}>
        <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>LOGIN</h1>
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
        <Form
          className="form"
          style={{ marginTop: "30px" }}
          onSubmit={() => {
            login();
          }}
        >
          <Form.Group className="mb-3" controlId="formBasicEmail">
            <Form.Label style={{ color: "#DDDDDD" }}>Email address</Form.Label>
            <Form.Control
              value={email}
              className={"form-control:focus"}
              style={{ width: "300px", alignSelf: "center", margin: "auto" }}
              type="email"
              placeholder="Email"
              onChange={(e) => setEmail(e.target.value)}
            />
          </Form.Group>
          <Form.Group className="mb-3" controlId="formBasicEmail">
            <div>
              <Form.Label style={{ color: "#DDDDDD" }}>Password</Form.Label>
            </div>
            <div style={{ display: "inline-block" }}>
              <Form.Control
                value={password}
                style={{ width: "300px", alignSelf: "center", margin: "auto" }}
                type={showPassword ? "text" : "password"}
                placeholder="Password"
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
            <div
              style={{
                display: "inline-block",
                marginLeft: "10px",
                position: "absolute",
              }}
            >
              <EyeButton
                show={showPassword}
                onClick={() => setShowPassword(!showPassword)}
              />
            </div>
          </Form.Group>
          {loading && (
            <div>
              <Spinner style={{ color: "#A0C1D9" }} />
            </div>
          )}
          <NavigationButton
            text={"Login"}
            type={"submit"}
            onClick={(e) => {
              e.preventDefault();
              login();
            }}
          />
        </Form>
        <div style={{ fontSize: "12px", color: "#EEEEEE", marginTop: "5px" }}>
          <span>Don't have an account?</span>{" "}
          <NavigationLink href={"/signup"} text={"Sign up"} />
        </div>
        <div style={{ fontSize: "12px", color: "#EEEEEE", marginTop: "5px" }}>
          <NavigationLink
            href={"/passwordreset"}
            text={"Forgot your password?"}
          />
        </div>

        {errorMessage && errorMessage === "EMAIL_NOT_ACTIVATED" && (
          <ErrorMessage
            withLink={true}
            close={() => setErrorMessage("")}
            text={
              <span>
                User account is not activated, check your email or{" "}
                <ErrorLink
                  text={"generate a new activation link"}
                  navigateTo={"/askvalidateemail"}
                />
              </span>
            }
          />
        )}
        {errorMessage && errorMessage !== "EMAIL_NOT_ACTIVATED" && (
          <ErrorMessage close={() => setErrorMessage("")} text={errorMessage} />
        )}
      </div>
    </>
  );
}

export default LoginPage;
