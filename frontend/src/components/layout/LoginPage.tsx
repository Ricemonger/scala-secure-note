import React, { useState } from 'react';
import {
    Box, Button, Link as MuiLink, Paper, TextField, Typography,
    InputAdornment, IconButton
} from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch } from '../../app/hooks';
import { login } from '../../features/authentication/authSlice';
import { useFormik } from 'formik';
import * as yup from 'yup';
import ClearIcon from '@mui/icons-material/Clear';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import {constraintRules} from "../../features/config/constraintRules";

const validationSchema = yup.object({
    username: yup
        .string()
        .required('Username is required')
        .matches(new RegExp(constraintRules.username.regex), constraintRules.username.message),
    password: yup
        .string()
        .required('Password is required')
        .matches(new RegExp(constraintRules.password.regex), constraintRules.password.message),
});

console.log(constraintRules.password.regex)

export const LoginPage: React.FC = () => {
    const [errorMsg, setErrorMsg] = useState<string | null>(null);
    const [showPassword, setShowPassword] = useState(false);

    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    const formik = useFormik({
        initialValues: {
            username: '',
            password: '',
        },
        validationSchema: validationSchema,
        onSubmit: async (values) => {
            setErrorMsg(null);

            const resultAction = await dispatch(login({ username: values.username, password: values.password }));

            if (login.fulfilled.match(resultAction)) {
                navigate('/note');
            } else {
                setErrorMsg(resultAction.payload as string || 'Failed to login');
            }
        },
    });

    const handleClickShowPassword = () => setShowPassword((show) => !show);
    const handleMouseDownPassword = (event: React.MouseEvent<HTMLButtonElement>) => {
        event.preventDefault();
    };

    return (
        <Box sx={{ width: '100%', maxWidth: 400, mt: 4 }}>
            <Paper elevation={3} sx={{ p: 4 }}>
                <Typography variant="h5" align="center" gutterBottom>
                    Login
                </Typography>

                {errorMsg && (
                    <Typography color="error" variant="body2" align="center" gutterBottom>
                        {errorMsg}
                    </Typography>
                )}

                <form onSubmit={formik.handleSubmit}>
                    <TextField
                        id="username"
                        name="username"
                        label="Username"
                        variant="outlined"
                        fullWidth
                        margin="normal"
                        value={formik.values.username}
                        onChange={formik.handleChange}
                        onBlur={formik.handleBlur}
                        error={formik.touched.username && Boolean(formik.errors.username)}
                        helperText={formik.touched.username && formik.errors.username}
                        required
                        InputProps={{
                            endAdornment: formik.values.username ? (
                                <InputAdornment position="end">
                                    <IconButton
                                        onClick={() => formik.setFieldValue('username', '')}
                                        edge="end"
                                        size="small"
                                    >
                                        <ClearIcon fontSize="small" />
                                    </IconButton>
                                </InputAdornment>
                            ) : null,
                        }}
                    />
                    <TextField
                        id="password"
                        name="password"
                        label="Password"
                        type={showPassword ? 'text' : 'password'}
                        variant="outlined"
                        fullWidth
                        margin="normal"
                        value={formik.values.password}
                        onChange={formik.handleChange}
                        onBlur={formik.handleBlur}
                        error={formik.touched.password && Boolean(formik.errors.password)}
                        helperText={formik.touched.password && formik.errors.password}
                        required
                        InputProps={{
                            endAdornment: (
                                <InputAdornment position="end">
                                    {formik.values.password && (
                                        <IconButton
                                            onClick={() => formik.setFieldValue('password', '')}
                                            edge="end"
                                            size="small"
                                            sx={{ mr: 0.5 }}
                                        >
                                            <ClearIcon fontSize="small" />
                                        </IconButton>
                                    )}
                                    <IconButton
                                        aria-label="toggle password visibility"
                                        onClick={handleClickShowPassword}
                                        onMouseDown={handleMouseDownPassword}
                                        edge="end"
                                        size="small"
                                    >
                                        {showPassword ? <VisibilityOff fontSize="small" /> : <Visibility fontSize="small" />}
                                    </IconButton>
                                </InputAdornment>
                            ),
                        }}
                    />
                    <Button
                        type="submit"
                        variant="contained"
                        color="primary"
                        fullWidth
                        sx={{ mt: 3, mb: 2 }}
                        disabled={formik.isSubmitting}
                    >
                        Login
                    </Button>
                </form>

                <Box textAlign="center">
                    <MuiLink component={Link} to="/register">
                        Register
                    </MuiLink>
                </Box>
            </Paper>
        </Box>
    );
};