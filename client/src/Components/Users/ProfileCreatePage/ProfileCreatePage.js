import { Form, Spinner } from "react-bootstrap";
import AppNavbar from "../../AppNavbar/AppNavbar";
import React, { useEffect, useState } from "react";
import NavigationButton from "../../Common/NavigationButton";
import NavigationLink from "../../Common/NavigationLink";
import ErrorMessage from "../../Common/ErrorMessage";
import { signupAPI } from "../../../API/Auth";
import { useNavigate } from "react-router-dom";
import SuccessMessage from "../../Common/SuccessMessage";

function ProfileCreatePage(props) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [password2, setPassword2] = useState("");
  const [name, setName] = useState("");
  const [surname, setSurname] = useState("");
  const [address, setAddress] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const [country, setCountry] = useState("");
  const [region, setRegion] = useState("");
  const [city, setCity] = useState("");

  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const [successMessage, setSuccessMessage] = useState(false);

  const loggedIn = props.loggedIn;
  function submit() {
    setErrorMessage("");
    setLoading(true);
    let missingFields = "";
    if (name.length === 0) {
      missingFields = missingFields + "first name, ";
    }
    if (surname.length === 0) {
      missingFields = missingFields + "last name, ";
    }
    if (country.length === 0) {
      missingFields = missingFields + "country, ";
    }
    if (region.length === 0) {
      missingFields = missingFields + "region, ";
    }
    if (city.length === 0) {
      missingFields = missingFields + "city, ";
    }
    if (address.length === 0) {
      missingFields = missingFields + "address, ";
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

    signupAPI({
      firstName: name,
      lastName: surname,
      email: email,
      username: email,
      password: password,
      expertCategories: [],
      address: {
        country: country,
        region: region,
        city: city,
        address: address,
      },
    })
      .then(() => {
        setLoading(false);
        setSuccessMessage(true);
      })
      .catch((err) => {
        setLoading(false);
        setErrorMessage(err);
      });
  }
  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  return (
    <>
      <AppNavbar user={props.user} logout={props.logout} loggedIn={loggedIn} />
      <div className="CenteredButton" style={{ marginTop: "50px" }}>
        <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>SIGN UP</h1>
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
        {!successMessage && (
          <>
            <Form className="form" style={{ marginTop: "30px" }}>
              <Form.Group className="mb-3" controlId="formBasicEmail">
                <Form.Label style={{ color: "#DDDDDD" }}>
                  Personal Info
                </Form.Label>
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
                <div style={{ marginTop: "15px" }}>
                  <h5 style={{ color: "white" }}>Address</h5>
                  <Form.Control
                    value={country}
                    className={"form-control:focus"}
                    style={{
                      width: "300px",
                      alignSelf: "center",
                      margin: "auto",
                      fontSize: 12,
                    }}
                    placeholder="Country"
                    onChange={(e) => setCountry(e.target.value)}
                  />
                  <Form.Control
                    value={region}
                    className={"form-control:focus"}
                    style={{
                      width: "300px",
                      alignSelf: "center",
                      margin: "auto",
                      fontSize: 12,
                      marginTop: "15px",
                    }}
                    placeholder="Region"
                    onChange={(e) => setRegion(e.target.value)}
                  />
                  <Form.Control
                    value={city}
                    className={"form-control:focus"}
                    style={{
                      width: "300px",
                      alignSelf: "center",
                      margin: "auto",
                      fontSize: 12,
                      marginTop: "15px",
                    }}
                    placeholder="City"
                    onChange={(e) => setCity(e.target.value)}
                  />
                  <Form.Control
                    value={address}
                    className={"form-control:focus"}
                    style={{
                      width: "300px",
                      alignSelf: "center",
                      margin: "auto",
                      fontSize: 12,
                      marginTop: "15px",
                    }}
                    placeholder="Address"
                    onChange={(e) => setAddress(e.target.value)}
                  />
                </div>
              </Form.Group>
              <Form.Group className="mb-3" controlId="formBasicEmail">
                <Form.Label style={{ color: "#DDDDDD" }}>
                  Email address
                </Form.Label>
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
              <NavigationButton
                text={"Sign up"}
                onClick={(e) => {
                  e.preventDefault();
                  submit();
                }}
              />
            </Form>

            <div
              style={{ fontSize: "12px", color: "#EEEEEE", marginTop: "5px" }}
            >
              <span>Already have an account?</span>{" "}
              <NavigationLink href={"/login"} text={"Sign in"} />
            </div>
            {loading && (
              <>
                <Spinner style={{ color: "#A0C1D9" }} />
              </>
            )}

            {errorMessage && (
              <ErrorMessage
                close={() => setErrorMessage("")}
                text={errorMessage}
              />
            )}
          </>
        )}

        {successMessage && (
          <>
            <div style={{margin:"10px"}}>
            <SuccessMessage
              text={"Email activation link sent, check your inbox mail"}
              noClose={true}
            />
            </div>
            <NavigationButton
              text={"Back to home"}
              onClick={(e) => {
                e.preventDefault();
                navigate("/");
              }}
            />
          </>
        )}
      </div>
    </>
  );
}

export default ProfileCreatePage;
