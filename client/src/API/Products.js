import { APIURL } from './API_URL';

async function getAllProducts(){
    const url = APIURL + "/API/products/";
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
        case 404:
            err.message = "404 - Not Found";
            break;
        default:
            err.message = "Generic Server Error";
            break;
    }
    throw(err);
}

async function getProductDetails(productID){
    const url = APIURL + "/API/products/" + productID;
    //TODO: implement fetch operations
}



export {getAllProducts, getProductDetails}