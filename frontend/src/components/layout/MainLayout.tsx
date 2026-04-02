import React from 'react';
import { Box, CssBaseline } from '@mui/material';
import { Outlet } from 'react-router-dom';
import { MainHeader } from './MainHeader';

interface MainLayoutProps {
    onToggleTheme: () => void;
}

export const MainLayout: React.FC<MainLayoutProps> = ({ onToggleTheme }) => {

    return (
        <Box sx={{ display: 'flex', minHeight: '100vh', flexDirection: 'column' }}>
            <CssBaseline />

            <MainHeader onToggleTheme={onToggleTheme} />

            <Box component="main" sx={{ flexGrow: 1, p: 3, overflow: 'auto', mt: 5, display: 'flex', justifyContent: 'center' }}>
                <Outlet />
            </Box>
        </Box>
    );
};