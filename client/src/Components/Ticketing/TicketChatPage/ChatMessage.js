import TextNewLine from "../../Common/TextNewLine";

function ChatMessage(props){
    const isExpert = props.isExpert;
    const timestamp = props.timestamp;
    const name = props.name
    const text = props.text
    const bgColor = isExpert ? "rgba(0,0,0,0.1)" : "rgba(255,255,255,0.1)"
    const align = isExpert ? "left" : "right"
    const role = isExpert ? "Expert" : "Client"
    console.log(bgColor)
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
                <img style={{boxShadow:"0px 4px 8px -4px rgba(0,0,0,0.8)", borderRadius:"20px", marginLeft:"15px", marginRight:"15px", marginTop:"15px", height:"75px", width:"75px", objectFit:"cover"}} src={"https://media.istockphoto.com/id/500430432/it/foto/broken-iphone-6.jpg?s=170667a&w=0&k=20&c=Eopt1H8m3N6h_1luxq-u76dKXHcY5t_WA2zMqvGsJ14="} />
                <img style={{boxShadow:"0px 4px 8px -4px rgba(0,0,0,0.8)", borderRadius:"20px", marginLeft:"15px", marginRight:"15px", marginTop:"15px", height:"75px", width:"75px", objectFit:"cover"}} src={"https://media.istockphoto.com/id/500431088/it/foto/broken-iphone-6.jpg?s=170667a&w=0&k=20&c=TwYYyEs-Plul9pe55A792htJJveexY0sdaXAaKpIhpE="} />

            </div>
        </div>
    </div>
}

export default ChatMessage;