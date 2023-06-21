import AppNavbar from "../../AppNavbar/AppNavbar";
import { useEffect, useState } from "react";
import { getAllProducts } from "../../../API/Products";
import NavigationButton from "../../Common/NavigationButton";
import {useNavigate} from "react-router-dom";
import AddButton from "../../Common/AddButton";
import ClientProductsTable from "./ClientProductsTable";
import {caretDownIcon, caretUpIcon, crossIcon, filterIcon} from "../../Common/Icons";
import {Row} from "react-bootstrap";
import {Slider} from "@mui/material";
import PriorityIndicator from "../../Ticketing/TicketCommon/PriorityIndicator";
import {getAllItemsAPI} from "../../../API/Item";

export function reformatCategory(category){
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

export function deformatCategory(category){
    switch(category){
        case "":
            return ""
        case "Smartphone":
            return "SMARTPHONE"
        case "PC":
            return "PC"
        case "TV":
            return "TV"
        case "Software":
            return "SOFTWARE"
        case "Storage Device":
            return "STORAGE_DEVICE"
        case "Other":
            return "OTHER"
    }
}

function ClientProductsPage(props) {
    const [errMessage, setErrMessage] = useState("");
    const [productsList, setProductsList] = useState([]);
    const [allProducts, setAllProducts] = useState([])
    const [showFilters, setShowFilters] = useState(false)


    useEffect(() => {
        window.scrollTo(0, 0)
        getAllItemsAPI().then(products => {
            console.log(products)
            setProductsList(products)
            setAllProducts(products)
        })
    },[])

    useEffect(() => {
        setCategories(allProducts.map(p => p.category).filter((v,i,a)=>a.indexOf(v)===i).sort())
    }, [productsList])



    const loggedIn = props.loggedIn
    const navigate = useNavigate()

    const [brandFilter, setBrandFilter] = useState("")
    const [categories, setCategories] = useState([])
    const [brands, setBrands] = useState([])
    const [categoryFilter, setCategoryFilter] = useState("")

    useEffect(() => {

        setBrands(
            allProducts
                .map(p => { return {category:p.category, brand:p.brand}})
                .filter((v,i,a)=>a.indexOf(v)===i).sort()
                .filter(v => (deformatCategory(categoryFilter) === "" || v.category === deformatCategory(categoryFilter)))
                .map(p => p.brand)
                .filter((v,i,a)=>a.indexOf(v)===i).sort())
    }, [productsList, categoryFilter])

    function applyFilter(){
        setProductsList(allProducts.filter(a =>
            (categoryFilter === "" || reformatCategory(a.category) === categoryFilter) &&
            (brandFilter === "" || a.brand === brandFilter)
        ))
    }

    return <>
        <AppNavbar user={props.user} logout={props.logout} loggedIn={loggedIn} selected={"products"}/>


        <div className="CenteredButton" style={{marginTop:"50px"}}>
            <h1 style={{color:"#EEEEEE", marginTop:"80px"}}>MY PRODUCTS</h1>
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
                                <span style={{cursor: categoryFilter ? "pointer" : ""}} onClick={() => setCategoryFilter("")} className="input-group-text">{categoryFilter ? crossIcon("black", 17) : filterIcon()}</span>
                                <select style={{ height: "40px", appearance:"searchfield"}} className="form-control" placeholder="---" value={categoryFilter} onChange={e => {setCategoryFilter(e.target.value); setBrandFilter("")}}>
                                    <option></option>
                                    {categories.map(c => <option>{reformatCategory(c)}</option>)}
                                </select>
                            </div>
                        </div>

                        <div style={{display:"inline-block", maxWidth:"250px"}}>
                            <span style={{ color: "#DDDDDD" }}>Brand</span>
                            <div className="input-group mb-3" style={{ marginTop: "8px" }}>
                                <span style={{cursor: brandFilter ? "pointer" : ""}} onClick={() => setBrandFilter("")} className="input-group-text">{brandFilter ? crossIcon("black", 17) : filterIcon()}</span>
                                <select style={{ height: "40px", appearance:"searchfield"}} className="form-control" placeholder="---" value={brandFilter} onChange={e => setBrandFilter(e.target.value)}>
                                    <option></option>
                                    {brands.map(b => <option>{b}</option>)}
                                </select>
                            </div>
                        </div>

                        <div style={{marginTop:"15px", marginBottom:"15px"}}>
                            {(brandFilter === "" && categoryFilter === "") ?
                                <NavigationButton text={"Reset"} onClick={e => {e.preventDefault(); applyFilter()} }/> :
                                <NavigationButton disabled={brandFilter === "" && categoryFilter === "" } text={"Filter"} onClick={e => {e.preventDefault(); applyFilter()} }/>
                            }

                        </div>

                    </Row>
                </>}

            </div>


            <ClientProductsTable products={productsList}/>
            <div style={{position:"fixed", bottom:"24px", right:"24px"}}>
                <AddButton onClick={()=>navigate("/newproduct")}/>
            </div>


        </div>
    </>
}

export default ClientProductsPage;