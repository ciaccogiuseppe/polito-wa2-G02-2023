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
                            <td className="text-info">{product.productId}</td>
                            <td className="text-info">{product.name}</td>
                            <td className="text-info">{product.brand}</td>
                        </tr>));
                }
            }
        ).catch(err => {
            setProductsList([]);
            setErrMessage(err.message);
        })
    }

    useEffect(() => getProducts(), []);
    return <>
        <div style={{
            position: 'absolute',
            backgroundColor: '#222222',
            width: '100%',
            height: '100%'
        }}>
            <AppNavbar />

            {productsList.length > 0 ?
                <div>
                    <table className="table table-striped table-dark" style={{ alignContent: "center", width: "70%", margin: "auto", marginTop: 30 }}>
                        <thead>
                            <tr className="text-light">
                                <th><h4>ProductID</h4></th>
                                <th><h4>Name</h4></th>
                                <th><h4>Brand</h4></th>
                            </tr>
                        </thead>
                        <tbody>
                            {productsList}
                        </tbody>
                    </table>
                    <hr style={{ color: "white", width: "90%", alignSelf: "center", marginLeft: "auto", marginRight: "auto", marginTop: "20px" }} />
                </div>
                : <></>}

            <div className="CenteredButton">
                {errMessage ? <h5 className="text-danger" style={{ marginTop: "100px" }}>{errMessage}</h5> : <></>}
            </div>
        </div>
    </>
}

export default ProductsPage;