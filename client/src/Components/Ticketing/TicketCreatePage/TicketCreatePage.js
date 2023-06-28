import AppNavbar from "../../AppNavbar/AppNavbar";
import { Form, Spinner } from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import React, { useEffect, useState } from "react";
import { getAllProducts } from "../../../API/Products";
import { reformatCategory } from "../../Products/ProductsPage/ProductsPage";
import ErrorMessage from "../../Common/ErrorMessage";
import { addTicketAPI } from "../../../API/Tickets";
import { useNavigate } from "react-router-dom";
import { getAllItemsAPI } from "../../../API/Item";

function TicketCreatePage(props) {
  const loggedIn = props.loggedIn;
  const [title, setTitle] = useState("");
  const [loading, setLoading] = useState(false);
  const [description, setDescription] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [brands, setBrands] = useState([]);
  const [categories, setCategories] = useState([]);
  const [category, setCategory] = useState("");
  const [brand, setBrand] = useState("");
  const [products, setProducts] = useState([]);
  const [product, setProduct] = useState("");
  const [serialNum, setSerialNum] = useState("");
  const [serialNums, setSerialNums] = useState([]);
  const [productsList, setProductsList] = useState([]);

  useEffect(() => {
    window.scrollTo(0, 0);
    getAllProducts()
      .then((prods) => {
        getAllItemsAPI().then((ps) => {
          const pd = ps.map((p) => {
            return {
              ...p,
              product: prods.filter((pd) => p.productId === pd.productId)[0],
            };
          });
          setProducts(pd);
        });
      })
      .catch((err) => {
        setErrorMessage("Unable to fetch data from server: " + err);
      });
  }, []);

  useEffect(() => {
    if (products.length > 0)
      setCategories(
        products
          .map((p) => reformatCategory(p.product.category))
          .filter((v, i, a) => a.indexOf(v) === i)
          .sort()
      );
  }, [products]);

  useEffect(() => {
    setBrand("");
    setProduct("");
    setBrands(
      products
        .filter((p) => reformatCategory(p.product.category) === category)
        .map((p) => p.product.brand)
        .filter((v, i, a) => a.indexOf(v) === i)
        .sort()
    );
  }, [category, products]);

  useEffect(() => {
    setProduct("");
    setProductsList(
      products
        .filter(
          (p) =>
            reformatCategory(p.product.category) === category &&
            p.product.brand === brand
        )
        .map((p) => {
          return { name: p.product.name, id: p.productId };
        })
        .filter((v, i, a) => a.findIndex((v) => v.id) === i)
        .sort((a, b) =>
          a.name.localeCompare(b.name, undefined, { numeric: true })
        )
    );
  }, [brand, category, products]);

  useEffect(() => {
    setSerialNum("");
    setSerialNums(
      products
        .filter(
          (p) =>
            reformatCategory(p.product.category) === category &&
            p.product.brand === brand &&
            p.productId === product
        )
        .map((p) => p.serialNum)
        .filter((v, i, a) => a.indexOf(v) === i)
        .sort((a, b) => a < b)
    );
  }, [product, brand, category, products]);

  const navigate = useNavigate();
  function addTicket() {
    setLoading(true);
    addTicketAPI({
      title: title,
      description: description,
      productId: product,
      serialNum: parseInt(serialNum),
    })
      .then((response) => {
        navigate("/tickets/" + response.data.ticketId);
        setLoading(false);
      })
      .catch((err) => {
        setErrorMessage(err);
        setLoading(false);
      });
  }

  return (
    <>
      <AppNavbar
        user={props.user}
        logout={props.logout}
        loggedIn={loggedIn}
        selected={"tickets"}
      />

      <div className="CenteredButton" style={{ marginTop: "50px" }}>
        <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>OPEN A TICKET</h1>
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
          <Form.Group className="mb-3" controlId="formBasicEmail">
            <Form.Label style={{ color: "#DDDDDD" }}>Ticket Info</Form.Label>

            <Form.Control
              value={title}
              className={"form-control:focus"}
              style={{
                width: "300px",
                fontSize: "12px",
                alignSelf: "center",
                margin: "auto",
              }}
              type="input"
              placeholder="Ticket Title"
              onChange={(e) => setTitle(e.target.value)}
            />
            <Form.Control
              value={description}
              className={"form-control:focus"}
              style={{
                width: "300px",
                fontSize: "12px",
                alignSelf: "center",
                margin: "auto",
                marginTop: "10px",
              }}
              type="textarea"
              as={"textarea"}
              placeholder="Ticket Description"
              onChange={(e) => setDescription(e.target.value)}
            />
          </Form.Group>

          <Form.Group className="mb-3" controlId="formBasicEmail">
            <Form.Label style={{ color: "#DDDDDD" }}>Product</Form.Label>
            <hr
              style={{
                color: "white",
                width: "150px",
                alignSelf: "center",
                marginLeft: "auto",
                marginRight: "auto",
                marginBottom: "2px",
                marginTop: "2px",
              }}
            />

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

            <h5 style={{ color: "#DDDDDD", fontSize: 12, marginTop: "10px" }}>
              Serial Number
            </h5>

            <Form.Select
              disabled={category === "" || brand === "" || product === ""}
              value={serialNum}
              onChange={(e) => {
                setSerialNum(e.target.value);
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
              {serialNums.map((p) => (
                <option value={p}>{p}</option>
              ))}
            </Form.Select>
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
          {loading && (
            <div>
              <Spinner style={{ color: "#A0C1D9" }} />
            </div>
          )}
          <NavigationButton
            disabled={
              category === "" ||
              brand === "" ||
              product === "" ||
              title === "" ||
              description === "" ||
              serialNum === ""
            }
            text={"Create Ticket"}
            onClick={(e) => {
              e.preventDefault();
              addTicket();
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
        </Form>
      </div>
    </>
  );
}

export default TicketCreatePage;
