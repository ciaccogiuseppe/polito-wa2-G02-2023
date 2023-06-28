import { api } from "../App";
import { APIURL } from "./API_URL";
import { setAuthToken } from "./AuthCommon";

async function getTicketHistoryAPI(ticketHistoryFilters) {
  const filters =
    "" +
    (ticketHistoryFilters.ticketId
      ? `&ticketId=${ticketHistoryFilters.ticketId}`
      : "") +
    (ticketHistoryFilters.userEmail
      ? `&userEmail=${ticketHistoryFilters.userEmail}`
      : "") +
    (ticketHistoryFilters.currentExpertEmail
      ? `&currentExpertEmail=${ticketHistoryFilters.currentExpertEmail}`
      : "") +
    (ticketHistoryFilters.updatedAfter
      ? `&updatedAfter=${ticketHistoryFilters.updatedAfter}`
      : "") +
    (ticketHistoryFilters.updatedBefore
      ? `&updatedBefore=${ticketHistoryFilters.updatedBefore}`
      : "");
  const url = APIURL + "/API/manager/ticketing/history/filter?" + filters;
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

export { getTicketHistoryAPI };
