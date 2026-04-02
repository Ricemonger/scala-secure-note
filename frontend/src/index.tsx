import React from 'react';
import ReactDOM from 'react-dom/client';
import { Provider } from 'react-redux';
import App from './App';
import { setCredentials, logout } from "./features/authentication/authSlice";
import { store } from "./app/store";
import { setupAxiosInterceptors } from "./app/axiosClient";

setupAxiosInterceptors(store);

const root = ReactDOM.createRoot(document.getElementById('root') as HTMLElement);

const token = localStorage.getItem('jwt_token');

if (token) {
    try {
        store.dispatch(setCredentials({ token }));
    } catch (error) {
        console.error("Failed to restore session from token", error);
        store.dispatch(logout());
    }
}

root.render(
    <React.StrictMode>
        <Provider store={store}>
            <App />
        </Provider>
    </React.StrictMode>
);