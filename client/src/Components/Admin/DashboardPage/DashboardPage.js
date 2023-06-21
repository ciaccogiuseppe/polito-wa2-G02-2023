import { Button, Col, Row, Form } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { Bar } from 'react-chartjs-2';
import Chart from 'chart.js/auto';
import { getAllTicketsManager } from "../../../API/Tickets";


function DashboardPage(props) {
    const [dataOpen, setDataOpen] = useState([]);
    const [dataClosed, setDataClosed] = useState([]);
    const [dataReopened, setDataReopened] = useState([]);
    const [dataInProgress, setDataInProgress] = useState([]);
    const [dataResolved, setDataResolved] = useState([]);
    const [numDays, setNumDays] = useState(7);

    let horizontalLabels = getHorizontalLabels(numDays);
    var initialDate = horizontalLabels[0];

    const data = {
        labels: horizontalLabels.map(date => date.toDateString().substring(4, 10)),
        datasets: [
            {
                label: 'OPEN',
                backgroundColor: '#FFFFFF',
                borderColor: '#000000',
                data: dataOpen,
            },
            {
                label: 'CLOSED',
                backgroundColor: '#000000',
                borderColor: '#000000',
                data: dataClosed,
            },
            {
                label: 'REOPENED',
                backgroundColor: '#9a9a9a',
                borderColor: '#000000',
                data: dataReopened,
            },
            {
                label: 'IN PROGRESS',
                backgroundColor: '#b087c7',
                borderColor: '#000000',
                data: dataInProgress,
            },
            {
                label: 'RESOLVED',
                backgroundColor: '#53b02f',
                borderColor: '#000000',
                data: dataResolved,
            }
        ],
    };


    const navigate = useNavigate();
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
                // .sort((a, b) => a.createdTimestamp > b.createdTimestamp)
                .reduce((r, t) => {
                    let formattedDate = new Date(t.createdTimestamp).toDateString().substring(4, 10);
                    let status = t.status;
                    r[formattedDate] = r[formattedDate] || {};
                    r[formattedDate][status] = r[formattedDate][status] || 0;
                    r[formattedDate][status] += 1;
                    return r;
                }, Object.create(null))
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
                values.open.push(dataOfDay ? dataOfDay["OPEN"] ? dataOfDay["OPEN"] : 0 : 0)
                values.closed.push(dataOfDay ? dataOfDay["CLOSED"] ? dataOfDay["CLOSED"] : 0 : 0)
                values.reopened.push(dataOfDay ? dataOfDay["REOPENED"] ? dataOfDay["REOPENED"] : 0 : 0)
                values.inProgress.push(dataOfDay ? dataOfDay["IN_PROGRESS"] ? dataOfDay["IN_PROGRESS"] : 0 : 0)
                values.resolved.push(dataOfDay ? dataOfDay["RESOLVED"] ? dataOfDay["RESOLVED"] : 0 : 0)
            }
            // console.log(values)
            setDataOpen(values.open);
            setDataClosed(values.closed);
            setDataReopened(values.reopened);
            setDataInProgress(values.inProgress);
            setDataResolved(values.resolved);
        })
    }, [numDays]);


    return <>
        <div className="CenteredButton">
            <div style={{ padding: "10px", marginTop: "50px", paddingBottom: "40px", width: "75%", height: "450px", margin: "auto", borderRadius: "25px", backgroundColor: "rgba(0,0,0,0.2)" }}>
                <h4 style={{ color: "#EEEEEE" }}>Recent tickets</h4>
                <Form.Select value={"Last " + numDays + " days"} onChange={(e) => { setNumDays(getNumDaysFromString(e.target.value)) }} className={"form-select:focus"} style={{ width: "150px", height: "30px", alignSelf: "center", margin: "auto", marginTop: "10px", fontSize: "12px" }}>
                    <option></option>
                    {[3, 7, 14, 30].map(c => <option>Last {c} days</option>)}
                </Form.Select>
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
            </div>
        </div>
    </>
}


function getHorizontalLabels(numDays) {
    const today = new Date();
    let retVal = [];
    for (let i = 0; i < numDays; ++i) {
        let date = new Date();
        date.setDate(today.getDate() - i)
        retVal = [date, ...retVal]
    }
    return retVal;
}

function getNumDaysFromString(string) {
    switch (string) {
        case "Last 3 days":
            return 3
        case "Last 7 days":
            return 7
        case "Last 14 days":
            return 14
        case "Last 30 days":
            return 30
    }
}

export default DashboardPage;
