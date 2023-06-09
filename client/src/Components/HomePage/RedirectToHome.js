import { useNavigate } from "react-router-dom";
import {useEffect} from "react";
import AppNavbar from "../AppNavbar/AppNavbar";
function RedirectToHome(props){
    const navigate = useNavigate();
    useEffect(() => {
        navigate("/");
    });
    return <>
    <AppNavbar/>
    </>
}

export default RedirectToHome;