import React, { useEffect, useState } from 'react';
import {
    Box, Typography, Paper, Table, TableBody, TableCell,
    TableContainer, TableHead, TableRow, Button, Chip, CircularProgress, Alert
} from '@mui/material';
import FlagIcon from '@mui/icons-material/Flag';
import DeleteIcon from '@mui/icons-material/Delete';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { getAllUsers, deleteUser, flagUser, unflagUser } from '../../api/ApiService';
import { useAuth } from '../../global_component/AuthContext';

export default function ManageUsers() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const { user: currentUser } = useAuth();

    // Safety check: Optional chaining
    const isAdmin = currentUser?.roles?.includes('ROLE_ADMIN');
    const isManager = currentUser?.roles?.includes('ROLE_MANAGER');

    useEffect(() => {
        // FIXED: Handle promise rejection
        const init = async () => {
            setLoading(true);
            try {
                const data = await getAllUsers();
                setUsers(Array.isArray(data) ? data : []);
                setError(null);
            } catch (err) {
                console.error("Failed to load users", err);
                setError("Failed to fetch user list.");
            } finally {
                setLoading(false);
            }
        };
        init();
    }, []);

    const refreshList = async () => {
        try {
            const data = await getAllUsers();
            setUsers(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error("Silent refresh failed", error);
        }
    }

    const handleDelete = async (id) => {
        if(!window.confirm("Are you sure? This deletes the user permanently.")) return;
        try {
            await deleteUser(id);
            await refreshList();
        } catch {
            // FIXED: Removed unused 'err'
            alert("Failed to delete user.");
        }
    };

    const handleFlag = async (id) => {
        try {
            await flagUser(id);
            await refreshList();
        } catch {
            alert("Failed to flag user.");
        }
    };

    const handleUnflag = async (id) => {
        try {
            await unflagUser(id);
            await refreshList();
        } catch {
            alert("Failed to unflag user.");
        }
    };

    if (loading) return <Box p={4}><CircularProgress /></Box>;
    if (error) return <Box p={4}><Alert severity="error">{error}</Alert></Box>;

    return (
        <Box sx={{ p: 4 }}>
            <Typography variant="h4" gutterBottom>User Management</Typography>
            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>ID</TableCell>
                            <TableCell>Email</TableCell>
                            <TableCell>Username</TableCell>
                            <TableCell>Status</TableCell>
                            <TableCell>Roles</TableCell>
                            <TableCell>Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {users.map((u) => (
                            <TableRow key={u.id} sx={{ backgroundColor: (u.isFlagged || u.flagged) ? '#fff4f4' : 'inherit' }}>
                                <TableCell>{u.id}</TableCell>
                                <TableCell>{u.email}</TableCell>
                                <TableCell>{u.realUsername || u.username || 'N/A'}</TableCell>
                                <TableCell>
                                    {(u.isFlagged || u.flagged) ? (
                                        <Chip icon={<FlagIcon />} label="Flagged" color="error" size="small" />
                                    ) : (
                                        <Chip label="Active" color="success" size="small" />
                                    )}
                                </TableCell>
                                <TableCell>
                                    {Array.isArray(u.roles)
                                        ? u.roles.map(r => (typeof r === 'string' ? r : r.name)).join(", ")
                                        : "No Role"}
                                </TableCell>
                                <TableCell>
                                    {isManager && !(u.isFlagged || u.flagged) && (
                                        <Button
                                            size="small"
                                            color="warning"
                                            startIcon={<FlagIcon />}
                                            onClick={() => handleFlag(u.id)}
                                        >
                                            Flag
                                        </Button>
                                    )}

                                    {isAdmin && (
                                        <>
                                            {(u.isFlagged || u.flagged) && (
                                                <Button
                                                    size="small"
                                                    color="success"
                                                    startIcon={<CheckCircleIcon />}
                                                    onClick={() => handleUnflag(u.id)}
                                                    sx={{ mr: 1 }}
                                                >
                                                    Review OK
                                                </Button>
                                            )}
                                            <Button
                                                size="small"
                                                color="error"
                                                startIcon={<DeleteIcon />}
                                                onClick={() => handleDelete(u.id)}
                                            >
                                                Delete
                                            </Button>
                                        </>
                                    )}
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </Box>
    );
}