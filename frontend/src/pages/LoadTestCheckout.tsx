import { Box, Button, Card, CardContent, Container, Stack, Toolbar, Typography } from "@mui/material";
import { useMemo } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

export default function LoadTestCheckout() {
    const navigate = useNavigate();
    const [params] = useSearchParams();

    const sessionId = params.get("sessionId") || "";
    const paymentId = params.get("paymentId") || "";
    const orderId = params.get("orderId") || "";

    const details = useMemo(
        () => [
            ["Order", orderId],
            ["Session", sessionId],
            ["Payment", paymentId],
        ].filter(([, value]) => Boolean(value)),
        [orderId, paymentId, sessionId],
    );

    return (
        <Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
            <Toolbar />
            <Container maxWidth="sm" sx={{ py: 4 }}>
                <Card>
                    <CardContent>
                        <Stack spacing={2}>
                            <Typography variant="h5">Load-Test Checkout</Typography>
                            <Typography variant="body2" color="text.secondary">
                                This local checkout page is used when the backend runs with APP_PAYMENT_GATEWAY=loadtest.
                                No Stripe or Razorpay API call is made.
                            </Typography>

                            {details.map(([label, value]) => (
                                <Stack key={label} direction="row" justifyContent="space-between" gap={2}>
                                    <Typography variant="body2" color="text.secondary">{label}</Typography>
                                    <Typography variant="body2" sx={{ wordBreak: "break-all", textAlign: "right" }}>{value}</Typography>
                                </Stack>
                            ))}

                            <Button variant="contained" onClick={() => navigate("/")}>
                                Return to Store
                            </Button>
                        </Stack>
                    </CardContent>
                </Card>
            </Container>
        </Box>
    );
}
