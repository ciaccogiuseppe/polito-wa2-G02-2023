import {Button} from "react-bootstrap";
import {useNavigate} from "react-router-dom";
import {useState} from "react";
import {plusIcon} from "./Icons";

function AddButton(props){
    const [color, setColor] = useState("#758C9F")
    const [textColor, setTextColor] = useState("#333333")
    const styles = props.style
    return <Button
        style={{
            backgroundColor:color,
            color:textColor,
            boxShadow:"0px 3px 6px -3px rgba(0,0,0,0.8)",
            borderRadius:"50%", height:"48px", width:"48px",
            borderColor: "#374f5d", borderWidth: "0px", ...styles}} className="HomeButton"
        onClick={(e) => { props.onClick(e)}}
        onMouseOver={() => {setColor("#374f5d"); setTextColor("#444444")}}
        onMouseLeave={() => {setColor("#758C9F");  setTextColor("#333333")}}>

        {plusIcon("white", 24)}
    </Button>
}

export default AddButton;
