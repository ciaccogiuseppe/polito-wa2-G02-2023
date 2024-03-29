import AppNavbar from "../../AppNavbar/AppNavbar";
import { Form, Spinner } from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  updateClientAPI,
  updateExpertAPI,
  updateManagerAPI,
} from "../../../API/Profiles";
import ErrorMessage from "../../Common/ErrorMessage";
import { getProfileInfo } from "../../../API/Auth";

//const Profile = {firstName:"Mario", lastName:"Rossi", email:"mariorossi@polito.it", address:"Corso Duca degli Abruzzi, 24 - Torino"}

function ProfileUpdatePage(props) {
  const loggedIn = props.loggedIn;
  const [Profile, setProfile] = useState({});
  const navigate = useNavigate();

  const [loading, setLoading] = useState(false);
  const [firstName, setFirstName] = useState(Profile.name || "");
  const [lastName, setLastName] = useState(Profile.surname || "");
  const [address, setAddress] = useState(
    (Profile.address && Profile.address.address) || ""
  );
  const [errorMessage, setErrorMessage] = useState("");

  const [country, setCountry] = useState(
    (Profile.address && Profile.address.country) || ""
  );
  const [region, setRegion] = useState(
    (Profile.address && Profile.address.region) || ""
  );
  const [city, setCity] = useState(
    (Profile.address && Profile.address.city) || ""
  );
  function updateProfile() {
    setLoading(true);
    if (Profile.role === "CLIENT") {
      updateClientAPI(
        {
          firstName: firstName,
          lastName: lastName,
          address: {
            country: country,
            region: region,
            city: city,
            address: address,
          },
          email: Profile.email,
          username: Profile.email,
          expertCategories: [],
        },
        Profile.email
      )
        .then(() => {
          setLoading(false);
          navigate("/profileinfo", {
            replace: true,
            state: { message: "Profile updated successfully" },
          });
        })
        .catch((err) => {
          setLoading(false);
          setErrorMessage(err);
        });
    } else if (Profile.role === "EXPERT") {
      updateExpertAPI(
        {
          firstName: firstName,
          lastName: lastName,
          address: {},
          email: Profile.email,
          username: Profile.email,
          expertCategories: Profile.expertCategories,
        },
        Profile.email
      )
        .then(() => {
          setLoading(false);
          navigate("/profileinfo", {
            replace: true,
            state: { message: "Profile updated successfully" },
          });
        })
        .catch((err) => {
          setLoading(false);
          setErrorMessage(err);
        });
    } else if (Profile.role === "MANAGER") {
      updateManagerAPI(
        {
          firstName: firstName,
          lastName: lastName,
          address: {},
          email: Profile.email,
          username: Profile.email,
          expertCategories: [],
        },
        Profile.email
      )
        .then(() => {
          setLoading(false);
          navigate("/profileinfo", {
            replace: true,
            state: { message: "Profile updated successfully" },
          });
        })
        .catch((err) => {
          setLoading(false);
          setErrorMessage(err);
        });
    }
  }
  useEffect(() => {
    window.scrollTo(0, 0);
    getProfileInfo()
      .then((response) => {
        setProfile(response.data);
      })
      .catch((err) => {
        setErrorMessage("Unable to fetch data from server: " + err);
      });
  }, []);

  useEffect(() => {
    setFirstName(Profile.name);
    setLastName(Profile.surname);
    if (Profile.address) {
      setCountry(Profile.address.country);
      setRegion(Profile.address.region);
      setCity(Profile.address.city);
      setAddress(Profile.address.address);
    }
  }, [Profile]);

  return (
    <>
      <AppNavbar
        user={props.user}
        loggedIn={loggedIn}
        selected={"profile"}
        logout={props.logout}
      />

      <div className="CenteredButton" style={{ marginTop: "50px" }}>
        <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>PROFILE EDIT</h1>
        <hr
          style={{
            color: "white",
            width: "25%",
            alignSelf: "center",
            marginLeft: "auto",
            marginRight: "auto",
            marginBottom: "20px",
            marginTop: "2px",
          }}
        />
        <div
          style={{
            width: "350px",
            borderRadius: "25px",
            marginTop: "20px",
            paddingTop: "5px",
            paddingBottom: "20px",
            margin: "auto",
            backgroundColor: "rgba(0,0,0,0.1)",
          }}
        >
          <div>
            <h5 style={{ color: "white" }}>First name</h5>
            <Form.Control
              value={firstName}
              className={"form-control:focus"}
              style={{
                width: "300px",
                alignSelf: "center",
                margin: "auto",
                fontSize: 12,
              }}
              placeholder="First Name"
              onChange={(e) => setFirstName(e.target.value)}
            />
          </div>
          <div style={{ marginTop: "15px" }}>
            <h5 style={{ color: "white" }}>Last name</h5>
            <Form.Control
              value={lastName}
              className={"form-control:focus"}
              style={{
                width: "300px",
                alignSelf: "center",
                margin: "auto",
                fontSize: 12,
              }}
              placeholder="Last Name"
              onChange={(e) => setLastName(e.target.value)}
            />
          </div>

          <div style={{ marginTop: "15px" }}>
            <h5 style={{ color: "white" }}>E-mail</h5>
            <Form.Control
              value={Profile.email || ""}
              disabled={true}
              className={"form-control:focus"}
              style={{
                width: "300px",
                alignSelf: "center",
                margin: "auto",
                fontSize: 12,
                opacity: "0.8",
              }}
              placeholder="E-mail"
              onChange={(e) => {}}
            />
          </div>

          {Profile.role === "CLIENT" && (
            <div style={{ marginTop: "15px" }}>
              <h5 style={{ color: "white" }}>Address</h5>
              <Form.Control
                value={country || ""}
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
                value={region || ""}
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
                value={city || ""}
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
                value={address || ""}
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
          )}
        </div>
        {errorMessage && (
          <ErrorMessage close={() => setErrorMessage("")} text={errorMessage} />
        )}

        {loading && (
          <>
            <Spinner style={{ color: "#A0C1D9" }} />
          </>
        )}
        <div style={{ marginTop: "20px" }}>
          <NavigationButton
            text={"Submit"}
            onClick={(e) => {
              e.preventDefault();
              updateProfile();
            }}
          />
        </div>
        <div style={{ marginTop: "20px" }}>
          <NavigationButton
            text={"Back"}
            onClick={(e) => {
              e.preventDefault();
              navigate("/profileinfo");
            }}
          />
        </div>
      </div>
    </>
  );
}

export default ProfileUpdatePage;
