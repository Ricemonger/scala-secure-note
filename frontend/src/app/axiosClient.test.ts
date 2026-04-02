import MockAdapter from 'axios-mock-adapter';
import apiClient, {setupAxiosInterceptors} from './axiosClient';
import {logger} from './logger';
import {logoutUser} from '../features/authentication/authSlice';

jest.mock('./logger');

jest.mock('../features/authentication/authSlice', () => ({
    logoutUser: jest.fn(() => ({ type: 'mocked_logout_action' })),
}));

describe('axiosClient', () => {
    let axiosMockAdapter: MockAdapter;
    let reduxMockStore: any;

    beforeAll(() => {
        axiosMockAdapter = new MockAdapter(apiClient);
    });

    afterEach(() => {
        axiosMockAdapter.reset();
        jest.clearAllMocks();

        (apiClient.interceptors.request as any).handlers = [];
        (apiClient.interceptors.response as any).handlers = [];
    });

    afterAll(() => {
        axiosMockAdapter.restore();
    });

    it('setupAxiosInterceptors should attach Authorization header when token exists', async () => {
        const token = 'token';
        reduxMockStore = {
            getState: jest.fn().mockReturnValue({
                auth: {token: token},
            }),
            dispatch: jest.fn(),
            subscribe: jest.fn(),
            replaceReducer: jest.fn(),
        };

        setupAxiosInterceptors(reduxMockStore);

        axiosMockAdapter.onGet('/test').reply(200, {});
        await apiClient.get('/test');

        expect(axiosMockAdapter.history.get[0].headers?.Authorization).toBe(`Bearer ${token}`);
    });

    it('setupAxiosInterceptors should not attach Authorization header when token doesnt exist', async () => {
        reduxMockStore = {
            getState: jest.fn().mockReturnValue({
                auth: {token: null},
            }),
            dispatch: jest.fn(),
            subscribe: jest.fn(),
            replaceReducer: jest.fn(),
        };

        setupAxiosInterceptors(reduxMockStore);

        axiosMockAdapter.onGet('/test').reply(200, {});
        await apiClient.get('/test');

        expect(axiosMockAdapter.history.get[0].headers?.Authorization).toBeUndefined();
    });

    it('setupAxiosInterceptors should dispatch logoutUser on 401 Unauthorized response', async () => {
        reduxMockStore = {
            getState: jest.fn().mockReturnValue({auth: {}}),
            dispatch: jest.fn()
        };

        setupAxiosInterceptors(reduxMockStore);

        const errorMessage = {message: 'Token expired'};
        axiosMockAdapter.onGet('/test').reply(401, errorMessage);

        await expect(apiClient.get('/test')).rejects.toThrow(
            `Unexpected Error Occurred: {"message":"${errorMessage.message}"}`
        );

        expect(logger.warn).toHaveBeenCalledWith('Token expired or unauthorized access (401). Logging out.');

        expect(logoutUser).toHaveBeenCalled();
        expect(reduxMockStore.dispatch).toHaveBeenCalledWith({ type: 'mocked_logout_action' });
    });

    it('setupAxiosInterceptors should handle API error from response', async () => {
        reduxMockStore = {getState: jest.fn().mockReturnValue({auth: {}})};

        setupAxiosInterceptors(reduxMockStore);

        const errorMessage = {message: 'Error message'};

        axiosMockAdapter.onGet('/test').reply(400, errorMessage);

        await expect(apiClient.get('/test')).rejects.toThrow(
            `Unexpected Error Occurred: {"message":"${errorMessage.message}"}`
        );

        expect(logger.error).toHaveBeenCalledWith(
            expect.stringContaining(`Unexpected Error Occurred: {"message":"${errorMessage.message}"}`),
            expect.objectContaining({url: '/test'})
        );
    });

    it('setupAxiosInterceptors should handle Network Errors from request', async () => {
        reduxMockStore = {getState: jest.fn().mockReturnValue({auth: {}})};

        setupAxiosInterceptors(reduxMockStore);

        axiosMockAdapter.onGet('/test').networkError();

        await expect(apiClient.get('/test')).rejects.toThrow(
            'Unexpected Error Occurred: Network Error'
        );

        expect(logger.error).toHaveBeenCalledWith(
            expect.stringContaining('Unexpected Error Occurred: Network Error'),
            expect.objectContaining({url: '/test'})
        );
    });
});