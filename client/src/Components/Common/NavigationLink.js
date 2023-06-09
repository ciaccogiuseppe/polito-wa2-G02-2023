import {Link, useNavigate} from "react-router-dom";
import {useState} from "react";

function NavigationLink(props){
    const navigate = useNavigate()
    const [linkColor, setLinkColor] = useState("#FDE0BE")
    return <Link
        onClick={(e)=>{e.preventDefault(); navigate(props.href)}}
        style={{color:linkColor}}
        onMouseOver={()=>setLinkColor("#c79e4d")}
        onMouseLeave={()=>setLinkColor("#FDE0BE")}>{props.text}</Link>
}

export default NavigationLink;