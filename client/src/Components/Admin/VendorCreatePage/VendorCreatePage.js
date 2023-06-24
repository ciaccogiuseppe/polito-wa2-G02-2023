import { Form, Spinner } from "react-bootstrap";
import AppNavbar from "../../AppNavbar/AppNavbar";
import React, { useEffect, useState } from "react";
import NavigationButton from "../../Common/NavigationButton";
import ErrorMessage from "../../Common/ErrorMessage";
import { createVendorAPI } from "../../../API/Auth";
import { useNavigate } from "react-router-dom";

function VendorCreatePage(props) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [password2, setPassword2] = useState("");
  const [name, setName] = useState("");
  const [surname, setSurname] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const loggedIn = props.loggedIn;
  function submit() {
    setErrorMessage("");
    let missingFields = "";
    if (name.length === 0) {
      missingFields = missingFields + "first name, ";
    }
    if (surname.length === 0) {
      missingFields = missingFields + "last name, ";
    }
    if (email.length === 0) {
      missingFields = missingFields + "email, ";
    }
    if (password.length === 0) {
      missingFields = missingFields + "password, ";
    }
    if (password2.length === 0) {
      missingFields = missingFields + "password match, ";
    }

    if (missingFields.length > 0) {
      missingFields = missingFields.substring(0, missingFields.length - 2);
      setErrorMessage("Missing fields: " + missingFields);
      return;
    }

    if (!/^[A-Za-z]+$/i.test(name)) {
      setErrorMessage("Error in form: wrong name format (first name)");
      return;
    }
    if (!/^[A-Za-z]+$/i.test(surname)) {
      setErrorMessage("Error in form: wrong name format (last name)");
      return;
    }

    if (!/^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i.test(email)) {
      setErrorMessage("Error in form: Email address is not valid");
      return;
    }
    if (password !== password2) {
      setErrorMessage("Error in form: Passwords do not match");
      return;
    }
    setLoading(true);
    createVendorAPI({
      firstName: name,
      lastName: surname,
      email: email,
      username: email,
      password: password,
      expertCategories: [],
      address: {},
    })
      .then(() => {
        setLoading(false);
        navigate("/");
      })
      .catch((err) => {
        setErrorMessage(err);
        setLoading(false);
      });
  }
  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  return (
    <>
      <AppNavbar
        user={props.user}
        logout={props.logout}
        loggedIn={loggedIn}
        selected="userscreate"
      />
      <div className="CenteredButton" style={{ marginTop: "50px" }}>
        <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>CREATE VENDOR</h1>
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
        <Form className="form" style={{ marginTop: "30px" }}>
          <Form.Group className="mb-3" controlId="formBasicEmail">
            <Form.Label style={{ color: "#DDDDDD" }}>Personal Info</Form.Label>
            <div style={{ width: "300px", margin: "auto" }}>
              <Form.Control
                value={name}
                className={"form-control:focus"}
                style={{
                  display: "inline-block",
                  marginRight: "10px",
                  width: "140px",
                  alignSelf: "center",
                  marginTop: "5px",
                  fontSize: 12,
                }}
                type="input"
                placeholder="First Name"
                onChange={(e) => setName(e.target.value)}
              />
              <Form.Control
                value={surname}
                className={"form-control:focus"}
                style={{
                  display: "inline-block",
                  marginLeft: "10px",
                  width: "140px",
                  alignSelf: "center",
                  marginTop: "5px",
                  fontSize: 12,
                }}
                type="input"
                placeholder="Last Name"
                onChange={(e) => setSurname(e.target.value)}
              />
            </div>
          </Form.Group>
          <Form.Group className="mb-3" controlId="formBasicEmail">
            <Form.Label style={{ color: "#DDDDDD" }}>Email address</Form.Label>
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
          </Form.Group>
          <Form.Group className="mb-3" controlId="formBasicEmail">
            <Form.Label style={{ color: "#DDDDDD" }}>Password</Form.Label>
            <Form.Control
              value={password}
              style={{
                width: "300px",
                alignSelf: "center",
                margin: "auto",
                fontSize: 12,
              }}
              type="password"
              placeholder="Password"
              onChange={(e) => setPassword(e.target.value)}
            />
            <Form.Control
              value={password2}
              style={{
                width: "300px",
                alignSelf: "center",
                margin: "auto",
                marginTop: "10px",
                fontSize: 12,
              }}
              type="password"
              placeholder="Confirm Password"
              onChange={(e) => setPassword2(e.target.value)}
            />
          </Form.Group>
          {loading && (
            <div>
              <Spinner style={{ color: "#A0C1D9" }} />
            </div>
          )}
          <NavigationButton
            text={"Create vendor"}
            onClick={(e) => {
              e.preventDefault();
              submit();
            }}
          />
          <div style={{ marginTop: "20px" }}>
            <NavigationButton
              text={"Back"}
              onClick={(e) => {
                e.preventDefault();
                navigate(-1);
              }}
            />
          </div>
        </Form>

        {errorMessage && (
          <ErrorMessage close={() => setErrorMessage("")} text={errorMessage} />
        )}
      </div>
    </>
  );
}

export default VendorCreatePage;
