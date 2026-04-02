import MockAdapter from 'axios-mock-adapter';
import apiClient from '../../app/axiosClient';
import { noteService } from './noteService';
import { notesUrl } from '../util/apiEndpoints';

jest.mock("../util/apiEndpoints");

describe('noteService', () => {
    let axiosMockAdapter: MockAdapter;

    beforeAll(() => {
        axiosMockAdapter = new MockAdapter(apiClient);
    });

    afterEach(() => {
        axiosMockAdapter.reset();
        jest.clearAllMocks();
    });

    describe('putNote', () => {
        const MOCK_URL = 'http://gateway/api/notes';
        const notePayload = 'new note';
        const responseData = { secretNote: notePayload };

        it('should put and return note', async () => {
            (notesUrl as jest.Mock).mockReturnValue(MOCK_URL);

            axiosMockAdapter.onPut(MOCK_URL).reply(200, responseData);

            const result = await noteService.putNote(notePayload);

            expect(result).toBe(notePayload);
            expect(notesUrl).toHaveBeenCalledTimes(1);
            expect(axiosMockAdapter.history.put[0].url).toBe(MOCK_URL);
            expect(JSON.parse(axiosMockAdapter.history.put[0].data)).toEqual({
                secretNote: notePayload,
            });
        });
    });

    describe('getNote', () => {
        const MOCK_URL = 'http://gateway/api/notes';
        const retrievedNote = 'retrieved note';
        const responseData = { secretNote: retrievedNote };

        it('should get note', async () => {
            (notesUrl as jest.Mock).mockReturnValue(MOCK_URL);

            axiosMockAdapter.onGet(MOCK_URL).reply(200, responseData);

            const result = await noteService.getNote();

            expect(result).toBe(retrievedNote);
            expect(notesUrl).toHaveBeenCalledTimes(1);
            expect(axiosMockAdapter.history.get[0].url).toBe(MOCK_URL);
        });
    });
});