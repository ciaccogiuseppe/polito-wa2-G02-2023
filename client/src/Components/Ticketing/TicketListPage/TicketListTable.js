
function TicketListTable(props){
    const ticketList = props.ticketList
    return <>
        {ticketList.length >= 0  &&
        <div>
            <table className="table table-striped table-bordered table-dark"  style={{ alignContent: "center", width: "70%", margin: "auto", marginTop:"20px" }}>
                <thead>
                <tr className="text-light">
                    <th><h5>Title</h5></th>
                    <th width={"15%"}><h5>Status</h5></th>
                    <th width={"15%"}><h5>Created</h5></th>
                </tr>
                </thead>
                <tbody>
                <tr className="text-light"><td className="text-light">Can't use touchscreen on my phone</td><td className="text-warning">IN PROGRESS</td><td className="text-light">05/02/2022</td></tr>
                <tr className="text-light"><td className="text-light">Tablet camera not working</td><td className="text-success">RESOLVED</td><td className="text-light">b</td></tr>
                <tr className="text-light"><td className="text-light">Broke phone screen</td><td className="text-danger">CLOSED</td><td className="text-light">b</td></tr>
                <tr className="text-light"><td className="text-light">a</td><td className="text-light">a</td><td className="text-light">b</td></tr>
                <tr className="text-light"><td className="text-light">a</td><td className="text-light">a</td><td className="text-light">b</td></tr>
                </tbody>
            </table>
        </div>}
    </>
}


export default TicketListTable;