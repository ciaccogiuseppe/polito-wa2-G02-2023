import { Col, Row, Form } from "react-bootstrap";
import { useEffect, useState } from "react";
import { Bar, Doughnut } from 'react-chartjs-2';
import _Chart from 'chart.js/auto'; // needed to correctly compile!
import { getAllTicketsManager } from "../../../API/Tickets";


function DashboardPage() {
    const [dataOpen, setDataOpen] = useState([]);
    const [dataClosed, setDataClosed] = useState([]);
    const [dataReopened, setDataReopened] = useState([]);
    const [dataInProgress, setDataInProgress] = useState([]);
    const [dataResolved, setDataResolved] = useState([]);

    const [priorities, setPriorities] = useState([]);

    const [numDays, setNumDays] = useState(7);

    let horizontalLabels = getHorizontalLabels(numDays);
    var initialDate = horizontalLabels[0];

    const data = {
        labels: horizontalLabels.map(date => date.substring(4, 10)),
        datasets: [
            {
                label: 'OPEN',
                backgroundColor: '#FFFFFF99',
                hoverBackgroundColor: '#FFFFFF',
                borderColor: '#FFFFFF',
                data: dataOpen,
                borderWidth: 2,
            },
            {
                label: 'CLOSED',
                backgroundColor: '#00000099',
                hoverBackgroundColor: '#000000',
                borderColor: '#000000',
                data: dataClosed,
                borderWidth: 2,
            },
            {
                label: 'REOPENED',
                backgroundColor: '#9a9a9a99',
                hoverBackgroundColor: '#9a9a9a',
                borderColor: '#9a9a9a',
                data: dataReopened,
                borderWidth: 2,
            },
            {
                label: 'IN PROGRESS',
                backgroundColor: '#b087c799',
                hoverBackgroundColor: '#b087c7',
                borderColor: '#b087c7',
                data: dataInProgress,
                borderWidth: 2,
            },
            {
                label: 'RESOLVED',
                backgroundColor: '#53b02f99',
                hoverBackgroundColor: '#53b02f',
                borderColor: '#53b02f',
                data: dataResolved,
                borderWidth: 2,
            }
        ],
    };

    const data2 = {
        labels: ["HIGH", "MEDIUM", "LOW", "NONE"],
        datasets: [
            {
                label: 'Tickets',
                data: priorities,
                backgroundColor: [
                    '#ad3b1799',
                    '#f8aa4899',
                    '#47bd7899',
                    '#a1a1a199',
                ],
                borderColor: [
                    '#ad3b17',
                    '#f8aa48',
                    '#47bd78',
                    '#a1a1a1',
                ],
                hoverBackgroundColor: [
                    '#ad3b17',
                    '#f8aa48',
                    '#47bd78',
                    '#a1a1a1',
                ],
                borderWidth: 2,
            },
        ],
    };

    useEffect(() => {
        window.scrollTo(0, 0);
    }, []);

    useEffect(() => {
        getAllTicketsManager(
            {
                minTimestamp: new Date(initialDate).toISOString().replace(/.$/, ''),
            }
        ).then(tickets => {
            let ticketsData = tickets
                .reduce((r, t) => {
                    let formattedDate = new Date(t.createdTimestamp)
                        .toDateString()
                        .substring(4, 10);
                    let status = t.status;
                    let priority = t.priority.toString();
                    // used for status chart
                    r[formattedDate] = r[formattedDate] || {};
                    r[formattedDate][status] = r[formattedDate][status] || 0;
                    r[formattedDate][status] += 1;
                    // used for priority chart
                    r[priority] = r[priority] || 0;
                    r[priority] += 1;
                    return r;
                }, Object.create(null));
            let values = {
                open: [],
                closed: [],
                reopened: [],
                inProgress: [],
                resolved: []
            };
            for (let i = 0; i < numDays; ++i) {
                let currDate = data.labels[i];
                let dataOfDay = ticketsData[currDate];
                values.open.push(dataOfDay ?
                    dataOfDay["OPEN"] ?
                        dataOfDay["OPEN"] :
                        0 :
                    0)
                values.closed.push(dataOfDay ?
                    dataOfDay["CLOSED"] ?
                        dataOfDay["CLOSED"] :
                        0 :
                    0)
                values.reopened.push(dataOfDay ?
                    dataOfDay["REOPENED"] ?
                        dataOfDay["REOPENED"] :
                        0 :
                    0)
                values.inProgress.push(dataOfDay ?
                    dataOfDay["IN_PROGRESS"] ?
                        dataOfDay["IN_PROGRESS"] :
                        0 :
                    0)
                values.resolved.push(dataOfDay ?
                    dataOfDay["RESOLVED"] ?
                        dataOfDay["RESOLVED"] :
                        0 :
                    0)
            }
            // statuses
            setDataOpen(values.open);
            setDataClosed(values.closed);
            setDataReopened(values.reopened);
            setDataInProgress(values.inProgress);
            setDataResolved(values.resolved);
            // priorities
            setPriorities([
                ticketsData["3"] || 0,
                ticketsData["2"] || 0,
                ticketsData["1"] || 0,
                ticketsData["0"] || 0
            ])
        })
    }, [numDays]);


    return <>
        <div className="CenteredButton">
            <div style={{ padding: "10px", marginTop: "50px", paddingBottom: "50px", padding: "10px", width: "90%", height: "500px", margin: "auto", borderRadius: "25px", backgroundColor: "rgba(0,0,0,0.2)" }}>
                <Row className="d-flex justify-content-center g-0" style={{ marginBottom: "15px" }}>
                    <Col xs={2}>
                        <h4 style={{ color: "#EEEEEE" }}>Recent tickets</h4>
                    </Col>
                    &nbsp;&nbsp;
                    <Col xs={1}>
                        <Form.Select value={numDays == 1 ?"Today" : "Last " + numDays + " days"} onChange={(e) => { setNumDays(getNumDaysFromString(e.target.value)) }}
                        className={"form-select:focus"} style={{ width: "150px", height: "30px", alignSelf: "center", margin: "auto", fontSize: "12px" }}>
                        <option></option>
                        {[1, 3, 7, 14, 30].map(c => c == 1 ?
                            <option>Today</option> :
                            <option>Last {c} days</option>)}
                    </Form.Select>
                </Col>
            </Row>
            <Row className="d-flex justify-content-center g-1">
                <Col xs={8} style={{ marginTop: "20px", paddingBottom: "50px", height: "450px", margin: "auto" }}>
                    <h6 style={{ color: "#EEEEEE" }}>Status</h6>
                    <Bar data={data} options={{
                        maintainAspectRatio: false,
                        responsive: true, color: "#EEEEEE",
                        scales: {
                            y: {
                                ticks: { color: '#EEEEEE', stepSize: 1 }
                            },
                            x: {
                                ticks: { color: '#EEEEEE' }
                            }
                        }
                    }} />
                </Col>
                <Col xs={3} style={{ marginTop: "20px", paddingBottom: "50px", height: "450px", margin: "auto" }}>
                    <h6 style={{ color: "#EEEEEE" }}>Priority</h6>
                    <Doughnut data={data2} options={{
                        maintainAspectRatio: false,
                        responsive: true, color: "#EEEEEE"
                    }} />
                </Col>
            </Row>
        </div>
    </div >
    </>
}


function getHorizontalLabels(numDays) {
    const today = new Date();
    let retVal = [];
    for (let i = 0; i < numDays; ++i) {
        let date = new Date();
        date.setDate(today.getDate() - i)
        retVal = [date.toDateString(), ...retVal]
    }
    return retVal;
}


function getNumDaysFromString(string) {
    switch (string) {
        case "Today":
            return 1
        case "Last 3 days":
            return 3
        case "Last 7 days":
            return 7
        case "Last 14 days":
            return 14
        case "Last 30 days":
            return 30
        default:
            return 7
    }
}

export default DashboardPage;
