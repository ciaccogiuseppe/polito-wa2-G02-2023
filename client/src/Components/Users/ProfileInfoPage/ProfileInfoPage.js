import AppNavbar from "../../AppNavbar/AppNavbar";
import { Spinner } from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { getProfileInfo } from "../../../API/Auth";
import SuccessMessage from "../../Common/SuccessMessage";
import { reformatCategory } from "../../Products/ProductsPage/ProductsPage";

function ProfileInfoPage(props) {
  const location = useLocation();
  let message = location.state && location.state.message;
  const loggedIn = props.loggedIn;
  const [Profile, setProfile] = useState(null);
  const [successMessage, setSuccessMessage] = useState(message || "");
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    window.scrollTo(0, 0);
    getProfileInfo()
      .then((response) => {
        setProfile(response.data);
      })
      .catch((err) => console.log(err));
  }, []);

  useEffect(() => {
    if (Profile !== null) {
      setLoading(false);
    }
  }, [Profile]);


  return (
    <>
      <AppNavbar
        user={props.user}
        logout={props.logout}
        loggedIn={loggedIn}
        selected={"profile"}
      />

      <div className="CenteredButton" style={{ marginTop: "50px" }}>
        <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>PROFILE INFO</h1>
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
            paddingBottom: "5px",
            margin: "auto",
            backgroundColor: "rgba(0,0,0,0.1)",
          }}
        >
          {loading ? (
            <>
              <Spinner style={{ color: "#A0C1D9" }} />
            </>
          ) : (
            <>
              <div>
                <h5 style={{ color: "white" }}>First name</h5>
                <hr
                  style={{
                    color: "white",
                    width: "125px",
                    alignSelf: "center",
                    marginLeft: "auto",
                    marginRight: "auto",
                    marginBottom: "4px",
                    marginTop: "4px",
                  }}
                />
                <h5 style={{ color: "#e3e3e3", fontSize: 13 }}>
                  {Profile.name}
                </h5>
              </div>
              <div>
                <h5 style={{ color: "white" }}>Last name</h5>
                <hr
                  style={{
                    color: "white",
                    width: "125px",
                    alignSelf: "center",
                    marginLeft: "auto",
                    marginRight: "auto",
                    marginBottom: "4px",
                    marginTop: "4px",
                  }}
                />
                <h5 style={{ color: "#e3e3e3", fontSize: 13 }}>
                  {Profile.surname}
                </h5>
              </div>

              <div>
                <h5 style={{ color: "white" }}>E-mail</h5>
                <hr
                  style={{
                    color: "white",
                    width: "125px",
                    alignSelf: "center",
                    marginLeft: "auto",
                    marginRight: "auto",
                    marginBottom: "4px",
                    marginTop: "4px",
                  }}
                />
                <h5 style={{ color: "#e3e3e3", fontSize: 13 }}>
                  {Profile.email}
                </h5>
              </div>

              {Profile.role === "EXPERT" && (
                <div>
                  <h5 style={{ color: "white" }}>Assigned categories</h5>
                  <hr
                    style={{
                      color: "white",
                      width: "125px",
                      alignSelf: "center",
                      marginLeft: "auto",
                      marginRight: "auto",
                      marginBottom: "4px",
                      marginTop: "4px",
                    }}
                  />
                  <h5 style={{ color: "#e3e3e3", fontSize: 13 }}>
                    {Profile.expertCategories
                      .map((c) => reformatCategory(c))
                      .toString()
                      .replaceAll(",", ", ")}
                  </h5>
                </div>
              )}

              {Profile.address && Profile.role === "CLIENT" && (
                <div>
                  <h5 style={{ color: "white" }}>Address</h5>
                  <hr
                    style={{
                      color: "white",
                      width: "125px",
                      alignSelf: "center",
                      marginLeft: "auto",
                      marginRight: "auto",
                      marginBottom: "4px",
                      marginTop: "4px",
                    }}
                  />
                  <h5 style={{ color: "#e3e3e3", fontSize: 13 }}>
                    {Profile.address &&
                      Profile.address.country +
                        ", " +
                        Profile.address.region +
                        ", " +
                        Profile.address.city +
                        ", " +
                        Profile.address.address}
                  </h5>
                </div>
              )}
            </>
          )}
        </div>

        {successMessage && (
          <SuccessMessage
            text={successMessage}
            close={() => setSuccessMessage("")}
          />
        )}

        <div style={{ marginTop: "20px" }}>
          <NavigationButton
            text={"Edit profile"}
            onClick={(e) => {
              e.preventDefault();
              navigate("/profileupdate");
            }}
          />
        </div>
      </div>
    </>
  );
}

export default ProfileInfoPage;
