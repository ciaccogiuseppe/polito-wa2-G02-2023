import './App.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import HomePage from "./Components/HomePage/HomePage";
import {BrowserRouter as Router, Routes, Route} from "react-router-dom";
import ProductsPage from "./Components/Products/ProductsPage/ProductsPage";
import ProductIdPage from "./Components/Products/ProductIdPage/ProductIdPage";
import UserInfoPage from "./Components/Users/UserInfoPage/UserInfoPage";
import RedirectToHome from "./Components/HomePage/RedirectToHome";

function App() {
  return (
      <div style={{backgroundColor: '#222222'}}>
      <Router>
        <Routes>
          <Route path='/' element= {<HomePage/>}/>
          <Route path='/products' element= {<ProductsPage/>}/>
          <Route path='/productid' element= {<ProductIdPage/>}/>
          <Route path='/userinfo' element= {<UserInfoPage/>}/>
          <Route path="*" element={<RedirectToHome/>} />
        </Routes>
      </Router>
      </div>
  );
}

export default App;
