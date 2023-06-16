import AppNavbar from "../../AppNavbar/AppNavbar";
import {Form} from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import {useEffect, useState} from "react";
import {getAllCategories} from "../../../API/Products";
import ErrorMessage from "../../Common/ErrorMessage";

function reformatCategory(category){
    switch(category){
        case "SMARTPHONE":
            return "Smartphone"
        case "PC":
            return "PC"
        case "TV":
            return "TV"
        case "SOFTWARE":
            return "Software"
        case "STORAGE_DEVICE":
            return "Storage Device"
        case "OTHER":
            return "Other"
    }
}

function ProductCreatePage(props) {
    const loggedIn=props.loggedIn
    const [title, setTitle] = useState("")
    const [description, setDescription] = useState("")
    const [file, setFile] = useState([])
    const [categories, setCategories] = useState([])
    const [errorMessage, setErrorMessage] = useState("")

    useEffect(() => {
        getAllCategories().then((categories => {
            setCategories(categories.map(c => reformatCategory(c.categoryName)).sort())
        })).catch(err => console.log(err))
    },[])


    function formElement(val, setVal) {
        return <Form.Control value={val} className={"form-control:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", marginTop:"10px"}} type="file" onChange={e => setVal(e.target.value)}/>

    }

    return <>
        <AppNavbar user={props.user} loggedIn={loggedIn} selected={"products"} logout={props.logout}/>


        <div className="CenteredButton" style={{marginTop:"50px"}}>
            <h1 style={{color:"#EEEEEE", marginTop:"80px"}}>INSERT PRODUCT</h1>
            <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>
            <Form className="form" style={{marginTop:"30px"}}>
                <Form.Group className="mb-3">
                    <Form.Label style={{color:"#DDDDDD"}}>Product ID</Form.Label>
                    <Form.Control  className={"form-control:focus"} placeholder={"Product ID"} style={{width: "300px", alignSelf:"center", margin:"auto", marginBottom:"20px"}}/>

                    <Form.Label style={{color:"#DDDDDD"}}>Category</Form.Label>
                    <Form.Select  className={"form-select:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", marginBottom:"20px"}}>
                        <option></option>
                        {categories.map(c => <option>{c}</option>)}
                    </Form.Select>
                    <Form.Label style={{color:"#DDDDDD"}}>Brand</Form.Label>
                    <Form.Select  className={"form-select:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", marginBottom:"20px"}}>

                    </Form.Select>
                    <Form.Label style={{color:"#DDDDDD"}}>Name</Form.Label>
                    <Form.Control  className={"form-control:focus"} placeholder={"Product Name"} style={{width: "300px", alignSelf:"center", margin:"auto", marginBottom:"20px"}}/>
                </Form.Group>
                {errorMessage && <><div style={{margin:"10px"}}>
                    <ErrorMessage text={errorMessage} close={()=>{setErrorMessage("")}}/> </div></>}

                <NavigationButton text={"Insert"} onClick={e => e.preventDefault()}/>
            </Form>

        </div>
    </>
}

export default ProductCreatePage;