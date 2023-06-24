import AppNavbar from "../../AppNavbar/AppNavbar";
import React, { useEffect, useState } from "react";
import NavigationButton from "../../Common/NavigationButton";
import { useNavigate } from "react-router-dom";
import AddButton from "../../Common/AddButton";
import ClientProductsTable from "./ClientProductsTable";
import { getAllItemsAPI } from "../../../API/Item";
import { Spinner } from "react-bootstrap";
import ErrorMessage from "../../Common/ErrorMessage";

export function reformatCategory(category) {
  switch (category) {
    case "SMARTPHONE":
      return "Smartphone";
    case "PC":
      return "PC";
    case "TV":
      return "TV";
    case "SOFTWARE":
      return "Software";
    case "STORAGE_DEVICE":
      return "Storage Device";
    case "OTHER":
      return "Other";
    default:
      return "";
  }
}

export function deformatCategory(category) {
  switch (category) {
    case "":
      return "";
    case "Smartphone":
      return "SMARTPHONE";
    case "PC":
      return "PC";
    case "TV":
      return "TV";
    case "Software":
      return "SOFTWARE";
    case "Storage Device":
      return "STORAGE_DEVICE";
    case "Other":
      return "OTHER";
    default:
      return "";
  }
}

function ClientProductsPage(props) {
  const [productsList, setProductsList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState(false);

  useEffect(() => {
    window.scrollTo(0, 0);
    setLoading(true);
    getAllItemsAPI()
      .then((products) => {
        setProductsList(products);
        setLoading(false);
      })
      .catch(() => {
        setErrorMessage("Error loading products");
        setLoading(false);
      });
  }, []);

  const loggedIn = props.loggedIn;
  const navigate = useNavigate();

  return (
    <>
      <AppNavbar
        user={props.user}
        logout={props.logout}
        loggedIn={loggedIn}
        selected={"products"}
      />

      <div className="CenteredButton" style={{ marginTop: "50px" }}>
        <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>MY PRODUCTS</h1>
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

        {loading && (
          <>
            <Spinner style={{ color: "#A0C1D9" }} />
          </>
        )}
        {errorMessage && (
          <>
            <div style={{ margin: "10px" }}>
              <ErrorMessage
                text={errorMessage}
                close={() => {
                  setErrorMessage("");
                }}
              />{" "}
            </div>
          </>
        )}
        {productsList.length > 0 ? (
          <ClientProductsTable products={productsList} />
        ) : (
          <>
            <div className="CenteredButton" style={{ marginTop: "70px" }}>
              <NavigationButton
                text={"Register a product"}
                onClick={(e) => {
                  e.preventDefault();
                  navigate("/productregister");
                }}
              />
            </div>
          </>
        )}

        <div style={{ position: "fixed", bottom: "24px", right: "24px" }}>
          <AddButton onClick={() => navigate("/productregister")} />
        </div>
      </div>
    </>
  );
}

export default ClientProductsPage;
