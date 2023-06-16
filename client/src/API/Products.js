import { APIURL } from './API_URL';
import {setAuthToken} from "./AuthCommon";
import axios from "axios";

async function getAllProducts(){
    const url = APIURL + "/API/public/products/";
    const token = localStorage.getItem("token");
    if (token) {
        setAuthToken(token);
    }

    axios.get(url).then(response => {
        //get token from response
        //console.log(response.data.accessToken)
        console.log(response.data)

    })
        .catch(err => console.log(err));


    /*const response = await fetch(url);
    let err = new Error();
    if(response.ok){
        const result = await response.json();
        return result;
    }
    switch(response.status){
        case 500:
            err.message = "500 - Internal Server Error";
            break;
        case 404:
            err.message = "404 - Not Found";
            break;
        default:
            err.message = "Generic Server Error";
            break;
    }
    throw(err);*/
}

async function getProductDetails(productID){
    const url = APIURL + "/API/products/" + productID;
    const response = await fetch(url);
    let err = new Error();
    if(response.ok){
        const result = await response.json();
        return result;
    }
    switch(response.status){
        case 500:
            err.message = "500 - Internal Server Error";
            break;
        case 422: 
            err.message = "422 - Unprocessable Entity";
            break;
        case 404:
            err.message = "404 - Not Found";
            break;
        default:
            err.message = "Generic Server Error";
            break;
    }
    throw(err);
}



export {getAllProducts, getProductDetails}