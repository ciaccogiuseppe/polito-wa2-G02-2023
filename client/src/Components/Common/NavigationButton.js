import {Button} from "react-bootstrap";
import {useNavigate} from "react-router-dom";
import {useState} from "react";

function NavigationButton(props){
    const [color, setColor] = useState("#A0C1D9")
    const [textColor, setTextColor] = useState("#333333")
    return <Button
    disabled={props.disabled}
        style={{
            backgroundColor:color,
            color:textColor,
            boxShadow:"0px 3px 6px -3px rgba(0,0,0,0.8)",
            borderColor: "#374f5d", borderWidth: "0px"}} className="HomeButton"
        onClick={(e) => { props.onClick(e)}}
        onMouseOver={() => {setColor("#4f7ca2"); setTextColor("#add8e6")}}
        onMouseLeave={() => {setColor("#A0C1D9");  setTextColor("#333333")}}>

        {props.text}
    </Button>
}

export default NavigationButton;
