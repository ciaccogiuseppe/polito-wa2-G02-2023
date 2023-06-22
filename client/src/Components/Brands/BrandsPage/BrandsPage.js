import AppNavbar from "../../AppNavbar/AppNavbar";
import { useEffect, useState } from "react";
import NavigationButton from "../../Common/NavigationButton";
import { useNavigate } from "react-router-dom";
import AddButton from "../../Common/AddButton";
import { Row } from "react-bootstrap";
import { getAllBrands } from "../../../API/Products";
import BrandsTable from "./BrandsTable";

function BrandsPage(props) {
  const [brandsList, setBrandsList] = useState([]);

  useEffect(() => {
    window.scrollTo(0, 0);
    getAllBrands().then((brands) => {
      setBrandsList(brands.map((b) => b.name).sort());
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
        selected={"brands"}
      />

      <div className="CenteredButton" style={{ marginTop: "50px" }}>
        <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>BRANDS</h1>
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

        <BrandsTable brands={brandsList} />
        <div style={{ position: "fixed", bottom: "24px", right: "24px" }}>
          <AddButton onClick={() => navigate("/newbrand")} />
        </div>
      </div>
    </>
  );
}

export default BrandsPage;
