import AppNavbar from "../../AppNavbar/AppNavbar";
import {Form} from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import {useState} from "react";
import {useNavigate} from "react-router-dom";

const Profile = {firstName:"Mario", lastName:"Rossi", email:"mariorossi@polito.it", address:"Corso Duca degli Abruzzi, 24 - Torino"}

function ProfileUpdatePage(props) {
    const loggedIn=props.loggedIn
    const navigate = useNavigate()

    const [firstName,setFirstName] = useState(Profile.firstName)
    const [lastName,setLastName] = useState(Profile.lastName)
    const [address,setAddress] = useState(Profile.address)
    function formElement(val, setVal) {
        return <Form.Control value={val} className={"form-control:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", marginTop:"10px"}} type="file" onChange={e => setVal(e.target.value)}/>

    }

    return <>
        <AppNavbar loggedIn={loggedIn} selected={"profile"} logout={props.logout}/>


        <div className="CenteredButton" style={{marginTop:"50px"}}>
            <h1 style={{color:"#EEEEEE", marginTop:"80px"}}>PROFILE EDIT</h1>
            <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"20px", marginTop:"2px"}}/>
            <div style={{width:"350px", borderRadius:"25px", marginTop:"20px", paddingTop:"5px", paddingBottom:"5px", margin:"auto", backgroundColor:"rgba(0,0,0,0.1)"}}>
                <div>
                    <h5 style={{color:"white"}}>First name</h5>
                    <Form.Control value={firstName} className={"form-control:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", fontSize:12}} placeholder="First Name" onChange={e => setFirstName(e.target.value)}/>
                </div>
                <div style={{marginTop:"15px"}}>
                    <h5 style={{color:"white"}}>Last name</h5>
                    <Form.Control value={lastName} className={"form-control:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", fontSize:12}} placeholder="Last Name" onChange={e => setLastName(e.target.value)}/>
                </div>

                <div style={{marginTop:"15px"}}>
                    <h5 style={{color:"white"}}>E-mail</h5>
                    <Form.Control value={Profile.email} disabled={true} className={"form-control:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", fontSize:12, opacity:"0.8"}} placeholder="E-mail" onChange={e => {}}/>
                </div>

                <div style={{marginTop:"15px", marginBottom:"15px"}}>
                    <h5 style={{color:"white"}}>Address</h5>
                    <Form.Control value={address} className={"form-control:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", fontSize:12}} placeholder="Address" onChange={e => setAddress(e.target.value)}/>
                </div>
            </div>

            <div style={{marginTop:"20px"}}>
                <NavigationButton text={"Submit"} onClick={e => {e.preventDefault(); navigate("/editprofile")}}/>
            </div>


        </div>
    </>
}

export default ProfileUpdatePage;