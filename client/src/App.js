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

function App() {
  return (
      <div style={{backgroundColor: '#222222'}}>
      <Router>
        <Routes>
          <Route path='/' element= {<HomePage/>}/>
          <Route path='/products' element= {<ProductsPage/>}/>
          <Route path='/productid' element= {<ProductIdPage/>}/>
          <Route path='/userinfo' element= {<ProfileInfoPage/>}/>
          <Route path='/usercreate' element= {<ProfileCreatePage/>}/>
          <Route path='/userupdate' element= {<ProfileUpdatePage/>}/>
          <Route path="*" element={<RedirectToHome/>} />
        </Routes>
      </Router>
      </div>
  );
}

export default App;
