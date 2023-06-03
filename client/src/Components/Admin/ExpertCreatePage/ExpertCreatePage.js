import AppNavbar from "../../AppNavbar/AppNavbar";

function ExpertCreatePage(props) {
    const loggedIn=props.loggedIn
    return <>
        <div style={{
            position: 'absolute',
            backgroundColor: '#537188',
            width: '100%',
            height: '100%'
        }}>
            <AppNavbar loggedIn={loggedIn}/>
        </div>
    </>
}

export default ExpertCreatePage;