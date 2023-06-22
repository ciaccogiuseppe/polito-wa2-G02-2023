import AppNavbar from "../../AppNavbar/AppNavbar";
import { Form } from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import { useEffect, useState } from "react";
import { getAllProducts } from "../../../API/Products";
import ErrorMessage from "../../Common/ErrorMessage";
import { reformatCategory } from "../ProductsPage/ProductsPage";
import { useNavigate } from "react-router-dom";
import { assignItemAPI } from "../../../API/Item";

function ProductRegisterPage(props) {
  const loggedIn = props.loggedIn;
  const [categories, setCategories] = useState([]);
  const [errorMessage, setErrorMessage] = useState("");
  const [brands, setBrands] = useState([]);
  const [brand, setBrand] = useState("");
  const [serialNum, setSerialNum] = useState("");
  const [category, setCategory] = useState("");
  const [products, setProducts] = useState([]);
  const [product, setProduct] = useState("");
  const [productsList, setProductsList] = useState([]);
  const [warranty, setWarranty] = useState([]);
  const [war1, setWar1] = useState("");
  const [war2, setWar2] = useState("");
  const [war3, setWar3] = useState("");
  const [war4, setWar4] = useState("");
  const [war5, setWar5] = useState("");
  useEffect(() => {
    window.scrollTo(0, 0);
    getAllProducts().then((products) => {
      setProducts(products);
      setCategories(
        products
          .map((p) => reformatCategory(p.category))
          .filter((v, i, a) => a.indexOf(v) === i)
          .sort()
      );
      //setBrands(products.map(p => p.brand).filter((v,i,a)=>a.indexOf(v)===i).sort())
    });
  }, []);

  useEffect(() => {
    setBrand("");
    setProduct("");
    setBrands(
      products
        .filter((p) => reformatCategory(p.category) === category)
        .map((p) => p.brand)
        .filter((v, i, a) => a.indexOf(v) === i)
        .sort()
    );
  }, [category, products]);

  useEffect(() => {
    setProduct("");
    setProductsList(
      products
        .filter(
          (p) => reformatCategory(p.category) === category && p.brand === brand
        )
        .map((p) => {
          return { name: p.name, id: p.productId };
        })
        .filter((v, i, a) => a.indexOf(v) === i)
        .sort((a, b) =>
          a.name.localeCompare(b.name, undefined, { numeric: true })
        )
    );
  }, [brand, category, products]);

  const navigate = useNavigate();
  function addProduct() {
    assignItemAPI({
      productId: product,
      serialNum: serialNum,
      uuid: warranty,
    })
      .then(() => navigate("/products"))
      .catch((err) => setErrorMessage(err));
  }

  useEffect(() => {
    setWarranty(war1 + "-" + war2 + "-" + war3 + "-" + war4 + "-" + war5);
  }, [war1, war2, war3, war4, war5]);

  return (
    <>
      <AppNavbar
        user={props.user}
        loggedIn={loggedIn}
        selected={"products"}
        logout={props.logout}
      />

      <div className="CenteredButton" style={{ marginTop: "50px" }}>
        <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>
          REGISTER PRODUCT
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
        <Form className="form" style={{ marginTop: "30px" }}>
          <Form.Group className="mb-3">
            {/*<Form.Label style={{color:"#DDDDDD"}}>Product ID</Form.Label>
                    <div>

                        <Form.Control maxLength={4} className={"form-control:focus"} value={pid1} onChange={(e) => setPid1(e.target.value)} placeholder={"XXXX"} style={{width: "70px", display:"inline-block", alignSelf:"center", margin:"auto", marginBottom:"20px", fontSize:13, marginRight:"8px", textAlign:"center"}}/>
                        <Form.Control maxLength={4} className={"form-control:focus"} value={pid2} onChange={(e) => setPid2(e.target.value)} placeholder={"XXXX"} style={{width: "70px", display:"inline-block", alignSelf:"center", margin:"auto", marginBottom:"20px", fontSize:13, marginRight:"8px", marginLeft:"8px", textAlign:"center"}}/>
                        <Form.Control maxLength={3} className={"form-control:focus"} value={pid3} onChange={(e) => setPid3(e.target.value)} placeholder={"XXX"} style={{width: "65px", display:"inline-block", alignSelf:"center", margin:"auto", marginBottom:"20px", fontSize:13, marginRight:"8px", marginLeft:"8px", textAlign:"center"}}/>
                        <Form.Control maxLength={2} className={"form-control:focus"} value={pid4} onChange={(e) => setPid4(e.target.value)} placeholder={"XX"} style={{width: "52px", display:"inline-block", alignSelf:"center", margin:"auto", marginBottom:"20px", fontSize:13, marginLeft:"8px", textAlign:"center"}}/>
                    </div>*/}

            <h5 style={{ color: "#DDDDDD", fontSize: 12, marginTop: "10px" }}>
              Category
            </h5>
            <Form.Select
              value={category}
              onChange={(e) => {
                setCategory(e.target.value);
              }}
              className={"form-select:focus"}
              style={{
                width: "300px",
                alignSelf: "center",
                margin: "auto",
                marginTop: "10px",
                fontSize: "12px",
              }}
            >
              <option></option>
              {categories.map((c) => (
                <option>{c}</option>
              ))}
            </Form.Select>

            <h5 style={{ color: "#DDDDDD", fontSize: 12, marginTop: "10px" }}>
              Brand
            </h5>
            <Form.Select
              disabled={category === ""}
              value={brand}
              onChange={(e) => setBrand(e.target.value)}
              className={"form-select:focus"}
              style={{
                width: "300px",
                alignSelf: "center",
                margin: "auto",
                marginTop: "10px",
                fontSize: "12px",
              }}
            >
              <option></option>
              {brands.map((b) => (
                <option>{b}</option>
              ))}
            </Form.Select>
            <h5 style={{ color: "#DDDDDD", fontSize: 12, marginTop: "10px" }}>
              Product
            </h5>

            <Form.Select
              disabled={category === "" || brand === ""}
              value={product}
              onChange={(e) => {
                setProduct(e.target.value);
              }}
              className={"form-select:focus"}
              style={{
                width: "300px",
                alignSelf: "center",
                margin: "auto",
                marginTop: "10px",
                fontSize: "12px",
              }}
            >
              <option></option>
              {productsList.map((p) => (
                <option value={p.id}>{p.name}</option>
              ))}
            </Form.Select>

            <Form.Label
              style={{ color: "#DDDDDD", fontSize: 12, marginTop: "10px" }}
            >
              Serial Number
            </Form.Label>
            <Form.Control
              value={serialNum}
              onChange={(e) => setSerialNum(e.target.value)}
              className={"form-control:focus"}
              placeholder={"Serial Number"}
              style={{
                width: "300px",
                alignSelf: "center",
                margin: "auto",
                marginBottom: "20px",
                fontSize: 12,
              }}
            />

            <h5 style={{ color: "#DDDDDD", fontSize: 12, marginTop: "10px" }}>
              Warranty code
            </h5>
            <div>
              <Form.Control
                maxLength={8}
                className={"form-control:focus"}
                value={war1}
                onChange={(e) => setWar1(e.target.value)}
                placeholder={"XXXXXXXX"}
                style={{
                  width: "100px",
                  display: "inline-block",
                  alignSelf: "center",
                  margin: "auto",
                  marginBottom: "20px",
                  fontSize: 11,
                  textAlign: "center",
                }}
              />
              <span style={{ color: "white", fontSize: 12, margin: "5px" }}>
                -
              </span>
              <Form.Control
                maxLength={4}
                className={"form-control:focus"}
                value={war2}
                onChange={(e) => setWar2(e.target.value)}
                placeholder={"XXXX"}
                style={{
                  width: "70px",
                  display: "inline-block",
                  alignSelf: "center",
                  margin: "auto",
                  marginBottom: "20px",
                  fontSize: 11,
                  textAlign: "center",
                }}
              />
              <span style={{ color: "white", fontSize: 12, margin: "5px" }}>
                -
              </span>
              <Form.Control
                maxLength={4}
                className={"form-control:focus"}
                value={war3}
                onChange={(e) => setWar3(e.target.value)}
                placeholder={"XXXX"}
                style={{
                  width: "70px",
                  display: "inline-block",
                  alignSelf: "center",
                  margin: "auto",
                  marginBottom: "20px",
                  fontSize: 11,
                  textAlign: "center",
                }}
              />
              <span style={{ color: "white", fontSize: 12, margin: "5px" }}>
                -
              </span>
              <Form.Control
                maxLength={4}
                className={"form-control:focus"}
                value={war4}
                onChange={(e) => setWar4(e.target.value)}
                placeholder={"XXXX"}
                style={{
                  width: "70px",
                  display: "inline-block",
                  alignSelf: "center",
                  margin: "auto",
                  marginBottom: "20px",
                  fontSize: 11,
                  textAlign: "center",
                }}
              />
              <span style={{ color: "white", fontSize: 12, margin: "5px" }}>
                -
              </span>
              <Form.Control
                maxLength={12}
                className={"form-control:focus"}
                value={war5}
                onChange={(e) => setWar5(e.target.value)}
                placeholder={"XXXXXXXXXXXX"}
                style={{
                  width: "140px",
                  display: "inline-block",
                  alignSelf: "center",
                  margin: "auto",
                  marginBottom: "20px",
                  fontSize: 11,
                  textAlign: "center",
                }}
              />
            </div>
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

export default ProductRegisterPage;
