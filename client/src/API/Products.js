import { APIURL } from './API_URL';

async function getAllProducts(){
    const url = APIURL + "/API/products/";
    //TODO: implement fetch operations
}

async function getProductDetails(productID){
    const url = APIURL + "/API/products/" + productID;
    //TODO: implement fetch operations
}



export {getAllProducts, getProductDetails}