import {Button, Form, Spinner} from "react-bootstrap";
import AppNavbar from "../../AppNavbar/AppNavbar";
import {useEffect, useState} from "react";
import {addNewProfile} from "../../../API/Profiles";
import NavigationButton from "../../Common/NavigationButton";
import NavigationLink from "../../Common/NavigationLink";




function ProfileCreatePage(props){
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [password2, setPassword2] = useState("");
    const [name, setName] = useState("");
    const [surname, setSurname] = useState("");
    const [errMessage, setErrMessage] = useState("");
    const [response, setResponse] = useState("");
    const [loading, setLoading] = useState(false);

    const loggedIn=props.loggedIn
    return <>
            <AppNavbar loggedIn={loggedIn}/>
            <div className="CenteredButton" style={{marginTop:"50px"}}>
                <h1 style={{color:"#EEEEEE", marginTop:"80px"}}>SIGN UP</h1>
                <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>
                <Form className="form" style={{marginTop:"30px"}}>
                    <Form.Group className="mb-3" controlId="formBasicEmail">
                        <Form.Label style={{color:"#DDDDDD"}}>Personal Info</Form.Label>
                        <Form.Control value={name} className={"form-control:focus"} style={{width: "350px", alignSelf:"center", margin:"auto"}} type="input" placeholder="First Name" onChange={e => setName(e.target.value)}/>
                        <Form.Control value={surname} className={"form-control:focus"} style={{width: "350px", alignSelf:"center", margin:"auto", marginTop:"10px"}} type="input" placeholder="Last Name" onChange={e => setSurname(e.target.value)}/>
                    </Form.Group>
                    <Form.Group className="mb-3" controlId="formBasicEmail">
                        <Form.Label style={{color:"#DDDDDD"}}>Email address</Form.Label>
                        <Form.Control value={email} className={"form-control:focus"} style={{width: "350px", alignSelf:"center", margin:"auto"}} type="email" placeholder="Email" onChange={e => setEmail(e.target.value)}/>
                    </Form.Group>
                    <Form.Group className="mb-3" controlId="formBasicEmail">
                        <Form.Label style={{color:"#DDDDDD"}}>Password</Form.Label>
                        <Form.Control value={password} style={{width: "350px", alignSelf:"center", margin:"auto"}} type="password" placeholder="Password" onChange={e => setPassword(e.target.value)}/>
                        <Form.Control value={password2} style={{width: "350px", alignSelf:"center", margin:"auto", marginTop:"10px"}} type="password" placeholder="Password" onChange={e => setPassword2(e.target.value)}/>
                    </Form.Group>
                    <NavigationButton text={"Sign up"} onClick={e => e.preventDefault()}/>
                </Form>

                <div style={{fontSize:"12px", color:"#EEEEEE", marginTop:"5px" }}>
                    <span>Already have an account?</span> <NavigationLink href={"/login"} text={"Sign in"}/>
                </div>

            </div>
    </>


    {/*function createProfile(){
        setLoading(true);
        addNewProfile({email:email, name:name, surname:surname}).then(
            res => {
                setErrMessage("");
                setResponse("Profile added succesfully");
                setLoading(false);
                setEmail("");
                setName("");
                setSurname("");
                //console.log(res);
            }
        ).catch(err => {
            //console.log(err);
            setResponse("");
            setErrMessage(err.message);
            setLoading(false);
        })
    }

    return <>
            <div className="CenteredButton">

                <Form className="form">
                    <Form.Group className="mb-3" controlId="formBasicEmail">
                        <Form.Label className="text-info">Email address</Form.Label>
                        <Form.Control style={{width: "400px", alignSelf:"center", margin:"auto"}} value={email} type="email" placeholder="Email" onChange={e => setEmail(e.target.value)}/>
                        <Form.Label style={{marginTop:"8px"}} className="text-info">Name</Form.Label>
                        <Form.Control style={{width: "400px", alignSelf:"center", margin:"auto"}} value={name} type="text" placeholder="Name" onChange={e => setName(e.target.value)}/>
                        <Form.Label style={{marginTop:"8px"}} className="text-info">Surname</Form.Label>
                        <Form.Control style={{width: "400px", alignSelf:"center", margin:"auto"}} value={surname} type="text" placeholder="Surname" onChange={e => setSurname(e.target.value)}/>
                    </Form.Group>
                    <Button type="submit" variant="outline-info" style={{borderWidth:"2px"}} className="HomeButton" onClick={(e) => {e.preventDefault(); createProfile();}}>Create Profile</Button>
                </Form>
                <hr style={{color:"white", width:"90%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginTop:"20px"}}/>
                {loading? <Spinner style={{alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginTop:"20px"}} animation="border" variant="info" /> :
                    <>
                        {response?<h4 className="text-success" style={{marginTop:"10px"}}>{response}</h4>:<></>}
                        {errMessage?<h5 className="text-danger" style={{marginTop:"10px"}}>{errMessage}</h5>:<></>}</>}

            </div>
    </>*/}
}

export default ProfileCreatePage;