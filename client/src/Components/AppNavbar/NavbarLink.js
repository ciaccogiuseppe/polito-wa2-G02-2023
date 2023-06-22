import { Nav } from "react-bootstrap";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

function NavbarLink(props) {
  const [color, setColor] = useState("#FDE0BE");
  const [textColor, setTextColor] = useState("#222222");
  const navigate = useNavigate();
  const selected = props.selected;

  return (
    <Nav.Link
      href="/"
      style={{
        backgroundColor: color,
        height: "100%",
        fontWeight: "bold",
        color: textColor,
        borderBottom: selected ? "3px solid #A07148" : "3px solid " + color,
      }}
      onMouseOver={() => {
        setColor("#E8AB75");
        setTextColor("#FFE9BC");
      }}
      onMouseLeave={() => {
        setColor("#FDE0BE");
        setTextColor("#321B1D");
      }}
      onClick={(e) => {
        e.preventDefault();
        navigate(props.href);
      }}
    >
      {props.text}
    </Nav.Link>
  );
}

export default NavbarLink;
