import authReducer, {login, logout, logoutUser, register, selectUserId, setCredentials, UserProfile} from './authSlice';
import {authService} from './authService';
import {jwtDecode} from 'jwt-decode';

jest.mock('./authService');
jest.mock('jwt-decode', () => ({
    jwtDecode: jest.fn(),
}));

describe('authSlice', () => {
    const initialState = {
        token: null,
        isAuthenticated: false,
        userProfile: null,
        operationLoading: false,
        operationError: null,
    };

    const mockService = authService as jest.Mocked<typeof authService>;
    const mockJwtDecode = jwtDecode as jest.MockedFunction<typeof jwtDecode>;

    beforeEach(() => {
        jest.clearAllMocks();
        Storage.prototype.setItem = jest.fn();
        Storage.prototype.removeItem = jest.fn();
    });

    describe('Thunk: register', () => {
        it('should call service, save token to localStorage, and update state on fulfillment', async () => {
            const mockToken = 'mock-jwt-token';
            mockService.register.mockResolvedValue(mockToken);

            const params = { username: 'testuser', password: 'password123' };
            const dispatch = jest.fn();
            const getState = jest.fn();

            const result = await register(params)(dispatch, getState, undefined);

            expect(result.payload).toBe(mockToken);
            expect(mockService.register).toHaveBeenCalledWith(params.username, params.password);
            expect(localStorage.setItem).toHaveBeenCalledWith('jwt_token', mockToken);
        });

        it('should reject if service throws error and not touch localStorage', async () => {
            const errorMessage = 'Registration failed';
            mockService.register.mockRejectedValue(new Error(errorMessage));

            const params = { username: 'testuser', password: 'password123' };
            const dispatch = jest.fn();
            const getState = jest.fn();

            const result = await register(params)(dispatch, getState, undefined);

            expect(result.meta.requestStatus).toBe('rejected');
            expect(result.payload).toBe(errorMessage);
            expect(localStorage.setItem).not.toHaveBeenCalled();
        });
    });

    describe('Thunk: login', () => {
        it('should call service, save token to localStorage, and update state on fulfillment', async () => {
            const mockToken = 'mock-jwt-token';
            mockService.login.mockResolvedValue(mockToken);

            const params = { username: 'testuser', password: 'password123' };
            const dispatch = jest.fn();
            const getState = jest.fn();

            const result = await login(params)(dispatch, getState, undefined);

            expect(result.payload).toBe(mockToken);
            expect(mockService.login).toHaveBeenCalledWith(params.username, params.password);
            expect(localStorage.setItem).toHaveBeenCalledWith('jwt_token', mockToken);
        });

        it('should reject if service throws error and not touch localStorage', async () => {
            const errorMessage = 'Login failed';
            mockService.login.mockRejectedValue(new Error(errorMessage));

            const params = { username: 'testuser', password: 'password123' };
            const dispatch = jest.fn();
            const getState = jest.fn();

            const result = await login(params)(dispatch, getState, undefined);

            expect(result.meta.requestStatus).toBe('rejected');
            expect(result.payload).toBe(errorMessage);
            expect(localStorage.setItem).not.toHaveBeenCalled();
        });
    });

    describe('Reducer: Standard Actions', () => {
        it('should handle setCredentials', () => {
            const mockToken = 'new-mock-token';
            const mockProfile: UserProfile = { id: 'user-123' };
            mockJwtDecode.mockReturnValue(mockProfile);

            const action = setCredentials({ token: mockToken });
            const state = authReducer(initialState, action);

            expect(mockJwtDecode).toHaveBeenCalledWith(mockToken);
            expect(state.token).toBe(mockToken);
            expect(state.userProfile).toEqual(mockProfile);
            expect(state.isAuthenticated).toBe(true);
        });

        it('should handle logout', () => {
            const activeState = {
                ...initialState,
                token: 'some-token',
                isAuthenticated: true,
                userProfile: { id: 'user-123' }
            };

            const state = authReducer(activeState, logout());

            expect(state.token).toBeNull();
            expect(state.userProfile).toBeNull();
            expect(state.isAuthenticated).toBe(false);
        });
    });

    describe('Reducer: register lifecycle', () => {
        it('pending should set operationLoading and clear error', () => {
            const state = authReducer(
                { ...initialState, operationError: 'old error' },
                { type: register.pending.type }
            );
            expect(state.operationLoading).toBe(true);
            expect(state.operationError).toBeNull();
        });

        it('fulfilled should set token, decode profile, and set auth status', () => {
            const mockToken = 'mock-register-token';
            const mockProfile: UserProfile = { id: 'user-999' };
            mockJwtDecode.mockReturnValue(mockProfile);

            const state = authReducer(
                { ...initialState, operationLoading: true },
                { type: register.fulfilled.type, payload: mockToken }
            );

            expect(state.operationLoading).toBe(false);
            expect(state.token).toBe(mockToken);
            expect(state.userProfile).toEqual(mockProfile);
            expect(state.isAuthenticated).toBe(true);
        });

        it('rejected should set operationError', () => {
            const state = authReducer(
                { ...initialState, operationLoading: true },
                { type: register.rejected.type, payload: 'Register Error' }
            );
            expect(state.operationLoading).toBe(false);
            expect(state.operationError).toBe('Register Error');
        });
    });

    describe('Reducer: login lifecycle', () => {
        it('pending should set operationLoading and clear error', () => {
            const state = authReducer(
                { ...initialState, operationError: 'old error' },
                { type: login.pending.type }
            );
            expect(state.operationLoading).toBe(true);
            expect(state.operationError).toBeNull();
        });

        it('fulfilled should set token, decode profile, and set auth status', () => {
            const mockToken = 'mock-login-token';
            const mockProfile: UserProfile = { id: 'user-777' };
            mockJwtDecode.mockReturnValue(mockProfile);

            const state = authReducer(
                { ...initialState, operationLoading: true },
                { type: login.fulfilled.type, payload: mockToken }
            );

            expect(state.operationLoading).toBe(false);
            expect(state.token).toBe(mockToken);
            expect(state.userProfile).toEqual(mockProfile);
            expect(state.isAuthenticated).toBe(true);
        });

        it('rejected should set operationError', () => {
            const state = authReducer(
                { ...initialState, operationLoading: true },
                { type: login.rejected.type, payload: 'Login Error' }
            );
            expect(state.operationLoading).toBe(false);
            expect(state.operationError).toBe('Login Error');
        });
    });

    describe('Selectors and Thunk Actions', () => {
        it('selectUserId should return the user ID if profile exists', () => {
            const state = {
                auth: { ...initialState, userProfile: { id: 'user-123' } }
            };
            expect(selectUserId(state)).toBe('user-123');
        });

        it('selectUserId should return undefined if profile does not exist', () => {
            const state = { auth: initialState };
            expect(selectUserId(state)).toBeUndefined();
        });

        it('logoutUser should remove token from localStorage and dispatch logout', () => {
            const dispatch = jest.fn();
            logoutUser()(dispatch);

            expect(localStorage.removeItem).toHaveBeenCalledWith('jwt_token');
            expect(dispatch).toHaveBeenCalledWith(logout());
        });
    });
});