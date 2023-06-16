import { Button, Form, Spinner, Col, Row } from "react-bootstrap";
import AppNavbar from "../../AppNavbar/AppNavbar";
import React, { useEffect, useState } from "react";
import NavigationButton from "../../Common/NavigationButton";
import NavigationLink from "../../Common/NavigationLink";

const categories = ["Smartphone", "TV", "PC", "Software", "Storage devices", "Other"]

function ExpertCreatePage(props) {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [password2, setPassword2] = useState("");
    const [name, setName] = useState("");
    const [surname, setSurname] = useState("");
    const [errMessage, setErrMessage] = useState("");
    const [response, setResponse] = useState("");
    const [loading, setLoading] = useState(false);

    const [expertCategories, setExpertCategories] = useState([]);
    const updateExpertCategories = (event) => {
        event.target.checked ?
        setExpertCategories(expertCategories => [...expertCategories, event.target.id]) :
        setExpertCategories(expertCategories => expertCategories.filter(e => e!=event.target.id))
    }

    let categoryCheckboxes = [];
    for (let category of categories) {
        categoryCheckboxes.push(
            <Form.Check style={{ color: "#DDDDDD", accentColor: "white", width: "200px", alignSelf: "center", textAlign: "left", margin: "auto", marginBottom: "10px" }}
                type={"checkbox"}
                id={category}
                key={category}
                label={category}
                checked={expertCategories.includes(category)}
                onChange={updateExpertCategories}
            />
        );
    }

    const loggedIn = props.loggedIn
    return <>
        <AppNavbar loggedIn={loggedIn} logout={props.logout}/>
        <div className="CenteredButton" style={{ marginTop: "50px" }}>
            <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>CREATE NEW EXPERT</h1>
            <hr style={{ color: "white", width: "25%", alignSelf: "center", marginLeft: "auto", marginRight: "auto", marginBottom: "2px", marginTop: "2px" }} />

            <Row className="d-flex justify-content-center" style={{ marginBottom: "30px" }}>
                <Col className="col-md-4">
                    <Form className="form" style={{ marginTop: "30px" }}>
                        <Form.Group className="mb-3" controlId="formBasicEmail">
                            <Form.Label style={{ color: "#DDDDDD" }}>Expert Details</Form.Label>
                            <Form.Control value={name} className={"form-control:focus"} style={{ width: "250px", alignSelf: "center", margin: "auto" }} type="input" placeholder="First Name" onChange={e => setName(e.target.value)} />
                            <Form.Control value={surname} className={"form-control:focus"} style={{ width: "250px", alignSelf: "center", margin: "auto", marginTop: "10px" }} type="input" placeholder="Last Name" onChange={e => setSurname(e.target.value)} />
                        </Form.Group>
                        <Form.Group className="mb-3" controlId="formBasicEmail">
                            <Form.Label style={{ color: "#DDDDDD" }}>Email address</Form.Label>
                            <Form.Control value={email} className={"form-control:focus"} style={{ width: "250px", alignSelf: "center", margin: "auto" }} type="email" placeholder="Email" onChange={e => setEmail(e.target.value)} />
                        </Form.Group>
                        <Form.Group className="mb-3" controlId="formBasicEmail">
                            <Form.Label style={{ color: "#DDDDDD" }}>Password</Form.Label>
                            <Form.Control value={password} style={{ width: "250px", alignSelf: "center", margin: "auto" }} type="password" placeholder="Password" onChange={e => setPassword(e.target.value)} />
                            <Form.Control value={password2} style={{ width: "250px", alignSelf: "center", margin: "auto", marginTop: "10px" }} type="password" placeholder="Confirm Password" onChange={e => setPassword2(e.target.value)} />
                        </Form.Group>
                    </Form>
                </Col>
                <Col className="col-md-4">
                    <Form style={{ marginTop: "30px" }}>
                        <Form.Group key={`default-checkbox`}>
                            <Form.Label style={{ color: "#DDDDDD" }}>Assigned Product Categories</Form.Label>
                            {categoryCheckboxes}
                        </Form.Group>
                    </Form>
                </Col>
            </Row>
            <NavigationButton text={"Create new expert"} onClick={e => e.preventDefault()} />


        </div>
    </>


    {/*function createExpert(){
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

export default ExpertCreatePage;