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
    }
}


export default PriorityIndicator