import AppNavbar from "../../AppNavbar/AppNavbar";
import {useParams} from "react-router-dom";
import {editIcon, xIcon} from "../../Common/Icons.js"
import StatusIndicator from "../TicketCommon/StatusIndicator";
import TextNewLine from "../../Common/TextNewLine";
import ChatMessage from "./ChatMessage";
import {Button, Form} from "react-bootstrap";
import AddButton from "../../Common/AddButton";



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
            <div style={{width: "250px", alignSelf:"center", margin:"auto"}}>
                <div style={{width:"150px", display:"inline-block", paddingLeft:"20px"}}>
                    {StatusIndicator("INPROGRESS")}
                </div>
                <div style={{display:"inline-block", float:"right"}}>
                    {editIcon("white", 20)}
                </div>

            </div>

            <div style={{backgroundColor:"rgba(255,255,255,0.1)", borderRadius:"20px", padding:"15px", width:"85%", alignSelf:"left", textAlign:"left", margin:"auto", fontSize:"14px", color:"#EEEEEE", marginTop:"15px" }}>
                {TextNewLine(`I am writing to report a problem I am experiencing with the touchscreen functionality on my phone. I am unable to use the touchscreen properly, which is causing significant inconvenience in using my device.
                
                Problem Description:
                I am unable to interact with the touchscreen on my phone. When I try to tap or swipe on the screen, there is no response or the response is delayed. This issue is persistent across the entire screen and not limited to specific areas.
                
                Troubleshooting Steps Taken:
                I have attempted the following troubleshooting steps to resolve the issue, but none of them have been successful:
                
                - Restarted the phone: I have powered off my phone and turned it back on, hoping that a simple reboot would fix the problem. However, the touchscreen issue persists even after the restart.
                
                - Checked for physical damage: I have carefully inspected the screen for any signs of physical damage, such as cracks or scratches. Fortunately, there are no visible damages that could be causing the issue.`)}
            </div>
            <h5 style={{color:"#EEEEEE", marginTop:"14px", marginBottom:"15px"}}>PRODUCT: Apple - iPhone 13 Pro 128GB</h5>
            <hr style={{color:"white", width:"75%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>
            <h1 style={{color:"#EEEEEE", marginTop:"30px"}}>CHAT</h1>
            <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"22px", marginTop:"2px"}}/>

            <div style={{backgroundColor:"rgba(255,255,255,0.1)", verticalAlign:"middle", borderRadius:"20px", padding:"15px", width:"95%", alignSelf:"left", textAlign:"left", margin:"auto", fontSize:"14px", color:"#EEEEEE", marginTop:"5px" }}>

                <ChatMessage isExpert={true} timestamp={"05/03/2023 - 10:12"} name={"Mario Rossi"} text={`Could you provide additional information on xyz?`}/>
                <ChatMessage isExpert={false} timestamp={"05/03/2023 - 10:13"} name={"Luigi Bianchi"}  text={`Here there are some info\n test test`}/>
                <ChatMessage isExpert={false} timestamp={"05/03/2023 - 10:23"} name={"Luigi Bianchi"}  text={`Could you provide additional information on xyz?`}/>
                <Form.Control style={{borderColor:"rgba(0,0,0,0.6)", paddingLeft:"32px", paddingTop:"15px", backgroundColor:"rgba(0,0,0,0.4)", color:"white", resize:"none", height:"200px", boxShadow:"0px 4px 8px -4px rgba(0,0,0,0.8)", borderRadius:"20px", marginTop:"5px"}} placeholder="Write your message here..." type="textarea" as="textarea"/>

                <div style={{flex:"true"}}>
                    <input style={{maxWidth:"230px", marginTop:"10px", marginLeft:"10px"}} type="file" className="filestyle" data-icon="false"/>
                    {xIcon("#d98080", "20")}
                </div>

                <div style={{flex:"true"}}>
                    <input style={{maxWidth:"230px", marginTop:"10px", marginLeft:"10px"}} type="file" className="filestyle" data-icon="false"/>
                    {xIcon("#d98080", "20")}
                </div>

                <div style={{width:"100%", height:"60px"}}>
                    <AddButton style={{marginTop:"10px", marginRight:"10px", float:"right"}} onClick={() => {}}/>
                </div>
            </div>

        </div>
    </>
}

export default TicketChatPage;