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
    const closeModal = props.closeModal;
    return <Modal  onBackdropClick={(e)=>{e.preventDefault(); console.log("here");}}  show={true} dialogClassName={"imageModal "} style={{position:"fixed"}} onHide={()=>closeModal()}>
    <Modal.Header style={{width:"100%", borderColor:"transparent", borderWidth:"0", alignItems:"right", alignContent:"right"}}>
        <div style={{float:"right", marginLeft:"auto"}}>
            <CloseOverlay onClick={()=>{closeModal()}}/>
        </div>

    </Modal.Header>
        <Modal.Body style={{marginBottom:"40px", backgroundColor:"transparent", alignItems:"center", alignContent:"center", verticalAlign:"middle"}}>

        <LeftNavigate onClick={() => {}}/>
        <img style={{maxWidth:"75%", alignSelf:"center", boxShadow:"0px 4px 8px -4px rgba(0,0,0,0.8)", borderRadius:"10px", margin:"auto", height:"80%"}} src={"https://media.istockphoto.com/id/500430432/it/foto/broken-iphone-6.jpg?s=170667a&w=0&k=20&c=Eopt1H8m3N6h_1luxq-u76dKXHcY5t_WA2zMqvGsJ14="} />

        <RightNavigate onClick={() => {}}/>

    </Modal.Body>
    </Modal>
}

export default AttachmentOverlay