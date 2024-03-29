import { APIURL } from "./API_URL";
import { setAuthToken } from "./AuthCommon";
import { api } from "../App";
import axios from "axios";

async function loginAPI(loginPayload) {
  setAuthToken("");
  return api
    .post(APIURL + "/API/login", loginPayload)
    .then((response) => {
      //get token from response
      //console.log(response.data.accessToken)
      const token = response.data.token;

      //set JWT token to local
      localStorage.setItem("token", token);
      localStorage.setItem("refreshToken", response.data.refreshToken);

      //set token to axios common header
      setAuthToken(token);
      return response;
    })
    .catch((err) => {
      return Promise.reject(err.response.data.detail);
    });
}

async function signupAPI(signupPayload) {
  return api
    .post(APIURL + "/API/signup", signupPayload)
    .then((response) => {
      return response;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}

async function createVendorAPI(signupPayload) {
  return api
    .post(APIURL + "/API/createVendor", signupPayload)
    .then((response) => {
      return response;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}
async function resetPasswordApplyAPI(passwordPayload) {
  return axios
    .put(APIURL + "/API/resetPassword/", passwordPayload)
    .then((response) => {
      return response;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}

async function validateMailAPI(mailValidatePayload) {
  return axios
    .put(APIURL + "/API/validateEmail/", mailValidatePayload)
    .then((response) => {
      return response;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}
async function emailValidateTriggerAPI(email) {
  return api
    .get(APIURL + "/API/validateEmail/" + email)
    .then((response) => {
      return response;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}

async function createExpertAPI(signupPayload) {
  return api
    .post(APIURL + "/API/createExpert", signupPayload)
    .then((response) => {
      return response;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}

async function getProfileInfo() {
  const token = localStorage.getItem("token");
  if (token) {
    setAuthToken(token);
  }
  return api
    .get(APIURL + "/API/authenticated/profile/")
    .then((response) => {
      return response;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}
async function passwordResetTriggerAPI(email) {
  return api
    .get(APIURL + "/API/resetPassword/" + email)
    .then((response) => {
      return response;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}

export {
  loginAPI,
  getProfileInfo,
  signupAPI,
  createExpertAPI,
  createVendorAPI,
  resetPasswordApplyAPI,
  passwordResetTriggerAPI,
  emailValidateTriggerAPI,
  validateMailAPI,
};
