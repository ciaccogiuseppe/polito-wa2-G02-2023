import AppNavbar from "../../AppNavbar/AppNavbar";
import { Form, Spinner } from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import { useEffect, useState } from "react";
import {useLocation, useNavigate, useParams} from "react-router-dom";
import { getProfileInfo } from "../../../API/Auth";
import SuccessMessage from "../../Common/SuccessMessage";
import { reformatCategory } from "../../Products/ProductsPage/ProductsPage";
import {getProfileDetails} from "../../../API/Profiles";

//const Profile = {firstName:"Mario", lastName:"Rossi", email:"mariorossi@polito.it", address:"Corso Duca degli Abruzzi, 24 - Torino"}

function ProfileInfoPageExpert(props) {
    const params = useParams()
    const email = params.email
    const location = useLocation()
    let message = location.state && location.state.message;
    const loggedIn = props.loggedIn
    const [Profile, setProfile] = useState(null)
    const [successMessage, setSuccessMessage] = useState(message || "")
    const [loading, setLoading] = useState(true)
    const navigate = useNavigate()

    useEffect(() => {
        console.log(email)
        window.scrollTo(0, 0)
        getProfileDetails(email).then(response => { setProfile(response.data) }).catch(err => console.log(err))
    }, [])

    useEffect(() => {
        if (Profile !== null) {
            setLoading(false);
        }
    }, [Profile])

    function formElement(val, setVal) {
        return <Form.Control value={val} className={"form-control:focus"} style={{ width: "300px", alignSelf: "center", margin: "auto", marginTop: "10px" }} type="file" onChange={e => setVal(e.target.value)} />

    }

    return <>
        <AppNavbar user={props.user} logout={props.logout} loggedIn={loggedIn} selected={"profile"} />


        <div className="CenteredButton" style={{ marginTop: "50px" }}>
            <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>PROFILE INFO</h1>
            <hr style={{ color: "white", width: "25%", alignSelf: "center", marginLeft: "auto", marginRight: "auto", marginBottom: "20px", marginTop: "2px" }} />
            <div style={{ width: "350px", borderRadius: "25px", marginTop: "20px", paddingTop: "5px", paddingBottom: "5px", margin: "auto", backgroundColor: "rgba(0,0,0,0.1)" }}>

                {loading ? <>
                    <Spinner style={{ color: "#A0C1D9" }} />
                </> : <><div>
                    <h5 style={{ color: "white" }}>First name</h5>
                    <hr style={{ color: "white", width: "125px", alignSelf: "center", marginLeft: "auto", marginRight: "auto", marginBottom: "4px", marginTop: "4px" }} />
                    <h5 style={{ color: "#e3e3e3", fontSize: 13 }}>{Profile.name}</h5>
                </div>
                    <div>
                        <h5 style={{ color: "white" }}>Last name</h5>
                        <hr style={{ color: "white", width: "125px", alignSelf: "center", marginLeft: "auto", marginRight: "auto", marginBottom: "4px", marginTop: "4px" }} />
                        <h5 style={{ color: "#e3e3e3", fontSize: 13 }}>{Profile.surname}</h5>
                    </div>

                    <div>
                        <h5 style={{ color: "white" }}>E-mail</h5>
                        <hr style={{ color: "white", width: "125px", alignSelf: "center", marginLeft: "auto", marginRight: "auto", marginBottom: "4px", marginTop: "4px" }} />
                        <h5 style={{ color: "#e3e3e3", fontSize: 13 }}>{Profile.email}</h5>
                    </div>

                    {Profile.role === "EXPERT" && <div>
                        <h5 style={{ color: "white" }}>Assigned categories</h5>
                        <hr style={{ color: "white", width: "125px", alignSelf: "center", marginLeft: "auto", marginRight: "auto", marginBottom: "4px", marginTop: "4px" }} />
                        <h5 style={{ color: "#e3e3e3", fontSize: 13 }}>{Profile.expertCategories.map(c => reformatCategory(c)).toString().replaceAll(",", ", ")}</h5>
                    </div>}

                    {(Profile.address && Profile.role === "CLIENT") && <div>
                        <h5 style={{ color: "white" }}>Address</h5>
                        <hr style={{ color: "white", width: "125px", alignSelf: "center", marginLeft: "auto", marginRight: "auto", marginBottom: "4px", marginTop: "4px" }} />
                        <h5 style={{ color: "#e3e3e3", fontSize: 13 }}>{Profile.address && (Profile.address.country + ", " + Profile.address.region + ", " + Profile.address.city + ", " + Profile.address.address)}</h5>
                    </div>}</>}


            </div>

            <div style={{ marginTop: "20px" }}>
                <NavigationButton text={"Back"} onClick={e => { e.preventDefault(); navigate(-1) }} />
            </div>


        </div>
    </>
}

export default ProfileInfoPageExpert;