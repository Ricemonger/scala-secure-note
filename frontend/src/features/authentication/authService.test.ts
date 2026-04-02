import MockAdapter from 'axios-mock-adapter';
import apiClient from '../../app/axiosClient';
import { authService } from './authService';

jest.mock("../util/apiEndpoints");

describe('authService', () => {
    let axiosMockAdapter: MockAdapter;
    let mockRegisterUrl: jest.Mock;
    let mockLoginUrl: jest.Mock;

    beforeAll(() => {
        jest.resetModules();

        const apiEndpointsMock = require('../util/apiEndpoints');
        mockRegisterUrl = apiEndpointsMock.registerUrl;
        mockLoginUrl = apiEndpointsMock.loginUrl;

        axiosMockAdapter = new MockAdapter(apiClient);
    });

    afterEach(() => {
        axiosMockAdapter.reset();
        jest.clearAllMocks();
    });

    describe('register', () => {
        const MOCK_REGISTER_URL = 'http://gateway/api/auth/register';
        const mockUsername = 'user';
        const mockPassword = 'password';
        const mockJwt = 'jwt';
        const responseData = { jwt: mockJwt };

        it('should register and return JWT', async () => {
            mockRegisterUrl.mockReturnValue(MOCK_REGISTER_URL);

            axiosMockAdapter.onPost(MOCK_REGISTER_URL).reply(200, responseData);

            const result = await authService.register(mockUsername, mockPassword);

            expect(result).toBe(mockJwt);
            expect(mockRegisterUrl).toHaveBeenCalledTimes(1);
            expect(axiosMockAdapter.history.post[0].url).toBe(MOCK_REGISTER_URL);
            expect(JSON.parse(axiosMockAdapter.history.post[0].data)).toEqual({
                username: mockUsername,
                password: mockPassword,
            });
        });
    });

    describe('login', () => {
        const MOCK_LOGIN_URL = 'http://gateway/api/auth/login';
        const mockUsername = 'user';
        const mockPassword = 'password';
        const mockJwt = 'jwt';
        const responseData = { jwt: mockJwt };

        it('should login and return JWT', async () => {
            mockLoginUrl.mockReturnValue(MOCK_LOGIN_URL);

            axiosMockAdapter.onPost(MOCK_LOGIN_URL).reply(200, responseData);

            const result = await authService.login(mockUsername, mockPassword);

            expect(result).toBe(mockJwt);
            expect(mockLoginUrl).toHaveBeenCalledTimes(1);
            expect(axiosMockAdapter.history.post[0].url).toBe(MOCK_LOGIN_URL);
            expect(JSON.parse(axiosMockAdapter.history.post[0].data)).toEqual({
                username: mockUsername,
                password: mockPassword,
            });
        });
    });
});