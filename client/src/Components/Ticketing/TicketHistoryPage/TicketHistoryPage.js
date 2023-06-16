import { Button, Col, Form, Row } from "react-bootstrap";
import AppNavbar from "../../AppNavbar/AppNavbar";
import { useEffect, useState } from "react";
import { addNewProfile } from "../../../API/Profiles";
import NavigationButton from "../../Common/NavigationButton";
import NavigationLink from "../../Common/NavigationLink";
import { crossIcon, filterIcon } from "../../Common/Icons"
import TicketHistoryTable from "./TicketHistoryTable";


function TicketHistoryPage(props) {
    const [userEmail, setUserEmail] = useState("");
    const [expertEmail, setExpertEmail] = useState("");
    const [ticketId, setTicketId] = useState("");
    const [initialDate, setInitialDate] = useState("");
    const [finalDate, setFinalDate] = useState("");
    const [errMessage, setErrMessage] = useState("");
    const [response, setResponse] = useState("");
    const [loading, setLoading] = useState(false);
    const [ticketList, setTicketList] = useState([])

    const loggedIn = props.loggedIn
    return <>
        <AppNavbar logout={props.logout} loggedIn={loggedIn} />
        <div className="CenteredButton" style={{ marginTop: "50px" }}>
            <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>TICKET HISTORY</h1>
            <hr style={{ color: "white", width: "25%", alignSelf: "center", marginLeft: "auto", marginRight: "auto", marginBottom: "2px", marginTop: "2px" }} />
            <h3 style={{ color: "#EEEEEE", marginTop: "15px" }}>Search Filters</h3>
            <div style={{ marginTop: "15px" }}>
                <Row className="d-flex justify-content-center" style={{ marginBottom: "10px" }}>
                    <Col xs={2}>
                        <span style={{ color: "#DDDDDD" }}>Ticket ID</span>
                        <div className="input-group mb-3" style={{ marginTop: "8px" }}>
                            <span style={{cursor: ticketId ? "pointer" : ""}} onClick={() => setTicketId("")} className="input-group-text">{ticketId ? crossIcon("black", 17) : filterIcon()}</span>
                            <input style={{ height: "40px" }} type="text" className="form-control" placeholder="---" value={ticketId} onChange={e => setTicketId(e.target.value)} />
                        </div>
                    </Col>
                    <Col xs={2}>
                        <span style={{ color: "#DDDDDD" }}>User Email</span>
                        <div className="input-group mb-3" style={{ marginTop: "8px" }}>
                        <span style={{cursor: userEmail ? "pointer" : ""}} onClick={() => setUserEmail("")} className="input-group-text">{userEmail ? crossIcon("black", 17) : filterIcon()}</span>
                            <input style={{ height: "40px" }} type="text" className="form-control" placeholder="---" value={userEmail} onChange={e => setUserEmail(e.target.value)} />
                        </div>
                    </Col>
                    <Col xs={2}>
                        <span style={{ color: "#DDDDDD" }}>Expert Email</span>
                        <div className="input-group mb-3" style={{ marginTop: "8px" }}>
                        <span style={{cursor: expertEmail ? "pointer" : ""}} onClick={() => setExpertEmail("")} className="input-group-text">{expertEmail ? crossIcon("black", 17) : filterIcon()}</span>
                            <input style={{ height: "40px" }} type="text" className="form-control" placeholder="---" value={expertEmail} onChange={e => setExpertEmail(e.target.value)} />
                        </div>
                    </Col>
                    <Col xs={2}>
                        <span style={{ color: "#DDDDDD" }}>Initial Date</span>
                        <div className="input-group mb-3" style={{ marginTop: "8px" }}>
                        <span style={{cursor: initialDate ? "pointer" : ""}} onClick={() => setInitialDate("")} className="input-group-text">{initialDate ? crossIcon("black", 17) : filterIcon()}</span>
                            <input style={{ height: "40px" }} type="date" className="form-control" placeholder="---" value={initialDate} onChange={e => setInitialDate(e.target.value)} />
                        </div>
                    </Col>
                    <Col xs={2}>
                        <span style={{ color: "#DDDDDD" }}>Final Date</span>
                        <div className="input-group mb-3" style={{ marginTop: "8px" }}>
                        <span style={{cursor: finalDate ? "pointer" : ""}} onClick={() => setFinalDate("")} className="input-group-text">{finalDate ? crossIcon("black", 17) : filterIcon()}</span>
                            <input style={{ height: "40px" }} type="date" className="form-control" placeholder="---" value={finalDate} onChange={e => setFinalDate(e.target.value)} />
                        </div>
                    </Col>
                </Row>
                <NavigationButton disabled={userEmail === "" && expertEmail === "" && ticketId === "" && initialDate === "" && finalDate === "" } text={"Search"} onClick={e => e.preventDefault()} />
            </div>

            {
                userEmail === "" && expertEmail === "" && ticketId === "" && initialDate === "" && finalDate === "" ?
                    <div style={{ fontSize: "12px", color: "#550000", marginTop: "5px" }}>
                        <span>At least one filter is required to start a search.</span>
                    </div> :
                    null
            }

            <hr style={{ color: "white", width: "90%", alignSelf: "center", marginLeft: "auto", marginRight: "auto", marginBottom: "20px", marginTop: "20px" }} />

            <TicketHistoryTable ticketList={ticketList}/>

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

export default TicketHistoryPage;