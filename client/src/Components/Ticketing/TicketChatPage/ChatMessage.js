import TextNewLine from "../../Common/TextNewLine";
import {Box} from "@mui/material";
import {Row} from "react-bootstrap";
import {attachmentIcon} from "../../Common/Icons";
import {useEffect, useState} from "react";
import {getProfileDetails} from "../../../API/Profiles";

export function isImage(data){
    let knownTypes = {
        '/': 'data:image/jpg;base64,',
        'i': 'data:image/png;base64,',
        /*ETC*/
    }

    let image = new Image()

    if(!knownTypes[data[0]]){
        console.log("encoded image didn't match known types");
        return false;
    }else{
        image.src = knownTypes[0]+data
        image.onload = function(){
            //This should load the image so that you can actually check
            //height and width.
            if(image.height === 0 || image.width === 0){
                console.log('encoded image missing width or height');
                return false;
            }
        }
        return true;
    }
}

function ChatMessage(props){
    const [isExpert, setIsExpert] = useState(true)
    const setAlbum = props.setAlbum
    const setStartPos = props.setStartPos;
    const timestamp = new Date(props.timestamp).toLocaleString();
    const [name, setName] = useState("")
    const text = props.text
    const imageList = props.imageList
    const bgColor = isExpert ? "rgba(0,0,0,0.1)" : "rgba(255,255,255,0.1)"
    const align = isExpert ? "left" : "right"
    const role = isExpert ? "Expert" : "Client"

    useEffect(() => {
        getProfileDetails(props.sender).then(response =>{
            setName(response.data.name + " " + response.data.surname)
            if(response.data.role === "EXPERT"){
                setIsExpert(true)
            }
            else{
                setIsExpert(false)
            }

        })
    }, [])

    return <div style={{marginBottom:"5px", boxShadow:"0px 4px 8px -4px rgba(0,0,0,0.8)", display:"inline-block", backgroundColor: bgColor, borderRadius:"20px", padding:"15px", width:"100%", alignSelf:"left", textAlign:"left", marginLeft:"auto", fontSize:"14px", color:"#EEEEEE", marginTop:"5px" }}>
        <div style={{display:"inline-block", float:align, backgroundColor:"rgba(0,0,0,0.1)", borderRadius:"20px", padding:"15px", width:"155px", alignSelf:"right", textAlign:"left", marginLeft:"auto", marginRight:"auto", fontSize:"14px", color:"#EEEEEE", marginTop:"5px" }}>
            <div style={{fontSize:"15px", textAlign:"center"}}>{name}</div>
            <hr style={{marginTop:1, marginBottom:1}}/>
            <div style={{fontSize:"13px", fontWeight:"bold", textAlign:"center"}}>{role}</div>
            <div style={{fontSize:"11px", textAlign:"center", marginTop:"2px"}}>{timestamp}</div>
        </div>

        <div style={{display:"inline-block", marginLeft:"20px", marginTop:"15px"}}>
            {TextNewLine(text)}
            <Row style={{flex:"true"}}>
                {imageList.map((img, index) =>
                    isImage(img) ?
                    <img onClick={() => {setAlbum(imageList); setStartPos(index); props.setOverlayShown(true)}} style={{cursor:"pointer", boxShadow:"0px 4px 8px -4px rgba(0,0,0,0.8)", borderRadius:"20px", marginLeft:"15px", marginRight:"15px", marginTop:"15px", padding:0, height:"75px", width:"75px", objectFit:"cover"}}
                         src={"data:image/png;base64, " + img} /> :
                        <></>)}
                        {/*<div
                            style={{cursor:"pointer", boxShadow:"0px 4px 8px -4px rgba(0,0,0,0.8)", backgroundColor:"rgba(255,255,255,0.2)", textAlign:"center", borderRadius:"20px", marginLeft:"15px", marginRight:"15px", marginTop:"15px", height:"75px", width:"75px"}}>
                            <div style={{marginTop:"10px"}}>{attachmentIcon("white", 25)}</div>
                            <div style={{fontSize:10, height:"35px", width:"50px", overflow:"clip", overflowWrap:"break-word", overflowY:"hidden"}}>{img.name.substring(24, img.name.length)}</div>
                        </div>*/}
            </Row>
        </div>
    </div>
}

export default ChatMessage;