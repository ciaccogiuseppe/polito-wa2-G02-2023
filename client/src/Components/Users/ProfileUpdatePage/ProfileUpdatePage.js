import {Button, Form, Spinner} from "react-bootstrap";
import AppNavbar from "../../AppNavbar/AppNavbar";
import {useEffect, useState} from "react";
import {addNewProfile, editProfile} from "../../../API/Profiles";




function ProfileUpdatePage(props){
    const [oldEmail, setOldEmail] = useState("");
    const [email, setEmail] = useState("");
    const [name, setName] = useState("");
    const [surname, setSurname] = useState("");
    const [errMessage, setErrMessage] = useState("");
    const [response, setResponse] = useState("");
    const [loading, setLoading] = useState(false);
    function updateProfile(){
        setLoading(true);
        editProfile({oldemail:oldEmail, email:email, name:name, surname:surname}).then(
            res => {
                setErrMessage("");
                setResponse("Profile updated successfully");
                setEmail("");
                setOldEmail("");
                setName("");
                setSurname("");
                setLoading(false);
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
        <div style={{
            position: 'absolute',
            backgroundColor: '#222222',
            width: '100%',
            height: '100%'}}>
            <AppNavbar/>

            <div className="CenteredButton">

                <Form className="form">
                    <Form.Group className="mb-3" controlId="formBasicEmail">
                        <Form.Label className="text-info">Email address</Form.Label>
                        <Form.Control style={{width: "400px", alignSelf:"center", margin:"auto", marginBottom:"4px"}} value={oldEmail} type="email" placeholder="Email address" onChange={e => setOldEmail(e.target.value)}/>
                        <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto"}}/>
                        <Form.Label style={{marginTop:"8px"}} className="text-info">New Email address</Form.Label>
                        <Form.Control style={{width: "400px", alignSelf:"center", margin:"auto"}} value={email} type="email" placeholder="New email" onChange={e => setEmail(e.target.value)}/>
                        <Form.Label style={{marginTop:"8px"}} className="text-info">Name</Form.Label>
                        <Form.Control style={{width: "400px", alignSelf:"center", margin:"auto"}} value={name} type="text" placeholder="Name" onChange={e => setName(e.target.value)}/>
                        <Form.Label style={{marginTop:"8px"}} className="text-info">Surname</Form.Label>
                        <Form.Control style={{width: "400px", alignSelf:"center", margin:"auto"}} value={surname} type="text" placeholder="Surname" onChange={e => setSurname(e.target.value)}/>
                    </Form.Group>
                    <Button type="submit" variant="outline-info" style={{borderWidth:"2px"}} className="HomeButton" onClick={(e) => {e.preventDefault(); updateProfile();}}>Update profile</Button>
                </Form>
                <hr style={{color:"white", width:"90%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginTop:"20px"}}/>
                {loading? <Spinner style={{alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginTop:"20px"}} animation="border" variant="info" /> :
                    <>
                        {response?<h4 className="text-success" style={{marginTop:"10px"}}>{response}</h4>:<></>}
                        {errMessage?<h5 className="text-danger" style={{marginTop:"10px"}}>{errMessage}</h5>:<></>}</>}

            </div>
        </div>
    </>
}

export default ProfileUpdatePage;