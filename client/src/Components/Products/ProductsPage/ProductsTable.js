import {useState} from "react";
import {useNavigate} from "react-router-dom";


function ProductsTableTR(props){
    const [BGcolor, setBGcolor] = useState("");
    const category = props.category
    const id = props.id
    const brand = props.brand
    const name = props.name
    return <tr className="text-light" style={{cursor:"pointer", backgroundColor:BGcolor}} onMouseOver={() => setBGcolor("rgba(0, 0, 0, 0.1)")} onMouseLeave={()=>setBGcolor("")} onClick={()=>{}}>
        <td className="text-light" style={{fontSize:12, verticalAlign:"middle"}}>{id}</td>
        <td className="text-light" style={{fontSize:15, verticalAlign:"middle"}}>{category}</td>
        <td className="text-light" style={{fontSize:15, verticalAlign:"middle"}}>{brand}</td>
        <td className="text-light" style={{fontSize:15, verticalAlign:"middle"}}>{name}</td>
    </tr>
}

function ProductsTable(props){
    const products=props.products

    return <>
        {products.length >= 0  &&
            <div style={{alignItems:"center", alignSelf:"center"}}>
                <table className="table  roundedTable"  style={{alignContent: "center", width: "70%", margin: "auto", marginTop:"20px"}}>
                    <thead>
                    <tr className="text-light">
                        <th width={"15%"}><h5>ID</h5></th>
                        <th width={"15%"}><h5>CATEGORY</h5></th>
                        <th width={"15%"}><h5>BRAND</h5></th>
                        <th width={"25%"}><h5>PRODUCT</h5></th>
                    </tr>
                    </thead>
                    <tbody>
                    {/*<tr className="text-light" style={{cursor:"pointer", backgroundColor:BGcolor}} onMouseOver={() => setBGcolor("rgba(0, 0, 0, 0.1)")} onMouseLeave={()=>setBGcolor("")}><td className="text-light">Can't use touchscreen on my phone</td><td style={{verticalAlign:"middle"}}><div  style={{borderRadius:"25px", color:"white", backgroundColor:"#dc8429", fontSize:10, textAlign:"center", verticalAlign:"middle", padding:5}}>IN PROGRESS</div></td><td className="text-light" style={{fontSize:12, verticalAlign:"middle"}}>05/02/2022</td></tr>*/}
                    <ProductsTableTR id={"0000 0000 000 00"} category={"Smartphone"} brand={"Apple"} name={"iPhone 13 Pro"}/>
                    <ProductsTableTR id={"0000 0000 000 01"} category={"Smartphone"} brand={"Samsung"} name={"Galaxy S10"}/>
                    <ProductsTableTR id={"0000 0000 000 02"} category={"PC"} brand={"HP"} name={"Omen Intel i7"}/>
                    <ProductsTableTR id={"0000 0000 000 03"} category={"PC"} brand={"HP"} name={"Omen Intel i5"}/>
                    <ProductsTableTR id={"0000 0000 000 04"} category={"Tablet"} brand={"Apple"} name={"iPad 7th Generation"}/>


                    </tbody>
                </table>
            </div>}
    </>
}


export default ProductsTable;