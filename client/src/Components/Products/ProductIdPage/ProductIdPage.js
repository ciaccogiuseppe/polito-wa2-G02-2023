import {Button, Form} from "react-bootstrap";
import AppNavbar from "../../AppNavbar/AppNavbar";
import {useState} from "react";
import {getProductDetails} from "../../../API/Products";




function ProductIdPage(props){
    const [productID, setProductID] = useState("");
    const [errMessage, setErrMessage] = useState("");
    const [response, setResponse] = useState("");
    function getProduct(){
        if(productID==""){
            setErrMessage("Product ID must not be empty");
            return;
        }
        else if ( productID.includes("/")){
            setErrMessage("Wrong ID format");
            return;
        }
        getProductDetails(productID).then(
            res => {
                setErrMessage("");
                setResponse(res);
            }
        ).catch(err => {
            setResponse("");
            setErrMessage(err.message);
        })
    }

    return <>
        <div style={{
            position: 'absolute',
            backgroundColor: '#222222',
            width: '100%',
            height: '100%'}}>
            <AppNavbar/>

            <div className="CenteredButton">
                
                <Form className="form">
                    <Form.Group className="mb-3">
                        <Form.Label className="text-info">Product ID</Form.Label>
                        <Form.Control style={{width: "400px", alignSelf:"center", margin:"auto"}} type="input" placeholder="Enter product ID" onChange={e => setProductID(e.target.value)}/>
                    </Form.Group>
                    <Button type="submit" variant="outline-info" style={{borderWidth:"2px"}} className="HomeButton" onClick={(e) => {e.preventDefault(); getProduct();}}>Search product</Button>
                </Form>
                <hr style={{color:"white", width:"90%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginTop:"20px"}}/>
                {response.productId?<h4 className="text-light" style={{marginTop:"10px"}}>Product ID</h4>:<></>}
                <div className="text-info">{response.productId}</div>
                {response.name?<h4 className="text-light" style={{marginTop:"10px"}}>Name</h4>:<></>}
                <div className="text-info">{response.name}</div>
                {response.brand?<h4 className="text-light" style={{marginTop:"10px"}}>Brand</h4>:<></>}
                <div className="text-info">{response.brand}</div>

                {errMessage?<h5 className="text-danger" style={{marginTop:"10px"}}>{errMessage}</h5>:<></>}

            </div>
        </div>
    </>
}

export default ProductIdPage;