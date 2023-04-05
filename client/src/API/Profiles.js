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
        case 422:
            const detail = JSON.parse(await response.text()).detail;
            err.message = "422 - " + detail;
            break;
        default:
            err.message = "Generic Server Error";
            break;
    }
    throw(err);
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
    const url = APIURL + "/API/profiles/" + profile.email;
    //TODO: implement fetch operations
}

export {getProfileDetails, addNewProfile, editProfile}