import AppNavbar from "../../AppNavbar/AppNavbar";
import { useEffect, useState } from "react";
import { getAllProducts } from "../../../API/Products";
import NavigationButton from "../../Common/NavigationButton";
import {useNavigate} from "react-router-dom";
import AddButton from "../../Common/AddButton";
import ProductsTable from "./ProductsTable";
import {caretDownIcon, caretUpIcon, crossIcon, filterIcon} from "../../Common/Icons";
import {Row} from "react-bootstrap";
import {Slider} from "@mui/material";
import PriorityIndicator from "../../Ticketing/TicketCommon/PriorityIndicator";

function ProductsPage(props) {
    const [errMessage, setErrMessage] = useState("");
    const [productsList, setProductsList] = useState([]);
    const [showFilters, setShowFilters] = useState(false)
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
    const loggedIn = props.loggedIn
    const navigate = useNavigate()

    const [brandFilter, setBrandFilter] = useState("")
    const [categoryFilter, setCategoryFilter] = useState("")

    return <>
        <AppNavbar loggedIn={loggedIn} selected={"products"}/>


        <div className="CenteredButton" style={{marginTop:"50px"}}>
            <h1 style={{color:"#EEEEEE", marginTop:"80px"}}>PRODUCTS</h1>
            <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"20px", marginTop:"2px"}}/>


            <div style={{width:"75%", alignSelf:"center", margin:"auto", borderRadius:"25px", marginTop: "15px", backgroundColor:"rgba(0,0,0,0.2)" }}>
                <h4 style={{color:"#EEEEEE", paddingTop:"10px" }}>FILTERS</h4>
                {showFilters?
                    <div onClick={()=>{setShowFilters(false)}} style={{display:"inline-block", paddingBottom:"10px", cursor:"pointer"}}>
                        {caretUpIcon("white", 30)}
                    </div>:
                    <div onClick={()=>{setShowFilters(true)}} style={{display:"inline-block", paddingBottom:"10px", cursor:"pointer"}}>
                        {caretDownIcon("white", 30)}
                    </div>}

                {showFilters && <>
                    <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>
                    <Row className="d-flex justify-content-center" style={{ marginBottom: "10px"}}>


                        <div style={{display:"inline-block", maxWidth:"250px"}}>
                            <span style={{ color: "#DDDDDD" }}>Category</span>
                            <div className="input-group mb-3" style={{ marginTop: "8px" }}>
                                <span style={{cursor: categoryFilter ? "pointer" : ""}} onClick={() => setBrandFilter("")} className="input-group-text">{categoryFilter ? crossIcon("black", 17) : filterIcon()}</span>
                                <select style={{ height: "40px", appearance:"searchfield"}} className="form-control" placeholder="---" value={categoryFilter} onChange={e => categoryFilter(e.target.value)} />
                            </div>
                        </div>

                        <div style={{display:"inline-block", maxWidth:"250px"}}>
                            <span style={{ color: "#DDDDDD" }}>Brand</span>
                            <div className="input-group mb-3" style={{ marginTop: "8px" }}>
                                <span style={{cursor: brandFilter ? "pointer" : ""}} onClick={() => setBrandFilter("")} className="input-group-text">{brandFilter ? crossIcon("black", 17) : filterIcon()}</span>
                                <select style={{ height: "40px", appearance:"searchfield"}} className="form-control" placeholder="---" value={brandFilter} onChange={e => setBrandFilter(e.target.value)} />
                            </div>
                        </div>

                        <div style={{marginTop:"15px", marginBottom:"15px"}}>
                            <NavigationButton disabled={brandFilter === "" && categoryFilter === "" } text={"Search"} onClick={e => e.preventDefault()} />

                        </div>

                    </Row>
                </>}

            </div>


            <ProductsTable products={[]}/>
            <div style={{position:"fixed", bottom:"24px", right:"24px"}}>
                <AddButton onClick={()=>navigate("/newproduct")}/>
            </div>


        </div>
    </>
}

export default ProductsPage;