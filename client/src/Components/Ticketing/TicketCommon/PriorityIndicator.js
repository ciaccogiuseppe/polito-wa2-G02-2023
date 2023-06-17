function PriorityIndicator(type) {
    switch (type) {
        case "LOW":
            return <div style={{
                borderRadius: "25px",
                color: "white",
                backgroundColor: "#47bd78",
                fontSize: 10,
                textAlign: "center",
                verticalAlign: "middle",
                padding: 5
            }}>LOW</div>
        case "MEDIUM":
            return <div style={{
                borderRadius: "25px",
                color: "white",
                backgroundColor: "#f8aa48",
                fontSize: 10,
                textAlign: "center",
                verticalAlign: "middle",
                padding: 5
            }}>MEDIUM</div>
        case "HIGH":
            return <div style={{
                borderRadius: "25px",
                color: "white",
                backgroundColor: "#ad3b17",
                fontSize: 10,
                textAlign: "center",
                verticalAlign: "middle",
                padding: 5
            }}>HIGH</div>
        default:
            return <div style={{
                borderRadius: "25px",
                color: "white",
                backgroundColor: "#a1a1a1",
                fontSize: 10,
                textAlign: "center",
                verticalAlign: "middle",
                padding: 5
            }}>NONE</div>
    }
}


export default PriorityIndicator