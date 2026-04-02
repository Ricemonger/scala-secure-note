import React, {useMemo, useState} from 'react';
import {BrowserRouter, Navigate, Route, Routes} from 'react-router-dom';
import {useAppSelector} from "./app/hooks";
import {createAppTheme} from "./app/theme";
import {ThemeProvider} from "@mui/material/styles";
import {CssBaseline} from "@mui/material";
import {selectUserId} from "./features/authentication/authSlice";
import {MainLayout} from "./components/layout/MainLayout";
import {SecretNotePage} from "./components/layout/SecretNotePage";
import {LoginPage} from "./components/layout/LoginPage";
import {RegisterPage} from "./components/layout/RegisterPage";

const App: React.FC = () => {

    const [mode, setMode] = useState<'light' | 'dark'>(
        (localStorage.getItem('themeMode') as 'light' | 'dark') || 'light'
    );

    const theme = useMemo(() => createAppTheme(mode), [mode]);

    const onToggleTheme = () => {
        setMode((prevMode) => {
            const newMode = prevMode === 'light' ? 'dark' : 'light';
            localStorage.setItem('themeMode', newMode);
            return newMode;
        });
    };

    const userId = useAppSelector(selectUserId);

    const getDefaultPath = (userId: string | undefined): string => {
        if (userId) return '/note';
        return '/login';
    };

    return (
        <ThemeProvider theme={theme}>
            <CssBaseline/>
            <BrowserRouter>
                <Routes>
                    <Route path="/" element={<MainLayout onToggleTheme={onToggleTheme}/>}>
                        <Route index element={<Navigate to={getDefaultPath(userId)} replace/>}/>
                        <Route path="note" element={<SecretNotePage/>}/>
                        <Route path="login" element={<LoginPage/>}/>
                        <Route path="register" element={<RegisterPage/>}/>
                        <Route path="*" element={<Navigate to={getDefaultPath(userId)} replace/>}/>
                    </Route>
                </Routes>
            </BrowserRouter>
        </ThemeProvider>
    );
};

export default App;