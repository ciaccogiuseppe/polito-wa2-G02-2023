import AppNavbar from "../../AppNavbar/AppNavbar";
import { useEffect, useState } from "react";
import { getAllProducts } from "../../../API/Products";
import {Button, Form} from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import "./LoginPage.css"
import {Link, useNavigate} from "react-router-dom";
import NavigationLink from "../../Common/NavigationLink";

function LoginPage(props) {
    const loggedIn=props.loggedIn
    const navigate = useNavigate()
    const [email, setEmail] = useState("")
    const [password, setPassword] = useState("")
    return <>
            <AppNavbar loggedIn={loggedIn}/>
            <div className="CenteredButton" style={{marginTop:"50px"}}>
                <h1 style={{color:"#EEEEEE", marginTop:"80px"}}>LOGIN</h1>
                <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>
                <Form className="form" style={{marginTop:"30px"}}>
                    <Form.Group className="mb-3" controlId="formBasicEmail">
                        <Form.Label style={{color:"#DDDDDD"}}>Email address</Form.Label>
                        <Form.Control value={email} className={"form-control:focus"} style={{width: "350px", alignSelf:"center", margin:"auto"}} type="email" placeholder="Email" onChange={e => setEmail(e.target.value)}/>
                    </Form.Group>
                    <Form.Group className="mb-3" controlId="formBasicEmail">
                        <Form.Label style={{color:"#DDDDDD"}}>Password</Form.Label>
                        <Form.Control value={password} style={{width: "350px", alignSelf:"center", margin:"auto"}} type="password" placeholder="Password" onChange={e => setPassword(e.target.value)}/>
                    </Form.Group>
                    <NavigationButton text={"Login"} onClick={e => e.preventDefault()}/>
                </Form>
                <div style={{fontSize:"12px", color:"#EEEEEE", marginTop:"5px" }}>
                    <span>Don't have an account?</span> <NavigationLink href={"/signup"} text={"Sign up"}/>
                </div>

            </div>
    </>
}

export default LoginPage;