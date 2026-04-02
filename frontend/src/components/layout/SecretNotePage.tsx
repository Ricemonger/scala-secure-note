import React, { useState, useEffect } from 'react';
import { Box, Button, Typography, Paper, TextField, CircularProgress } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import CancelIcon from '@mui/icons-material/Cancel';
import { useAppDispatch, useAppSelector } from '../../app/hooks';
import { getNote, putNote } from '../../features/notes/noteSlice';
import {RootState} from "../../app/store";

export const SecretNotePage: React.FC = () => {
    const dispatch = useAppDispatch();

    const { note, loading, operationLoading, error } = useAppSelector((state: RootState) => state.notes);

    const [isEditing, setIsEditing] = useState(false);
    const [draft, setDraft] = useState('');

    useEffect(() => {
        dispatch(getNote({}));
    }, [dispatch]);

    const handleEditClick = () => {
        setDraft(note);
        setIsEditing(true);
    };

    const handleCancelClick = () => {
        setIsEditing(false);
        setDraft('');
    };

    const handleSaveClick = async () => {
        const resultAction = await dispatch(putNote({ note: draft }));

        if (putNote.fulfilled.match(resultAction)) {
            setIsEditing(false);
        }
    };

    return (
        <Box sx={{ width: '100%', maxWidth: 800, mt: 4 }}>
            <Paper elevation={3} sx={{ p: 4 }}>
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                    <Typography variant="h5" component="h1">
                        Your Secret Note
                    </Typography>
                    {!isEditing && (
                        <Button
                            variant="contained"
                            color="primary"
                            startIcon={<EditIcon />}
                            onClick={handleEditClick}
                            disabled={loading}
                        >
                            Edit Note
                        </Button>
                    )}
                </Box>

                {error && (
                    <Typography color="error" variant="body2" mb={2}>
                        {error}
                    </Typography>
                )}

                {isEditing ? (
                    <Box>
                        <TextField
                            fullWidth
                            multiline
                            minRows={8}
                            variant="outlined"
                            value={draft}
                            onChange={(e) => setDraft(e.target.value)}
                            placeholder="Write your secrets here..."
                            autoFocus
                            disabled={operationLoading}
                        />
                        <Box display="flex" justifyContent="flex-end" gap={2} mt={3}>
                            <Button
                                variant="outlined"
                                color="inherit"
                                startIcon={<CancelIcon />}
                                onClick={handleCancelClick}
                                disabled={operationLoading}
                            >
                                Cancel
                            </Button>
                            <Button
                                variant="contained"
                                color="success"
                                startIcon={operationLoading ? <CircularProgress size={20} color="inherit" /> : <SaveIcon />}
                                onClick={handleSaveClick}
                                disabled={operationLoading}
                            >
                                {operationLoading ? 'Saving...' : 'Submit Edit'}
                            </Button>
                        </Box>
                    </Box>
                ) : (
                    <Box
                        sx={{
                            p: 3,
                            bgcolor: 'background.default',
                            borderRadius: 1,
                            minHeight: '200px',
                            whiteSpace: 'pre-wrap',
                            border: '1px solid',
                            borderColor: 'divider',
                            display: 'flex',
                            alignItems: loading ? 'center' : 'flex-start',
                            justifyContent: loading ? 'center' : 'flex-start'
                        }}
                    >
                        {loading ? (
                            <CircularProgress />
                        ) : (
                            <Typography variant="body1" sx={{ fontFamily: 'monospace' }}>
                                {note || "You don't have a secret note yet. Click edit to create one!"}
                            </Typography>
                        )}
                    </Box>
                )}
            </Paper>
        </Box>
    );
};