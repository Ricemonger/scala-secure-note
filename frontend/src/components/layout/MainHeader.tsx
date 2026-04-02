import React from 'react';
import { AppBar, Toolbar, Typography, IconButton, useTheme } from '@mui/material';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import LightModeIcon from '@mui/icons-material/LightMode';
import LogoutIcon from '@mui/icons-material/Logout';
import { useNavigate } from 'react-router-dom';
import { useAppSelector, useAppDispatch } from '../../app/hooks';
import { selectUserId, logoutUser } from '../../features/authentication/authSlice';

interface MainHeaderProps {
    onToggleTheme: () => void;
}

export const MainHeader: React.FC<MainHeaderProps> = ({ onToggleTheme }) => {
    const theme = useTheme();
    const isDarkMode = theme.palette.mode === 'dark';

    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    const userId = useAppSelector(selectUserId);

    const handleLogout = () => {
        dispatch(logoutUser());
        navigate('/login');
    };

    return (
        <AppBar position="fixed">
            <Toolbar
                variant="dense"
                sx={{ minHeight: '36px !important', px: 2 }}
            >
                <Typography variant="subtitle1" component="div" sx={{ flexGrow: 1, fontWeight: 'bold' }}>
                    Secret Note
                </Typography>

                {userId && (
                    <IconButton color="inherit" onClick={handleLogout} size="small" sx={{ mr: 1 }} title="Logout">
                        <LogoutIcon fontSize="small" />
                    </IconButton>
                )}

                <IconButton color="inherit" onClick={onToggleTheme} edge="end" size="small" title="Toggle Theme">
                    {isDarkMode ? <LightModeIcon fontSize="small" /> : <DarkModeIcon fontSize="small" />}
                </IconButton>
            </Toolbar>
        </AppBar>
    );
};