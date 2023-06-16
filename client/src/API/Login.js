import axios from "axios";
import {APIURL} from "./API_URL";
import {setAuthToken} from "./AuthCommon";



async function loginAPI(loginPayload){
    return axios.post(APIURL + "/API/login", loginPayload)
        .then(response => {
            //get token from response
            //console.log(response.data.accessToken)
            const token  =  response.data.token;

            //set JWT token to local
            localStorage.setItem("token", token);

            //set token to axios common header
            setAuthToken(token);
            return response
        })
        .catch(err =>{
                return Promise.reject(err.response.data.detail)
            }
        )
}

async function getProfileInfo(){
    const token = localStorage.getItem("token");
    if (token) {
        setAuthToken(token);
    }
    return axios.get(APIURL + "/API/authenticated/profile/")
        .then(response => {
            return response
        })
        .catch(err => console.log(err));
}

export {loginAPI, getProfileInfo}