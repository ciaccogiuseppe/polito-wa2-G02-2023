import axios from "axios";
import {APIURL} from "./API_URL";
import {setAuthToken} from "./AuthCommon";



async function loginAPI(loginPayload){
    axios.post(APIURL + "/API/login", loginPayload)
        .then(response => {
            //get token from response
            //console.log(response.data.accessToken)
            const token  =  response.data.token;

            //set JWT token to local
            localStorage.setItem("token", token);

            //set token to axios common header
            setAuthToken(token);

        })
        .catch(err => console.log(err));
}

export {loginAPI}