import {Button, Form, Spinner} from "react-bootstrap";
import AppNavbar from "../../AppNavbar/AppNavbar";
import {useEffect, useState} from "react";
import {getProfileDetails} from "../../../API/Profiles";




function ProfileInfoPage(props){
    const [email, setEmail] = useState("");
    const [errMessage, setErrMessage] = useState("");
    const [response, setResponse] = useState("");
    const [loading, setLoading] = useState(false);

    function getProfile(){
        setLoading(true);
        getProfileDetails(email).then(
            res => {
                setErrMessage("");
                setResponse(res);
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
                        <Form.Control style={{width: "400px", alignSelf:"center", margin:"auto"}} type="email" placeholder="Email" onChange={e => setEmail(e.target.value)}/>
                    </Form.Group>
                    <Button type="submit" variant="outline-info" style={{borderWidth:"2px"}} className="HomeButton" onClick={(e) => {e.preventDefault(); getProfile();}}>Search user</Button>
                </Form>
                <hr style={{color:"white", width:"90%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginTop:"20px"}}/>
                {loading? <Spinner style={{alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginTop:"20px"}} animation="border" variant="info" /> :
                    <>
                {response.name?<h4 className="text-light" style={{marginTop:"10px"}}>Name</h4>:<></>}
                <div className="text-info">{response.name}</div>
                {response.surname?<h4 className="text-light" style={{marginTop:"10px"}}>Surname</h4>:<></>}
                <div className="text-info">{response.surname}</div>
                {response.email?<h4 className="text-light" style={{marginTop:"10px"}}>E-Mail</h4>:<></>}
                <div className="text-info">{response.email}</div>

                {errMessage?<h5 className="text-danger" style={{marginTop:"10px"}}>{errMessage}</h5>:<></>}</>}

            </div>
        </div>
    </>
}

export default ProfileInfoPage;