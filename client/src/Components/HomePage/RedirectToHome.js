
import { useNavigate } from "react-router-dom";
import {useEffect} from "react";
import AppNavbar from "../AppNavbar/AppNavbar";
function RedirectToHome(props){
    const navigate = useNavigate();
    useEffect(() => {
        navigate("/");
    });
    return <>
        <div style={{
            position: 'absolute',
            backgroundColor: '#222222',
            width: '100%',
            height: '100%'}}>
            <AppNavbar/>
        </div>

    </>
}

export default RedirectToHome;