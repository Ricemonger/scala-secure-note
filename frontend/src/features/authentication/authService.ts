import apiClient from "../../app/axiosClient";
import {loginUrl, registerUrl} from "../util/apiEndpoints";
import {AuthPayload} from "../../types/api/AuthPayload";
import {JwtResponse} from "../../types/api/JwtResponse";

const register = async (username: string, password: string): Promise<string> => {
    const url = registerUrl();

    const requestBody: AuthPayload = {
        username: username,
        password: password,
    };

    const response = await apiClient.post<JwtResponse>(url, requestBody);
    return response.data.jwt;
};

const login = async (username: string, password: string): Promise<string> => {
    const url = loginUrl();

    const requestBody: AuthPayload = {
        username: username,
        password: password,
    };

    const response = await apiClient.post<JwtResponse>(url, requestBody);
    return response.data.jwt;
};

export const authService = {
    register: register,
    login: login,
};