import React from 'react';
import ReactDOM from 'react-dom/client';
import {HelmetProvider} from "react-helmet-async";
import './index.css';
import reportWebVitals from './reportWebVitals';
import {Provider} from 'react-redux';
import store from "./store/store"
import {BrowserRouter} from "react-router-dom";
import App from "./App";

ReactDOM.createRoot(document.getElementById('root')).render(
    <BrowserRouter>
        <HelmetProvider>
                <Provider store={store}>
                    <App/>
                </Provider>
        </HelmetProvider>
    </BrowserRouter>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
