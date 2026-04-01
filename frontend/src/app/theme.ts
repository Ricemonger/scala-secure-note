import {createTheme, responsiveFontSizes, Theme} from '@mui/material/styles';

declare module '@mui/material/styles' {
    interface Palette {
        status: {
            opened: string;
            inProgress: string;
            closed: string;
        };
    }

    interface PaletteOptions {
        status?: {
            opened?: string;
            inProgress?: string;
            closed?: string;
        };
    }
}

export const formatUserDisplay = (username?: string, userId?: string) => {
    if (username && userId) {
        return `${username}#${userId}`;
    }
    return userId || 'N/A';
};

export const createAppTheme = (mode: 'light' | 'dark'): Theme => {
    let theme = createTheme({
        palette: {
            mode,
            primary: {
                main: '#1976d2',
                light: '#42a5f5',
                dark: '#1565c0',
                contrastText: '#ffffff',
            },
            secondary: {
                main: '#9c27b0',
            },
            background: {
                default: mode === 'light' ? '#f4f6f8' : '#121212',
                paper: mode === 'light' ? '#ffffff' : '#1e1e1e',
            },
            status: {
                opened: '#ed6c02',
                inProgress: '#0288d1',
                closed: '#2e7d32',
            },
        },
        typography: {
            fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
            h1: {
                fontSize: '2rem',
                fontWeight: 600,
            },
            h2: {
                fontSize: '1.5rem',
                fontWeight: 500,
            },
        },
        components: {
            MuiButton: {
                styleOverrides: {
                    root: {
                        borderRadius: 8,
                        textTransform: 'none',
                    },
                },
            },
            MuiPaper: {
                styleOverrides: {
                    root: {
                        padding: '16px',
                        boxShadow: mode === 'light'
                            ? '0px 3px 6px rgba(0,0,0,0.1)'
                            : '0px 3px 6px rgba(0,0,0,0.5)',
                    },
                },
            },
            MuiChip: {
                styleOverrides: {
                    root: {
                        fontWeight: 500,
                    },
                },
            },
            MuiTablePagination: {
                defaultProps: {
                    rowsPerPageOptions: [5, 10, 25, 50],
                },
            },
            MuiIconButton: {
                variants: [
                    {
                        props: {className: 'bordered'},
                        style: {
                            border: '1px solid',
                            borderColor: mode === 'light'
                                ? 'rgba(0, 0, 0, 0.12)'
                                : 'rgba(255, 255, 255, 0.12)',
                            borderRadius: 4,
                        }
                    }
                ]
            },
        },
    });

    return responsiveFontSizes(theme);
};