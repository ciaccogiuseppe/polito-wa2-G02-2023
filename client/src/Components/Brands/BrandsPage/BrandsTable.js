import { useState } from "react";

function reformatId(id) {
  return (
    id.substring(0, 4) +
    " " +
    id.substring(4, 8) +
    " " +
    id.substring(8, 11) +
    " " +
    id.substring(11, 13)
  );
}

function BrandsTableTR(props) {
  const [BGcolor, setBGcolor] = useState("");
  const brand = props.brand;
  return (
    <tr
      className="text-light"
      style={{ cursor: "pointer", backgroundColor: BGcolor }}
      onMouseOver={() => setBGcolor("rgba(0, 0, 0, 0.1)")}
      onMouseLeave={() => setBGcolor("")}
      onClick={() => {}}
    >
      <td
        className="text-light"
        style={{ fontSize: 15, verticalAlign: "middle" }}
      >
        {brand}
      </td>
    </tr>
  );
}

function BrandsTable(props) {
  const brands = props.brands;

  return (
    <>
      {brands.length >= 0 && (
        <div style={{ alignItems: "center", alignSelf: "center" }}>
          <table
            className="table  roundedTable"
            style={{
              alignContent: "center",
              width: "70%",
              margin: "auto",
              marginTop: "20px",
            }}
          >
            <tbody>
              {/*<tr className="text-light" style={{cursor:"pointer", backgroundColor:BGcolor}} onMouseOver={() => setBGcolor("rgba(0, 0, 0, 0.1)")} onMouseLeave={()=>setBGcolor("")}><td className="text-light">Can't use touchscreen on my phone</td><td style={{verticalAlign:"middle"}}><div  style={{borderRadius:"25px", color:"white", backgroundColor:"#dc8429", fontSize:10, textAlign:"center", verticalAlign:"middle", padding:5}}>IN PROGRESS</div></td><td className="text-light" style={{fontSize:12, verticalAlign:"middle"}}>05/02/2022</td></tr>*/}
              {brands.map((p) => (
                <BrandsTableTR brand={p} />
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}

export default BrandsTable;
