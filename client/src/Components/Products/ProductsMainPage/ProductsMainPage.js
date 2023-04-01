import {Button} from "react-bootstrap";
import AppNavbar from "../../AppNavbar/AppNavbar";

function ProductsMainPage(props){
    return <>
        <div style={{
            position: 'absolute',
            backgroundColor: '#222222',
            width: '100%',
            height: '100%'}}>
            <AppNavbar/>

            <div className="CenteredButton">
                <Button variant="info" style={{borderColor:"black", borderWidth:"2px", marginTop:"50px"}} className="HomeButton">Get all products</Button>
            </div>
            <div className="CenteredButton">
                <Button variant="info" style={{borderColor:"black", borderWidth:"2px"}} className="HomeButton">Get product by ID</Button>
            </div>
        </div>
    </>
}

export default ProductsMainPage;