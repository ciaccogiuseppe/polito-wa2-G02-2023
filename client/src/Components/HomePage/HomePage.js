import { Button } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import "./HomePage.css";
import AppNavbar from "../AppNavbar/AppNavbar";

function HomePage(props) {
    const navigate = useNavigate();
    return <>
        <div style={{
            position: 'absolute',
            backgroundColor: '#222222',
            width: '100%',
            height: '100%'
        }}>
            <AppNavbar />
            <div className="CenteredButton">
                <Button variant="info" style={{ borderColor: "black", borderWidth: "2px", marginTop: "50px" }} className="HomeButton"
                    onClick={(e) => { e.preventDefault(); navigate("/products") }}>
                    Get all products
                </Button>
            </div>
            <div className="CenteredButton">
                <Button variant="info" style={{ borderColor: "black", borderWidth: "2px" }} className="HomeButton"
                    onClick={(e) => { e.preventDefault(); navigate("/productid") }}>
                    Get product by ID
                </Button>
            </div>
            <div className="CenteredButton">
                <Button variant="info" style={{ borderColor: "black", borderWidth: "2px" }} className="HomeButton" onClick={(e) => { e.preventDefault(); navigate("/userinfo") }}>Get profile by mail</Button>
            </div>
            <div className="CenteredButton">
                <Button variant="info" style={{ borderColor: "black", borderWidth: "2px" }} className="HomeButton" onClick={(e) => { e.preventDefault(); navigate("/usercreate") }}>Create new profile</Button>
            </div>
            <div className="CenteredButton">
                <Button variant="info" style={{ borderColor: "black", borderWidth: "2px" }} className="HomeButton" onClick={(e) => { e.preventDefault(); navigate("/userupdate") }}>Edit profile</Button>
            </div>
            <hr style={{color:"white", width:"90%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginTop:"20px"}}/>

            <div className="CenteredButton">
            <div className="text-white">This client is intended to test server APIs, therefore only few client-side validation checks are implemented</div>
            </div>
        </div>
    </>
}

export default HomePage;