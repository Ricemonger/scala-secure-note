import {createAsyncThunk, createSlice} from '@reduxjs/toolkit';
import {noteService} from "./noteService";

interface NoteState {
    note: string;
    loading: boolean;
    error: string | null;
    operationLoading: boolean;
    operationError: string | null;
}

const initialState: NoteState = {
    note: "",
    loading: false,
    error: null,
    operationLoading: false,
    operationError: null,
};

export const putNote = createAsyncThunk<
    string,
    { note: string },
    { state: any, rejectValue: string }
>(
    'notes/putNote',
    async (params, thunkAPI) => {
        try {
            return await noteService.putNote(params.note);
        } catch (err: any) {
            return thunkAPI.rejectWithValue(err.message || `Failed to put note with params: ${params}`);
        }
    }
);

export const getNote = createAsyncThunk<
    string,
    {},
    { state: any, rejectValue: string }
>(
    'notes/getNote',
    async (params, thunkAPI) => {
        try {
            return await noteService.getNote();
        } catch (err: any) {
            return thunkAPI.rejectWithValue(err.message || `Failed to get note with params: ${params}`);
        }
    }
);

export const noteSlice = createSlice({
    name: 'notes',
    initialState,
    reducers: {
        clearNote: (state) => {
            state.note = "";
            state.error = null;
            state.loading = false;
        },
        clearOperationError: (state) => {
            state.operationError = null;
        }
    },
    extraReducers: (builder) => {
        builder

            .addCase(putNote.pending, (state) => {
                state.operationLoading = true;
                state.operationError = null;
            })
            .addCase(putNote.fulfilled, (state, action) => {
                state.operationLoading = false;
                state.note = action.payload;
            })
            .addCase(putNote.rejected, (state, action) => {
                state.operationLoading = false;
                state.operationError = action.payload || 'Error putting note';
            })

            .addCase(getNote.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(getNote.fulfilled, (state, action) => {
                state.loading = false;
                state.note = action.payload;
            })
            .addCase(getNote.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload || 'Error getting note';
            });
    },
});

export const {clearNote, clearOperationError} = noteSlice.actions;
export default noteSlice.reducer;