import apiClient from "../../app/axiosClient";
import {notesUrl} from "../util/apiEndpoints";
import {NotePayload} from "../../types/api/NotePayload";

const put = async (note: string): Promise<string> => {
    const url = notesUrl();

    const requestBody: NotePayload = {
        secretNote: note,
    };

    const response = await apiClient.put<NotePayload>(url, requestBody);
    return response.data.secretNote;
};

const get = async (): Promise<string> => {
    const url = notesUrl();

    const response = await apiClient.get<NotePayload>(url);
    return response.data.secretNote;
};

export const noteService = {
    putNote: put,
    getNote: get,
};