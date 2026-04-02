import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { jwtDecode } from 'jwt-decode';
import { authService } from "./authService";

export interface UserProfile {
    id?: string;
}

interface AuthState {
    token: string | null;
    isAuthenticated: boolean;
    userProfile: UserProfile | null;
    operationLoading: boolean;
    operationError: string | null;
}

const initialState: AuthState = {
    token: null,
    isAuthenticated: false,
    userProfile: null,
    operationLoading: false,
    operationError: null,
};

export const register = createAsyncThunk<
    string,
    { username: string, password: string },
    { state: any, rejectValue: string }
>(
    'auth/register',
    async (params, thunkAPI) => {
        try {
            const token = await authService.register(params.username, params.password);
            localStorage.setItem('jwt_token', token);
            return token;
        } catch (err: any) {
            return thunkAPI.rejectWithValue(err.message || `Failed to register with username: ${params.username}`);
        }
    }
);

export const login = createAsyncThunk<
    string,
    { username: string, password: string },
    { state: any, rejectValue: string }
>(
    'auth/login',
    async (params, thunkAPI) => {
        try {
            const token = await authService.login(params.username, params.password);
            localStorage.setItem('jwt_token', token);
            return token;
        } catch (err: any) {
            return thunkAPI.rejectWithValue(err.message || `Failed to login with username: ${params.username}`);
        }
    }
);

const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        setCredentials: (
            state,
            action: PayloadAction<{ token: string }>
        ) => {
            const decodedToken = jwtDecode<UserProfile>(action.payload.token);
            state.token = action.payload.token;
            state.userProfile = decodedToken;
            state.isAuthenticated = true;
        },
        logout: (state) => {
            state.token = null;
            state.userProfile = null;
            state.isAuthenticated = false;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(register.pending, (state) => {
                state.operationLoading = true;
                state.operationError = null;
            })
            .addCase(register.fulfilled, (state, action) => {
                state.operationLoading = false;
                state.token = action.payload;
                state.userProfile = jwtDecode<UserProfile>(action.payload);
                state.isAuthenticated = true;

            })
            .addCase(register.rejected, (state, action) => {
                state.operationLoading = false;
                state.operationError = action.payload || 'Error registering';
            })

            .addCase(login.pending, (state) => {
                state.operationLoading = true;
                state.operationError = null;
            })
            .addCase(login.fulfilled, (state, action) => {
                state.operationLoading = false;
                state.token = action.payload;
                state.userProfile = jwtDecode<UserProfile>(action.payload);
                state.isAuthenticated = true;
            })
            .addCase(login.rejected, (state, action) => {
                state.operationLoading = false;
                state.operationError = action.payload || 'Error logging in';
            });
    },
});

export const selectUserId = (state: { auth: AuthState }): string | undefined => {
    return state.auth.userProfile?.id;
};

export const logoutUser = () => (dispatch: any) => {
    localStorage.removeItem('jwt_token');
    dispatch(logout());
};

export const { setCredentials, logout } = authSlice.actions;
export default authSlice.reducer;