// app/(protected)/layout.tsx

import { ProtectedRoute } from "./protected-route";

 

export default function ProtectedLayout({children,}: {
    children: React.ReactNode;
}) {
    return <ProtectedRoute>{children}</ProtectedRoute>;
}