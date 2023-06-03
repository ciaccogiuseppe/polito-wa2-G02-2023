import AppNavbar from "../../AppNavbar/AppNavbar";
import { useEffect, useState } from "react";
import { getAllProducts } from "../../../API/Products";
import {Button, Form} from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";

function LoginPage(props) {
    const loggedIn=props.loggedIn
    const [email, setEmail] = useState("")
    const [password, setPassword] = useState("")
    return <>
        <div style={{
            position: 'absolute',
            backgroundColor: '#537188',
            width: '100%',
            height: '100%'
        }}>
            <AppNavbar loggedIn={loggedIn}/>
            <div className="CenteredButton" style={{marginTop:"50px"}}>
                <h1 style={{color:"#EEEEEE"}}>LOGIN</h1>
                <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>

                <Form className="form" style={{marginTop:"30px"}}>
                    <Form.Group className="mb-3" controlId="formBasicEmail">
                        <Form.Label style={{color:"#DDDDDD"}}>Email address</Form.Label>
                        <Form.Control style={{width: "350px", alignSelf:"center", margin:"auto"}} type="email" placeholder="Email" onChange={e => setEmail(e.target.value)}/>
                    </Form.Group>
                    <Form.Group className="mb-3" controlId="formBasicEmail">
                        <Form.Label style={{color:"#DDDDDD"}}>Password</Form.Label>
                        <Form.Control style={{width: "350px", alignSelf:"center", margin:"auto"}} type="password" placeholder="Password" onChange={e => setEmail(e.target.value)}/>
                    </Form.Group>
                    <NavigationButton text={"Login"} onClick={e => e.preventDefault()}/>
                </Form>

            </div>
        </div>
    </>
}

export default LoginPage;