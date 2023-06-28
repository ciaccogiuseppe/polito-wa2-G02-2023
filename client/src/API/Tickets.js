import { api } from "../App";
import { APIURL } from "./API_URL";
import { setAuthToken } from "./AuthCommon";

async function addTicketAPI(ticketPayload) {
  return api
    .post(APIURL + "/API/client/ticketing/", ticketPayload)
    .then((response) => {
      return response;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}

async function getAllTicketsClient(ticketFilters) {
  const filters =
    "" +
    (ticketFilters.clientEmail
      ? `&clientEmail=${ticketFilters.clientEmail}`
      : "") +
    (ticketFilters.expertEmail
      ? `&expertEmail=${ticketFilters.expertEmail}`
      : "") +
    (ticketFilters.status && ticketFilters.status.length > 0
      ? ticketFilters.status.map((s) => `&status=${s}`).reduce((a, b) => a + b)
      : "") +
    (ticketFilters.productId ? `&productId=${ticketFilters.productId}` : "") +
    (ticketFilters.minPriority !== undefined
      ? `&minPriority=${ticketFilters.minPriority}`
      : "") +
    (ticketFilters.maxPriority !== undefined
      ? `&maxPriority=${ticketFilters.maxPriority}`
      : "") +
    (ticketFilters.minTimestamp
      ? `&createdAfter=${ticketFilters.minTimestamp}`
      : "") +
    (ticketFilters.maxTimestamp
      ? `&createdBefore=${ticketFilters.maxTimestamp}`
      : "");
  const url = APIURL + "/API/client/ticketing/filter?" + filters;
  const token = localStorage.getItem("token");
  if (token) {
    setAuthToken(token);
  }

  return api
    .get(url)
    .then((response) => {
      //get token from response
      //console.log(response.data.accessToken)
      return response.data;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}

async function getTicketClientAPI(ticketID) {
  const url = APIURL + "/API/client/ticketing/" + ticketID;
  const token = localStorage.getItem("token");
  if (token) {
    setAuthToken(token);
  }

  return api
    .get(url)
    .then((response) => {
      //get token from response
      //console.log(response.data.accessToken)
      return response.data;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}

async function getTicketExpertAPI(ticketID) {
  const url = APIURL + "/API/expert/ticketing/" + ticketID;
  const token = localStorage.getItem("token");
  if (token) {
    setAuthToken(token);
  }

  return api
    .get(url)
    .then((response) => {
      //get token from response
      //console.log(response.data.accessToken)
      return response.data;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}

async function getTicketManagerAPI(ticketID) {
  const url = APIURL + "/API/manager/ticketing/" + ticketID;
  const token = localStorage.getItem("token");
  if (token) {
    setAuthToken(token);
  }

  return api
    .get(url)
    .then((response) => {
      //get token from response
      //console.log(response.data.accessToken)
      return response.data;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}

async function getAllTicketsExpert(ticketFilters) {
  const filters =
    "" +
    (ticketFilters.clientEmail
      ? `&clientEmail=${ticketFilters.clientEmail}`
      : "") +
    (ticketFilters.expertEmail
      ? `&expertEmail=${ticketFilters.expertEmail}`
      : "") +
    (ticketFilters.status && ticketFilters.status.length > 0
      ? ticketFilters.status.map((s) => `&status=${s}`).reduce((a, b) => a + b)
      : "") +
    (ticketFilters.productId ? `&productId=${ticketFilters.productId}` : "") +
    (ticketFilters.minPriority !== undefined
      ? `&minPriority=${ticketFilters.minPriority}`
      : "") +
    (ticketFilters.maxPriority !== undefined
      ? `&maxPriority=${ticketFilters.maxPriority}`
      : "") +
    (ticketFilters.minTimestamp
      ? `&createdAfter=${ticketFilters.minTimestamp}`
      : "") +
    (ticketFilters.maxTimestamp
      ? `&createdBefore=${ticketFilters.maxTimestamp}`
      : "");
  const url = APIURL + "/API/expert/ticketing/filter?" + filters;
  const token = localStorage.getItem("token");
  if (token) {
    setAuthToken(token);
  }

  return api
    .get(url)
    .then((response) => {
      //get token from response
      //console.log(response.data.accessToken)
      return response.data;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}

async function getAllTicketsManager(ticketFilters) {
  const filters =
    "" +
    (ticketFilters.clientEmail
      ? `&clientEmail=${ticketFilters.clientEmail}`
      : "") +
    (ticketFilters.expertEmail
      ? `&expertEmail=${ticketFilters.expertEmail}`
      : "") +
    (ticketFilters.status && ticketFilters.status.length > 0
      ? ticketFilters.status.map((s) => `&status=${s}`).reduce((a, b) => a + b)
      : "") +
    (ticketFilters.productId ? `&productId=${ticketFilters.productId}` : "") +
    (ticketFilters.minPriority !== undefined
      ? `&minPriority=${ticketFilters.minPriority}`
      : "") +
    (ticketFilters.maxPriority !== undefined
      ? `&maxPriority=${ticketFilters.maxPriority}`
      : "") +
    (ticketFilters.minTimestamp
      ? `&createdAfter=${ticketFilters.minTimestamp}`
      : "") +
    (ticketFilters.maxTimestamp
      ? `&createdBefore=${ticketFilters.maxTimestamp}`
      : "");
  const url = APIURL + "/API/manager/ticketing/filter?" + filters;
  const token = localStorage.getItem("token");
  if (token) {
    setAuthToken(token);
  }

  return api
    .get(url)
    .then((response) => {
      //get token from response
      //console.log(response.data.accessToken)
      return response.data;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}

async function updateTicketClientAPI(updateTicket) {
  const token = localStorage.getItem("token");
  if (token) {
    setAuthToken(token);
  }
  return api
    .put(APIURL + "/API/client/ticketing/update", updateTicket)
    .then((response) => {
      return response;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}
async function updateTicketManagerAPI(updateTicket) {
  const token = localStorage.getItem("token");
  if (token) {
    setAuthToken(token);
  }
  return api
    .put(APIURL + "/API/manager/ticketing/update", updateTicket)
    .then((response) => {
      return response;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}
async function assignTicketManagerAPI(assignTicket) {
  const token = localStorage.getItem("token");
  if (token) {
    setAuthToken(token);
  }
  return api
    .put(APIURL + "/API/manager/ticketing/assign", assignTicket)
    .then((response) => {
      return response;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}
async function updateTicketExpertAPI(updateTicket) {
  const token = localStorage.getItem("token");
  if (token) {
    setAuthToken(token);
  }
  return api
    .put(APIURL + "/API/expert/ticketing/update", updateTicket)
    .then((response) => {
      return response;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}

export {
  addTicketAPI,
  assignTicketManagerAPI,
  updateTicketClientAPI,
  updateTicketManagerAPI,
  updateTicketExpertAPI,
  getAllTicketsClient,
  getTicketExpertAPI,
  getTicketManagerAPI,
  getAllTicketsExpert,
  getAllTicketsManager,
  getTicketClientAPI,
};
