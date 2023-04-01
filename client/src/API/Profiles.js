import { APIURL } from './API_URL';

async function getProfileDetails(email){
    const url = APIURL + "/API/profiles/" + email;
    //TODO: implement fetch operations
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