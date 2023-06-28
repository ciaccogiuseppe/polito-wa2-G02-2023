import AppNavbar from "../../AppNavbar/AppNavbar";
import { Row, Spinner } from "react-bootstrap";
import NavigationButton from "../../Common/NavigationButton";
import TicketListTable from "./TicketListTable";
import React, { useEffect, useState } from "react";
import {
  caretDownIcon,
  caretUpIcon,
  crossIcon,
  filterIcon,
} from "../../Common/Icons";
import { Slider } from "@mui/material";
import StatusIndicator from "../TicketCommon/StatusIndicator";
import PriorityIndicator from "../TicketCommon/PriorityIndicator";
import AddButton from "../../Common/AddButton";
import { useNavigate } from "react-router-dom";
import { getAllProducts } from "../../../API/Products";
import {
  getAllTicketsClient,
  getAllTicketsExpert,
  getAllTicketsManager,
} from "../../../API/Tickets";
import ErrorMessage from "../../Common/ErrorMessage";

function StatusSelector(props) {
  const selectedStatus = props.selectedStatus;
  const setSelectedStatus = props.setSelectedStatus;
  return (
    <>
      <div
        onClick={() => {
          if (selectedStatus.includes("OPEN"))
            setSelectedStatus(selectedStatus.filter((a) => a !== "OPEN"));
          else setSelectedStatus([...selectedStatus, "OPEN"]);
        }}
        style={{
          cursor: "pointer",
          opacity: selectedStatus.includes("OPEN") ? 1 : 0.4,
          display: "inline-block",
          width: "85px",
          marginLeft: "5px",
          marginRight: "5px",
        }}
      >
        {StatusIndicator("OPEN")}
      </div>
      <div
        onClick={() => {
          if (selectedStatus.includes("REOPENED"))
            setSelectedStatus(selectedStatus.filter((a) => a !== "REOPENED"));
          else setSelectedStatus([...selectedStatus, "REOPENED"]);
        }}
        style={{
          cursor: "pointer",
          opacity: selectedStatus.includes("REOPENED") ? 1 : 0.4,
          display: "inline-block",
          width: "85px",
          marginLeft: "5px",
          marginRight: "5px",
        }}
      >
        {StatusIndicator("REOPENED")}
      </div>
      <div
        onClick={() => {
          if (selectedStatus.includes("IN_PROGRESS"))
            setSelectedStatus(
              selectedStatus.filter((a) => a !== "IN_PROGRESS")
            );
          else setSelectedStatus([...selectedStatus, "IN_PROGRESS"]);
        }}
        style={{
          cursor: "pointer",
          opacity: selectedStatus.includes("IN_PROGRESS") ? 1 : 0.4,
          display: "inline-block",
          width: "85px",
          marginLeft: "5px",
          marginRight: "5px",
        }}
      >
        {StatusIndicator("IN_PROGRESS")}
      </div>
      <div
        onClick={() => {
          if (selectedStatus.includes("RESOLVED"))
            setSelectedStatus(selectedStatus.filter((a) => a !== "RESOLVED"));
          else setSelectedStatus([...selectedStatus, "RESOLVED"]);
        }}
        style={{
          cursor: "pointer",
          opacity: selectedStatus.includes("RESOLVED") ? 1 : 0.4,
          display: "inline-block",
          width: "85px",
          marginLeft: "5px",
          marginRight: "5px",
        }}
      >
        {StatusIndicator("RESOLVED")}
      </div>
      <div
        onClick={() => {
          if (selectedStatus.includes("CLOSED"))
            setSelectedStatus(selectedStatus.filter((a) => a !== "CLOSED"));
          else setSelectedStatus([...selectedStatus, "CLOSED"]);
        }}
        style={{
          cursor: "pointer",
          opacity: selectedStatus.includes("CLOSED") ? 1 : 0.4,
          display: "inline-block",
          width: "85px",
          marginLeft: "5px",
          marginRight: "5px",
        }}
      >
        {StatusIndicator("CLOSED")}
      </div>
    </>
  );
}

function getPageTitle(role) {
  switch (role) {
    case "CLIENT":
      return "MY TICKETS";
    case "EXPERT":
      return "ASSIGNED TICKETS";
    case "MANAGER":
      return "TICKETS";
    default:
      return "TICKETS";
  }
}

function TicketListPage(props) {
  const loggedIn = props.loggedIn;
  const user = props.user;
  const [userEmail, setUserEmail] = useState("");
  const [expertEmail, setExpertEmail] = useState("");
  const [initialDate, setInitialDate] = useState("");
  const [finalDate, setFinalDate] = useState("");
  const [ticketList, setTicketList] = useState([]);
  const [priority, setPriority] = useState([0, 3]);
  const [selectedStatus, setSelectedStatus] = useState([]);
  const [showFilters, setShowFilters] = useState(user.role === "MANAGER");
  const [products, setProducts] = useState([]);
  const [product, setProduct] = useState(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(
    () => {
      window.scrollTo(0, 0);

      if (user.role === "CLIENT") {
        getAllTicketsClient({
          clientEmail: user.email,
        })
          .then((tickets) => {
            setTicketList(tickets.sort((a, b) => a.ticketId < b.ticketId));
            const prodsIds = tickets.map((t) => t.productId);

            getAllProducts().then((products) =>
              setProducts(
                products
                  .map((p) => {
                    return { productId: p.productId, name: p.name };
                  })
                  .filter((p) => prodsIds.includes(p.productId))
                  .sort((a, b) =>
                    a.name.localeCompare(b.name, undefined, { numeric: true })
                  )
              )
            );
          })
          .catch((err) =>
            setErrorMessage("Unable to fetch data from server: " + err)
          );
      } else if (user.role === "EXPERT") {
        getAllTicketsExpert({
          expertEmail: user.email,
        })
          .then((tickets) => {
            setTicketList(tickets.sort((a, b) => a.ticketId < b.ticketId));
            const prodsIds = tickets.map((t) => t.productId);

            getAllProducts().then((products) =>
              setProducts(
                products
                  .map((p) => {
                    return { productId: p.productId, name: p.name };
                  })
                  .filter((p) => prodsIds.includes(p.productId))
                  .sort((a, b) =>
                    a.name.localeCompare(b.name, undefined, { numeric: true })
                  )
              )
            );
          })
          .catch((err) =>
            setErrorMessage("Unable to fetch data from server: " + err)
          );
      } else if (user.role === "MANAGER") {
        getAllProducts()
          .then((products) =>
            setProducts(
              products
                .map((p) => {
                  return { productId: p.productId, name: p.name };
                })
                .sort((a, b) =>
                  a.name.localeCompare(b.name, undefined, { numeric: true })
                )
            )
          )
          .catch((err) =>
            setErrorMessage("Unable to fetch data from server: " + err)
          );
      }
    },
    // eslint-disable-next-line
    []
  );

  function applyFilters() {
    setLoading(true);
    if (user.role === "CLIENT")
      getAllTicketsClient({
        clientEmail: user.email,
        status: selectedStatus,
        minPriority: Math.min(...priority),
        maxPriority: Math.max(...priority),
        minTimestamp:
          initialDate && new Date(initialDate).toISOString().replace(/.$/, ""),
        maxTimestamp:
          finalDate && new Date(finalDate).toISOString().replace(/.$/, ""),
        productId: product,
      })
        .then((tickets) => {
          setLoading(false);
          setTicketList(tickets.sort((a, b) => a.ticketId < b.ticketId));
          setShowFilters(false);
        })
        .catch((err) => {
          setLoading(false);
          setErrorMessage(err);
        });
    else if (user.role === "EXPERT")
      getAllTicketsExpert({
        expertEmail: user.email,
        status: selectedStatus,
        minPriority: Math.min(...priority),
        maxPriority: Math.max(...priority),
        minTimestamp:
          initialDate && new Date(initialDate).toISOString().replace(/.$/, ""),
        maxTimestamp:
          finalDate && new Date(finalDate).toISOString().replace(/.$/, ""),
        productId: product,
      })
        .then((tickets) => {
          setLoading(false);
          setTicketList(tickets.sort((a, b) => a.ticketId < b.ticketId));
          setShowFilters(false);
        })
        .catch((err) => {
          setLoading(false);
          setErrorMessage(err);
        });
    else if (user.role === "MANAGER")
      getAllTicketsManager({
        clientEmail: userEmail,
        expertEmail: expertEmail,
        status: selectedStatus,
        minPriority: Math.min(...priority),
        maxPriority: Math.max(...priority),
        minTimestamp:
          initialDate && new Date(initialDate).toISOString().replace(/.$/, ""),
        maxTimestamp:
          finalDate && new Date(finalDate).toISOString().replace(/.$/, ""),
        productId: product,
      })
        .then((tickets) => {
          setLoading(false);
          setTicketList(tickets.sort((a, b) => a.ticketId < b.ticketId));
          setShowFilters(false);
        })
        .catch((err) => {
          setLoading(false);
          setErrorMessage(err);
        });
  }

  return (
    <>
      <AppNavbar
        user={props.user}
        loggedIn={loggedIn}
        selected={"tickets"}
        logout={props.logout}
      />

      {user.role === "CLIENT" && (
        <div style={{ position: "fixed", bottom: "24px", right: "24px" }}>
          <AddButton onClick={() => navigate("/newticket")} />
        </div>
      )}

      <div className="CenteredButton" style={{ marginTop: "50px" }}>
        <h1 style={{ color: "#EEEEEE", marginTop: "80px" }}>
          {getPageTitle(user.role)}
        </h1>
        <hr
          style={{
            color: "white",
            width: "25%",
            alignSelf: "center",
            marginLeft: "auto",
            marginRight: "auto",
            marginBottom: "15px",
            marginTop: "2px",
          }}
        />

        <div
          style={{
            width: "75%",
            alignSelf: "center",
            margin: "auto",
            borderRadius: "25px",
            marginTop: "15px",
            backgroundColor: "rgba(0,0,0,0.2)",
          }}
        >
          <h4 style={{ color: "#EEEEEE", paddingTop: "10px" }}>FILTERS</h4>
          {showFilters ? (
            <div
              onClick={() => {
                setShowFilters(false);
              }}
              style={{
                display: "inline-block",
                paddingBottom: "10px",
                cursor: "pointer",
              }}
            >
              {caretUpIcon("white", 30)}
            </div>
          ) : (
            <div
              onClick={() => {
                setShowFilters(true);
              }}
              style={{
                display: "inline-block",
                paddingBottom: "10px",
                cursor: "pointer",
              }}
            >
              {caretDownIcon("white", 30)}
            </div>
          )}

          {showFilters && (
            <>
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
              <Row
                className="d-flex justify-content-center"
                style={{ marginBottom: "10px" }}
              >
                <div style={{ display: "inline-block", maxWidth: "250px" }}>
                  <span style={{ color: "#DDDDDD" }}>Product</span>
                  <div
                    className="input-group mb-3"
                    style={{ marginTop: "8px" }}
                  >
                    <span
                      style={{ cursor: product ? "pointer" : "" }}
                      onClick={() => setProduct("")}
                      className="input-group-text"
                    >
                      {product ? crossIcon("black", 17) : filterIcon()}
                    </span>
                    <select
                      style={{ appearance: "searchfield", fontSize: 13 }}
                      value={product}
                      className="form-control"
                      placeholder="---"
                      onChange={(e) => {
                        setProduct(e.target.value);
                      }}
                    >
                      <option></option>
                      {products.map((p) => (
                        <option value={p.productId}>{p.name}</option>
                      ))}
                    </select>
                  </div>
                </div>
                <div style={{ display: "inline-block", maxWidth: "250px" }}>
                  <span style={{ color: "#DDDDDD" }}>Created After</span>
                  <div
                    className="input-group mb-3"
                    style={{ marginTop: "8px" }}
                  >
                    <span
                      style={{ cursor: initialDate ? "pointer" : "" }}
                      onClick={() => setInitialDate("")}
                      className="input-group-text"
                    >
                      {initialDate ? crossIcon("black", 17) : filterIcon()}
                    </span>
                    <input
                      style={{ fontSize: 13 }}
                      type="date"
                      className="form-control"
                      placeholder="---"
                      value={initialDate}
                      onChange={(e) => setInitialDate(e.target.value)}
                    />
                  </div>
                </div>
                <div style={{ display: "inline-block", maxWidth: "250px" }}>
                  <span style={{ color: "#DDDDDD" }}>Created Before</span>
                  <div
                    className="input-group mb-3"
                    style={{ marginTop: "8px" }}
                  >
                    <span
                      style={{ cursor: finalDate ? "pointer" : "" }}
                      onClick={() => setFinalDate("")}
                      className="input-group-text"
                    >
                      {finalDate ? crossIcon("black", 17) : filterIcon()}
                    </span>
                    <input
                      style={{ fontSize: 13 }}
                      type="date"
                      className="form-control"
                      placeholder="---"
                      value={finalDate}
                      onChange={(e) => {
                        setFinalDate(e.target.value);
                      }}
                    />
                  </div>
                </div>
              </Row>
              <Row
                className="d-flex justify-content-center"
                style={{ marginBottom: "10px" }}
              >
                {user.role === "MANAGER" && (
                  <>
                    <div style={{ display: "inline-block", maxWidth: "250px" }}>
                      <span style={{ color: "#DDDDDD" }}>Client Email</span>
                      <div
                        className="input-group mb-3"
                        style={{ marginTop: "8px" }}
                      >
                        <span
                          style={{ cursor: userEmail ? "pointer" : "" }}
                          onClick={() => setUserEmail("")}
                          className="input-group-text"
                        >
                          {userEmail ? crossIcon("black", 17) : filterIcon()}
                        </span>
                        <input
                          style={{ fontSize: 13 }}
                          type="text"
                          className="form-control"
                          placeholder="client@email.com"
                          value={userEmail}
                          onChange={(e) => setUserEmail(e.target.value)}
                        />
                      </div>
                    </div>
                    <div style={{ display: "inline-block", maxWidth: "250px" }}>
                      <span style={{ color: "#DDDDDD" }}>Expert Email</span>
                      <div
                        className="input-group mb-3"
                        style={{ marginTop: "8px" }}
                      >
                        <span
                          style={{ cursor: expertEmail ? "pointer" : "" }}
                          onClick={() => setExpertEmail("")}
                          className="input-group-text"
                        >
                          {expertEmail ? crossIcon("black", 17) : filterIcon()}
                        </span>
                        <input
                          style={{ fontSize: 13 }}
                          type="text"
                          className="form-control"
                          placeholder="expert@email.com"
                          value={expertEmail}
                          onChange={(e) => setExpertEmail(e.target.value)}
                        />
                      </div>
                    </div>
                  </>
                )}

                <div style={{ display: "inline-block", marginTop: "20px" }}>
                  <StatusSelector
                    selectedStatus={selectedStatus}
                    setSelectedStatus={setSelectedStatus}
                  />
                </div>

                <div style={{ maxWidth: "200px", marginTop: "20px" }}>
                  <span style={{ color: "#DDDDDD" }}>Priority</span>
                  <Slider
                    getAriaLabel={() => "Priority Range"}
                    value={priority}
                    max={3}
                    onChange={(event, newValue) => {
                      setPriority(newValue);
                    }}
                    valueLabelDisplay="off"
                    getAriaValueText={() => {
                      "a";
                    }}
                    style={{ color: "#A0C1D9" }}
                    marks={[
                      {
                        value: 0,
                        label: (
                          <div
                            style={{ opacity: priority.includes(0) ? 1 : 0.5 }}
                          >
                            {PriorityIndicator("NONE")}
                          </div>
                        ),
                      },
                      {
                        value: 1,
                        label: (
                          <div
                            style={{
                              opacity:
                                Math.min(...priority) <= 1 &&
                                Math.max(...priority) >= 1
                                  ? 1
                                  : 0.5,
                            }}
                          >
                            {PriorityIndicator("LOW")}
                          </div>
                        ),
                      },
                      {
                        value: 2,
                        label: (
                          <div
                            style={{
                              opacity:
                                Math.min(...priority) <= 2 &&
                                Math.max(...priority) >= 2
                                  ? 1
                                  : 0.5,
                            }}
                          >
                            {PriorityIndicator("MEDIUM")}
                          </div>
                        ),
                      },
                      {
                        value: 3,
                        label: (
                          <div
                            style={{ opacity: priority.includes(3) ? 1 : 0.5 }}
                          >
                            {PriorityIndicator("HIGH")}
                          </div>
                        ),
                      },
                    ]}
                  />
                </div>

                <div style={{ marginTop: "15px", marginBottom: "15px" }}>
                  {!(
                    userEmail === "" &&
                    expertEmail === "" &&
                    !product &&
                    initialDate === "" &&
                    finalDate === "" &&
                    selectedStatus.length === 0 &&
                    Math.max(...priority) - Math.min(...priority) === 3
                  ) ? (
                    <NavigationButton
                      disabled={
                        userEmail === "" &&
                        expertEmail === "" &&
                        !product &&
                        initialDate === "" &&
                        finalDate === "" &&
                        selectedStatus.length === 0 &&
                        Math.max(...priority) - Math.min(...priority) === 3
                      }
                      text={"Search"}
                      onClick={(e) => {
                        e.preventDefault();
                        applyFilters();
                      }}
                    />
                  ) : (
                    <NavigationButton
                      text={"Reset"}
                      onClick={(e) => {
                        e.preventDefault();
                        if (user.role === "MANAGER") setTicketList([]);
                        else applyFilters();
                      }}
                    />
                  )}
                </div>
              </Row>
            </>
          )}
        </div>
        {loading && (
          <>
            <Spinner style={{ color: "#A0C1D9" }} />
          </>
        )}
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
        <TicketListTable
          ticketList={
            ticketList &&
            ticketList.map((t) => {
              return {
                ...t,
                product: products
                  .filter((p) => p.productId === t.productId)
                  .map((p) => p.name)[0],
              };
            })
          }
        />
      </div>
    </>
  );
}

export default TicketListPage;
