import React, {useState} from 'react';
import {Box, Button, Link as MuiLink, Paper, TextField, Typography} from '@mui/material';
import {Link, useNavigate} from 'react-router-dom';
import {useAppDispatch} from '../../app/hooks';
import {register} from '../../features/authentication/authSlice';

export const RegisterPage: React.FC = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [errorMsg, setErrorMsg] = useState<string | null>(null);

    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setErrorMsg(null);

        const resultAction = await dispatch(register({username, password}));

        if (register.fulfilled.match(resultAction)) {
            navigate('/note');
        } else {
            setErrorMsg(resultAction.payload as string || 'Failed to register');
        }
    };

    return (
        <Box sx={{width: '100%', maxWidth: 400, mt: 4}}>
            <Paper elevation={3} sx={{p: 4}}>
                <Typography variant="h5" align="center" gutterBottom>
                    Register
                </Typography>

                {errorMsg && (
                    <Typography color="error" variant="body2" align="center" gutterBottom>
                        {errorMsg}
                    </Typography>
                )}

                <form onSubmit={handleSubmit}>
                    <TextField
                        label="Username"
                        variant="outlined"
                        fullWidth
                        margin="normal"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        required
                    />
                    <TextField
                        label="Password"
                        type="password"
                        variant="outlined"
                        fullWidth
                        margin="normal"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                    <Button type="submit" variant="contained" color="primary" fullWidth sx={{mt: 3, mb: 2}}>
                        Register
                    </Button>
                </form>

                <Box textAlign="center">
                    <MuiLink component={Link} to="/login">
                        Login
                    </MuiLink>
                </Box>
            </Paper>
        </Box>
    );
};