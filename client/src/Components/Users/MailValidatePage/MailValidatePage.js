import AppNavbar from "../../AppNavbar/AppNavbar";
import React, { useEffect, useState } from "react";
import { Spinner } from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import { useNavigate, useParams } from "react-router-dom";
import { validateMailAPI } from "../../../API/Auth";
import ErrorMessage from "../../Common/ErrorMessage";

import SuccessMessage from "../../Common/SuccessMessage";

function MailValidatePage(props) {
  const navigate = useNavigate();
  const token = useParams().id;

  const loggedIn = props.loggedIn;
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState(false);
  const [loading, setLoading] = useState(false);

  const isExpired = false;

  useEffect(() => {
    window.scrollTo(0, 0);
    if (token) {
      setLoading(true);
      validateMailAPI({
        token: token,
      })
        .then((response) => {
          setSuccessMessage(true);
          setLoading(false);
        })
        .catch((err) => {
          setErrorMessage(err);
          setLoading(false);
        });
    }
  }, [token]);

  return (
    <>
      <AppNavbar user={props.user} loggedIn={loggedIn} logout={props.logout} />
      <div className="CenteredButton" style={{ marginTop: "50px" }}>
        <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>
          EMAIL VALIDATION
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
        {loading && (
          <div>
            <Spinner style={{ color: "#A0C1D9" }} />
          </div>
        )}
        {!isExpired ? (
          <>
            {successMessage && (
              <div style={{ margin: "10px" }}>
                <SuccessMessage
                  text={"Email validated correctly"}
                  noClose={true}
                  onClose={() => setSuccessMessage(false)}
                />
              </div>
            )}

            <NavigationButton
              text={"Back to home"}
              onClick={() => {
                navigate("/");
              }}
            />
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
              Email verification token has expired, generate a new one
            </h5>
            <div style={{ margin: "10px" }}>
              <NavigationButton
                text={"Generate a new token"}
                onClick={() => {}}
              />
            </div>
            <NavigationButton
              text={"Back to home"}
              onClick={() => {
                navigate("/");
              }}
            />
          </>
        )}

        {errorMessage && (
          <ErrorMessage close={() => setErrorMessage("")} text={errorMessage} />
        )}
      </div>
    </>
  );
}

export default MailValidatePage;
