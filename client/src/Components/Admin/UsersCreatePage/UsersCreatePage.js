import { Button, Col, Row, Spinner } from "react-bootstrap";
import AppNavbar from "../../AppNavbar/AppNavbar";
import { useEffect, useState } from "react";
import NavigationButton from "../../Common/NavigationButton";
import NavigationLink from "../../Common/NavigationLink";
import ErrorMessage from "../../Common/ErrorMessage";
import { useNavigate } from "react-router-dom";


function UsersCreatePage(props) {
    const [errorMessage, setErrorMessage] = useState("")
    const [response, setResponse] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate()

    const loggedIn = props.loggedIn
    useEffect(() => {
        window.scrollTo(0, 0)
    }, [])

    return <>
        <AppNavbar user={props.user} logout={props.logout} loggedIn={loggedIn} selected="userscreate"/>
        <div className="CenteredButton" style={{ marginTop: "50px" }}>
            <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>CREATE USERS</h1>
            <hr style={{ color: "white", width: "25%", alignSelf: "center", marginLeft: "auto", marginRight: "auto", marginBottom: "60px", marginTop: "2px" }} />

            <Row className="d-flex justify-content-center">
                <Col className="col-md-4">
                    <NavigationButton text={"Create expert"} onClick={e => { e.preventDefault(); navigate("/expertcreate") }} />
                </Col>
                <Col className="col-md-4">
                    <NavigationButton text={"Create vendor"} onClick={e => { e.preventDefault(); navigate("/vendorcreate") }} />
                </Col>
            </Row>

        </div>
    </>


}

export default UsersCreatePage;