import noteReducer, {
    putNote,
    getNote,
    clearNote,
    clearOperationError
} from './noteSlice';
import { noteService } from './noteService';

jest.mock('./noteService');

describe('noteSlice', () => {
    const initialState = {
        note: "",
        loading: false,
        error: null,
        operationLoading: false,
        operationError: null,
    };

    const mockService = noteService as jest.Mocked<typeof noteService>;

    beforeEach(() => {
        jest.clearAllMocks();
    });

    describe('Thunk: putNote', () => {
        it('should call service and update state on fulfillment', async () => {
            const mockNoteResponse = 'Saved Note Content';
            mockService.putNote.mockResolvedValue(mockNoteResponse);

            const params = { note: 'New Note Content' };
            const dispatch = jest.fn();
            const getState = jest.fn();

            const result = await putNote(params)(dispatch, getState, undefined);

            expect(result.payload).toBe(mockNoteResponse);
            expect(mockService.putNote).toHaveBeenCalledWith(params.note);
        });

        it('should reject if service throws error', async () => {
            const errorMessage = 'Failed to put note';
            mockService.putNote.mockRejectedValue(new Error(errorMessage));

            const params = { note: 'Failed Note' };
            const dispatch = jest.fn();
            const getState = jest.fn();

            const result = await putNote(params)(dispatch, getState, undefined);

            expect(result.meta.requestStatus).toBe('rejected');
            expect(result.payload).toBe(errorMessage);
        });
    });

    describe('Thunk: getNote', () => {
        it('should call service and update state on fulfillment', async () => {
            const mockFetchedNote = 'Fetched Note Content';
            mockService.getNote.mockResolvedValue(mockFetchedNote);

            const dispatch = jest.fn();
            const getState = jest.fn();

            const result = await getNote({})(dispatch, getState, undefined);

            expect(result.payload).toBe(mockFetchedNote);
            expect(mockService.getNote).toHaveBeenCalled();
        });

        it('should reject if service throws error', async () => {
            const errorMessage = 'Failed to get note';
            mockService.getNote.mockRejectedValue(new Error(errorMessage));

            const dispatch = jest.fn();
            const getState = jest.fn();

            const result = await getNote({})(dispatch, getState, undefined);

            expect(result.meta.requestStatus).toBe('rejected');
            expect(result.payload).toBe(errorMessage);
        });
    });

    describe('Reducer: Standard Actions', () => {
        it('should handle clearNote', () => {
            const state = {
                ...initialState,
                note: 'Some existing note',
                error: 'Previous error',
                loading: true
            };
            const updated = noteReducer(state, clearNote());

            expect(updated.note).toBe("");
            expect(updated.error).toBeNull();
            expect(updated.loading).toBe(false);
        });

        it('should handle clearOperationError', () => {
            const state = {
                ...initialState,
                operationError: 'Existing operation error'
            };
            const updated = noteReducer(state, clearOperationError());

            expect(updated.operationError).toBeNull();
        });
    });

    describe('Reducer: putNote lifecycle', () => {
        it('pending should set operationLoading and clear error', () => {
            const action = { type: putNote.pending.type };
            const state = noteReducer({ ...initialState, operationError: 'old error' }, action);

            expect(state.operationLoading).toBe(true);
            expect(state.operationError).toBeNull();
        });

        it('fulfilled should update note and stop operationLoading', () => {
            const newNote = 'Successfully updated note';
            const action = { type: putNote.fulfilled.type, payload: newNote };
            const state = noteReducer({ ...initialState, operationLoading: true }, action);

            expect(state.operationLoading).toBe(false);
            expect(state.note).toBe(newNote);
        });

        it('rejected should set operationError and stop operationLoading', () => {
            const action = { type: putNote.rejected.type, payload: 'Put Error' };
            const state = noteReducer({ ...initialState, operationLoading: true }, action);

            expect(state.operationLoading).toBe(false);
            expect(state.operationError).toBe('Put Error');
        });
    });

    describe('Reducer: getNote lifecycle', () => {
        it('pending should set loading and clear error', () => {
            const action = { type: getNote.pending.type };
            const state = noteReducer({ ...initialState, error: 'old error' }, action);

            expect(state.loading).toBe(true);
            expect(state.error).toBeNull();
        });

        it('fulfilled should update note and stop loading', () => {
            const fetchedNote = 'Successfully fetched note';
            const action = { type: getNote.fulfilled.type, payload: fetchedNote };
            const state = noteReducer({ ...initialState, loading: true }, action);

            expect(state.loading).toBe(false);
            expect(state.note).toBe(fetchedNote);
        });

        it('rejected should set error and stop loading', () => {
            const action = { type: getNote.rejected.type, payload: 'Fetch Error' };
            const state = noteReducer({ ...initialState, loading: true }, action);

            expect(state.loading).toBe(false);
            expect(state.error).toBe('Fetch Error');
        });
    });
});