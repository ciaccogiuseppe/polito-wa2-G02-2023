import { APIURL } from "./API_URL";
import { api } from "../App";

async function addItemAPI(itemPayload) {
  return api
    .post(APIURL + "/API/vendor/products/items/", itemPayload)
    .then((response) => {
      return response;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}
async function assignItemAPI(itemPayload) {
  return api
    .put(APIURL + "/API/client/products/items/register", itemPayload)
    .then((response) => {
      return response;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}
async function getItemAPI(itemPayload) {
  return api
    .get(
      APIURL +
        "/API/public/products/" +
        itemPayload.productId +
        "/items/" +
        itemPayload.serialNum
    )
    .then((response) => {
      return response;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}

async function getAllItemsAPI(itemPayload) {
  return api
    .get(APIURL + "/API/client/profiles/items")
    .then((response) => {
      return response.data;
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err.response.data.detail);
    });
}

export { addItemAPI, getItemAPI, getAllItemsAPI, assignItemAPI };
