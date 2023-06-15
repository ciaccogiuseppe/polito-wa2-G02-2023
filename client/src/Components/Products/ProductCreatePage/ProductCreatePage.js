import AppNavbar from "../../AppNavbar/AppNavbar";
import {Form} from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import {useState} from "react";

function ProductCreatePage(props) {
    const loggedIn=props.loggedIn
    const [title, setTitle] = useState("")
    const [description, setDescription] = useState("")
    const [file, setFile] = useState([])


    function formElement(val, setVal) {
        return <Form.Control value={val} className={"form-control:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", marginTop:"10px"}} type="file" onChange={e => setVal(e.target.value)}/>

    }

    return <>
        <AppNavbar loggedIn={loggedIn} selected={"products"}/>


        <div className="CenteredButton" style={{marginTop:"50px"}}>
            <h1 style={{color:"#EEEEEE", marginTop:"80px"}}>INSERT PRODUCT</h1>
            <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>
            <Form className="form" style={{marginTop:"30px"}}>
                <Form.Group className="mb-3">
                    <Form.Label style={{color:"#DDDDDD"}}>Product ID</Form.Label>
                    <Form.Control  className={"form-control:focus"} placeholder={"Product ID"} style={{width: "300px", alignSelf:"center", margin:"auto", marginBottom:"20px"}}/>

                    <Form.Label style={{color:"#DDDDDD"}}>Category</Form.Label>
                    <Form.Select  className={"form-select:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", marginBottom:"20px"}}>
                        <option>Category</option>
                    </Form.Select>
                    <Form.Label style={{color:"#DDDDDD"}}>Brand</Form.Label>
                    <Form.Select  className={"form-select:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", marginBottom:"20px"}}>
                        <option>Product</option>
                    </Form.Select>
                    <Form.Label style={{color:"#DDDDDD"}}>Name</Form.Label>
                    <Form.Control  className={"form-control:focus"} placeholder={"Product Name"} style={{width: "300px", alignSelf:"center", margin:"auto", marginBottom:"20px"}}/>
                </Form.Group>
                <NavigationButton text={"Insert"} onClick={e => e.preventDefault()}/>
            </Form>

        </div>
    </>
}

export default ProductCreatePage;