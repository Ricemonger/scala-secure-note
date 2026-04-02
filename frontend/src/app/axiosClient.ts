import axios, { AxiosError } from 'axios';
import { Store } from '@reduxjs/toolkit';
import { logger } from "./logger";
import { RootState } from "./store";
import { logoutUser } from "../features/authentication/authSlice";

const apiClient = axios.create({
    baseURL: process.env.REACT_APP_API_GATEWAY_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

export const setupAxiosInterceptors = (store: Store) => {
    apiClient.interceptors.request.use(
        (config) => {
            const state: RootState = store.getState();
            const token = state.auth.token;

            if (token) {
                config.headers.Authorization = `Bearer ${token}`;
            }
            return config;
        },
        (error) => {
            return Promise.reject(error);
        }
    );

    apiClient.interceptors.response.use(
        (response) => response,
        (error: AxiosError | Error) => {
            let finalMessage = 'Unexpected Error Occurred';

            if (axios.isAxiosError(error)) {
                if (error.response) {
                    if (error.response.status === 401) {
                        logger.warn('Token expired or unauthorized access (401). Logging out.');

                        store.dispatch(logoutUser() as any);
                    }

                    const rawData = error.response.data;
                    const stringifiedData = typeof rawData === 'object' ? JSON.stringify(rawData) : String(rawData);
                    finalMessage = `Unexpected Error Occurred: ${stringifiedData}`;

                } else if (error.request) {
                    finalMessage = 'Unexpected Error Occurred: Network Error (No response from server)';
                } else {
                    finalMessage = `Unexpected Error Occurred: ${error.message}`;
                }

            } else {
                finalMessage = `Unexpected Error Occurred: ${error.message}`;
            }

            logger.error(finalMessage, {
                originalError: error,
                url: axios.isAxiosError(error) ? error.config?.url : 'unknown'
            });

            return Promise.reject(new Error(finalMessage));
        }
    );
};

export default apiClient;