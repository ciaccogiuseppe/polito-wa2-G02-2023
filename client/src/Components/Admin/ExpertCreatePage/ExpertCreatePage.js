import { Button, Form, Spinner, Col, Row } from "react-bootstrap";
import AppNavbar from "../../AppNavbar/AppNavbar";
import React, { useEffect, useState } from "react";
import NavigationButton from "../../Common/NavigationButton";
import NavigationLink from "../../Common/NavigationLink";
import ErrorMessage from "../../Common/ErrorMessage";
import { createExpertAPI } from "../../../API/Auth";
import { useNavigate } from "react-router-dom";
import { deformatCategory } from "../../Products/ProductsPage/ProductsPage";
import "./ExpertCreatePage.css"

const categories = ["Smartphone", "TV", "PC", "Software", "Storage Device", "Other"];

function ExpertCreatePage(props) {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [password2, setPassword2] = useState("");
    const [name, setName] = useState("");
    const [surname, setSurname] = useState("");
    const [errorMessage, setErrorMessage] = useState("");
    const [response, setResponse] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const [expertCategories, setExpertCategories] = useState([]);
    const updateExpertCategories = (event) => {
        event.target.checked ?
            setExpertCategories(expertCategories => [...expertCategories, event.target.id]) :
            setExpertCategories(expertCategories => expertCategories.filter(e => e != event.target.id))
    }

    function submit() {
        setErrorMessage("")
        let missingFields = ""
        if (name.length === 0) {
            missingFields = missingFields + "first name, "
        }
        if (surname.length === 0) {
            missingFields = missingFields + "last name, "
        }
        if (email.length === 0) {
            missingFields = missingFields + "email, "
        }
        if (password.length === 0) {
            missingFields = missingFields + "password, "
        }
        if (password2.length === 0) {
            missingFields = missingFields + "password match, "
        }

        if (missingFields.length > 0) {
            missingFields = missingFields.substring(0, missingFields.length - 2)
            setErrorMessage("Missing fields: " + missingFields)
            return
        }


        if (!/^[A-Za-z]+$/i.test(name)) {
            setErrorMessage("Error in form: wrong name format (first name)")
            return
        }
        if (!/^[A-Za-z]+$/i.test(surname)) {
            setErrorMessage("Error in form: wrong name format (last name)")
            return
        }

        if (!/^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i.test(email)) {
            setErrorMessage("Error in form: Email address is not valid")
            return
        }
        if (password !== password2) {
            setErrorMessage("Error in form: Passwords do not match")
            return
        }

        if (expertCategories.length === 0) {
            setErrorMessage("Error in form: No product categories selected")
            return
        }

        createExpertAPI({
            firstName: name,
            lastName: surname,
            email: email,
            userName: email,
            password: password,
            expertCategories: expertCategories
        }).then(
            () => navigate("/")
        ).catch(err => setErrorMessage(err))
    }

    useEffect(() => {
        window.scrollTo(0, 0)
    }, [])

    let categoryCheckboxes = [];
    for (let category of categories) {
        categoryCheckboxes.push(
            <Form.Check className={"form-check"} style={{cursor:"pointer", color: "#DDDDDD", accentColor: "white", width: "200px", alignSelf: "center", textAlign: "left", margin: "auto", marginBottom: "10px" }}
                type={"checkbox"}
                id={deformatCategory(category)}
                key={deformatCategory(category)}
                label={category}
                checked={expertCategories.includes(deformatCategory(category))}
                onChange={updateExpertCategories}
            />
        );
    }

    const loggedIn = props.loggedIn
    return <>
        <AppNavbar user={props.user} loggedIn={loggedIn} logout={props.logout} selected={"expertcreate"}/>
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
            <NavigationButton text={"Create new expert"} onClick={e => { e.preventDefault(); submit() }} />

            {errorMessage && <ErrorMessage close={() => setErrorMessage("")} text={errorMessage} />}

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