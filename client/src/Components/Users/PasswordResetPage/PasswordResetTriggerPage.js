import AppNavbar from "../../AppNavbar/AppNavbar";
import { useEffect, useState } from "react";
import { Form } from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import { useNavigate } from "react-router-dom";
import { passwordResetTriggerAPI } from "../../../API/Auth";
import ErrorMessage from "../../Common/ErrorMessage";
import SuccessMessage from "../../Common/SuccessMessage";

function PasswordResetTriggerPage(props) {
  const loggedIn = props.loggedIn;
  const [errorMessage, setErrorMessage] = useState("");
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [successMessage, setSuccessMessage] = useState(false);

  function reset() {
    passwordResetTriggerAPI(email)
      .then((response) => {
        setSuccessMessage(true);
      })
      .catch((err) => {
        console.log(err);
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
          </Form.Group>
          <div style={{ margin: "20px" }}>
            <NavigationButton
              text={"Reset password"}
              type={"submit"}
              onClick={(e) => {
                e.preventDefault();
                reset();
              }}
            />
          </div>

          <NavigationButton
            text={"Back to login"}
            onClick={(e) => {
              e.preventDefault();
              navigate("/login");
            }}
          />
        </Form>
        {successMessage && (
          <SuccessMessage
            text={"Password reset link sent, check your inbox mail"}
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

export default PasswordResetTriggerPage;
