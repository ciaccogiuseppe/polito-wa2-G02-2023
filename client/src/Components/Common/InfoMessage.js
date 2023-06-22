import { crossIcon, xIcon } from "./Icons";
import { useState } from "react";

function InfoMessage(props) {
  const text = props.text;
  const [color, setColor] = useState("#faecc4");
  const close = props.close;
  return (
    <div
      style={{
        border: "solid",
        borderWidth: 2,
        borderColor: "#faecc4",
        margin: "auto",
        paddingTop: "20px",
        paddingBottom: "20px",
        borderRadius: "25px",
        width: "350px",
        fontSize: "12px",
        backgroundColor: "rgba(154,144,68,0.66)",
        color: "#faecc4",
        marginTop: "15px",
      }}
    >
      <div style={{ display: "inline-block", width: "200px" }}>{text}</div>
      <div
        style={{
          marginTop: "auto",
          marginBottom: "auto",
          display: "inline-block",
          float: "right",
          marginRight: "10px",
        }}
      ></div>
    </div>
  );
}

export default InfoMessage;
