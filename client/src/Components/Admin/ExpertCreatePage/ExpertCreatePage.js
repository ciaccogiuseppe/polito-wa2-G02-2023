import { Form, Col, Row, Spinner } from "react-bootstrap";
import AppNavbar from "../../AppNavbar/AppNavbar";
import React, { useEffect, useState } from "react";
import NavigationButton from "../../Common/NavigationButton";
import ErrorMessage from "../../Common/ErrorMessage";
import { createExpertAPI } from "../../../API/Auth";
import { useNavigate } from "react-router-dom";
import { deformatCategory } from "../../Products/ProductsPage/ProductsPage";
import "./ExpertCreatePage.css";
import EyeButton from "../../Common/EyeButton";

const categories = [
  "Smartphone",
  "TV",
  "PC",
  "Software",
  "Storage Device",
  "Other",
];

function ExpertCreatePage(props) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [password2, setPassword2] = useState("");
  const [name, setName] = useState("");
  const [surname, setSurname] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const [expertCategories, setExpertCategories] = useState([]);
  const updateExpertCategories = (event) => {
    event.target.checked
      ? setExpertCategories((expertCategories) => [
          ...expertCategories,
          event.target.id,
        ])
      : setExpertCategories((expertCategories) =>
          expertCategories.filter((e) => e !== event.target.id)
        );
  };

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

    if (expertCategories.length === 0) {
      setErrorMessage("Error in form: No product categories selected");
      return;
    }
    setLoading(true);
    createExpertAPI({
      firstName: name,
      lastName: surname,
      email: email,
      username: email,
      password: password,
      expertCategories: expertCategories,
    })
      .then(() => {
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
  }, []);

  let categoryCheckboxes = [];
  for (let category of categories) {
    categoryCheckboxes.push(
      <Form.Check
        className={"form-check"}
        style={{
          cursor: "pointer",
          color: "#DDDDDD",
          accentColor: "white",
          width: "200px",
          alignSelf: "center",
          textAlign: "left",
          margin: "auto",
          marginBottom: "10px",
        }}
        type={"checkbox"}
        id={deformatCategory(category)}
        key={deformatCategory(category)}
        label={category}
        checked={expertCategories.includes(deformatCategory(category))}
        onChange={updateExpertCategories}
      />
    );
  }

  const loggedIn = props.loggedIn;
  return (
    <>
      <AppNavbar
        user={props.user}
        loggedIn={loggedIn}
        logout={props.logout}
        selected="userscreate"
      />
      <div className="CenteredButton" style={{ marginTop: "50px" }}>
        <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>CREATE EXPERT</h1>
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

        <Row
          className="d-flex justify-content-center"
          style={{ marginBottom: "30px" }}
        >
          <Col className="col-md-4">
            <Form className="form" style={{ marginTop: "30px" }}>
              <Form.Group className="mb-3" controlId="formBasicEmail">
                <Form.Label style={{ color: "#DDDDDD" }}>
                  Expert Details
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
              </Form.Group>
              <Form.Group className="mb-3" controlId="formBasicEmail">
                <Form.Label style={{ color: "#DDDDDD" }}>
                  Email address
                </Form.Label>
                <Form.Control
                  value={email}
                  className={"form-control:focus"}
                  style={{
                    fontSize: 12,
                    width: "300px",
                    alignSelf: "center",
                    margin: "auto",
                  }}
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
                    style={{
                      fontSize: 12,
                      width: "300px",
                      alignSelf: "center",
                      margin: "auto",
                    }}
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
                <Form.Control
                  value={password2}
                  style={{
                    fontSize: 12,
                    width: "300px",
                    alignSelf: "center",
                    margin: "auto",
                    marginTop: "10px",
                  }}
                  type={showPassword ? "text" : "password"}
                  placeholder="Confirm Password"
                  onChange={(e) => setPassword2(e.target.value)}
                />
              </Form.Group>
            </Form>
          </Col>
          <Col className="col-md-4">
            <Form style={{ marginTop: "30px" }}>
              <Form.Group key={`default-checkbox`}>
                <Form.Label style={{ color: "#DDDDDD" }}>
                  Assigned Product Categories
                </Form.Label>
                {categoryCheckboxes}
              </Form.Group>
            </Form>
          </Col>
        </Row>
        {loading && (
          <>
            <Spinner style={{ color: "#A0C1D9" }} />
          </>
        )}
        <NavigationButton
          text={"Create new expert"}
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
        {errorMessage && (
          <ErrorMessage close={() => setErrorMessage("")} text={errorMessage} />
        )}
      </div>
    </>
  );
}

export default ExpertCreatePage;
