import AppNavbar from "../../AppNavbar/AppNavbar";
import {useParams} from "react-router-dom";

function TextNewLine(text){
    return text.toString().split('\n').map(str => <p>{str}</p>)
}

function TicketChatPage(props) {
    const loggedIn=props.loggedIn
    const params = useParams()
    const ticketID = params.id
    return <>
        <AppNavbar loggedIn={loggedIn} selected={"tickets"}/>
        <div className="CenteredButton" style={{marginTop:"50px"}}>
            <h1 style={{color:"#EEEEEE", marginTop:"80px"}}>TICKET</h1>
            <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>
            <h5 style={{color:"#EEEEEE"}}>Can't use touchscreen on my phone</h5>
            <div style={{backgroundColor:"rgba(255,255,255,0.1)", borderRadius:"20px", padding:"15px", width:"85%", alignSelf:"left", textAlign:"left", margin:"auto", fontSize:"14px", color:"#EEEEEE", marginTop:"5px" }}>
                {TextNewLine(`I am writing to report a problem I am experiencing with the touchscreen functionality on my phone. I am unable to use the touchscreen properly, which is causing significant inconvenience in using my device.
                \n\n
                Problem Description:\n
                I am unable to interact with the touchscreen on my phone. When I try to tap or swipe on the screen, there is no response or the response is delayed. This issue is persistent across the entire screen and not limited to specific areas.
                \n\n
                Troubleshooting Steps Taken:\n
                I have attempted the following troubleshooting steps to resolve the issue, but none of them have been successful:
                \n\n
                Restarted the phone: I have powered off my phone and turned it back on, hoping that a simple reboot would fix the problem. However, the touchscreen issue persists even after the restart.
                \n\n
                Checked for physical damage: I have carefully inspected the screen for any signs of physical damage, such as cracks or scratches. Fortunately, there are no visible damages that could be causing the issue.`)}
            </div>
            <h5 style={{color:"#EEEEEE", marginTop:"14px", marginBottom:"15px"}}>PRODUCT: Apple - iPhone 13 Pro 128GB</h5>
            <hr style={{color:"white", width:"75%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>
            <h1 style={{color:"#EEEEEE", marginTop:"30px"}}>CHAT</h1>
            <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>
        </div>
    </>
}

export default TicketChatPage;