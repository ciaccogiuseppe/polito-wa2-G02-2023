import "./App.css";
import "bootstrap/dist/css/bootstrap.min.css";
import HomePage from "./Components/HomePage/HomePage";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import ProductsPage from "./Components/Products/ProductsPage/ProductsPage";
import ProductIdPage from "./Components/Products/ProductIdPage/ProductIdPage";
import ProfileInfoPage from "./Components/Users/ProfileInfoPage/ProfileInfoPage";
import RedirectToHome from "./Components/HomePage/RedirectToHome";
import ProfileCreatePage from "./Components/Users/ProfileCreatePage/ProfileCreatePage";
import ProfileUpdatePage from "./Components/Users/ProfileUpdatePage/ProfileUpdatePage";
import LoginPage from "./Components/Users/LoginPage/LoginPage";
import { useEffect, useState } from "react";
import AboutUsPage from "./Components/Public/AboutUsPage/AboutUsPage";
import TicketListPage from "./Components/Ticketing/TicketListPage/TicketListPage";
import TicketCreatePage from "./Components/Ticketing/TicketCreatePage/TicketCreatePage";
import TicketChatPage from "./Components/Ticketing/TicketChatPage/TicketChatPage";
import ExpertCreatePage from "./Components/Admin/ExpertCreatePage/ExpertCreatePage";
import TicketHistoryPage from "./Components/Ticketing/TicketHistoryPage/TicketHistoryPage";
import ProductCreatePage from "./Components/Products/ProductCreatePage/ProductCreatePage";
import jwtDecode from "jwt-decode";
import { getProfileInfo } from "./API/Auth";
import { APIURL } from "./API/API_URL";
import axios from "axios";
import { setAuthToken } from "./API/AuthCommon";
import BrandsPage from "./Components/Brands/BrandsPage/BrandsPage";
import BrandCreatePage from "./Components/Brands/BrandCreatePage/BrandCreatePage";
import VendorCreatePage from "./Components/Admin/VendorCreatePage/VendorCreatePage";
import ProfileInfoPageExpert from "./Components/Users/ProfileInfoPageByExpert/ProfileInfoPageExpert";
import UsersCreatePage from "./Components/Admin/UsersCreatePage/UsersCreatePage";
import ClientProductsPage from "./Components/Products/ClientProductsPage/ClientProductsPage";
import ProductRegisterPage from "./Components/Products/ProductRegisterPage/ProductRegisterPage";
import PasswordResetPage from "./Components/Users/PasswordResetPage/PasswordResetPage";
import PasswordResetTriggerPage from "./Components/Users/PasswordResetPage/PasswordResetTriggerPage";

export const api = axios.create({
  baseURL: APIURL,
});

function App() {
  const [loggedIn, setLoggedIn] = useState(localStorage.token !== undefined);
  const [tempDisableRedirect, setTempDisableRedirect] = useState(true);
  const [user, setUser] = useState(null);

  useEffect(() => {
    // Add a request interceptor to attach the access token to all outgoing requests
    const requestInterceptor = api.interceptors.request.use(
      (config) => {
        const accessToken = localStorage.getItem("token");
        if (accessToken) {
          config.headers.Authorization = `Bearer ${accessToken}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Add a response interceptor to handle token refresh
    const responseInterceptor = api.interceptors.response.use(
      (response) => response,
      (error) => {
        const originalRequest = error.config;
        const refreshToken = localStorage.getItem("refreshToken");

        // If the error response status is 401 and the original request has not already been retried
        if (
          error.response.status === 401 &&
          loggedIn &&
          !originalRequest._retry
        ) {
          originalRequest._retry = true;
          setAuthToken("");
          return axios
            .post(APIURL + "/API/refreshtoken", {
              refreshToken: refreshToken,
            })
            .then((res) => {
              if (res.status === 200) {
                const newAccessToken = res.data.token;
                localStorage.setItem("token", newAccessToken);
                localStorage.setItem("refreshToken", res.data.refreshToken);
                originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                return axios(originalRequest);
              }
            })
            .catch((error) => {
              // Handle token refresh failure (e.g., logout user)
              logout();
              console.log("Failed to refresh access token: ", error);
            });
        }

        return Promise.reject(error);
      }
    );

    /*const int = setInterval(() => {
        const refreshToken = localStorage.getItem('refreshToken');
        axios
            .post(APIURL+"/API/refreshtoken", {
                refreshToken: refreshToken,
            })
            .then((res) => {
                if (res.status === 200) {
                    const newAccessToken = res.data.token;
                    localStorage.setItem('token', newAccessToken);
                    localStorage.setItem('refreshToken', res.data.refreshToken);
                }
            })
            .catch((error) => {
                // Handle token refresh failure (e.g., logout user)
                logout()
                console.log('Failed to refresh access token: ', error);
            });
    }, 120000)*/

    return () => {
      // Remove the request and response interceptors on component unmount
      api.interceptors.request.eject(requestInterceptor);
      api.interceptors.response.eject(responseInterceptor);
      //clearInterval(int)
    };
  }, [loggedIn]);

  useEffect(() => {
    if (localStorage.token) {
      let role;
      const data = jwtDecode(localStorage.token);
      if (data.realm_access.roles.includes("app_client")) {
        role = "CLIENT";
      } else if (data.realm_access.roles.includes("app_expert")) {
        role = "EXPERT";
      } else if (data.realm_access.roles.includes("app_manager")) {
        role = "MANAGER";
      } else if (data.realm_access.roles.includes("app_vendor")) {
        role = "VENDOR";
      }
      getProfileInfo()
        .then((response) => {
          setUser({ ...response.data, role: role });
          setTempDisableRedirect(false);
        })
        .catch((e) => {
          if (window.location.pathname !== "/") {
            window.location.href = "/";
          }
        });
    }

    if (loggedIn === false) {
      setUser(null);
      setTempDisableRedirect(false);
    }
  }, [loggedIn]);

  function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("refreshToken");
    setLoggedIn(false);
    window.location.href = "/";
  }

  return (
    <Router>
      <Routes>
        <Route
          path="/"
          element={<HomePage user={user} loggedIn={loggedIn} logout={logout} />}
        />

        {!loggedIn && (
          <>
            <Route
              path="/login"
              element={
                <LoginPage
                  user={user}
                  loggedIn={loggedIn}
                  setLoggedIn={setLoggedIn}
                  logout={logout}
                />
              }
            />
            <Route
              path="/passwordreset"
              element={
                <PasswordResetTriggerPage
                  user={user}
                  loggedIn={loggedIn}
                  setLoggedIn={setLoggedIn}
                  logout={logout}
                />
              }
            />
            <Route
              path="/resetpasswordapply/:id"
              element={
                <PasswordResetPage
                  user={user}
                  loggedIn={loggedIn}
                  setLoggedIn={setLoggedIn}
                  logout={logout}
                />
              }
            />
            <Route
              path="/signup"
              element={
                <ProfileCreatePage
                  user={user}
                  loggedIn={loggedIn}
                  logout={logout}
                />
              }
            />
          </>
        )}

        <Route
          path="/aboutus"
          element={
            <AboutUsPage user={user} loggedIn={loggedIn} logout={logout} />
          }
        />

        {loggedIn && user !== null && (
          <>
            <Route
              path="/tickets"
              element={
                <TicketListPage
                  user={user}
                  loggedIn={loggedIn}
                  logout={logout}
                />
              }
            />
            <Route
              path="/tickets/:id"
              element={
                <TicketChatPage
                  user={user}
                  loggedIn={loggedIn}
                  logout={logout}
                />
              }
            />
            {user.role === "CLIENT" && (
              <>
                <Route
                  path="/newticket"
                  element={
                    <TicketCreatePage
                      user={user}
                      loggedIn={loggedIn}
                      logout={logout}
                    />
                  }
                />
                <Route
                  path="/products"
                  element={
                    <ClientProductsPage
                      user={user}
                      loggedIn={loggedIn}
                      logout={logout}
                    />
                  }
                />
                <Route
                  path="/productregister"
                  element={
                    <ProductRegisterPage
                      user={user}
                      loggedIn={loggedIn}
                      logout={logout}
                    />
                  }
                />
              </>
            )}
            {(user.role === "EXPERT" || user.role === "MANAGER") && (
              <Route
                path="/profiledata/:email"
                element={
                  <ProfileInfoPageExpert
                    user={user}
                    loggedIn={loggedIn}
                    logout={logout}
                  />
                }
              />
            )}
            {user.role === "MANAGER" && (
              <>
                <Route
                  path="/userscreate"
                  element={
                    <UsersCreatePage
                      user={user}
                      loggedIn={loggedIn}
                      logout={logout}
                    />
                  }
                />
                <Route
                  path="/expertcreate"
                  element={
                    <ExpertCreatePage
                      user={user}
                      loggedIn={loggedIn}
                      logout={logout}
                    />
                  }
                />
                <Route
                  path="/vendorcreate"
                  element={
                    <VendorCreatePage
                      user={user}
                      loggedIn={loggedIn}
                      logout={logout}
                    />
                  }
                />
                <Route
                  path="/tickethistory"
                  element={
                    <TicketHistoryPage
                      user={user}
                      loggedIn={loggedIn}
                      logout={logout}
                    />
                  }
                />
                <Route
                  path="/products"
                  element={
                    <ProductsPage
                      user={user}
                      loggedIn={loggedIn}
                      logout={logout}
                    />
                  }
                />
                <Route
                  path="/brands"
                  element={
                    <BrandsPage
                      user={user}
                      loggedIn={loggedIn}
                      logout={logout}
                    />
                  }
                />
                <Route
                  path="/newbrand"
                  element={
                    <BrandCreatePage
                      user={user}
                      loggedIn={loggedIn}
                      logout={logout}
                    />
                  }
                />
                <Route
                  path="/newproduct"
                  element={
                    <ProductCreatePage
                      user={user}
                      loggedIn={loggedIn}
                      logout={logout}
                    />
                  }
                />
              </>
            )}
            <Route path="/productid" element={<ProductIdPage />} />
            {user.role !== "VENDOR" && (
              <Route
                path="/profileinfo"
                element={
                  <ProfileInfoPage
                    loggedIn={loggedIn}
                    user={user}
                    logout={logout}
                  />
                }
              />
            )}
            <Route path="/usercreate" element={<ProfileCreatePage />} />
            <Route
              path="/profileupdate"
              element={
                <ProfileUpdatePage
                  user={user}
                  loggedIn={loggedIn}
                  logout={logout}
                />
              }
            />
          </>
        )}
        {!tempDisableRedirect && (
          <Route path="*" element={<RedirectToHome />} />
        )}
      </Routes>
    </Router>
  );
}

export default App;
