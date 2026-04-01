
const host = process.env.REACT_APP_BACKEND_HOST!
const port = process.env.REACT_APP_BACKEND_PORT!

const BACKEND_BASE_URL = `http://${host}:${port}`;

export const registerUrl = (): string => {
    return BACKEND_BASE_URL + "/register";
};

export const loginUrl = (): string => {
    return BACKEND_BASE_URL + "/login";
};

export const notesUrl = (): string => {
    return BACKEND_BASE_URL + "/notes";
};