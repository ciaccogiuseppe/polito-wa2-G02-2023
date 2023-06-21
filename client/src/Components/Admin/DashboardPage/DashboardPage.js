import { Button, Col, Row } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { Bar } from 'react-chartjs-2';
import Chart from 'chart.js/auto';
import { getAllTicketsManager } from "../../../API/Tickets";


function DashboardPage(props) {
    const [dataAssigned, setDataAssigned] = useState([]);
    const [dataUnassigned, setDataUnassigned] = useState([]);

    let horizontalLabels = getHorizontalLabels();
    var initialDate = horizontalLabels[0];

    const data = {
        labels: horizontalLabels.map(date => date.toDateString().substring(4, 10)),
        datasets: [
            {
                label: 'Assigned',
                backgroundColor: '#A2F1A2',
                borderColor: '#00FF00',
                data: dataAssigned,
            },
            {
                label: 'Unassigned',
                backgroundColor: '#F47E7E',
                borderColor: '#FF0000',
                data: [0, 0, 0, 0, 0, 0, 0],
            },
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
                    r[formattedDate] = r[formattedDate] || 0;
                    r[formattedDate] += 1;
                    return r;
                }, Object.create(null))
            // console.log(ticketsData)
            let values = [];
            for (let i = 0; i < 7; ++i) {
                values.push(ticketsData[data.labels[i]])
            }
            setDataAssigned(values);
        })
    }, []);


    return <>
        <div className="CenteredButton">
            <div style={{ padding: "10px", marginTop: "50px", paddingBottom: "40px", width: "75%", height: "450px", margin: "auto", borderRadius: "25px", backgroundColor: "rgba(0,0,0,0.2)" }}>
                <h4 style={{ color: "#EEEEEE" }}>Recent tickets</h4>
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


function getHorizontalLabels() {
    const today = new Date();
    let retVal = [];
    for (let i = 0; i < 7; ++i) {
        let date = new Date();
        date.setDate(today.getDate() - i)
        retVal = [date, ...retVal]
    }
    return retVal;
}

export default DashboardPage;
