import { Button } from "react-bootstrap";
import { useState } from "react";
import { crossIcon } from "./Icons";

function DeleteButton(props) {
  const [color, setColor] = useState("#d26363");
  const [textColor, setTextColor] = useState("#333333");
  const styles = props.style;
  return (
    <Button
      style={{
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
        setColor("#963b3b");
        setTextColor("#444444");
      }}
      onMouseLeave={() => {
        setColor("#d26363");
        setTextColor("#333333");
      }}
    >
      {crossIcon("white", 24)}
    </Button>
  );
}

export default DeleteButton;
