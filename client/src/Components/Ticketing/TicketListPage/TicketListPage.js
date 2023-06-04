import AppNavbar from "../../AppNavbar/AppNavbar";
import {Form} from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import NavigationLink from "../../Common/NavigationLink";
import TicketListTable from "./TicketListTable";
import {useState} from "react";

function TicketListPage(props) {
    const loggedIn=props.loggedIn
    const [ticketList, setTicketList] = useState([])
    return <>
        <AppNavbar loggedIn={loggedIn} selected={"tickets"}/>
        <div className="CenteredButton" style={{marginTop:"50px"}}>
            <h1 style={{color:"#EEEEEE", marginTop:"80px"}}>MY TICKETS</h1>
            <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>
            <TicketListTable ticketList={ticketList}/>
        </div>
    </>
}

export default TicketListPage;