import logo from './logo.svg';
import './App.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import HomePage from "./Components/HomePage/HomePage";
import {BrowserRouter as Router, Routes, Route, Navigate} from "react-router-dom";
import ProductsMainPage from "./Components/Products/ProductsMainPage/ProductsMainPage";
import UserInfoPage from "./Components/Users/UserInfoPage/UserInfoPage";

function App() {
  return (
      <>
      <Router>
        <Routes>
          <Route path='/' element= {<HomePage/>}/>
          <Route path='/products' element= {<ProductsMainPage/>}/>
          <Route path='/userinfo' element= {<UserInfoPage/>}/>
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </Router>
      </>
  );
}

export default App;
