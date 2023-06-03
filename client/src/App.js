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

function App() {
    const [loggedIn, setLoggedIn] = useState(true)
  return (
      <Router>
        <Routes>
          <Route path='/' element= {<HomePage loggedIn={loggedIn}/>}/>
          <Route path='/login' element= {<LoginPage loggedIn={loggedIn}/>}/>
          <Route path='/signup' element= {<ProfileCreatePage loggedIn={loggedIn}/>}/>
          <Route path='/tickets' element= {<TicketListPage loggedIn={loggedIn}/>}/>
          <Route path='/aboutus' element= {<AboutUsPage loggedIn={loggedIn}/>}/>
          <Route path='/products' element= {<ProductsPage/>}/>
          <Route path='/productid' element= {<ProductIdPage/>}/>
          <Route path='/userinfo' element= {<ProfileInfoPage/>}/>
          <Route path='/usercreate' element= {<ProfileCreatePage/>}/>
          <Route path='/userupdate' element= {<ProfileUpdatePage/>}/>
          <Route path="*" element={<RedirectToHome/>} />
        </Routes>
      </Router>
  );
}

export default App;
