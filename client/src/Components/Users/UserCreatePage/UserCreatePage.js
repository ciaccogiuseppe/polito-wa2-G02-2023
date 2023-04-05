import {Button, Form, Spinner} from "react-bootstrap";
import AppNavbar from "../../AppNavbar/AppNavbar";
import {useEffect, useState} from "react";
import {addNewProfile} from "../../../API/Profiles";




function UserCreatePage(props){
    const [email, setEmail] = useState("");
    const [name, setName] = useState("");
    const [surname, setSurname] = useState("");
    const [errMessage, setErrMessage] = useState("");
    const [response, setResponse] = useState("");
    const [loading, setLoading] = useState(false);
    function createProfile(){
        setLoading(true);
        addNewProfile({email:email, name:name, surname:surname}).then(
            res => {
                setErrMessage("");
                setResponse("User added succesfully");
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
                        <Form.Label>Email address</Form.Label>
                        <Form.Control style={{width: "400px", alignSelf:"center", margin:"auto"}} type="email" placeholder="Enter email" onChange={e => setEmail(e.target.value)}/>
                        <Form.Label>Name</Form.Label>
                        <Form.Control style={{width: "400px", alignSelf:"center", margin:"auto"}} type="text" placeholder="Name" onChange={e => setName(e.target.value)}/>
                        <Form.Label>Surname</Form.Label>
                        <Form.Control style={{width: "400px", alignSelf:"center", margin:"auto"}} type="text" placeholder="Surname" onChange={e => setSurname(e.target.value)}/>
                    </Form.Group>
                    <Button type="submit" variant="outline-info" style={{borderWidth:"2px"}} className="HomeButton" onClick={(e) => {e.preventDefault(); createProfile();}}>Create user</Button>
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

export default UserCreatePage;