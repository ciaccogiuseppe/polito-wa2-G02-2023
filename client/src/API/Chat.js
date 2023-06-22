import { api } from "../App";
import { APIURL } from "./API_URL";
import { setAuthToken } from "./AuthCommon";

async function addMessageAPI(messagePayload, ticketId) {
  const token = localStorage.getItem("token");
  if (token) {
    setAuthToken(token);
  }
  return api
    .post(APIURL + "/API/chat/" + ticketId, messagePayload)
    .then((response) => {
      return response;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}

async function getChatClient(ticketId) {
  const token = localStorage.getItem("token");
  if (token) {
    setAuthToken(token);
  }
  return api
    .get(APIURL + "/API/chat/" + ticketId)
    .then((response) => {
      return response.data;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}

async function getChatExpert(ticketId) {
  const token = localStorage.getItem("token");
  if (token) {
    setAuthToken(token);
  }
  return api
    .get(APIURL + "/API/chat/" + ticketId)
    .then((response) => {
      return response.data;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}
async function getChatManager(ticketId) {
  const token = localStorage.getItem("token");
  if (token) {
    setAuthToken(token);
  }
  return api
    .get(APIURL + "/API/manager/chat/" + ticketId)
    .then((response) => {
      return response.data;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}

export { addMessageAPI, getChatClient, getChatExpert, getChatManager };
