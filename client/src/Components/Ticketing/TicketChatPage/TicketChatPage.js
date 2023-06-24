import AppNavbar from "../../AppNavbar/AppNavbar";
import { useNavigate, useParams } from "react-router-dom";
import {
  closeIcon,
  downArrowIcon,
  editIcon,
  xIcon,
} from "../../Common/Icons.js";
import StatusIndicator from "../TicketCommon/StatusIndicator";
import TextNewLine from "../../Common/TextNewLine";
import ChatMessage from "./ChatMessage";
import { Form, Spinner } from "react-bootstrap";
import AddButton from "../../Common/AddButton";
import AttachmentOverlay from "./AttachmentOverlay";
import React, { useEffect, useRef, useState } from "react";
import DeleteButton from "../../Common/DeleteButton";
import SendButton from "../../Common/SendButton";
import PriorityIndicator from "../TicketCommon/PriorityIndicator";
import NavigationButton from "../../Common/NavigationButton";
import {
  assignTicketManagerAPI,
  getTicketClientAPI,
  getTicketExpertAPI,
  getTicketManagerAPI,
  updateTicketClientAPI,
  updateTicketExpertAPI,
  updateTicketManagerAPI,
} from "../../../API/Tickets";
import ErrorMessage from "../../Common/ErrorMessage";
import { priorityMap } from "../TicketListPage/TicketListTable";
import { getProductByIdAPI } from "../../../API/Products";
import { getExpertsByCategory, getProfileDetails } from "../../../API/Profiles";
import InfoMessage from "../../Common/InfoMessage";
import {
  addMessageAPI,
  getChatClient,
  getChatExpert,
  getChatManager,
} from "../../../API/Chat";

function UploadButton(props) {
  const inputRef = useRef(null);
  const addFile = props.addFile;
  const enabled = props.enabled;
  const handleUpload = () => {
    inputRef.current?.click();
  };

  const convertBase64 = (file) => {
    return new Promise((resolve, reject) => {
      const fileReader = new FileReader();
      fileReader.readAsDataURL(file);
      fileReader.onload = () => {
        resolve(fileReader.result);
      };
      fileReader.onerror = (error) => {
        reject(error);
      };
    });
  };

  const handleDisplayFileDetails = (e) => {
    //addFile({name:e.target.files[0].name, attachment: e.target.files[0]})
    //convertBase64(e.target.files[0]).then(a=>console.log(a))
    inputRef.current?.files &&
      convertBase64(e.target.files[0]).then((a) =>
        addFile({
          name: e.target.files[0].name,
          attachment: a.split(",")[1],
          timestamp: new Date().toISOString(),
        })
      );
  };
  return (
    <div className="m-3">
      <input
        ref={inputRef}
        onChange={handleDisplayFileDetails}
        className="d-none"
        type="file"
      />
      <NavigationButton
        disabled={!enabled}
        text={<div>Upload attachment</div>}
        onClick={handleUpload}
      />
    </div>
  );
}

function DeleteAttachmentButton(props) {
  const [color, setColor] = useState("#d98080");
  const onClick = props.onClick;
  return (
    <a
      href={"/"}
      style={{ cursor: "pointer" }}
      onClick={(e) => {
        e.preventDefault();
        onClick();
      }}
      onMouseOver={() => setColor("#a63030")}
      onMouseLeave={() => setColor("#d98080")}
    >
      {xIcon(color, "20")}
    </a>
  );
}

function AddAttachment(props) {
  //const [attachment, setAttachment] = useState(attachment_old)
  const attachments = props.attachments;
  const setAttachments = props.setAttachments;

  /*useEffect(() => {
        setNewAttachment(attachment)
    }, [attachment])*/

  return (
    <>
      <div>
        {attachments.filter((t) => t !== undefined).length > 0 && (
          <div
            style={{
              borderRadius: "25px",
              marginLeft: "25px",
              marginTop: "10px",
              paddingTop: "10px",
              paddingBottom: "10px",
              paddingLeft: "20px",
              backgroundColor: "rgba(0,0,0,0.1)",
              width: "250px",
            }}
          >
            {attachments.map((t, index) =>
              t !== undefined ? (
                <>
                  <div>
                    <div
                      style={{
                        maxWidth: "180px",
                        textOverflow: "ellipsis",
                        whiteSpace: "nowrap",
                        overflow: "hidden",
                        display: "inline-block",
                      }}
                    >
                      {t.name}{" "}
                    </div>
                    <div
                      style={{
                        display: "inline-block",
                        float: "right",
                        marginRight: "15px",
                      }}
                    >
                      <DeleteAttachmentButton
                        onClick={() => {
                          const tmp = attachments;
                          tmp.splice(index, 1);
                          setAttachments([...tmp]);
                        }}
                      />
                    </div>{" "}
                  </div>{" "}
                </>
              ) : (
                <></>
              )
            )}
          </div>
        )}
        <div style={{ flex: "true", marginLeft: "30px" }}>
          <UploadButton
            enabled={attachments.length < 5}
            addFile={(file) => {
              const tmp = attachments;
              tmp.push(file);
              setAttachments([...tmp]);
            }}
          />
        </div>
      </div>
    </>
  );
}

function EditButton(props) {
  const [color, setColor] = useState("white");
  const onClick = props.onClick;
  const disabled = props.disabled;
  return (
    <>
      <a
        href={"/"}
        style={{ pointerEvents: disabled ? "none" : "", cursor: "pointer" }}
        onClick={(e) => {
          e.preventDefault();
          onClick();
        }}
        onMouseOver={() => setColor("#a0c1d9")}
        onMouseLeave={() => setColor("white")}
      >
        {editIcon(disabled ? "rgba(0,0,0,0.4)" : color, 20)}
      </a>
    </>
  );
}

function CloseEditButton(props) {
  const [color, setColor] = useState("#d98080");
  const onClick = props.onClick;
  const disabled = props.disabled;
  return (
    <>
      <a
        href={"/"}
        style={{ pointerEvents: disabled ? "none" : "", cursor: "pointer" }}
        onClick={(e) => {
          e.preventDefault();
          onClick();
        }}
        onMouseOver={() => setColor("#a63030")}
        onMouseLeave={() => setColor("#d98080")}
      >
        {closeIcon(disabled ? "rgba(0,0,0,0.4)" : color, 20)}
      </a>
    </>
  );
}

function StatusEdit(props) {
  const [opacity, setOpacity] = useState("1");
  const type = props.type;
  return (
    <div
      onMouseOver={() => {
        setOpacity("0.6");
      }}
      onClick={() => props.onClick()}
      onMouseLeave={() => {
        setOpacity("1");
      }}
      style={{
        margin: "7px",
        cursor: "pointer",
        borderRadius: "25px",
        opacity: opacity,
      }}
    >
      {StatusIndicator(type)}
    </div>
  );
}

function StatusEditList(props) {
  const type = props.type;
  const role = props.role;
  let types = [];
  switch (type) {
    case "IN_PROGRESS":
      if (role === "MANAGER") types = ["OPEN", "CLOSED", "RESOLVED"];
      else if (role === "CLIENT") types = ["RESOLVED"];
      else if (role === "EXPERT") types = ["CLOSED"];
      break;
    case "OPEN":
      if (role === "MANAGER") types = ["CLOSED", "RESOLVED"];
      else if (role === "CLIENT") types = ["RESOLVED"];
      else if (role === "EXPERT") types = ["CLOSED"];
      break;
    case "REOPENED":
      if (role === "MANAGER") types = ["CLOSED", "RESOLVED"];
      else if (role === "CLIENT") types = ["RESOLVED"];
      else if (role === "EXPERT") types = ["CLOSED"];
      break;
    case "CLOSED":
      if (role === "CLIENT" || role === "MANAGER") types = ["REOPENED"];
      break;
    case "RESOLVED":
      if (role === "EXPERT" || role === "MANAGER")
        types = ["REOPENED", "CLOSED"];
      else if (role === "CLIENT") types = ["REOPENED"];
      break;
    default:
      break;
  }
  return types.map((t) => (
    <StatusEdit
      type={t}
      onClick={() => {
        props.onClick(t);
      }}
    />
  ));
}

function PriorityEdit(props) {
  const [opacity, setOpacity] = useState("1");
  const type = props.type;
  return (
    <div
      onMouseOver={() => {
        setOpacity("0.6");
      }}
      onClick={() => props.onClick()}
      onMouseLeave={() => {
        setOpacity("1");
      }}
      style={{
        margin: "7px",
        cursor: "pointer",
        borderRadius: "25px",
        opacity: opacity,
      }}
    >
      {PriorityIndicator(type)}
    </div>
  );
}

function TicketChatPage(props) {
  const loggedIn = props.loggedIn;
  const user = props.user;
  const [album, setAlbum] = useState([]);
  const params = useParams();
  const [overlayShown, setOverlayShown] = useState(false);
  const [startPos, setStartPos] = useState(0);
  const [addingMessage, setAddingMessage] = useState(false);
  const [editingStatus, setEditingStatus] = useState(false);
  const [editingPriority, setEditingPriority] = useState(false);
  const [editingExpert, setEditingExpert] = useState(false);
  const [attachments, setAttachments] = useState([]);
  const [updateAttachments, setUpdateAttachments] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [expertErrorMessage, setExpertErrorMessage] = useState("");
  const [ticket, setTicket] = useState("");
  const [product, setProduct] = useState("");
  const [expert, setExpert] = useState("");
  const [category, setCategory] = useState("");
  const [newExperts, setNewExperts] = useState("");
  const [chat, setChat] = useState([]);
  const [chatErrorMessage, setChatErrorMessage] = useState("");
  const [newMessage, setNewMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [loadingChat, setLoadingChat] = useState(false);

  const [newPriority, setNewPriority] = useState("");
  const [newStatus, setNewStatus] = useState("");
  const [newExpert, setNewExpert] = useState("");

  const navigate = useNavigate();

  const ticketID = params.id;

  useEffect(
    () => {
      window.scrollTo(0, 0);
      if (user.role === "CLIENT") {
        getTicketClientAPI(ticketID)
          .then((response) => {
            setTicket(response);
            getProductByIdAPI(response.productId).then((p) => {
              setCategory(p.category);
              setProduct(p.brand + " - " + p.name);
            });
            getProfileDetails(response.expertEmail).then((e) => {
              setExpert(e.data);
            });
          })
          .catch((err) => setErrorMessage(err));

        setLoadingChat(true);
        getChatClient(ticketID)
          .then((response) => {
            setChat(response.sort((a, b) => a.messageId > b.messageId));
            setLoadingChat(false);
          })
          .catch((err) => {
            setChatErrorMessage(err);
            setLoadingChat(false);
          });
      } else if (user.role === "EXPERT") {
        getTicketExpertAPI(ticketID)
          .then((response) => {
            setTicket(response);
            getProductByIdAPI(response.productId).then((p) => {
              setCategory(p.category);
              setProduct(p.brand + " - " + p.name);
            });
            getProfileDetails(response.expertEmail).then((e) => {
              setExpert(e.data);
            });
          })
          .catch((err) => setErrorMessage(err));
        getChatExpert(ticketID)
          .then((response) =>
            setChat(response.sort((a, b) => a.messageId > b.messageId))
          )
          .catch((err) => setChatErrorMessage(err));
      } else if (user.role === "MANAGER") {
        getTicketManagerAPI(ticketID)
          .then((response) => {
            setTicket(response);
            getProductByIdAPI(response.productId).then((p) => {
              setCategory(p.category);
              setProduct(p.brand + " - " + p.name);
            });
            if (response.expertEmail)
              getProfileDetails(response.expertEmail).then((e) => {
                setExpert(e.data);
              });
          })
          .catch((err) => setErrorMessage(err));
        getChatManager(ticketID)
          .then((response) =>
            setChat(response.sort((a, b) => a.messageId > b.messageId))
          )
          .catch((err) => setChatErrorMessage(err));
      }
    },
    // eslint-disable-next-line
    []
  );

  useEffect(() => {
    if (updateAttachments) {
      const tmp = attachments.filter((t) => t !== "");
      while (tmp.length !== 5) {
        tmp.push("");
      }
      setAttachments([...tmp]);
      setUpdateAttachments(false);
    }
  }, [attachments, updateAttachments]);

  useEffect(
    () => {
      if (category && user.role === "MANAGER") {
        getExpertsByCategory(category).then((a) => setNewExperts(a.data));
      }
    },
    // eslint-disable-next-line
    [category]
  );

  function addMessage() {
    setLoading(true);
    addMessageAPI(
      {
        ticketId: parseInt(ticketID),
        senderEmail: user.email,
        text: newMessage,
        attachments: attachments.map((a) => {
          return { attachment: a.attachment, name: a.timestamp + a.name };
        }),
      },
      ticketID
    )
      .then(() => {
        setNewMessage("");
        setAttachments([]);
        setChatErrorMessage("");
        setAddingMessage(false);
        window.location.reload();
        setLoading(false);
      })
      .catch((err) => {
        setChatErrorMessage(err);
        setLoading(false);
      });
  }

  function ticketUpdate() {
    if (user.role === "CLIENT") {
      updateTicketClientAPI({
        newState: newStatus,
        ticketId: ticketID,
      }).then((response) => window.location.reload());
    } else if (user.role === "EXPERT") {
      updateTicketExpertAPI({
        newState: newStatus,
        ticketId: ticketID,
      }).then((response) => window.location.reload());
    } else if (user.role === "MANAGER") {
      updateTicketManagerAPI({
        newState: newStatus,
        ticketId: ticketID,
      }).then((response) => window.location.reload());
    }
  }

  function ticketAssign() {
    let priority = 0;
    switch (newPriority) {
      case "LOW":
        priority = 1;
        break;
      case "MEDIUM":
        priority = 2;
        break;
      case "HIGH":
        priority = 3;
        break;
      default:
        priority = 0;
    }
    if (user.role === "MANAGER") {
      assignTicketManagerAPI({
        expertEmail: newExpert,
        priority: priority,
        ticketId: ticketID,
      })
        .then((response) => window.location.reload())
        .catch((err) => setExpertErrorMessage(err));
    }
  }

  return (
    <>
      <AppNavbar
        user={props.user}
        logout={props.logout}
        loggedIn={loggedIn}
        selected={"tickets"}
      />
      {overlayShown && (
        <AttachmentOverlay
          startPos={startPos}
          imageList={album}
          closeModal={() => setOverlayShown(false)}
        />
      )}
      <div className="CenteredButton" style={{ marginTop: "50px" }}>
        <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>TICKET</h1>
        <hr
          style={{
            color: "white",
            width: "25%",
            alignSelf: "center",
            marginLeft: "auto",
            marginRight: "auto",
            marginBottom: "2px",
            marginTop: "2px",
          }}
        />
        {errorMessage && (
          <>
            <div style={{ margin: "10px" }}>
              <ErrorMessage
                text={errorMessage}
                close={() => {
                  setErrorMessage("");
                }}
              />{" "}
            </div>
          </>
        )}

        {(ticket.status === "OPEN" || ticket.status === "REOPENED") &&
          user.role === "MANAGER" && (
            <InfoMessage
              text={
                "Ticket is not assigned, set a priority and assign to an expert"
              }
            />
          )}

        {ticket && (
          <>
            <h5 style={{ color: "#EEEEEE" }}>{ticket.title}</h5>
            <div
              style={{ width: "250px", alignSelf: "center", margin: "auto" }}
            >
              <div
                style={{
                  width: "150px",
                  display: "inline-block",
                  paddingLeft: "20px",
                }}
              >
                {StatusIndicator(ticket.status)}
              </div>
              <div style={{ display: "inline-block", float: "right" }}>
                {!editingStatus && (
                  <EditButton onClick={() => setEditingStatus(true)} />
                )}
                {editingStatus && (
                  <CloseEditButton onClick={() => setEditingStatus(false)} />
                )}
              </div>

              {newStatus && (
                <>
                  <div style={{ width: "150px", display: "inline-block" }}>
                    <div style={{ margin: "5px" }}>
                      {downArrowIcon("white", 20)}
                    </div>
                  </div>
                  <div
                    style={{
                      width: "150px",
                      display: "inline-block",
                      paddingLeft: "20px",
                    }}
                  >
                    {StatusIndicator(newStatus)}
                  </div>
                  <div style={{ display: "inline-block", float: "right" }}>
                    <CloseEditButton onClick={() => setNewStatus("")} />
                  </div>
                </>
              )}

              {editingStatus && (
                <div
                  style={{
                    borderRadius: "15px",
                    backgroundColor: "rgba(0,0,0,0.2)",
                    marginTop: "10px",
                    marginBottom: "10px",
                    width: "130px",
                    display: "inline-block",
                    alignSelf: "center",
                  }}
                >
                  <StatusEditList
                    role={user.role}
                    type={ticket.status}
                    onClick={(t) => {
                      setNewStatus(t);
                      setEditingStatus(false);
                    }}
                  />
                </div>
              )}
              <div
                style={{
                  marginTop: "7px",
                  width: "150px",
                  display: "inline-block",
                  paddingLeft:
                    user.role === "MANAGER" &&
                    (ticket.status === "OPEN" || ticket.status === "REOPENED")
                      ? "20px"
                      : "10px",
                  paddingRight:
                    user.role === "MANAGER" &&
                    (ticket.status === "OPEN" || ticket.status === "REOPENED")
                      ? "0px"
                      : "10px",
                }}
              >
                {PriorityIndicator(priorityMap(ticket.priority))}
              </div>

              {user.role === "MANAGER" &&
                (ticket.status === "OPEN" || ticket.status === "REOPENED") && (
                  <>
                    <div
                      style={{
                        marginTop: "7px",
                        display: "inline-block",
                        float: "right",
                      }}
                    >
                      {!editingPriority && (
                        <EditButton onClick={() => setEditingPriority(true)} />
                      )}
                      {editingPriority && (
                        <CloseEditButton
                          onClick={() => setEditingPriority(false)}
                        />
                      )}
                    </div>
                    {newPriority && (
                      <>
                        <div
                          style={{ width: "150px", display: "inline-block" }}
                        >
                          <div style={{ margin: "5px" }}>
                            {downArrowIcon("white", 20)}
                          </div>
                        </div>
                        <div
                          style={{
                            width: "150px",
                            display: "inline-block",
                            paddingLeft: "20px",
                          }}
                        >
                          {PriorityIndicator(newPriority)}
                        </div>
                        <div
                          style={{ display: "inline-block", float: "right" }}
                        >
                          <CloseEditButton onClick={() => setNewPriority("")} />
                        </div>
                      </>
                    )}
                    {editingPriority && (
                      <div
                        style={{
                          borderRadius: "15px",
                          backgroundColor: "rgba(0,0,0,0.2)",
                          marginTop: "10px",
                          marginBottom: "10px",
                          width: "130px",
                          display: "inline-block",
                          alignSelf: "center",
                        }}
                      >
                        <PriorityEdit
                          type={"LOW"}
                          onClick={() => {
                            setNewPriority("LOW");
                            setEditingPriority(false);
                          }}
                        />
                        <PriorityEdit
                          type={"MEDIUM"}
                          onClick={() => {
                            setNewPriority("MEDIUM");
                            setEditingPriority(false);
                          }}
                        />
                        <PriorityEdit
                          type={"HIGH"}
                          onClick={() => {
                            setNewPriority("HIGH");
                            setEditingPriority(false);
                          }}
                        />
                      </div>
                    )}
                  </>
                )}
            </div>

            {user.role === "MANAGER" && (
              <div style={{ marginTop: "20px" }}>
                <NavigationButton
                  text={"View history"}
                  onClick={(e) => {
                    e.preventDefault();
                    navigate(`/tickethistory`, {
                      state: { ticketId: ticket.ticketId },
                    });
                  }}
                />
              </div>
            )}

            <div
              style={{
                backgroundColor: "rgba(255,255,255,0.1)",
                borderRadius: "20px",
                padding: "15px",
                width: "85%",
                alignSelf: "left",
                textAlign: "left",
                margin: "auto",
                fontSize: "14px",
                color: "#EEEEEE",
                marginTop: "15px",
              }}
            >
              {TextNewLine(ticket.description)}
            </div>
            <div
              style={{
                backgroundColor: "rgba(0,0,0,0.2)",
                paddingLeft: "25px",
                paddingRight: "25px",
                maxWidth: "350px",
                borderRadius: "25px",
                alignSelf: "center",
                margin: "auto",
              }}
            >
              <div
                style={{
                  color: "#EEEEEE",
                  paddingTop: "5px",
                  paddingBottom: "5px",
                  marginTop: "14px",
                  marginBottom: "15px",
                  fontSize: 14,
                }}
              >
                PRODUCT: {product}
              </div>
            </div>
            <div
              style={{
                backgroundColor: "rgba(0,0,0,0.1)",
                paddingLeft: "25px",
                paddingRight: "25px",
                paddingBottom: "5px",
                maxWidth: "300px",
                borderRadius: "25px",
                alignSelf: "center",
                margin: "auto",
                marginBottom: "10px",
              }}
            >
              <div
                style={{
                  color: "#EEEEEE",
                  display: "inline-block",
                  paddingTop: "5px",
                  marginTop: "4px",
                  fontSize: 14,
                }}
              >
                {expert ? (
                  <>
                    Expert: {expert.name} {expert.surname}
                  </>
                ) : (
                  <>Expert not assigned</>
                )}
              </div>

              {user.role === "MANAGER" &&
                (ticket.status === "OPEN" || ticket.status === "REOPENED") && (
                  <div style={{ display: "inline-block", marginLeft: "14px" }}>
                    {!editingExpert && (
                      <EditButton onClick={() => setEditingExpert(true)} />
                    )}
                    {editingExpert && (
                      <CloseEditButton
                        onClick={() => setEditingExpert(false)}
                      />
                    )}
                  </div>
                )}

              {editingExpert && (
                <>
                  <div style={{ marginBottom: "10px", marginTop: "10px" }}>
                    <Form.Select
                      className={"form-control:focus"}
                      value={newExpert}
                      onChange={(e) => setNewExpert(e.target.value)}
                      placeholder={"Expert E-mail"}
                      style={{ fontSize: 12, marginBottom: "20px" }}
                    >
                      <option></option>
                      {newExperts.map((p) => (
                        <option>{p.email}</option>
                      ))}

                      {}
                    </Form.Select>
                  </div>
                </>
              )}
            </div>

            <div>
              {expertErrorMessage && (
                <>
                  <div style={{ margin: "10px" }}>
                    <ErrorMessage
                      text={expertErrorMessage}
                      close={() => {
                        setExpertErrorMessage("");
                      }}
                    />{" "}
                  </div>
                </>
              )}
            </div>
            {newStatus && (
              <NavigationButton
                text={"Update ticket"}
                onClick={() => {
                  ticketUpdate();
                }}
              />
            )}
            {newExpert && newPriority && (
              <NavigationButton
                text={"Assign ticket"}
                onClick={() => {
                  ticketAssign();
                }}
              />
            )}
            <hr
              style={{
                color: "white",
                width: "75%",
                alignSelf: "center",
                marginLeft: "auto",
                marginRight: "auto",
                marginBottom: "2px",
                marginTop: "20px",
              }}
            />
            <h1 style={{ color: "#EEEEEE", marginTop: "30px" }}>CHAT</h1>
            <hr
              style={{
                color: "white",
                width: "25%",
                alignSelf: "center",
                marginLeft: "auto",
                marginRight: "auto",
                marginBottom: "22px",
                marginTop: "2px",
              }}
            />

            {loadingChat && (
              <>
                <Spinner style={{ color: "#A0C1D9" }} />
              </>
            )}
            <div
              style={{
                backgroundColor: "rgba(255,255,255,0.1)",
                verticalAlign: "middle",
                borderRadius: "20px",
                padding: "15px",
                width: "95%",
                alignSelf: "left",
                textAlign: "left",
                margin: "auto",
                fontSize: "14px",
                color: "#EEEEEE",
                marginTop: "5px",
              }}
            >
              {chat.map((c) => (
                <ChatMessage
                  user={props.user}
                  imageList={c.attachments}
                  setAlbum={setAlbum}
                  setStartPos={setStartPos}
                  setOverlayShown={setOverlayShown}
                  sender={c.senderEmail}
                  isExpert={true}
                  timestamp={c.sentTimestamp}
                  text={c.text}
                />
              ))}
              {/*<ChatMessage imageList={[]} setStartPos={setStartPos} setOverlayShown={setOverlayShown} isExpert={true} timestamp={"05/03/2023 - 10:12"} name={"Mario Rossi"} text={`Could you provide additional information on xyz?`}/>
                        <ChatMessage imageList={imageList} setStartPos={setStartPos} setOverlayShown={setOverlayShown} isExpert={false} timestamp={"05/03/2023 - 10:13"} name={"Luigi Bianchi"}  text={`Here there are some info\n test test`}/>
                        <ChatMessage imageList={[]} setStartPos={setStartPos} setOverlayShown={setOverlayShown} isExpert={false} timestamp={"05/03/2023 - 10:23"} name={"Luigi Bianchi"}  text={`Could you provide additional information on xyz?`}/>*/}

              {addingMessage && (
                <>
                  <Form.Control
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    style={{
                      borderColor: "rgba(0,0,0,0.6)",
                      paddingLeft: "32px",
                      paddingTop: "15px",
                      backgroundColor: "rgba(0,0,0,0.4)",
                      color: "white",
                      resize: "none",
                      height: "200px",
                      boxShadow: "0px 4px 8px -4px rgba(0,0,0,0.8)",
                      borderRadius: "20px",
                      marginTop: "5px",
                    }}
                    placeholder="Write your message here..."
                    type="textarea"
                    as="textarea"
                  />

                  <AddAttachment
                    attachments={attachments}
                    setAttachments={setAttachments}
                  />
                </>
              )}

              {user.role !== "MANAGER" && ticket.status === "IN_PROGRESS" && (
                <div style={{ width: "100%", height: "60px" }}>
                  {!addingMessage ? (
                    <>
                      <AddButton
                        style={{
                          marginTop: "10px",
                          marginRight: "10px",
                          float: "right",
                        }}
                        onClick={() => setAddingMessage(true)}
                      />
                    </>
                  ) : (
                    <>
                      <SendButton
                        style={{
                          marginTop: "10px",
                          marginRight: "10px",
                          float: "right",
                        }}
                        onClick={() => addMessage()}
                      />
                      <DeleteButton
                        style={{
                          marginTop: "10px",
                          marginRight: "10px",
                          float: "right",
                        }}
                        onClick={() => setAddingMessage(false)}
                      />
                    </>
                  )}
                </div>
              )}
              {loading && (
                <>
                  <Spinner style={{ color: "#A0C1D9" }} />
                </>
              )}
              {chatErrorMessage && (
                <>
                  <div style={{ margin: "10px" }}>
                    <ErrorMessage
                      text={chatErrorMessage}
                      close={() => {
                        setChatErrorMessage("");
                      }}
                    />{" "}
                  </div>
                </>
              )}
            </div>
          </>
        )}
      </div>
    </>
  );
}

export default TicketChatPage;
