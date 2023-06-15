function StatusIndicator(type) {
    switch (type) {
        case "OPEN":
            return <div className="text-bg-light" style={{
                borderRadius: "25px",
                fontSize: 10,
                textAlign: "center",
                verticalAlign: "middle",
                padding: 5
            }}>OPEN</div>
        case "CLOSED":
            return <div className="text-bg-dark" style={{
                borderRadius: "25px",
                fontSize: 10,
                textAlign: "center",
                verticalAlign: "middle",
                padding: 5
            }}>CLOSED</div>
        case "REOPENED":
            return <div style={{
                borderRadius: "25px",
                color: "white",
                backgroundColor: "#9a9a9a",
                fontSize: 10,
                textAlign: "center",
                verticalAlign: "middle",
                padding: 5
            }}>REOPENED</div>
        case "INPROGRESS":
            return <div style={{
                borderRadius: "25px",
                color: "white",
                backgroundColor: "#b087c7",
                fontSize: 10,
                textAlign: "center",
                verticalAlign: "middle",
                padding: 5
            }}>IN PROGRESS</div>
        case "RESOLVED":
            return <div style={{
                borderRadius: "25px",
                color: "white",
                backgroundColor: "#53b02f",
                fontSize: 10,
                textAlign: "center",
                verticalAlign: "middle",
                padding: 5
            }}>RESOLVED</div>
    }
}


export default StatusIndicator