import AppNavbar from "../../AppNavbar/AppNavbar";

function TicketListPage(props) {
    const loggedIn=props.loggedIn
    return <>
            <AppNavbar loggedIn={loggedIn}/>
    </>
}

export default TicketListPage;