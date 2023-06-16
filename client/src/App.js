import './App.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import HomePage from "./Components/HomePage/HomePage";
import {BrowserRouter as Router, Routes, Route, useNavigate} from "react-router-dom";
import ProductsPage from "./Components/Products/ProductsPage/ProductsPage";
import ProductIdPage from "./Components/Products/ProductIdPage/ProductIdPage";
import ProfileInfoPage from "./Components/Users/ProfileInfoPage/ProfileInfoPage";
import RedirectToHome from "./Components/HomePage/RedirectToHome";
import ProfileCreatePage from "./Components/Users/ProfileCreatePage/ProfileCreatePage";
import ProfileUpdatePage from "./Components/Users/ProfileUpdatePage/ProfileUpdatePage";
import LoginPage from "./Components/Users/LoginPage/LoginPage";
import {useEffect, useState} from "react";
import AboutUsPage from "./Components/Public/AboutUsPage/AboutUsPage";
import TicketListPage from "./Components/Ticketing/TicketListPage/TicketListPage";
import TicketCreatePage from "./Components/Ticketing/TicketCreatePage/TicketCreatePage";
import TicketChatPage from "./Components/Ticketing/TicketChatPage/TicketChatPage";
import ExpertCreatePage from './Components/Admin/ExpertCreatePage/ExpertCreatePage';
import TicketHistoryPage from './Components/Ticketing/TicketHistoryPage/TicketHistoryPage';
import ProductCreatePage from "./Components/Products/ProductCreatePage/ProductCreatePage";
import jwtDecode from "jwt-decode";
import {getProfileInfo} from "./API/Login";

function App() {

  const [loggedIn, setLoggedIn] = useState(localStorage.token !== undefined)
  const [role, setRole] = useState(null)
  const [user, setUser] = useState(null)
  useEffect(() => {
    if(localStorage.token)
      getProfileInfo().then(response => setUser(response.data))
    if(loggedIn===false){
      setRole(null)
      setUser(null)
    }
  }, [loggedIn])

  function logout(){
    localStorage.removeItem("token")
    setLoggedIn(false)
    window.location.href = "/"
  }

  return (
      <Router>
        <Routes>
          <Route path='/' element= {<HomePage loggedIn={loggedIn} logout={logout}/>}/>

          {!loggedIn && <>
          <Route path='/login' element= {<LoginPage loggedIn={loggedIn} setLoggedIn={setLoggedIn} logout={logout}/>}/>
          <Route path='/signup' element= {<ProfileCreatePage loggedIn={loggedIn} logout={logout}/>}/>
          </>}

          <Route path='/aboutus' element= {<AboutUsPage loggedIn={loggedIn} logout={logout}/>}/>

          {(loggedIn && user!==null) && <>
            <Route path='/tickets' element= {<TicketListPage loggedIn={loggedIn} logout={logout}/>}/>
            <Route path='/tickets/:id' element= {<TicketChatPage loggedIn={loggedIn} logout={logout}/>}/>
            <Route path='/newticket' element= {<TicketCreatePage loggedIn={loggedIn} logout={logout}/>}/>
            <Route path='/expertcreate' element= {<ExpertCreatePage loggedIn={loggedIn} logout={logout}/>}/>
            <Route path='/tickethistory' element= {<TicketHistoryPage loggedIn={loggedIn} logout={logout}/>}/>
            <Route path='/products' element= {<ProductsPage loggedIn={loggedIn} logout={logout}/>}/>
            <Route path='/newproduct' element= {<ProductCreatePage loggedIn={loggedIn} logout={logout}/>}/>
            <Route path='/productid' element= {<ProductIdPage/>}/>
            <Route path='/profileinfo' element= {<ProfileInfoPage loggedIn={loggedIn} user={user} logout={logout}/>}/>
            <Route path='/usercreate' element= {<ProfileCreatePage/>}/>
            <Route path='/profileupdate' element= {<ProfileUpdatePage loggedIn={loggedIn} logout={logout}/>}/>

          </>}
          <Route path="*" element={<RedirectToHome/>} />
        </Routes>
      </Router>
  );
}

export default App;
