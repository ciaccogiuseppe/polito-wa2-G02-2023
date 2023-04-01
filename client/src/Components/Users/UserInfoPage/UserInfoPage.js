import {Button, Form} from "react-bootstrap";
import AppNavbar from "../../AppNavbar/AppNavbar";

function UserInfoPage(props){
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
                        <Form.Control style={{width: "400px", alignSelf:"center", margin:"auto"}} type="email" placeholder="Enter email" />
                    </Form.Group>
                    <Button type="submit" variant="outline-info" style={{borderWidth:"2px"}} className="HomeButton" onClick={(e) => e.preventDefault()}>Search user</Button>
                </Form>
            </div>
        </div>
    </>
}

export default UserInfoPage;