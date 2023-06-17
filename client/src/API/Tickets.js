import {api} from "../App";
import {APIURL} from "./API_URL";
import {setAuthToken} from "./AuthCommon";

async function addTicketAPI(ticketPayload){
    return api.post(APIURL + "/API/client/ticketing/", ticketPayload)
        .then(response => {
            return response
        })
        .catch(err =>{
                console.log(err);
                return Promise.reject(err.response.data.detail)
            }
        )
}

async function getAllTicketsClient(ticketFilters){
    const  filters = "" +
        (ticketFilters.customerEmail ? `&customerEmail=${ticketFilters.customerEmail}` : "") +
        ((ticketFilters.status && ticketFilters.status.length > 0) ? ticketFilters.status.map(s => `&status=${s}`).reduce((a,b)=>a+b) : "") +
        (ticketFilters.productId ? `&productId=${ticketFilters.productId}` : "") +
        (ticketFilters.minPriority!==undefined ? `&minPriority=${ticketFilters.minPriority}` : "") +
        (ticketFilters.maxPriority!==undefined ? `&maxPriority=${ticketFilters.maxPriority}` : "") +
        (ticketFilters.minTimestamp ? `&createdAfter=${ticketFilters.minTimestamp}` : "") +
        (ticketFilters.maxTimestamp ? `&createdBefore=${ticketFilters.maxTimestamp}` : "")
    const url = APIURL + "/API/client/ticketing/filter?"+filters;
    const token = localStorage.getItem("token");
    if (token) {
        setAuthToken(token);
    }

    return api.get(url).then(response => {
        //get token from response
        //console.log(response.data.accessToken)
        return response.data

    })
        .catch(err => {
            console.log(err)
            Promise.reject(err.response.data.detail)
        });
}

export {addTicketAPI, getAllTicketsClient}