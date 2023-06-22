import "./TicketListPage.css";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import StatusIndicator from "../TicketCommon/StatusIndicator";
import PriorityIndicator from "../TicketCommon/PriorityIndicator";

export function priorityMap(priority) {
  switch (priority) {
    case 1:
      return "LOW";
    case 2:
      return "MEDIUM";
    case 3:
      return "HIGH";
    default:
      return "UNASSIGNED";
  }
}

function TicketTableTR(props) {
  const [BGcolor, setBGcolor] = useState("");
  const navigate = useNavigate();
  const ticketId = props.id;
  const title = props.title;
  const createdTime = props.createdTime;
  const product = props.product;
  const status = StatusIndicator(props.status);
  const priority = PriorityIndicator(priorityMap(props.priority));
  return (
    <tr
      className="text-light"
      style={{ cursor: "pointer", backgroundColor: BGcolor }}
      onMouseOver={() => setBGcolor("rgba(0, 0, 0, 0.1)")}
      onMouseLeave={() => setBGcolor("")}
      onClick={() => navigate(`/tickets/${ticketId}`)}
    >
      <td className="text-light">{title}</td>
      <td className="text-light">{product}</td>
      <td style={{ verticalAlign: "middle" }}>{status}</td>
      <td style={{ verticalAlign: "middle" }}>{priority}</td>
      <td
        className="text-light"
        style={{ fontSize: 12, verticalAlign: "middle" }}
      >
        {createdTime}
      </td>
    </tr>
  );
}

function TicketListTable(props) {
  const ticketList = props.ticketList;

  return (
    <>
      {ticketList && ticketList.length > 0 && (
        <div style={{ alignItems: "center", alignSelf: "center" }}>
          <table
            className="table  roundedTable"
            style={{
              alignContent: "center",
              width: "70%",
              margin: "auto",
              marginTop: "20px",
            }}
          >
            <thead>
              <tr className="text-light">
                <th>
                  <h5>TITLE</h5>
                </th>
                <th width={"15%"}>
                  <h5>PRODUCT</h5>
                </th>
                <th width={"15%"}>
                  <h5>STATUS</h5>
                </th>
                <th width={"10px"}>
                  <h5>PRIORITY</h5>
                </th>
                <th width={"15%"}>
                  <h5>CREATED</h5>
                </th>
              </tr>
            </thead>
            <tbody>
              {ticketList.map((t) => (
                <TicketTableTR
                  title={t.title}
                  product={t.product}
                  priority={t.priority}
                  status={t.status}
                  id={t.ticketId}
                  createdTime={
                    new Date(t.createdTimestamp)
                      .getDate()
                      .toString()
                      .padStart(2, "0") +
                    "/" +
                    (new Date(t.createdTimestamp).getMonth() + 1)
                      .toString()
                      .padStart(2, "0") +
                    "/" +
                    new Date(t.createdTimestamp).getFullYear()
                  }
                />
              ))}
              {/*<tr className="text-light" style={{cursor:"pointer", backgroundColor:BGcolor}} onMouseOver={() => setBGcolor("rgba(0, 0, 0, 0.1)")} onMouseLeave={()=>setBGcolor("")}><td className="text-light">Can't use touchscreen on my phone</td><td style={{verticalAlign:"middle"}}><div  style={{borderRadius:"25px", color:"white", backgroundColor:"#dc8429", fontSize:10, textAlign:"center", verticalAlign:"middle", padding:5}}>IN PROGRESS</div></td><td className="text-light" style={{fontSize:12, verticalAlign:"middle"}}>05/02/2022</td></tr>*/}
              {/*<TicketTableTR id={1} title = "Can't use touchscreen on my phone" createdTime="05/03/2022" status="IN_PROGRESS"/>
                <TicketTableTR id={2} title = "Tablet camera not working" createdTime="05/02/2022" status="RESOLVED"/>
                <TicketTableTR id={3} title = "Smartphone camera not working" createdTime="15/02/2022" status="OPEN"/>
                <TicketTableTR id={4} title = "Broke phone screen" createdTime="05/02/2022" status="CLOSED"/>
                <TicketTableTR id={5} title = "Tablet screen not working" createdTime="15/02/2022" status="REOPENED"/>*/}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}

export default TicketListTable;
