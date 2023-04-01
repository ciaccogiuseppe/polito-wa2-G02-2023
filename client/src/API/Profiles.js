import { APIURL } from './API_URL';

async function getProfileDetails(email){
    const url = APIURL + "/API/profiles/" + email;
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

async function addNewProfile(profile){
    const url = APIURL + "/API/profiles/";
    //TODO: implement fetch operations
}

async function editProfile(profile){
    const url = APIURL + "/API/profiles/" + profile.email;
    //TODO: implement fetch operations
}

export {getProfileDetails, addNewProfile, editProfile}