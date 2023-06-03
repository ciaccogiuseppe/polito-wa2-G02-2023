import AppNavbar from "../../AppNavbar/AppNavbar";

function TicketChatPage(props) {
    const loggedIn=props.loggedIn
    return <>
        <AppNavbar loggedIn={loggedIn}/>
    </>
}

export default TicketChatPage;