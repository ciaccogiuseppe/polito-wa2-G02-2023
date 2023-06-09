import AppNavbar from "../../AppNavbar/AppNavbar";

function AboutUsPage(props) {
    const loggedIn=props.loggedIn
    return <>
            <AppNavbar loggedIn={loggedIn} selected={"aboutus"}/>
            <div className="CenteredButton" style={{marginTop:"50px"}}>
                <h1 style={{color:"#EEEEEE", marginTop:"80px"}}>ABOUT US</h1>
                <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"20px", marginTop:"2px"}}/>
                <h4 style={{color:"#DDDDDD"}}>Made by:</h4>
                <h5 style={{fontSize:"15px", color:"#EEEEEE", marginTop:"15px" }}>Giuliano Bellini - S294739</h5>
                <h5 style={{fontSize:"15px", color:"#EEEEEE", marginTop:"5px" }}>Giuseppe Ciacco - S295982</h5>
                <h5 style={{fontSize:"15px", color:"#EEEEEE", marginTop:"5px" }}>Giacomo Bruno - S301311</h5>
                <h5 style={{fontSize:"15px", color:"#EEEEEE", marginTop:"5px" }}>Flavio Ciravegna - S303398</h5>
            </div>
    </>
}

export default AboutUsPage;