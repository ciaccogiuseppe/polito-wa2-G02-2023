import {api} from "../App";
import {APIURL} from "./API_URL";

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

export {addTicketAPI}