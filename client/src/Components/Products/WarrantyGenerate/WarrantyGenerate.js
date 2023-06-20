import AppNavbar from "../../AppNavbar/AppNavbar";
import {Form} from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import {useEffect, useState} from "react";
import {addProductAPI, getAllBrands, getAllCategories, getAllProducts} from "../../../API/Products";
import ErrorMessage from "../../Common/ErrorMessage";
import {deformatCategory, reformatCategory} from "../ProductsPage/ProductsPage";
import {useNavigate} from "react-router-dom";
import {addItemAPI, getItemAPI} from "../../../API/Item";



function WarrantyGenerate(props) {
    const loggedIn=props.loggedIn
    const [title, setTitle] = useState("")
    const [description, setDescription] = useState("")
    const [file, setFile] = useState([])
    const [categories, setCategories] = useState([])
    const [errorMessage, setErrorMessage] = useState("")
    const [brands, setBrands] = useState([])
    const [productId, setProductId] = useState("")
    const [brand, setBrand] = useState("")
    const [category, setCategory] = useState("")
    const [useProductId, setUseProductId] = useState(false)
    const [product, setProduct] = useState("")
    const [products, setProducts] = useState([])
    const [selectedProduct, setSelectedProduct] = useState("")
    const [productsList, setProductsList] = useState([])
    const [serialNumber, setSerialNumber] = useState("")
    const [warrantyCode, setWarrantyCode] = useState("")
    const [expiration, setExpiration] = useState("")
    const [generated, setGenerated] = useState("")
    const [pid1, setPid1] = useState("")
    const [pid2, setPid2] = useState("")
    const [pid3, setPid3] = useState("")
    const [pid4, setPid4] = useState("")

    useEffect(() => {
        window.scrollTo(0, 0)
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
    function addItem(){
        getItemAPI({productId:useProductId?productId:product,
            serialNum:serialNumber})
            .then((response) => {
            setSelectedProduct(
                products.filter(p => p.productId === response.data.productId).map(p=>p.brand + " - " + p.name)[0])
            setWarrantyCode(response.data.uuid)
            let date = new Date(response.data.validFromTimestamp)
            date.setMonth(date.getMonth()+12)
            setExpiration(
                date.toLocaleDateString())
            setGenerated(new Date(response.data.validFromTimestamp).toLocaleDateString())
        })
            .catch(() => {
                addItemAPI({
                    productId:useProductId?productId:product,
                    serialNum:serialNumber,
                    durationMonths:12
                }).then((response) => {
                    setSelectedProduct(
                        products.filter(p => p.productId === response.data.productId).map(p=>p.brand + " - " + p.name)[0])
                    setWarrantyCode(response.data.uuid)
                    let date = new Date(response.data.validFromTimestamp)
                    date.setMonth(date.getMonth()+12)
                    setExpiration(
                        date.toLocaleDateString())
                    setGenerated(new Date(response.data.validFromTimestamp).toLocaleDateString())
                }).catch(err => setErrorMessage(err))
            })

    }




    useEffect(() => {
        setProductId(pid1 + pid2 + pid3 + pid4)
    }, [pid1, pid2, pid3, pid4])

    return <>

        <div className="CenteredButton" style={{marginTop:"25px"}}>
            <h2 className={"no-printme"} style={{color:"#EEEEEE", marginTop:"50px"}}>GENERATE CODE</h2>
            <hr style={{color:"white", width:"15%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>
            {!warrantyCode && <Form className="form" style={{marginTop:"30px"}}>
                <Form.Group className="mb-3">

                    <Form.Select onChange={(e)=>{setUseProductId(e.target.value !== "true")}} className={"form-select:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", marginTop:"10px", fontSize:"12px"}}>
                        <option value={true}>Select product</option>
                        <option value={false}>Insert product ID</option>
                    </Form.Select>


                    {useProductId ? <>
                        <Form.Label style={{color:"#DDDDDD", fontSize:12, marginTop:"10px"}}>Product ID</Form.Label>
                        <div>

                            <Form.Control maxLength={4} className={"form-control:focus"} value={pid1} onChange={(e) => setPid1(e.target.value)} placeholder={"XXXX"} style={{width: "70px", display:"inline-block", alignSelf:"center", margin:"auto", marginBottom:"20px", fontSize:12, marginRight:"6px", textAlign:"center"}}/>
                            <Form.Control maxLength={4} className={"form-control:focus"} value={pid2} onChange={(e) => setPid2(e.target.value)} placeholder={"XXXX"} style={{width: "70px", display:"inline-block", alignSelf:"center", margin:"auto", marginBottom:"20px", fontSize:12, marginRight:"7px", marginLeft:"8px", textAlign:"center"}}/>
                            <Form.Control maxLength={3} className={"form-control:focus"} value={pid3} onChange={(e) => setPid3(e.target.value)} placeholder={"XXX"} style={{width: "65px", display:"inline-block", alignSelf:"center", margin:"auto", marginBottom:"20px", fontSize:12, marginRight:"7px", marginLeft:"8px", textAlign:"center"}}/>
                            <Form.Control maxLength={2} className={"form-control:focus"} value={pid4} onChange={(e) => setPid4(e.target.value)} placeholder={"XX"} style={{width: "52px", display:"inline-block", alignSelf:"center", margin:"auto", marginBottom:"20px", fontSize:12, marginLeft:"6px", textAlign:"center"}}/>
                        </div>
                    </> : <>
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

                    </>}





                    <Form.Label style={{color:"#DDDDDD", fontSize:12, marginTop:"10px"}}>Serial Number</Form.Label>
                    <Form.Control value={serialNumber} onChange={(e) => setSerialNumber(e.target.value)}  className={"form-control:focus"} placeholder={"Serial Number"} style={{width: "300px", alignSelf:"center", margin:"auto", marginBottom:"20px", fontSize:12}}/>
                </Form.Group>
                {errorMessage && <><div style={{margin:"10px"}}>
                    <ErrorMessage text={errorMessage} close={()=>{setErrorMessage("")}}/> </div></>}

                <NavigationButton text={"Generate"} onClick={e => {e.preventDefault(); addItem()}}/>

            </Form>}
            {warrantyCode && <div>
            <h5 className={"printme"} style={{color:"#DDDDDD", fontSize:15, marginTop:"10px"}}>{selectedProduct}</h5>
            <h5 className={"printme"} style={{color:"#DDDDDD", fontSize:15, marginTop:"15px", marginBottom:"15px"}}>SN: <span style={{backgroundColor:"rgba(0,0,0,0.3)", padding:"5px", borderRadius:"25px"}}>{serialNumber}</span></h5>
            <h5 className={"printme"} style={{color:"#DDDDDD", fontSize:15, marginTop:"10px", backgroundColor:"rgba(0,0,0,0.3)", borderRadius:"25px", margin:"auto", width:"340px", padding:"10px"}}>{warrantyCode}</h5>
                <h5 className={"printme"} style={{color:"#DDDDDD", fontSize:15, marginTop:"10px"}}>Generated: {generated}</h5>
                <h5 className={"printme"} style={{color:"#DDDDDD", fontSize:15, marginTop:"10px"}}>Expiration: {expiration}</h5>
            <h5 className={"no-printme"} style={{color:"#DDDDDD", fontSize:15, marginTop:"10px"}}>{selectedProduct}</h5>
            <h5 className={"no-printme"} style={{color:"#DDDDDD", fontSize:15, marginTop:"15px", marginBottom:"15px"}}>SN: <span style={{backgroundColor:"rgba(0,0,0,0.3)", padding:"5px", borderRadius:"25px"}}>{serialNumber}</span></h5>
            <h5 className={"no-printme"} style={{color:"#DDDDDD", fontSize:15, marginTop:"10px", backgroundColor:"rgba(0,0,0,0.3)", borderRadius:"25px", margin:"auto", width:"340px", padding:"10px"}}>{warrantyCode}</h5>
                <h5 className={"no-printme"} style={{color:"#DDDDDD", fontSize:15, marginTop:"10px"}}>Generated: {generated}</h5>
                <h5 className={"no-printme"} style={{color:"#DDDDDD", fontSize:15, marginTop:"10px"}}>Expiration: {expiration}</h5>

                <div className={"no-printme"}><NavigationButton text={"Print warranty"}  onClick={e => {e.preventDefault(); window.print()}}/>


                <hr style={{color:"white", width:"15%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"12px", marginTop:"12px"}}/>


                <NavigationButton text={"Generate new code"} onClick={e => {e.preventDefault(); window.location.href="/"}}/>
            </div>

            </div>}

        </div>
    </>
}

export default WarrantyGenerate;