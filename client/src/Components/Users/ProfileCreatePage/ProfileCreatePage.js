import {Button, Form, Spinner} from "react-bootstrap";
import AppNavbar from "../../AppNavbar/AppNavbar";
import {useEffect, useState} from "react";
import {addNewProfile} from "../../../API/Profiles";
import NavigationButton from "../../Common/NavigationButton";
import NavigationLink from "../../Common/NavigationLink";
import ErrorMessage from "../../Common/ErrorMessage";
import {loginAPI, signupAPI} from "../../../API/Auth";
import {useNavigate} from "react-router-dom";




function ProfileCreatePage(props){
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [password2, setPassword2] = useState("");
    const [name, setName] = useState("");
    const [surname, setSurname] = useState("");
    const [errorMessage, setErrorMessage] = useState("")
    const [response, setResponse] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate()

    const loggedIn=props.loggedIn
    function submit() {
        setErrorMessage("")
        let missingFields = ""
        if(name.length === 0){
            missingFields = missingFields + "first name, "
        }
        if(surname.length === 0){
            missingFields = missingFields + "last name, "
        }
        if(email.length === 0){
            missingFields = missingFields + "email, "
        }
        if(password.length === 0){
            missingFields = missingFields + "password, "
        }
        if(password2.length === 0){
            missingFields = missingFields + "password match, "
        }

        if(missingFields.length > 0){
            missingFields = missingFields.substring(0, missingFields.length - 2)
            setErrorMessage("Missing fields: " + missingFields)
            return
        }


        if(!/^[A-Za-z]+$/i.test(name)){
            setErrorMessage("Error in form: wrong name format (first name)")
            return
        }
        if(!/^[A-Za-z]+$/i.test(surname)){
            setErrorMessage("Error in form: wrong name format (last name)")
            return
        }

        if(!/^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i.test(email)){
            setErrorMessage("Error in form: Email address is not valid")
            return
        }
        if(password !== password2){
            setErrorMessage("Error in form: Passwords do not match")
            return
        }

        signupAPI({
            firstName:name,
            lastName:surname,
            email:email,
            userName:email,
            password:password,
            expertCategories:[]
        }).then(
            () => navigate("/")
        ).catch(err => setErrorMessage(err))
    }

    return <>
            <AppNavbar user={props.user} logout={props.logout} loggedIn={loggedIn}/>
            <div className="CenteredButton" style={{marginTop:"50px"}}>
                <h1 style={{color:"#EEEEEE", marginTop:"80px"}}>SIGN UP</h1>
                <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>
                <Form className="form" style={{marginTop:"30px"}}>
                    <Form.Group className="mb-3" controlId="formBasicEmail" >
                        <Form.Label style={{color:"#DDDDDD"}}>Personal Info</Form.Label>
                        <div style={{width:"300px", margin:"auto"}}>
                            <Form.Control value={name} className={"form-control:focus"} style={{display:"inline-block",  marginRight:"10px", width: "140px", alignSelf:"center", marginTop:"5px", fontSize:12}} type="input" placeholder="First Name" onChange={e => setName(e.target.value)}/>
                            <Form.Control value={surname} className={"form-control:focus"} style={{display:"inline-block", marginLeft:"10px", width: "140px", alignSelf:"center", marginTop:"5px", fontSize:12}} type="input" placeholder="Last Name" onChange={e => setSurname(e.target.value)}/>
                        </div>
                        </Form.Group>
                    <Form.Group className="mb-3" controlId="formBasicEmail">
                        <Form.Label style={{color:"#DDDDDD"}}>Email address</Form.Label>
                        <Form.Control value={email} className={"form-control:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", fontSize:12}} type="email" placeholder="Email" onChange={e => setEmail(e.target.value)}/>
                    </Form.Group>
                    <Form.Group className="mb-3" controlId="formBasicEmail">
                        <Form.Label style={{color:"#DDDDDD"}}>Password</Form.Label>
                        <Form.Control value={password} style={{width: "300px", alignSelf:"center", margin:"auto", fontSize:12}} type="password" placeholder="Password" onChange={e => setPassword(e.target.value)}/>
                        <Form.Control value={password2} style={{width: "300px", alignSelf:"center", margin:"auto", marginTop:"10px", fontSize:12}} type="password" placeholder="Confirm Password" onChange={e => setPassword2(e.target.value)}/>
                    </Form.Group>
                    <NavigationButton text={"Sign up"} onClick={e => {e.preventDefault(); submit()}}/>
                </Form>

                <div style={{fontSize:"12px", color:"#EEEEEE", marginTop:"5px" }}>
                    <span>Already have an account?</span> <NavigationLink href={"/login"} text={"Sign in"}/>
                </div>

                {errorMessage && <ErrorMessage close={()=>setErrorMessage("")} text={errorMessage}/>}


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