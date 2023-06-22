import AppNavbar from "../../AppNavbar/AppNavbar";
import { Form } from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import { useEffect, useState } from "react";
import {
  addProductAPI,
  getAllBrands,
  getAllCategories,
} from "../../../API/Products";
import ErrorMessage from "../../Common/ErrorMessage";
import {
  deformatCategory,
  reformatCategory,
} from "../ProductsPage/ProductsPage";
import { useNavigate } from "react-router-dom";

function ProductCreatePage(props) {
  const loggedIn = props.loggedIn;
  const [categories, setCategories] = useState([]);
  const [errorMessage, setErrorMessage] = useState("");
  const [brands, setBrands] = useState([]);
  const [productId, setProductId] = useState("");
  const [brand, setBrand] = useState("");
  const [category, setCategory] = useState("");
  const [name, setName] = useState("");
  const [pid1, setPid1] = useState("");
  const [pid2, setPid2] = useState("");
  const [pid3, setPid3] = useState("");
  const [pid4, setPid4] = useState("");
  useEffect(() => {
    window.scrollTo(0, 0);
    getAllCategories()
      .then((categories) => {
        setCategories(
          categories.map((c) => reformatCategory(c.categoryName)).sort()
        );
      })
      .catch((err) => console.log(err));
    getAllBrands().then((brands) => {
      setBrands(brands.map((b) => b.name).sort());
    });
  }, []);

  const navigate = useNavigate();
  function addProduct() {
    addProductAPI({
      productId: productId,
      brand: brand,
      category: deformatCategory(category),
      name: name,
    })
      .then(() => navigate("/products"))
      .catch((err) => setErrorMessage(err));
  }

  useEffect(() => {
    setProductId(pid1 + pid2 + pid3 + pid4);
  }, [pid1, pid2, pid3, pid4]);

  return (
    <>
      <AppNavbar
        user={props.user}
        loggedIn={loggedIn}
        selected={"products"}
        logout={props.logout}
      />

      <div className="CenteredButton" style={{ marginTop: "50px" }}>
        <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>INSERT PRODUCT</h1>
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
          onSubmit={() => addProduct()}
        >
          <Form.Group className="mb-3">
            <Form.Label style={{ color: "#DDDDDD" }}>Product ID</Form.Label>
            <div>
              <Form.Control
                maxLength={4}
                className={"form-control:focus"}
                value={pid1}
                onChange={(e) => setPid1(e.target.value)}
                placeholder={"XXXX"}
                style={{
                  width: "70px",
                  display: "inline-block",
                  alignSelf: "center",
                  margin: "auto",
                  marginBottom: "20px",
                  fontSize: 13,
                  marginRight: "8px",
                  textAlign: "center",
                }}
              />
              <Form.Control
                maxLength={4}
                className={"form-control:focus"}
                value={pid2}
                onChange={(e) => setPid2(e.target.value)}
                placeholder={"XXXX"}
                style={{
                  width: "70px",
                  display: "inline-block",
                  alignSelf: "center",
                  margin: "auto",
                  marginBottom: "20px",
                  fontSize: 13,
                  marginRight: "8px",
                  marginLeft: "8px",
                  textAlign: "center",
                }}
              />
              <Form.Control
                maxLength={3}
                className={"form-control:focus"}
                value={pid3}
                onChange={(e) => setPid3(e.target.value)}
                placeholder={"XXX"}
                style={{
                  width: "65px",
                  display: "inline-block",
                  alignSelf: "center",
                  margin: "auto",
                  marginBottom: "20px",
                  fontSize: 13,
                  marginRight: "8px",
                  marginLeft: "8px",
                  textAlign: "center",
                }}
              />
              <Form.Control
                maxLength={2}
                className={"form-control:focus"}
                value={pid4}
                onChange={(e) => setPid4(e.target.value)}
                placeholder={"XX"}
                style={{
                  width: "52px",
                  display: "inline-block",
                  alignSelf: "center",
                  margin: "auto",
                  marginBottom: "20px",
                  fontSize: 13,
                  marginLeft: "8px",
                  textAlign: "center",
                }}
              />
            </div>

            <Form.Label style={{ color: "#DDDDDD" }}>Category</Form.Label>
            <Form.Select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className={"form-select:focus"}
              style={{
                width: "300px",
                alignSelf: "center",
                margin: "auto",
                marginBottom: "20px",
              }}
            >
              <option></option>
              {categories.map((c) => (
                <option>{c}</option>
              ))}
            </Form.Select>
            <Form.Label style={{ color: "#DDDDDD" }}>Brand</Form.Label>
            <Form.Select
              value={brand}
              onChange={(e) => setBrand(e.target.value)}
              className={"form-select:focus"}
              style={{
                width: "300px",
                alignSelf: "center",
                margin: "auto",
                marginBottom: "20px",
              }}
            >
              <option></option>
              {brands.map((b) => (
                <option>{b}</option>
              ))}
            </Form.Select>
            <Form.Label style={{ color: "#DDDDDD" }}>Name</Form.Label>
            <Form.Control
              value={name}
              onChange={(e) => setName(e.target.value)}
              className={"form-control:focus"}
              placeholder={"Product Name"}
              style={{
                width: "300px",
                alignSelf: "center",
                margin: "auto",
                marginBottom: "20px",
              }}
            />
          </Form.Group>
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

          <NavigationButton
            type={"submit"}
            text={"Insert"}
            onClick={(e) => {
              e.preventDefault();
              addProduct();
            }}
          />
        </Form>
      </div>
    </>
  );
}

export default ProductCreatePage;
