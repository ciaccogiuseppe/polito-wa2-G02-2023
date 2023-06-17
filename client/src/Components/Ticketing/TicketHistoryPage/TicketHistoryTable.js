import "./TicketHistoryPage.css"
import {useState} from "react";
import {useNavigate} from "react-router-dom";
import StatusIndicator from "../TicketCommon/StatusIndicator";


function TicketHistoryTR(props){
    const [BGcolor, setBGcolor] = useState("");
    const navigate = useNavigate()
    const ticketId = props.id
    const user = props.user
    const expert = props.expert
    const oldStatus = StatusIndicator(props.oldStatus)
    const newStatus = StatusIndicator(props.newStatus)
    const updated = props.updated
    return <tr className="text-light" style={{cursor:"pointer", backgroundColor:BGcolor}} onMouseOver={() => setBGcolor("rgba(0, 0, 0, 0.1)")} onMouseLeave={()=>setBGcolor("")} onClick={()=>navigate(`/tickets/${ticketId}`)}>
        <td className="text-light">{ticketId}</td>
        <td style={{verticalAlign:"middle"}}>{user}</td>
        <td style={{verticalAlign:"middle"}}>{expert}</td>
        <td style={{verticalAlign:"middle"}}>{oldStatus}</td>
        <td style={{verticalAlign:"middle"}}>{newStatus}</td>
        <td className="text-light" style={{fontSize:12, verticalAlign:"middle"}}>{updated}</td></tr>
}

function TicketHistoryTable(props){
    const ticketList = props.ticketList

    return <>
        {ticketList.length >= 0  &&
        <div style={{alignItems:"center", alignSelf:"center"}}>
            <table className="table  roundedTable"  style={{alignContent: "center", width: "90%", margin: "auto", marginTop:"20px"}}>
                <thead>
                <tr className="text-light">
                    <th width={"15%"}><h5>TICKET ID</h5></th>
                    <th width={"15%"}><h5>USER</h5></th>
                    <th width={"15%"}><h5>EXPERT</h5></th>
                    <th width={"15%"}><h5>OLD STATUS</h5></th>
                    <th width={"15%"}><h5>NEW STATUS</h5></th>
                    <th width={"15%"}><h5>UPDATED</h5></th>
                </tr>
                </thead>
                <tbody>
                {/*<tr className="text-light" style={{cursor:"pointer", backgroundColor:BGcolor}} onMouseOver={() => setBGcolor("rgba(0, 0, 0, 0.1)")} onMouseLeave={()=>setBGcolor("")}><td className="text-light">Can't use touchscreen on my phone</td><td style={{verticalAlign:"middle"}}><div  style={{borderRadius:"25px", color:"white", backgroundColor:"#dc8429", fontSize:10, textAlign:"center", verticalAlign:"middle", padding:5}}>IN PROGRESS</div></td><td className="text-light" style={{fontSize:12, verticalAlign:"middle"}}>05/02/2022</td></tr>*/}
                <TicketHistoryTR id={2} user="giuliano.rossi@polito.it" expert="morisio@polito.it" oldStatus="REOPENED" newStatus="RESOLVED"updated="05/03/2022" />
                <TicketHistoryTR id={3} user="giuseppe.rossi@polito.it" expert="cabodi@polito.it" oldStatus="IN_PROGRESS" newStatus="RESOLVED"updated="05/03/2022" />
                <TicketHistoryTR id={4} user="flavio.rossi@polito.it" expert="malnati@polito.it" oldStatus="CLOSED" newStatus="REOPENED"updated="05/03/2022" />
                <TicketHistoryTR id={5} user="giacomo.rossi@polito.it" expert="camurati@polito.it" oldStatus="OPEN" newStatus="CLOSED"updated="05/03/2022" />

                </tbody>
            </table>
        </div>}
    </>
}


export default TicketHistoryTable;