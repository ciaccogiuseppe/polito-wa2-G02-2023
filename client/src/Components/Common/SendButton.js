import { Button } from "react-bootstrap";
import { useState } from "react";
import { sendIcon } from "./Icons";

function SendButton(props) {
  const [color, setColor] = useState("#5fa960");
  const [textColor, setTextColor] = useState("#333333");
  const styles = props.style;
  return (
    <Button
      style={{
        verticalAlign: "middle",
        alignItems: "center",
        backgroundColor: color,
        color: textColor,
        boxShadow: "0px 3px 6px -3px rgba(0,0,0,0.8)",
        borderRadius: "50%",
        height: "48px",
        width: "48px",
        borderColor: "#374f5d",
        borderWidth: "0px",
        ...styles,
      }}
      className="HomeButton"
      onClick={(e) => {
        props.onClick(e);
      }}
      onMouseOver={() => {
        setColor("#3e7a3f");
        setTextColor("#444444");
      }}
      onMouseLeave={() => {
        setColor("#5fa960");
        setTextColor("#333333");
      }}
    >
      <div style={{ alignSelf: "center" }}>{sendIcon("white", 20)}</div>
    </Button>
  );
}

export default SendButton;
