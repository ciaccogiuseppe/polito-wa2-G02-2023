import TextNewLine from "../../Common/TextNewLine";

function ChatMessage(props){
    const isExpert = props.isExpert;
    const setStartPos = props.setStartPos;
    const timestamp = props.timestamp;
    const name = props.name
    const text = props.text
    const imageList = props.imageList
    const bgColor = isExpert ? "rgba(0,0,0,0.1)" : "rgba(255,255,255,0.1)"
    const align = isExpert ? "left" : "right"
    const role = isExpert ? "Expert" : "Client"

    return <div style={{marginBottom:"5px", boxShadow:"0px 4px 8px -4px rgba(0,0,0,0.8)", display:"inline-block", backgroundColor: bgColor, borderRadius:"20px", padding:"15px", width:"100%", alignSelf:"left", textAlign:"left", marginLeft:"auto", fontSize:"14px", color:"#EEEEEE", marginTop:"5px" }}>
        <div style={{display:"inline-block", float:align, backgroundColor:"rgba(0,0,0,0.1)", borderRadius:"20px", padding:"15px", width:"155px", alignSelf:"right", textAlign:"left", marginLeft:"auto", marginRight:"auto", fontSize:"14px", color:"#EEEEEE", marginTop:"5px" }}>
            <div style={{fontSize:"15px", textAlign:"center"}}>{name}</div>
            <hr style={{marginTop:1, marginBottom:1}}/>
            <div style={{fontSize:"13px", fontWeight:"bold", textAlign:"center"}}>{role}</div>
            <div style={{fontSize:"11px", textAlign:"center", marginTop:"2px"}}>{timestamp}</div>
        </div>

        <div style={{display:"inline-block", marginLeft:"20px", marginTop:"15px"}}>
            {TextNewLine(text)}
            <div style={{flex:"true"}}>
                {imageList.map((img, index) =>
                    <img onClick={() => {setStartPos(index); props.setOverlayShown(true)}} style={{cursor:"pointer", boxShadow:"0px 4px 8px -4px rgba(0,0,0,0.8)", borderRadius:"20px", marginLeft:"15px", marginRight:"15px", marginTop:"15px", height:"75px", width:"75px", objectFit:"cover"}} src={img} />
                )}
            </div>
        </div>
    </div>
}

export default ChatMessage;