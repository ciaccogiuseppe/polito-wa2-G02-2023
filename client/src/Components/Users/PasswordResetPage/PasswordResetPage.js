import AppNavbar from "../../AppNavbar/AppNavbar";
import React, { useEffect, useState } from "react";
import { Form, Spinner } from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import { useNavigate, useParams } from "react-router-dom";
import { resetPasswordApplyAPI } from "../../../API/Auth";
import ErrorMessage from "../../Common/ErrorMessage";

import SuccessMessage from "../../Common/SuccessMessage";
import EyeButton from "../../Common/EyeButton";

function PasswordResetPage(props) {
  const navigate = useNavigate();
  const token = useParams().id;

  const loggedIn = props.loggedIn;
  const [errorMessage, setErrorMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const isExpired = false;
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [password2, setPassword2] = useState("");
  const [successMessage, setSuccessMessage] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  function reset() {
    if (password !== password2) {
      setErrorMessage("Passwords do not match");
      return;
    }
    setLoading(true);
    resetPasswordApplyAPI({
      password: password,
      token: token,
      email: email,
    })
      .then((response) => {
        setSuccessMessage(true);
        setLoading(false);
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
        <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>PASSWORD RESET</h1>
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
        {!isExpired ? (
          <>
            <Form
              className="form"
              style={{ marginTop: "30px" }}
              onSubmit={() => {
                reset();
              }}
            >
              <Form.Group className="mb-3" controlId="formBasicEmail">
                <Form.Label style={{ color: "#DDDDDD" }}>Email</Form.Label>
                <Form.Control
                  value={email}
                  className={"form-control:focus"}
                  style={{
                    width: "300px",
                    alignSelf: "center",
                    margin: "auto",
                    fontSize: 12,
                  }}
                  type="email"
                  placeholder="Email"
                  onChange={(e) => setEmail(e.target.value)}
                />
                <div>
                  <Form.Label style={{ color: "#DDDDDD", marginTop: "10px" }}>
                    Password
                  </Form.Label>
                </div>
                <div style={{ display: "inline-block" }}>
                  <Form.Control
                    value={password}
                    style={{
                      width: "300px",
                      alignSelf: "center",
                      fontSize: 12,
                      marginLeft: "auto",
                      marginRight: "auto",
                    }}
                    type={showPassword ? "text" : "password"}
                    placeholder="Password"
                    onChange={(e) => setPassword(e.target.value)}
                  />
                </div>
                <div
                  style={{
                    display: "inline-block",
                    position: "fixed",
                    verticalAlign: "middle",
                    marginTop: "2px",
                    marginLeft: "10px",
                  }}
                >
                  <EyeButton
                    show={showPassword}
                    onClick={() => setShowPassword(!showPassword)}
                  />
                </div>
                <Form.Control
                  value={password2}
                  style={{
                    width: "300px",
                    marginTop: "10px",
                    fontSize: 12,
                    alignSelf: "center",
                    marginLeft: "auto",
                    marginRight: "auto",
                  }}
                  type={showPassword ? "text" : "password"}
                  placeholder="Password confirm"
                  onChange={(e) => setPassword2(e.target.value)}
                />
              </Form.Group>
              {loading && (
                <>
                  <Spinner style={{ color: "#A0C1D9" }} />
                </>
              )}
              <NavigationButton
                text={"Reset password"}
                type={"submit"}
                onClick={(e) => {
                  e.preventDefault();
                  reset();
                }}
              />
            </Form>
          </>
        ) : (
          <>
            <h5
              style={{
                color: "#EEEEEE",
                marginTop: "80px",
                marginBottom: "30px",
              }}
            >
              Password reset link has expired, generate a new one
            </h5>
            <NavigationButton
              text={"Back to home"}
              onClick={() => {
                navigate("/");
              }}
            />
          </>
        )}
        {successMessage && (
          <SuccessMessage
            text={"Password updated correctly"}
            close={() => setSuccessMessage(false)}
          />
        )}

        {errorMessage && (
          <ErrorMessage close={() => setErrorMessage("")} text={errorMessage} />
        )}
      </div>
    </>
  );
}

export default PasswordResetPage;
