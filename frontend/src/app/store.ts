import {configureStore} from '@reduxjs/toolkit';
import authReducer from '../features/authentication/authSlice';
import noteReducer from '../features/notes/noteSlice';


export const store = configureStore({
    reducer: {
        auth: authReducer,
        notes: noteReducer,
    },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;