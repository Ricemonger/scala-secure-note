import React from 'react';
import {Box, CssBaseline, ListItemButton, ListItemIcon, ListItemText, Switch, useTheme} from '@mui/material';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import LightModeIcon from '@mui/icons-material/LightMode';
import {Outlet} from 'react-router-dom';

interface MainLayoutProps {
    onToggleTheme: () => void;
}

export const MainLayout: React.FC<MainLayoutProps> = ({onToggleTheme}) => {

    const theme = useTheme();

    return (
        <Box sx={{display: 'flex', minHeight: '100vh'}}>
            <CssBaseline/>

            <ListItemButton
                sx={{
                    minHeight: 48,
                    justifyContent: 'initial',
                    px: 2.5,
                }}
                onClick={onToggleTheme}
            >
                <ListItemIcon
                    sx={{
                        minWidth: 0,
                        mr: 3,
                        justifyContent: 'center',
                    }}
                >
                    {theme.palette.mode === 'dark' ? <LightModeIcon/> : <DarkModeIcon/>}
                </ListItemIcon>
                <ListItemText
                    primary={theme.palette.mode === 'dark' ? "Light Mode" : "Dark Mode"}
                    sx={{opacity: 1}}
                />
                {(<Switch
                    edge="end"
                    size="small"
                    checked={theme.palette.mode === 'dark'}
                    readOnly
                />)}
            </ListItemButton>

            <Box component="main" sx={{flexGrow: 1, p: 0, overflow: 'auto', height: '100vh'}}>
                <Box sx={{mt: 8}}>
                    <Outlet/>
                </Box>
            </Box>
        </Box>
    );
};