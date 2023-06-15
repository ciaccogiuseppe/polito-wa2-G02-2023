import AppNavbar from "../../AppNavbar/AppNavbar";
import {Form} from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import {useState} from "react";
import {useNavigate} from "react-router-dom";

const Profile = {firstName:"Mario", lastName:"Rossi", email:"mariorossi@polito.it", address:"Corso Duca degli Abruzzi, 24 - Torino"}

function ProfileInfoPage(props) {
    const loggedIn=props.loggedIn
    const navigate = useNavigate()

    function formElement(val, setVal) {
        return <Form.Control value={val} className={"form-control:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", marginTop:"10px"}} type="file" onChange={e => setVal(e.target.value)}/>

    }

    return <>
        <AppNavbar loggedIn={loggedIn} selected={"profile"}/>


        <div className="CenteredButton" style={{marginTop:"50px"}}>
            <h1 style={{color:"#EEEEEE", marginTop:"80px"}}>PROFILE INFO</h1>
            <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>
            <div style={{width:"350px", borderRadius:"25px", marginTop:"20px", paddingTop:"5px", paddingBottom:"5px", margin:"auto", backgroundColor:"rgba(0,0,0,0.1)"}}>
                <div>
                    <h5 style={{color:"white"}}>Name</h5>
                    <hr style={{color:"white", width:"125px", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"4px", marginTop:"4px"}}/>
                    <h5 style={{color:"#e3e3e3", fontSize:13}}>{Profile.firstName}</h5>
                </div>
                <div>
                    <h5 style={{color:"white"}}>Surname</h5>
                    <hr style={{color:"white", width:"125px", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"4px", marginTop:"4px"}}/>
                    <h5 style={{color:"#e3e3e3", fontSize:13}}>{Profile.lastName}</h5>
                </div>

                <div>
                    <h5 style={{color:"white"}}>E-mail</h5>
                    <hr style={{color:"white", width:"125px", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"4px", marginTop:"4px"}}/>
                    <h5 style={{color:"#e3e3e3", fontSize:13}}>{Profile.email}</h5>
                </div>

                <div>
                    <h5 style={{color:"white"}}>Address</h5>
                    <hr style={{color:"white", width:"125px", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"4px", marginTop:"4px"}}/>
                    <h5 style={{color:"#e3e3e3", fontSize:13}}>{Profile.address}</h5>
                </div>
            </div>

            <div style={{marginTop:"20px"}}>
                <NavigationButton text={"Edit profile"} onClick={e => {e.preventDefault(); navigate("/editprofile")}}/>
            </div>


        </div>
    </>
}

export default ProfileInfoPage;