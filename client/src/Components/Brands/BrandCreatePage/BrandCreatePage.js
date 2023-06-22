import AppNavbar from "../../AppNavbar/AppNavbar";
import {Form} from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import {useEffect, useState} from "react";
import {addBrandAPI} from "../../../API/Products";
import ErrorMessage from "../../Common/ErrorMessage";
import {useNavigate} from "react-router-dom";



function BrandCreatePage(props) {
    const loggedIn=props.loggedIn
    const [errorMessage, setErrorMessage] = useState("")
    const [brand, setBrand] = useState("")


    const navigate = useNavigate()
    function addProduct(){
        addBrandAPI({name:brand
        }).then(() => navigate("/brands")).catch(err => setErrorMessage(err))
    }

    useEffect(() => {
        window.scrollTo(0, 0)
    }, [])

    return <>
        <AppNavbar user={props.user} loggedIn={loggedIn} selected={"brands"} logout={props.logout}/>


        <div className="CenteredButton" style={{marginTop:"50px"}}>
            <h1 style={{color:"#EEEEEE", marginTop:"80px"}}>INSERT BRAND</h1>
            <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>
            <Form className="form" style={{marginTop:"30px"}} onSubmit={()=>addProduct()}>
                <Form.Group className="mb-3">

                    <Form.Label style={{color:"#DDDDDD"}}>Brand</Form.Label>
                    <Form.Control value={brand} onChange={(e) => setBrand(e.target.value)} className={"form-select:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", marginBottom:"20px"}}/>
                </Form.Group>
                {errorMessage && <><div style={{margin:"10px"}}>
                    <ErrorMessage text={errorMessage} close={()=>{setErrorMessage("")}}/> </div></>}

                <NavigationButton text={"Insert"} type={"submit"} onClick={e => {e.preventDefault(); addProduct()}}/>
                <div style={{marginTop:"20px"}}>
                    <NavigationButton text={"Back"} onClick={e => {e.preventDefault(); navigate(-1)}}/>
                </div>
            </Form>

        </div>
    </>
}

export default BrandCreatePage;