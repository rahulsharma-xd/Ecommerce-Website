import { Navigate, Route, Routes, BrowserRouter } from 'react-router-dom';
import { useAuth, AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import { WishlistProvider } from './context/WishlistContext';

import Login from './components/Login';
import Register from './components/Register'; // <-- ADDED
import Home from './components/Home';
import ProductDetails from './components/ProductDetails';
import ShoppingCart from './components/ShoppingCart';
import Checkout from './components/Checkout';
import OrderHistory from './components/OrderHistory';
import OrderDetails from './components/OrderDetails';

import AdminDashboard from './components/AdminDashboard';
import AdminProducts from './components/AdminProducts';
import AdminOrders from './components/AdminOrders';
import AdminOrderDetails from './components/AdminOrderDetails';
import AdminUsers from './components/AdminUsers';

import './App.css';

// ========================================
// PROTECTED ROUTE
// ========================================
const ProtectedRoute = ({
  children,
}: {
  children: React.ReactNode;
}) => {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

// ========================================
// ADMIN ROUTE
// ========================================
const AdminRoute = ({
  children,
}: {
  children: React.ReactNode;
}) => {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (user?.role !== 'ADMIN') {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
};

// ========================================
// APP
// ========================================
function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <WishlistProvider>

            <Routes>

              {/* LOGIN - PUBLIC */}
              <Route
                path="/login"
                element={<Login />}
              />

              {/* REGISTER - PUBLIC - THIS WAS MISSING */}
              <Route
                path="/register"
                element={<Register />}
              />

              {/* CUSTOMER HOME */}
              <Route
                path="/dashboard"
                element={
                  <ProtectedRoute>
                    <Home />
                  </ProtectedRoute>
                }
              />

              {/* PRODUCT DETAILS */}
              <Route
                path="/products/:id"
                element={
                  <ProtectedRoute>
                    <ProductDetails />
                  </ProtectedRoute>
                }
              />

              {/* SHOPPING CART */}
              <Route
                path="/cart"
                element={
                  <ProtectedRoute>
                    <ShoppingCart />
                  </ProtectedRoute>
                }
              />

              {/* CHECKOUT */}
              <Route
                path="/checkout"
                element={
                  <ProtectedRoute>
                    <Checkout />
                  </ProtectedRoute>
                }
              />

              {/* CUSTOMER ORDER HISTORY */}
              <Route
                path="/orders"
                element={
                  <ProtectedRoute>
                    <OrderHistory />
                  </ProtectedRoute>
                }
              />

              {/* CUSTOMER ORDER DETAILS */}
              <Route
                path="/orders/:id"
                element={
                  <ProtectedRoute>
                    <OrderDetails />
                  </ProtectedRoute>
                }
              />

              {/* ADMIN DASHBOARD */}
              <Route
                path="/admin"
                element={
                  <AdminRoute>
                    <AdminDashboard />
                  </AdminRoute>
                }
              />

              {/* ADMIN - MANAGE PRODUCTS */}
              <Route
                path="/admin/products"
                element={
                  <AdminRoute>
                    <AdminProducts />
                  </AdminRoute>
                }
              />

              {/* ADMIN - MANAGE ORDERS */}
              <Route
                path="/admin/orders"
                element={
                  <AdminRoute>
                    <AdminOrders />
                  </AdminRoute>
                }
              />

              {/* ADMIN - ORDER DETAILS */}
              <Route
                path="/admin/orders/:id"
                element={
                  <AdminRoute>
                    <AdminOrderDetails />
                  </AdminRoute>
                }
              />

              {/* ADMIN - MANAGE USERS */}
              <Route
                path="/admin/users"
                element={
                  <AdminRoute>
                    <AdminUsers />
                  </AdminRoute>
                }
              />

              {/* ROOT */}
              <Route
                path="/"
                element={
                  <Navigate
                    to="/login"
                    replace
                  />
                }
              />

              {/* UNKNOWN URL */}
              <Route
                path="*"
                element={
                  <Navigate
                    to="/login"
                    replace
                  />
                }
              />

            </Routes>

          </WishlistProvider>
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
