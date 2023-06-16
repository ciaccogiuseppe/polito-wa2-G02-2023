import { APIURL } from './API_URL';
import {setAuthToken} from "./AuthCommon";
import axios from "axios";
import {api} from "../App";

async function getAllProducts(){
    const url = APIURL + "/API/public/products/";
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

async function getAllCategories(){
    const url = APIURL + "/API/public/categories/";
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

async function getAllBrands(){
    const url = APIURL + "/API/public/brands/";
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


async function addProductAPI(productPayload){
    return api.post(APIURL + "/API/manager/products/", productPayload)
        .then(response => {
            return response
        })
        .catch(err =>{
                console.log(err);
                return Promise.reject(err.response.data.detail)
            }
        )
}

async function addBrandAPI(brandPayload){
    return api.post(APIURL + "/API/manager/brand/", brandPayload)
        .then(response => {
            return response
        })
        .catch(err =>{
                console.log(err);
                return Promise.reject(err.response.data.detail)
            }
        )
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



export {getAllProducts, getProductDetails, getAllCategories, getAllBrands, addProductAPI, addBrandAPI}