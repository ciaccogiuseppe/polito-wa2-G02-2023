import AppNavbar from "../../AppNavbar/AppNavbar";

function TicketHistoryPage(props) {
    const loggedIn=props.loggedIn
    return <>
        <AppNavbar loggedIn={loggedIn}/>
    </>
}

export default TicketHistoryPage;