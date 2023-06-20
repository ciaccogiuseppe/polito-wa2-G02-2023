import { APIURL } from './API_URL';
import {setAuthToken} from "./AuthCommon";
import axios from "axios";
import {api} from "../App";

async function addItemAPI(itemPayload){
    return api.post(APIURL + "/API/vendor/products/items/", itemPayload)
        .then(response => {
            return response
        })
        .catch(err =>{
                console.log(err);
                return Promise.reject(err.response.data.detail)
            }
        )
}
async function getItemAPI(itemPayload){
    return api.get(APIURL +
        "/API/public/products/"+itemPayload.productId+"/items/"+itemPayload.serialNum)
        .then(response => {
            return response
        })
        .catch(err =>{
                console.log(err);
                return Promise.reject(err.response.data.detail)
            }
        )
}


export {addItemAPI, getItemAPI}