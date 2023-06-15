import './App.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import HomePage from "./Components/HomePage/HomePage";
import {BrowserRouter as Router, Routes, Route} from "react-router-dom";
import ProductsPage from "./Components/Products/ProductsPage/ProductsPage";
import ProductIdPage from "./Components/Products/ProductIdPage/ProductIdPage";
import ProfileInfoPage from "./Components/Users/ProfileInfoPage/ProfileInfoPage";
import RedirectToHome from "./Components/HomePage/RedirectToHome";
import ProfileCreatePage from "./Components/Users/ProfileCreatePage/ProfileCreatePage";
import ProfileUpdatePage from "./Components/Users/ProfileUpdatePage/ProfileUpdatePage";
import LoginPage from "./Components/Users/LoginPage/LoginPage";
import {useState} from "react";
import AboutUsPage from "./Components/Public/AboutUsPage/AboutUsPage";
import TicketListPage from "./Components/Ticketing/TicketListPage/TicketListPage";
import TicketCreatePage from "./Components/Ticketing/TicketCreatePage/TicketCreatePage";
import TicketChatPage from "./Components/Ticketing/TicketChatPage/TicketChatPage";
import ExpertCreatePage from './Components/Admin/ExpertCreatePage/ExpertCreatePage';
import TicketHistoryPage from './Components/Ticketing/TicketHistoryPage/TicketHistoryPage';

function App() {
    const [loggedIn, setLoggedIn] = useState(true)
  return (
      <Router>
        <Routes>
          <Route path='/' element= {<HomePage loggedIn={loggedIn}/>}/>
          <Route path='/login' element= {<LoginPage loggedIn={loggedIn}/>}/>
          <Route path='/signup' element= {<ProfileCreatePage loggedIn={loggedIn}/>}/>
          <Route path='/tickets' element= {<TicketListPage loggedIn={loggedIn}/>}/>
          <Route path='/tickets/:id' element= {<TicketChatPage loggedIn={loggedIn}/>}/>
          <Route path='/newticket' element= {<TicketCreatePage loggedIn={loggedIn}/>}/>
          <Route path='/aboutus' element= {<AboutUsPage loggedIn={loggedIn}/>}/>
          <Route path='/expertcreate' element= {<ExpertCreatePage loggedIn={loggedIn}/>}/>
          <Route path='/tickethistory' element= {<TicketHistoryPage loggedIn={loggedIn}/>}/>
          <Route path='/products' element= {<ProductsPage/>}/>
          <Route path='/productid' element= {<ProductIdPage/>}/>
          <Route path='/profileinfo' element= {<ProfileInfoPage loggedIn={loggedIn}/>}/>
          <Route path='/usercreate' element= {<ProfileCreatePage/>}/>
          <Route path='/profileupdate' element= {<ProfileUpdatePage loggedIn={loggedIn}/>}/>
          <Route path="*" element={<RedirectToHome/>} />
        </Routes>
      </Router>
  );
}

export default App;
