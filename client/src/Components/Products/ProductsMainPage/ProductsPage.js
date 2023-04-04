import AppNavbar from "../../AppNavbar/AppNavbar";
import { useEffect, useState } from "react";
import { getAllProducts } from "../../../API/Products";

function ProductsPage(props) {
    const [errMessage, setErrMessage] = useState("");
    const [productsList, setProductsList] = useState([]);
    function getProducts() {
        getAllProducts().then(
            res => {
                setErrMessage("");
                setProductsList([]);
                for (let product of res) {
                    setProductsList((oldList) => oldList.concat(
                    <tr key={product.productId}>
                        <td>{product.productId}</td>
                        <td>{product.name}</td>
                        <td>{product.brand}</td>
                        </tr>));
                }
            }
        ).catch(err => {
            setProductsList([]);
            setErrMessage(err.message);
        })
    }

    useEffect(() => getProducts(), []);
    console.log(productsList)
    return <>
        <div style={{
            position: 'absolute',
            backgroundColor: '#222222',
            width: '100%',
            height: '100%'
        }}>
            <AppNavbar />

            {productsList.length > 0 ?
                <div className="text-info">
                    <table style={{ alignContent: "center", width:"70%", margin: "auto", marginTop: 30}}>
                        <tr>
                            <th className="text-light"><h4>ProductID</h4></th>
                            <th className="text-light"><h4>Name</h4></th>
                            <th className="text-light"><h4>Brand</h4></th>
                        </tr>
                        {productsList}
                    </table>
                </div>
                : <></>}

            <div className="CenteredButton">
                {errMessage ? <h5 className="text-danger" style={{ marginTop: "100px" }}>{errMessage}</h5> : <></>}
            </div>
        </div>
    </>
}

export default ProductsPage;