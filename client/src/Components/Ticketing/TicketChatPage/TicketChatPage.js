import AppNavbar from "../../AppNavbar/AppNavbar";
import {useParams} from "react-router-dom";
import {closeIcon, editIcon, xIcon} from "../../Common/Icons.js"
import StatusIndicator from "../TicketCommon/StatusIndicator";
import TextNewLine from "../../Common/TextNewLine";
import ChatMessage from "./ChatMessage";
import {Button, CloseButton, Form} from "react-bootstrap";
import AddButton from "../../Common/AddButton";
import AttachmentOverlay from "./AttachmentOverlay";
import {useEffect, useRef, useState} from "react";
import DeleteButton from "../../Common/DeleteButton";
import SendButton from "../../Common/SendButton";
import PriorityIndicator from "../TicketCommon/PriorityIndicator";
import NavigationButton from "../../Common/NavigationButton";


const imageList = [
    "https://media.istockphoto.com/id/500430432/it/foto/broken-iphone-6.jpg?s=170667a&w=0&k=20&c=Eopt1H8m3N6h_1luxq-u76dKXHcY5t_WA2zMqvGsJ14=",
    "https://media.istockphoto.com/id/500431088/it/foto/broken-iphone-6.jpg?s=170667a&w=0&k=20&c=TwYYyEs-Plul9pe55A792htJJveexY0sdaXAaKpIhpE="

]

function UploadButton(props) {
    const [uploadedFileName, setUploadedFileName] = useState(null);
    const inputRef = useRef(null);
    const addFile = props.addFile
    const enabled = props.enabled
    const handleUpload = () => {
        inputRef.current?.click();
    };

    const handleDisplayFileDetails = () => {
        inputRef.current?.files &&
            addFile(inputRef.current.files[0])
    };
    return (
        <div className="m-3">
            <input
                ref={inputRef}
                onChange={handleDisplayFileDetails}
                className="d-none"
                type="file"
            />
            <NavigationButton disabled={!enabled} text={<div>Upload attachment</div>} onClick={handleUpload}/>
        </div>
    );
}


function DeleteAttachmentButton(props){
    const [color, setColor] = useState("#d98080")
    const onClick = props.onClick
    return <a style={{cursor:"pointer"}} onClick={(e)=>{e.preventDefault(); onClick()}} onMouseOver={()=>setColor("#a63030")} onMouseLeave={()=>setColor("#d98080")}>
        {xIcon(color, "20")}
    </a>
}

function AddAttachment(props) {
    const attachment_old = props.attachment
    const [color, setColor] = useState("#d98080")
    //const [attachment, setAttachment] = useState(attachment_old)
    const attachments = props.attachments
    const setAttachments=props.setAttachments;

    /*useEffect(() => {
        setNewAttachment(attachment)
    }, [attachment])*/

    return <><div>
        {attachments.filter(t => t!==undefined).length > 0 &&
            <div style={{borderRadius:"25px",marginLeft:"25px", marginTop:"10px", paddingTop:"10px", paddingBottom:"10px", paddingLeft:"20px", backgroundColor:"rgba(0,0,0,0.1)", width:"250px"}}>
                {attachments.map((t,index) => t !== undefined?
                    <><div>

                        <div style={{maxWidth:"180px", textOverflow:"ellipsis", whiteSpace:"nowrap", overflow:"hidden", display:"inline-block"}}>{t.name} </div>
                        <div style={{display:"inline-block", float:"right", marginRight:"15px"}}><DeleteAttachmentButton onClick={()=>{const tmp = attachments; tmp.splice(index,1); setAttachments([...tmp])}}/></div> </div> </>: <></>)}

            </div>}
        <div style={{flex:"true", marginLeft:"30px"}}>
            <UploadButton enabled={attachments.length < 5} addFile={(file) => {const tmp = attachments; tmp.push(file); setAttachments([...tmp])}}/>
        </div>
    </div>
    </>
}


function EditButton(props){
    const [color, setColor] = useState("white")
    const onClick = props.onClick
    const disabled = props.disabled
    return <>
        <a style={{pointerEvents:disabled?"none":"", cursor:"pointer"}} onClick={(e)=>{e.preventDefault(); onClick()}} onMouseOver={()=>setColor("#a0c1d9")} onMouseLeave={()=>setColor("white")}>
            {editIcon(disabled?"rgba(0,0,0,0.4)":color, 20)}
        </a>
    </>
}

function CloseEditButton(props){
    const [color, setColor] = useState("#d98080")
    const onClick = props.onClick
    const disabled = props.disabled
    return <>
        <a style={{pointerEvents:disabled?"none":"", cursor:"pointer"}} onClick={(e)=>{e.preventDefault(); onClick()}} onMouseOver={()=>setColor("#a63030")} onMouseLeave={()=>setColor("#d98080")}>
            {closeIcon(disabled?"rgba(0,0,0,0.4)":color, 20)}
        </a>
    </>
}

function StatusEdit(props){
    const onClick = props.onClick();
    const [opacity, setOpacity] = useState("1")
    const type = props.type
    return <div onMouseOver={()=>{setOpacity("0.6")}} onMouseLeave={()=>{setOpacity("1")}} style={{margin:"7px", cursor:"pointer", borderRadius:"25px", opacity:opacity}} >
        {StatusIndicator(type)}
    </div>
}

function StatusEditList(props){
    const type = props.type
    let types=[]
    switch(type){
        case("INPROGRESS"):
            types=["OPEN", "CLOSED", "RESOLVED"]
            break;
        case("OPEN"):
            types=["CLOSED", "RESOLVED"]
            break;
        case("REOPENED"):
            types=["CLOSED", "RESOLVED"]
            break;
        case("CLOSED"):
            types=["REOPENED"]
            break;
        case("RESOLVED"):
            types=["REOPENED", "CLOSED"]
            break;
    }
    return types.map(t => <StatusEdit type={t} onClick={()=>{}}/>)
}

function PriorityEdit(props){
    const onClick = props.onClick();
    const [opacity, setOpacity] = useState("1")
    const type = props.type
    return <div onMouseOver={()=>{setOpacity("0.6")}} onMouseLeave={()=>{setOpacity("1")}} style={{margin:"7px", cursor:"pointer", borderRadius:"25px", opacity:opacity}} >
        {PriorityIndicator(type)}
    </div>
}

function TicketChatPage(props) {
    const loggedIn = props.loggedIn
    const maxAttachments = 5
    const [curAttachments, setCurAttachments] = useState(0)
    const params = useParams()
    const [overlayShown, setOverlayShown] = useState(false)
    const [startPos, setStartPos] = useState(0)
    const [addingMessage, setAddingMessage] = useState(false)
    const [editingStatus, setEditingStatus] = useState(false)
    const [editingPriority, setEditingPriority] = useState(false)
    const [editingExpert, setEditingExpert] = useState(false)
    const [attachments, setAttachments] = useState([])
    const [updateAttachments, setUpdateAttachments] = useState(false)
    const [editExpert, setEditExpert] = useState("")
    //let attachments = []
    console.log(attachments)
    console.log(attachments.filter(t => t !== "").length)
    const ticketID = params.id

    useEffect(()=>{
        if(updateAttachments){
            const tmp = attachments.filter(t => t !== "")
            while(tmp.length !== 5){
                tmp.push("")
            }
            setAttachments([...tmp])
            setUpdateAttachments(false)
        }

    }, [attachments, updateAttachments])

    return <>
        <AppNavbar loggedIn={loggedIn} selected={"tickets"}/>
        {overlayShown &&<AttachmentOverlay startPos={startPos} imageList={imageList} closeModal={() => setOverlayShown(false)}/>}
        <div className="CenteredButton" style={{marginTop:"50px"}}>
            <h1 style={{color:"#EEEEEE", marginTop:"80px"}}>TICKET</h1>
            <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"2px"}}/>
            <h5 style={{color:"#EEEEEE"}}>Can't use touchscreen on my phone</h5>
            <div style={{width: "250px", alignSelf:"center", margin:"auto"}}>
                <div style={{width:"150px", display:"inline-block", paddingLeft:"20px"}}>
                    {StatusIndicator("INPROGRESS")}
                </div>
                <div style={{display:"inline-block", float:"right"}}>
                    {!editingStatus && <EditButton onClick={()=>setEditingStatus(true)}/>}
                    {editingStatus && <CloseEditButton onClick={()=>setEditingStatus(false)}/>}
                </div>
                {editingStatus && <div style={{borderRadius:"15px", backgroundColor:"rgba(0,0,0,0.2)", marginTop:"10px", marginBottom:"10px", width:"130px", display: "inline-block", alignSelf:"center"}}>
                    <StatusEditList type={"INPROGRESS"} onClick={(t)=>{}}/>
                </div>}
                <div style={{marginTop:"7px", width:"150px", display:"inline-block", paddingLeft:"20px"}}>
                    {PriorityIndicator("LOW")}
                </div>
                <div style={{marginTop:"7px",display:"inline-block", float:"right"}}>
                    {!editingPriority && <EditButton onClick={()=>setEditingPriority(true)}/>}
                    {editingPriority && <CloseEditButton onClick={()=>setEditingPriority(false)}/>}
                </div>
                {editingPriority && <div style={{borderRadius:"15px", backgroundColor:"rgba(0,0,0,0.2)", marginTop:"10px", marginBottom:"10px", width:"130px", display: "inline-block", alignSelf:"center"}}>
                    <PriorityEdit type={"LOW"} onClick={()=>{}}/>
                    <PriorityEdit type={"MEDIUM"} onClick={()=>{}}/>
                    <PriorityEdit type={"HIGH"} onClick={()=>{}}/>
                </div>}


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
            <div style={{backgroundColor:"rgba(0,0,0,0.2)", paddingLeft:"25px", paddingRight:"25px", maxWidth:"350px", borderRadius:"25px", alignSelf:"center", margin:"auto"}}>
                <div style={{color:"#EEEEEE", paddingTop:"5px", paddingBottom:"5px",  marginTop:"14px", marginBottom:"15px", fontSize:14}}>PRODUCT: Apple - iPhone 13 Pro 128GB</div>
            </div>
            <div style={{backgroundColor:"rgba(0,0,0,0.1)", paddingLeft:"25px", paddingRight:"25px", paddingBottom:"5px", maxWidth:"300px", borderRadius:"25px", alignSelf:"center", margin:"auto", marginBottom:"10px"}}>
                <div style={{ color:"#EEEEEE",display:"inline-block", paddingTop:"5px", marginTop:"4px", fontSize:14}}>Expert: Mario Rossi</div>
                <div style={{display:"inline-block", marginLeft:"14px", marginRight:"14px", marginBottom:"10px"}}>

                        {!editingExpert && <EditButton onClick={()=>setEditingExpert(true)}/>}
                        {editingExpert && <CloseEditButton onClick={()=>setEditingExpert(false)}/>}

                </div>

                {editingExpert && <div style={{marginBottom:"10px"}}><Form.Control className={"form-control:focus"} placeholder={"Expert E-mail"} style={{fontSize:12}}/></div>}
            </div>
            <NavigationButton text={"Update ticket"} onClick={()=>{}}/>
            <hr style={{color:"white", width:"75%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"2px", marginTop:"20px"}}/>
            <h1 style={{color:"#EEEEEE", marginTop:"30px"}}>CHAT</h1>
            <hr style={{color:"white", width:"25%", alignSelf:"center", marginLeft:"auto", marginRight:"auto", marginBottom:"22px", marginTop:"2px"}}/>



            <div style={{backgroundColor:"rgba(255,255,255,0.1)", verticalAlign:"middle", borderRadius:"20px", padding:"15px", width:"95%", alignSelf:"left", textAlign:"left", margin:"auto", fontSize:"14px", color:"#EEEEEE", marginTop:"5px" }}>

                <ChatMessage imageList={[]} setStartPos={setStartPos} setOverlayShown={setOverlayShown} isExpert={true} timestamp={"05/03/2023 - 10:12"} name={"Mario Rossi"} text={`Could you provide additional information on xyz?`}/>
                <ChatMessage imageList={imageList} setStartPos={setStartPos} setOverlayShown={setOverlayShown} isExpert={false} timestamp={"05/03/2023 - 10:13"} name={"Luigi Bianchi"}  text={`Here there are some info\n test test`}/>
                <ChatMessage imageList={[]} setStartPos={setStartPos} setOverlayShown={setOverlayShown} isExpert={false} timestamp={"05/03/2023 - 10:23"} name={"Luigi Bianchi"}  text={`Could you provide additional information on xyz?`}/>


                {addingMessage && <><Form.Control style={{borderColor:"rgba(0,0,0,0.6)", paddingLeft:"32px", paddingTop:"15px", backgroundColor:"rgba(0,0,0,0.4)", color:"white", resize:"none", height:"200px", boxShadow:"0px 4px 8px -4px rgba(0,0,0,0.8)", borderRadius:"20px", marginTop:"5px"}} placeholder="Write your message here..." type="textarea" as="textarea"/>

                        <AddAttachment attachments={attachments} setAttachments={setAttachments}/>

                </>}

                <div style={{width:"100%", height:"60px"}}>
                    {!addingMessage ?
                        <>
                            <AddButton style={{marginTop:"10px", marginRight:"10px", float:"right"}} onClick={() => setAddingMessage(true)}/>
                        </> :
                        <>
                            <SendButton style={{marginTop:"10px", marginRight:"10px", float:"right"}} onClick={() => setAddingMessage(false)}/>
                            <DeleteButton style={{marginTop:"10px", marginRight:"10px", float:"right"}} onClick={() => setAddingMessage(false)}/>
                        </>}
                </div>
            </div>

        </div>
    </>
}

export default TicketChatPage;