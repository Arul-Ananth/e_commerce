import React, { useEffect, useState } from 'react';
import {
    Box, Typography, Paper, Table, TableBody, TableCell,
    TableContainer, TableHead, TableRow, Button, Chip, CircularProgress, Alert, TextField
} from '@mui/material';
import FlagIcon from '@mui/icons-material/Flag';
import DeleteIcon from '@mui/icons-material/Delete';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { getAllUsers, deleteUser, flagUser, unflagUser, updateUserDiscount, setEmployeeRole } from '../../api/ApiService';
import { useAuth } from '../../global_component/AuthContext';

export default function ManageUsers() {
    const [users, setUsers] = useState([]);
    const [discountEdits, setDiscountEdits] = useState({});
    const [discountStartEdits, setDiscountStartEdits] = useState({});
    const [discountEndEdits, setDiscountEndEdits] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const { user: currentUser } = useAuth();

    // Safety check: Optional chaining
    const isAdmin = currentUser?.roles?.includes('ROLE_ADMIN');
    const isManager = currentUser?.roles?.includes('ROLE_MANAGER');

    useEffect(() => {
        loadUsers();
    }, []);

    const loadUsers = async () => {
        setLoading(true);
        try {
            const data = await getAllUsers();
            const safeData = Array.isArray(data) ? data : [];

            // --- SORTING LOGIC ---
            // Flagged users (true) come before Active users (false)
            safeData.sort((a, b) => {
                // Check both property names in case JSON format varies
                const aFlagged = a.isFlagged || a.flagged || false;
                const bFlagged = b.isFlagged || b.flagged || false;

                // If 'a' is flagged and 'b' is not, 'a' moves up (-1)
                // If 'b' is flagged and 'a' is not, 'b' moves up (1)
                // If both are same, don't move (0)
                return (aFlagged === bFlagged) ? 0 : aFlagged ? -1 : 1;
            });

            setUsers(safeData);
            setError(null);
        } catch (err) {
            console.error("Failed to load users", err);
            setError("Failed to fetch user list.");
        } finally {
            setLoading(false);
        }
    };

    // Helper to refresh list silently (without full loading spinner)
    const refreshList = async () => {
        try {
            const data = await getAllUsers();
            const safeData = Array.isArray(data) ? data : [];

            // Apply the same sort on refresh
            safeData.sort((a, b) => {
                const aFlagged = a.isFlagged || a.flagged || false;
                const bFlagged = b.isFlagged || b.flagged || false;
                return (aFlagged === bFlagged) ? 0 : aFlagged ? -1 : 1;
            });

            setUsers(safeData);
            setDiscountEdits({});
            setDiscountStartEdits({});
            setDiscountEndEdits({});
        } catch (error) {
            console.error("Silent refresh failed", error);
        }
    }

    const handleDelete = async (id) => {
        if(!window.confirm("Are you sure? This deletes the user permanently.")) return;
        try {
            await deleteUser(id);
            await refreshList();
        } catch (err) {
            alert("Failed to delete user.");
        }
    };

    const handleFlag = async (id) => {
        try {
            await flagUser(id);
            await refreshList();
        } catch (err) {
            alert("Failed to flag user.");
        }
    };

    const handleUnflag = async (id) => {
        try {
            await unflagUser(id);
            await refreshList();
        } catch (err) {
            alert("Failed to unflag user.");
        }
    };

    const handleDiscountChange = (id, value) => {
        setDiscountEdits((prev) => ({ ...prev, [id]: value }));
    };

    const handleDiscountStartChange = (id, value) => {
        setDiscountStartEdits((prev) => ({ ...prev, [id]: value }));
    };

    const handleDiscountEndChange = (id, value) => {
        setDiscountEndEdits((prev) => ({ ...prev, [id]: value }));
    };

    const handleDiscountSave = async (id) => {
        const rawValue = discountEdits[id];
        const percentage = rawValue === "" || rawValue === undefined ? 0 : Number(rawValue);
        if (Number.isNaN(percentage) || percentage < 0 || percentage > 100) {
            alert("Discount must be a number between 0 and 100.");
            return;
        }
        const startDate = discountStartEdits[id];
        const endDate = discountEndEdits[id];
        if (percentage > 0 && !startDate) {
            alert("Start date is required for a discount.");
            return;
        }
        try {
            await updateUserDiscount(id, percentage, startDate || null, endDate || null);
            setDiscountEdits((prev) => {
                const next = { ...prev };
                delete next[id];
                return next;
            });
            setDiscountStartEdits((prev) => {
                const next = { ...prev };
                delete next[id];
                return next;
            });
            setDiscountEndEdits((prev) => {
                const next = { ...prev };
                delete next[id];
                return next;
            });
            await refreshList();
        } catch (err) {
            alert("Failed to update discount.");
        }
    };

    const handleEmployeeToggle = async (id, enabled) => {
        try {
            await setEmployeeRole(id, enabled);
            await refreshList();
        } catch (err) {
            alert("Failed to update employee role.");
        }
    };

    const getRoleNames = (roles) => (
        Array.isArray(roles)
            ? roles.map(r => (typeof r === 'string' ? r : r.name))
            : []
    );

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
                            <TableCell>User Discount (%)</TableCell>
                            <TableCell>Discount Start</TableCell>
                            <TableCell>Discount End</TableCell>
                            <TableCell>Employee</TableCell>
                            <TableCell>Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {users.map((u) => {
                            // Determine flag status for rendering
                            const isUserFlagged = u.isFlagged || u.flagged;
                            const roleNames = getRoleNames(u.roles);
                            const isEmployee = roleNames.includes('ROLE_EMPLOYEE');
                            const discountValue = discountEdits[u.id] ?? (u.userDiscountPercentage ?? 0);
                            const discountStartValue = discountStartEdits[u.id] ?? (u.userDiscountStartDate ?? "");
                            const discountEndValue = discountEndEdits[u.id] ?? (u.userDiscountEndDate ?? "");

                            return (
                                <TableRow key={u.id} sx={{ backgroundColor: isUserFlagged ? '#d32f2f' : 'inherit' }}>
                                    <TableCell>{u.id}</TableCell>
                                    <TableCell>{u.email}</TableCell>
                                    <TableCell>{u.realUsername || u.username || 'N/A'}</TableCell>
                                    <TableCell>
                                        {isUserFlagged ? (
                                            <Chip icon={<FlagIcon />} label="Flagged" color="error" size="small" />
                                        ) : (
                                            <Chip label="Active" color="success" size="small" />
                                        )}
                                    </TableCell>
                                    <TableCell>
                                        {roleNames.length ? roleNames.join(", ") : "No Role"}
                                    </TableCell>
                                    <TableCell>
                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                            <TextField
                                                size="small"
                                                type="number"
                                                value={discountValue}
                                                onChange={(e) => handleDiscountChange(u.id, e.target.value)}
                                                inputProps={{ min: 0, max: 100, step: 1 }}
                                                sx={{ width: 90 }}
                                            />
                                            <Button size="small" variant="outlined" onClick={() => handleDiscountSave(u.id)}>
                                                Save
                                            </Button>
                                        </Box>
                                    </TableCell>
                                    <TableCell>
                                        <TextField
                                            size="small"
                                            type="date"
                                            value={discountStartValue}
                                            onChange={(e) => handleDiscountStartChange(u.id, e.target.value)}
                                            InputLabelProps={{ shrink: true }}
                                            sx={{ width: 150 }}
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <TextField
                                            size="small"
                                            type="date"
                                            value={discountEndValue}
                                            onChange={(e) => handleDiscountEndChange(u.id, e.target.value)}
                                            InputLabelProps={{ shrink: true }}
                                            sx={{ width: 150 }}
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <Chip
                                            label={isEmployee ? "Employee" : "Not Employee"}
                                            color={isEmployee ? "success" : "default"}
                                            size="small"
                                        />
                                        {isAdmin && (
                                            <Button
                                                size="small"
                                                sx={{ ml: 1 }}
                                                onClick={() => handleEmployeeToggle(u.id, !isEmployee)}
                                            >
                                                {isEmployee ? "Remove" : "Make Employee"}
                                            </Button>
                                        )}
                                    </TableCell>
                                    <TableCell>
                                        {/* Managers can Flag active users */}
                                        {isManager && !isUserFlagged && (
                                            <Button
                                                size="small"
                                                color="warning"
                                                startIcon={<FlagIcon />}
                                                onClick={() => handleFlag(u.id)}
                                            >
                                                Flag
                                            </Button>
                                        )}

                                        {/* Admins can Unflag and Delete */}
                                        {isAdmin && (
                                            <>
                                                {isUserFlagged && (
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
                            );
                        })}
                    </TableBody>
                </Table>
            </TableContainer>
        </Box>
    );
}
