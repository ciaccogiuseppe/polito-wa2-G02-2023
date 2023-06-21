import {useState} from "react";
import {useNavigate} from "react-router-dom";
import {reformatCategory} from "./ClientProductsPage";

function reformatId(id){
    return id.substring(0,4) + " " + id.substring(4,8) + " " + id.substring(8,11) + " " + id.substring(11,13)
}



function ProductsTableTR(props){
    const [BGcolor, setBGcolor] = useState("");
    const category = reformatCategory(props.category)
    const id = reformatId(props.id)
    const brand = props.brand
    const name = props.name
    return <tr className="text-light" style={{cursor:"pointer", backgroundColor:BGcolor}} onMouseOver={() => setBGcolor("rgba(0, 0, 0, 0.1)")} onMouseLeave={()=>setBGcolor("")} onClick={()=>{}}>
        <td className="text-light" style={{fontSize:12, verticalAlign:"middle"}}>{id}</td>
        <td className="text-light" style={{fontSize:15, verticalAlign:"middle"}}>{category}</td>
        <td className="text-light" style={{fontSize:15, verticalAlign:"middle"}}>{brand}</td>
        <td className="text-light" style={{fontSize:15, verticalAlign:"middle"}}>{name}</td>
    </tr>
}

function ClientProductsTable(props){
    const products=props.products

    return <>
        {products.length >= 0  &&
            <div style={{alignItems:"center", alignSelf:"center"}}>
                <table className="table  roundedTable"  style={{alignContent: "center", width: "70%", margin: "auto", marginTop:"20px"}}>
                    <thead>
                    <tr className="text-light">

                        <th width={"15%"}><h5>CATEGORY</h5></th>
                        <th width={"15%"}><h5>BRAND</h5></th>
                        <th width={"25%"}><h5>PRODUCT</h5></th>
                        <th width={"15%"}><h5>REGISTERED</h5></th>
                        <th width={"15%"}><h5>EXPIRATION</h5></th>
                    </tr>
                    </thead>
                    <tbody>
                    {/*<tr className="text-light" style={{cursor:"pointer", backgroundColor:BGcolor}} onMouseOver={() => setBGcolor("rgba(0, 0, 0, 0.1)")} onMouseLeave={()=>setBGcolor("")}><td className="text-light">Can't use touchscreen on my phone</td><td style={{verticalAlign:"middle"}}><div  style={{borderRadius:"25px", color:"white", backgroundColor:"#dc8429", fontSize:10, textAlign:"center", verticalAlign:"middle", padding:5}}>IN PROGRESS</div></td><td className="text-light" style={{fontSize:12, verticalAlign:"middle"}}>05/02/2022</td></tr>*/}
                    {products.map(p =>
                        <ProductsTableTR
                        id={p.productId}
                        category={p.category}
                        brand={p.brand}
                        name={p.name}/>)}

                    </tbody>
                </table>
            </div>}
    </>
}


export default ClientProductsTable;