import {Modal} from "react-bootstrap";
import "./Modals.css"
import {closeIcon, leftArrow, rightArrow} from "../../Common/Icons";
import {useState} from "react";



function RightNavigate(props){
    const onClick = props.onClick;
    const [color, setColor] = useState("white")
    return <a style={{cursor:"pointer"}}
              onClick={(e) => {e.preventDefault(); onClick()}}
              onMouseOver={() => setColor("#455877")}
              onMouseLeave={() => setColor("#FFFFFF")}
    >{rightArrow(color, 40)}</a>
}

function LeftNavigate(props){
    const onClick = props.onClick;
    const [color, setColor] = useState("white")
    return <a style={{cursor:"pointer"}}
              onClick={(e) => {e.preventDefault(); onClick()}}
              onMouseOver={() => setColor("#455877")}
              onMouseLeave={() => setColor("#FFFFFF")}
    >{leftArrow(color, 40)}</a>
}

function CloseOverlay(props){
    const onClick = props.onClick;
    const [color, setColor] = useState("white")
    return <a style={{cursor:"pointer"}}
              onClick={(e) => {e.preventDefault(); onClick()}}
              onMouseOver={() => setColor("#9d3a48")}
              onMouseLeave={() => setColor("#FFFFFF")}
    >{closeIcon(color, 30)}</a>
}

function AttachmentOverlay(props){
    const imageList = props.imageList;
    const closeModal = props.closeModal;
    const startPos = props.startPos;
    const [curPos,setCurPos] = useState(startPos);
    return <Modal  onBackdropClick={(e)=>{e.preventDefault(); console.log("here");}}  show={true} dialogClassName={"imageModal "} style={{position:"fixed"}} onHide={()=>closeModal()}>
    <Modal.Header style={{width:"100%", borderColor:"transparent", borderWidth:"0", alignItems:"right", alignContent:"right"}}>
        <div style={{float:"right", marginLeft:"auto"}}>
            <CloseOverlay onClick={()=>{closeModal()}}/>
        </div>

    </Modal.Header>
        <Modal.Body style={{marginBottom:"40px", backgroundColor:"transparent", alignItems:"center", alignContent:"center", verticalAlign:"middle"}}>
        <LeftNavigate onClick={() => {setCurPos((curPos-1) < 0 ? imageList.length - 1 : curPos - 1)}}/>
        <img style={{maxWidth:"75%", alignSelf:"center", boxShadow:"0px 4px 8px -4px rgba(0,0,0,0.8)", borderRadius:"10px", margin:"auto", height:"80%"}} src={"data:image/png;base64, " + imageList[curPos]} />

        <RightNavigate onClick={() => {setCurPos((curPos+1)%imageList.length)}}/>

    </Modal.Body>
    </Modal>
}

export default AttachmentOverlay