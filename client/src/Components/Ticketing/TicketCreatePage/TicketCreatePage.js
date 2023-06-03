import AppNavbar from "../../AppNavbar/AppNavbar";

function TicketCreatePage(props) {
    const loggedIn=props.loggedIn
    return <>
            <AppNavbar loggedIn={loggedIn}/>
    </>
}

export default TicketCreatePage;