import { APIURL } from './API_URL';
import axios from "axios";
import {setAuthToken} from "./AuthCommon";
import {api} from "../App";

async function updateClientAPI(updatePayload, email){
    const token = localStorage.getItem("token");
    if (token) {
        setAuthToken(token);
    }
    return api.put(APIURL + "/API/client/user/" + email, updatePayload)
        .then(response => {
            return response
        })
        .catch(err =>{
                console.log(err);
                return Promise.reject(err.response.data.detail)
            }
        )
}

async function updateExpertAPI(updatePayload, email){
    const token = localStorage.getItem("token");
    if (token) {
        setAuthToken(token);
    }
    return api.put(APIURL + "/API/expert/user/" + email, updatePayload)
        .then(response => {
            return response
        })
        .catch(err =>{
                console.log(err);
                return Promise.reject(err.response.data.detail)
            }
        )
}

async function updateManagerAPI(updatePayload, email){
    const token = localStorage.getItem("token");
    if (token) {
        setAuthToken(token);
    }
    return api.put(APIURL + "/API/manager/user/" + email, updatePayload)
        .then(response => {
            return response
        })
        .catch(err =>{
                console.log(err);
                return Promise.reject(err.response.data.detail)
            }
        )
}


async function getProfileDetails(email){
    const token = localStorage.getItem("token");
    if (token) {
        setAuthToken(token);
    }
    return api.get(APIURL + "/API/authenticated/profiles/"+email)
        .then(response => {
            return response
        })
        .catch(err =>{
                console.log(err);
                return Promise.reject(err.response.data.detail)
            }
        )
}
async function getExpertsByCategory(category){
    const token = localStorage.getItem("token");
    if (token) {
        setAuthToken(token);
    }
    return api.get(APIURL + "/API/manager/profiles/experts/"+category)
        .then(response => {
            return response
        })
        .catch(err =>{
                console.log(err);
                return Promise.reject(err.response.data.detail)
            }
        )
}

async function addNewProfile(profile){
    const url = APIURL + "/API/profiles";

    const response = await fetch(url, {
        method:"POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(profile)});
    let err = new Error();
    if(response.ok){
        const result = "success";
        return result;
    }
    let detail = "";
    switch(response.status){
        case 500:
            err.message = "500 - Internal Server Error";
            break;
        case 400:
            detail = JSON.parse(await response.text()).detail;
            err.message = "400 - " + detail;
            break;
        case 409:
            detail = JSON.parse(await response.text()).detail;
            err.message = "409 - " + detail;
            break;
        case 422:
            detail = JSON.parse(await response.text()).detail;
            err.message = "422 - " + detail;
            break;
        default:
            err.message = "Generic Server Error";
            break;
    }
    throw(err);
}

async function editProfile(profile){
    const url = APIURL + "/API/profiles/" + profile.oldemail;
    const response = await fetch(url, {
        method:"PUT",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(profile)});
    let err = new Error();
    if(response.ok){
        const result = "success";
        return result;
    }
    let detail = "";
    switch(response.status){
        case 500:
            err.message = "500 - Internal Server Error";
            break;
        case 400:
            detail = JSON.parse(await response.text()).detail;
            err.message = "400 - " + detail;
            break;
        case 404:
            err.message = "404 - Not Found";
            break;
        case 409:
            detail = JSON.parse(await response.text()).detail;
            err.message = "409 - " + detail;
            break;
        case 422:
            detail = JSON.parse(await response.text()).detail;
            err.message = "422 - " + detail;
            break;
        default:
            err.message = "Generic Server Error";
            break;
    }
    throw(err);
}

export {getProfileDetails, getExpertsByCategory, addNewProfile, editProfile, updateClientAPI, updateManagerAPI, updateExpertAPI}