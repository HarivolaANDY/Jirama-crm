# ADR-001: Frontend Routing Strategy

## Status
Accepted

## Context
The JIRAMA CRM platform has multiple user roles (Customer, Agent, Technician, Admin, Manager) each with distinct pages. The frontend needs a routing strategy that:
- Supports role-based access control
- Lazy-loads feature modules for performance
- Maintains a clean URL structure
- Works with Keycloak authentication

## Decision
Use React Router v7 with a nested route structure + role guards.

## Implementation

### Route Tree
```typescript
const router = createBrowserRouter([
  // Public routes
  {
    element: <PublicLayout />,
    children: [
      { path: '/', element: lazy(() => import('@/features/landing/pages/HomePage')) },
      { path: '/services', element: lazy(() => import('@/features/landing/pages/ServicesPage')) },
      { path: '/contact', element: lazy(() => import('@/features/landing/pages/ContactPage')) },
    ],
  },
  
  // Auth routes (no layout)
  { path: '/login', element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },
  
  // Protected customer routes
  {
    path: '/customer',
    element: <ProtectedRoute roles={['CUSTOMER']}><AppLayout /></ProtectedRoute>,
    children: [
      { path: 'dashboard', element: lazy(() => import('@/features/customer/pages/DashboardPage')) },
      { path: 'bills', element: lazy(() => import('@/features/customer/pages/BillsPage')) },
      { path: 'bills/:id', element: lazy(() => import('@/features/customer/pages/BillDetailPage')) },
      // ...
    ],
  },
  
  // Protected agent routes
  {
    path: '/agent',
    element: <ProtectedRoute roles={['AGENT']}><AppLayout /></ProtectedRoute>,
    children: [ /* ... */ ],
  },
  
  // Protected technician routes
  {
    path: '/technician',
    element: <ProtectedRoute roles={['TECHNICIAN']}><AppLayout /></ProtectedRoute>,
    children: [ /* ... */ ],
  },
  
  // Protected admin routes
  {
    path: '/admin',
    element: <ProtectedRoute roles={['ADMIN']}><AppLayout /></ProtectedRoute>,
    children: [ /* ... */ ],
  },
  
  // Protected manager routes
  {
    path: '/management',
    element: <ProtectedRoute roles={['MANAGER', 'ADMIN']}><AppLayout /></ProtectedRoute>,
    children: [ /* ... */ ],
  },
]);
```

### Role Guard Component
```typescript
function ProtectedRoute({ roles, children }: Props) {
  const { isAuthenticated, userRoles } = useAuth();
  const hasRole = roles.some(r => userRoles.includes(r));
  
  if (!isAuthenticated) return <Navigate to="/login" />;
  if (!hasRole) return <Navigate to="/unauthorized" />;
  
  return children;
}
```

## Consequences
- Clear separation of routes by role
- Lazy loading reduces initial bundle size
- Simple guard mechanism that's easy to extend
- URLs are semantic and RESTful
- Slightly more boilerplate than flat routing, but much clearer at scale
