import AppNavbar from "../../AppNavbar/AppNavbar";
import {Form} from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import {useState} from "react";

function TicketCreatePage(props) {
    const loggedIn=props.loggedIn
    const [title, setTitle] = useState("")
    const [description, setDescription] = useState("")
    const [file, setFile] = useState([])


    function formElement(val, setVal) {
        return <Form.Control value={val} className={"form-control:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", marginTop:"10px"}} type="file" onChange={e => setVal(e.target.value)}/>

    }

    return <>
            <AppNavbar user={props.user} logout={props.logout} loggedIn={loggedIn} selected={"tickets"}/>


        <div className="CenteredButton" style={{marginTop:"50px"}}>
            <h1 style={{color:"#EEEEEE", marginTop:"80px"}}>OPEN A TICKET</h1>
            <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>
            <Form className="form" style={{marginTop:"30px"}}>
                <Form.Group className="mb-3" controlId="formBasicEmail">
                    <Form.Label style={{color:"#DDDDDD"}}>Ticket Info</Form.Label>
                    <Form.Control value={title} className={"form-control:focus"} style={{width: "300px", alignSelf:"center", margin:"auto"}} type="input" placeholder="Ticket Title" onChange={e => setTitle(e.target.value)}/>
                    <Form.Control value={description} className={"form-control:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", marginTop:"10px"}} type="textarea" as={"textarea"} placeholder="Ticket Description" onChange={e => setDescription(e.target.value)}/>

                </Form.Group>

                <Form.Group className="mb-3" controlId="formBasicEmail">
                    <Form.Label style={{color:"#DDDDDD"}}>Product</Form.Label>
                    <Form.Select  className={"form-select:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", marginTop:"10px"}}>
                        <option>Brand</option>
                    </Form.Select>
                    <Form.Select  className={"form-select:focus"} style={{width: "300px", alignSelf:"center", margin:"auto", marginTop:"10px"}}>
                        <option>Product</option>
                    </Form.Select>
                </Form.Group>
                <NavigationButton text={"Create Ticket"} onClick={e => e.preventDefault()}/>
            </Form>

        </div>
    </>
}

export default TicketCreatePage;