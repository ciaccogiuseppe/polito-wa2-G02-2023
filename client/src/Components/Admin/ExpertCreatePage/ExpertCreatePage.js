import AppNavbar from "../../AppNavbar/AppNavbar";

function ExpertCreatePage(props) {
    const loggedIn=props.loggedIn
    return <>
            <AppNavbar loggedIn={loggedIn}/>
    </>
}

export default ExpertCreatePage;