import AppNavbar from "../../AppNavbar/AppNavbar";
import {Form} from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import {useEffect, useState} from "react";
import {addProductAPI, getAllBrands, getAllProducts} from "../../../API/Products";
import {reformatCategory} from "../../Products/ProductsPage/ProductsPage";
import ErrorMessage from "../../Common/ErrorMessage";
import {addTicketAPI} from "../../../API/Tickets";
import {useNavigate} from "react-router-dom";

function TicketCreatePage(props) {
    const loggedIn=props.loggedIn
    const [title, setTitle] = useState("")
    const [description, setDescription] = useState("")
    const [file, setFile] = useState([])
    const [errorMessage, setErrorMessage] = useState("")
    const [brands, setBrands] = useState([])
    const [categories, setCategories] = useState([])
    const [category, setCategory] = useState("")
    const [brand, setBrand] = useState("")
    const [products, setProducts] = useState([])
    const [product, setProduct] = useState("")
    const [productsList, setProductsList] = useState([])
    const [warranty, setWarranty] = useState([])

    useEffect(() => {
        getAllProducts().then(products => {
            setProducts(products)
            setCategories(products.map(p => reformatCategory(p.category)).filter((v,i,a)=>a.indexOf(v)===i).sort())
            //setBrands(products.map(p => p.brand).filter((v,i,a)=>a.indexOf(v)===i).sort())
        })
    },[])

    useEffect(() => {
        setBrand("")
        setProduct("")
        setBrands(products
            .filter(p => reformatCategory(p.category) === category)
            .map(p => p.brand)
            .filter((v,i,a)=>a.indexOf(v)===i).sort())
    }, [category])

    useEffect(() => {
        setProduct("")
        setProductsList(
            products
                .filter(p => reformatCategory(p.category) === category && p.brand === brand)
                .map(p => {return{name:p.name, id:p.productId}})
                .filter((v,i,a)=>a.indexOf(v)===i)
                .sort((a, b) => a.name.localeCompare(b.name, undefined, { numeric: true }))
        )
    }, [brand])


    const navigate = useNavigate()
    function addTicket(){
        addTicketAPI({
            title:title,
            description:description,
            productId:product,
            warranty: warranty
        })
            .then(response => {
                navigate("/tickets/"+response.data.ticketId)})
            .catch(err => setErrorMessage(err))
    }

    function formElement(val, setVal) {
        return <Form.Control value={val} className={"form-control:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", marginTop:"10px"}} type="file" onChange={e => setVal(e.target.value)}/>

    }

    return <>
            <AppNavbar user={props.user} logout={props.logout} loggedIn={loggedIn} selected={"tickets"}/>


        <div className="CenteredButton" style={{marginTop:"50px"}}>
            <h1 style={{color:"#EEEEEE", marginTop:"80px"}}>OPEN A TICKET</h1>
            <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>
            <Form className="form" style={{marginTop:"30px"}}>
                <Form.Group className="mb-3" controlId="formBasicEmail">
                    <Form.Label style={{color:"#DDDDDD"}}>Ticket Info</Form.Label>

                    <Form.Control value={title} className={"form-control:focus"} style={{width: "300px", fontSize:"12px", alignSelf:"center", margin:"auto"}} type="input" placeholder="Ticket Title" onChange={e => setTitle(e.target.value)}/>
                    <Form.Control value={description} className={"form-control:focus"} style={{width: "300px", fontSize:"12px", alignSelf:"center", margin:"auto", marginTop:"10px"}} type="textarea" as={"textarea"} placeholder="Ticket Description" onChange={e => setDescription(e.target.value)}/>

                </Form.Group>

                <Form.Group className="mb-3" controlId="formBasicEmail">

                    <Form.Label style={{color:"#DDDDDD"}}>Product</Form.Label>
                    <hr style={{color:"white", width:"150px", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>


                    <h5 style={{color:"#DDDDDD", fontSize:12, marginTop:"10px"}}>Category</h5>
                    <Form.Select value={category} onChange={(e)=>{setCategory(e.target.value)}} className={"form-select:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", marginTop:"10px", fontSize:"12px"}}>
                        <option></option>
                        {categories.map(c => <option>{c}</option>)}
                    </Form.Select>


                    <h5 style={{color:"#DDDDDD", fontSize:12, marginTop:"10px"}}>Brand</h5>
                    <Form.Select disabled={category===""} value={brand} onChange={(e) => setBrand(e.target.value)} className={"form-select:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", marginTop:"10px", fontSize:"12px"}}>
                        <option></option>
                        {brands.map(b => <option>{b}</option>)}
                    </Form.Select>
                    <h5 style={{color:"#DDDDDD", fontSize:12, marginTop:"10px"}}>Product</h5>

                    <Form.Select disabled={category==="" || brand===""} value={product} onChange={(e) => {setProduct(e.target.value)}} className={"form-select:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", marginTop:"10px", fontSize:"12px"}}>
                        <option></option>
                        {productsList.map(p => <option value={p.id}>{p.name}</option>)}
                    </Form.Select>

                    <h5 style={{color:"#DDDDDD", fontSize:12, marginTop:"10px"}}>Warranty code</h5>
                    <Form.Control value={warranty} className={"form-control:focus"} style={{width: "300px", fontSize:"12px", alignSelf:"center", margin:"auto", marginTop:" 10px"}} type="input" placeholder="" onChange={e => setWarranty(e.target.value)}/>

                </Form.Group>
                {errorMessage && <><div style={{margin:"10px"}}>
                    <ErrorMessage text={errorMessage} close={()=>{setErrorMessage("")}}/> </div></>}

                <NavigationButton
                    disabled={category === "" || brand === ""|| warranty === "" || product === "" || title === "" || description ===""}
                    text={"Create Ticket"}
                    onClick={e => {e.preventDefault(); addTicket()}}/>
            </Form>

        </div>
    </>
}

export default TicketCreatePage;