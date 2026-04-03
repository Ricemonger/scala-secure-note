export const constraintRules = {
    username: {
        regex: "^[a-zA-Z0-9._-]{4,20}$",
        message: "Username must be between 4 and 20 characters long and can only contain English letters, numbers and ._-"
    },
    password: {
        regex: "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{4,20}$",
        message: "Password must be between 4 and 20 characters long and can contain English letters, numbers and ! @ # $ % ^ & * ( ) _ + - = [ ] { } ; ' : \" \\ | , . < > / ?"
    }
};