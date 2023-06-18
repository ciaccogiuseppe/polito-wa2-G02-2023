import { Button, Col, Form, Row } from "react-bootstrap";
import AppNavbar from "../../AppNavbar/AppNavbar";
import { useEffect, useState } from "react";
import NavigationButton from "../../Common/NavigationButton";
import NavigationLink from "../../Common/NavigationLink";
import { crossIcon, filterIcon, caretDownIcon, caretUpIcon, } from "../../Common/Icons"
import TicketHistoryTable from "./TicketHistoryTable";
import { getTicketHistoryAPI } from "../../../API/TicketHistory";


function TicketHistoryPage(props) {
    const [userEmail, setUserEmail] = useState("");
    const [expertEmail, setExpertEmail] = useState("");
    const [ticketId, setTicketId] = useState("");
    const [initialDate, setInitialDate] = useState("");
    const [finalDate, setFinalDate] = useState("");
    const [errMessage, setErrMessage] = useState("");
    const [response, setResponse] = useState("");
    const [loading, setLoading] = useState(false);
    const [ticketList, setTicketList] = useState([]);
    const [showFilters, setShowFilters] = useState(true);

    useEffect(() => {
        window.scrollTo(0, 0)
    }, [])

    function applyFilters() {
        getTicketHistoryAPI(
            {
                ticketId: ticketId,
                userEmail: userEmail,
                currentExpertEmail: expertEmail,
                updatedAfter: initialDate && new Date(initialDate).toISOString().replace(/.$/, ''),
                updatedBefore: finalDate && new Date(finalDate).toISOString().replace(/.$/, ''),
            }
        ).then(tickets => {
            setTicketList(tickets.sort((a, b) => a.ticketId < b.ticketId))
            setShowFilters(false)
        })
    }

    const loggedIn = props.loggedIn
    return <>
        <AppNavbar user={props.user} logout={props.logout} loggedIn={loggedIn} selected={"tickethistory"}/>
        <div className="CenteredButton" style={{ marginTop: "50px" }}>
            <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>TICKET HISTORY</h1>
            <hr style={{ color: "white", width: "25%", alignSelf: "center", marginLeft: "auto", marginRight: "auto", marginBottom: "2px", marginTop: "2px" }} />

            <div style={{ paddingBottom: "10px", width: "75%", alignSelf: "center", margin: "auto", borderRadius: "25px", marginTop: "15px", backgroundColor: "rgba(0,0,0,0.2)" }}>
                <h4 style={{ color: "#EEEEEE", paddingTop: "10px" }}>FILTERS</h4>
                {showFilters ?
                    <div onClick={() => { setShowFilters(false) }} style={{ display: "inline-block", paddingBottom: "10px", cursor: "pointer" }}>
                        {caretUpIcon("white", 30)}
                    </div> :
                    <div onClick={() => { setShowFilters(true) }} style={{ display: "inline-block", paddingBottom: "10px", cursor: "pointer" }}>
                        {caretDownIcon("white", 30)}
                    </div>}

                {showFilters && <>
                    <hr style={{ color: "white", width: "25%", alignSelf: "center", marginLeft: "auto", marginRight: "auto", marginBottom: "2px", marginTop: "2px" }} />

                    <div style={{ marginTop: "15px" }}>
                        <Row className="d-flex justify-content-center" style={{ marginBottom: "10px" }}>
                            <Col xs={3}>
                                <span style={{ color: "#DDDDDD" }}>Ticket ID</span>
                                <div className="input-group mb-3" style={{ marginTop: "8px" }}>
                                    <span style={{ cursor: ticketId ? "pointer" : "" }} onClick={() => setTicketId("")} className="input-group-text">{ticketId ? crossIcon("black", 17) : filterIcon()}</span>
                                    <input style={{ height: "40px" }} type="text" className="form-control" placeholder="---" value={ticketId} onChange={e => setTicketId(e.target.value)} />
                                </div>
                            </Col>
                            <Col xs={3}>
                                <span style={{ color: "#DDDDDD" }}>Updated After</span>
                                <div className="input-group mb-3" style={{ marginTop: "8px" }}>
                                    <span style={{ cursor: initialDate ? "pointer" : "" }} onClick={() => setInitialDate("")} className="input-group-text">{initialDate ? crossIcon("black", 17) : filterIcon()}</span>
                                    <input style={{ height: "40px" }} type="date" className="form-control" placeholder="---" value={initialDate} onChange={e => setInitialDate(e.target.value)} />
                                </div>
                            </Col>
                            <Col xs={3}>
                                <span style={{ color: "#DDDDDD" }}>Updated Before</span>
                                <div className="input-group mb-3" style={{ marginTop: "8px" }}>
                                    <span style={{ cursor: finalDate ? "pointer" : "" }} onClick={() => setFinalDate("")} className="input-group-text">{finalDate ? crossIcon("black", 17) : filterIcon()}</span>
                                    <input style={{ height: "40px" }} type="date" className="form-control" placeholder="---" value={finalDate} onChange={e => setFinalDate(e.target.value)} />
                                </div>
                            </Col>
                        </Row>
                        <Row className="d-flex justify-content-center" style={{ marginBottom: "10px" }}>
                            <Col xs={3}>
                                <span style={{ color: "#DDDDDD" }}>User Email</span>
                                <div className="input-group mb-3" style={{ marginTop: "8px" }}>
                                    <span style={{ cursor: userEmail ? "pointer" : "" }} onClick={() => setUserEmail("")} className="input-group-text">{userEmail ? crossIcon("black", 17) : filterIcon()}</span>
                                    <input style={{ height: "40px" }} type="text" className="form-control" placeholder="---" value={userEmail} onChange={e => setUserEmail(e.target.value)} />
                                </div>
                            </Col>
                            <Col xs={3}>
                                <span style={{ color: "#DDDDDD" }}>Expert Email</span>
                                <div className="input-group mb-3" style={{ marginTop: "8px" }}>
                                    <span style={{ cursor: expertEmail ? "pointer" : "" }} onClick={() => setExpertEmail("")} className="input-group-text">{expertEmail ? crossIcon("black", 17) : filterIcon()}</span>
                                    <input style={{ height: "40px" }} type="text" className="form-control" placeholder="---" value={expertEmail} onChange={e => setExpertEmail(e.target.value)} />
                                </div>
                            </Col>
                        </Row>
                        <NavigationButton disabled={userEmail === "" && expertEmail === "" && ticketId === "" && initialDate === "" && finalDate === ""} text={"Search"} onClick={e => { e.preventDefault(); applyFilters() }} />
                    </div>

                    {
                        userEmail === "" && expertEmail === "" && ticketId === "" && initialDate === "" && finalDate === "" ?
                            <div style={{ fontSize: "12px", color: "#550000", marginTop: "5px" }}>
                                <span>At least one filter is required to start a search.</span>
                            </div> :
                            null
                    }</>}
            </div>


            <TicketHistoryTable ticketList={ticketList} />

        </div>

    </>
}

export default TicketHistoryPage;