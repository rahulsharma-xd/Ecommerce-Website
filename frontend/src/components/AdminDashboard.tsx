import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

interface AdminProduct {
  id: number;
  name: string;
  price: number;
  stockQuantity: number;
  category: string;
  active?: boolean;
}

interface AdminOrder {
  id: number;
  status: string;
  totalAmount: number;
}

interface AdminUser {
  id: number;
  username: string;
  email: string;
  role: string;
  active: boolean;
}

const AdminDashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [products, setProducts] = useState<AdminProduct[]>([]);
  const [orders, setOrders] = useState<AdminOrder[]>([]);
  const [users, setUsers] = useState<AdminUser[]>([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError('');

      const [productsResponse, ordersResponse, usersResponse] =
        await Promise.all([
          api.get<AdminProduct[]>('/admin/products'),
          api.get<AdminOrder[]>('/admin/orders'),
          api.get<AdminUser[]>('/admin/users'),
        ]);

      setProducts(productsResponse.data);
      setOrders(ordersResponse.data);
      setUsers(usersResponse.data);
    } catch (err: any) {
      console.error('Failed to load admin dashboard:', err);

      if (err.response?.status === 403) {
        setError('You do not have permission to access the Admin Panel.');
      } else {
        setError('Failed to load dashboard data. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const pendingOrders = orders.filter(
    (order) => order.status === 'PENDING'
  ).length;

  const activeProducts = products.filter(
    (product) => product.active !== false
  ).length;

  return (
    <div className="min-h-screen bg-gray-100">

      {/* Header */}
      <header className="bg-white border-b shadow-sm sticky top-0 z-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-4">

          <div className="flex items-center justify-between">

            {/* Logo */}
            <div>
              <h1 className="text-2xl font-bold text-gray-900">
                Shop<span className="text-blue-600">Hub</span>
              </h1>

              <p className="text-sm text-gray-500">
                Admin Panel
              </p>
            </div>

            {/* Right side */}
            <div className="flex items-center gap-4">

              <div className="hidden sm:block text-right">
                <p className="text-sm font-semibold text-gray-800">
                  {user?.username}
                </p>

                <p className="text-xs text-gray-500">
                  Administrator
                </p>
              </div>

              <button
                onClick={handleLogout}
                className="px-4 py-2 bg-red-600 text-white rounded-lg
                           hover:bg-red-700 transition font-medium"
              >
                Logout
              </button>

            </div>
          </div>

        </div>
      </header>

      {/* Main */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 py-8">

        {/* Page heading */}
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-gray-900">
            Dashboard
          </h2>

          <p className="text-gray-500 mt-1">
            Manage your ShopHub store from one place.
          </p>
        </div>

        {/* Loading */}
        {loading && (
          <div className="bg-white rounded-xl shadow-sm p-10 text-center">
            <div className="animate-spin rounded-full h-10 w-10
                            border-b-2 border-blue-600 mx-auto mb-4">
            </div>

            <p className="text-gray-600">
              Loading dashboard...
            </p>
          </div>
        )}

        {/* Error */}
        {!loading && error && (
          <div className="bg-red-50 border border-red-200
                          text-red-700 rounded-xl p-5 mb-8">

            <p className="font-semibold">
              {error}
            </p>

            <button
              onClick={fetchDashboardData}
              className="mt-3 text-sm font-medium underline"
            >
              Try Again
            </button>
          </div>
        )}

        {/* Dashboard content */}
        {!loading && !error && (
          <>
            {/* Statistics */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">

              {/* Products */}
              <div className="bg-white rounded-xl shadow-sm p-6">
                <div className="flex items-center justify-between">

                  <div>
                    <p className="text-sm text-gray-500">
                      Total Products
                    </p>

                    <p className="text-3xl font-bold text-gray-900 mt-2">
                      {products.length}
                    </p>

                    <p className="text-xs text-green-600 mt-1">
                      {activeProducts} active
                    </p>
                  </div>

                  <div className="w-12 h-12 rounded-lg bg-blue-100
                                  flex items-center justify-center">

                    <span className="text-2xl">
                      🛍️
                    </span>

                  </div>

                </div>
              </div>

              {/* Orders */}
              <div className="bg-white rounded-xl shadow-sm p-6">
                <div className="flex items-center justify-between">

                  <div>
                    <p className="text-sm text-gray-500">
                      Total Orders
                    </p>

                    <p className="text-3xl font-bold text-gray-900 mt-2">
                      {orders.length}
                    </p>

                    <p className="text-xs text-gray-500 mt-1">
                      All customer orders
                    </p>
                  </div>

                  <div className="w-12 h-12 rounded-lg bg-purple-100
                                  flex items-center justify-center">

                    <span className="text-2xl">
                      📦
                    </span>

                  </div>

                </div>
              </div>

              {/* Users */}
              <div className="bg-white rounded-xl shadow-sm p-6">
                <div className="flex items-center justify-between">

                  <div>
                    <p className="text-sm text-gray-500">
                      Total Users
                    </p>

                    <p className="text-3xl font-bold text-gray-900 mt-2">
                      {users.length}
                    </p>

                    <p className="text-xs text-gray-500 mt-1">
                      Registered accounts
                    </p>
                  </div>

                  <div className="w-12 h-12 rounded-lg bg-green-100
                                  flex items-center justify-center">

                    <span className="text-2xl">
                      👥
                    </span>

                  </div>

                </div>
              </div>

              {/* Pending Orders */}
              <div className="bg-white rounded-xl shadow-sm p-6">
                <div className="flex items-center justify-between">

                  <div>
                    <p className="text-sm text-gray-500">
                      Pending Orders
                    </p>

                    <p className="text-3xl font-bold text-gray-900 mt-2">
                      {pendingOrders}
                    </p>

                    <p className="text-xs text-orange-600 mt-1">
                      Require processing
                    </p>
                  </div>

                  <div className="w-12 h-12 rounded-lg bg-orange-100
                                  flex items-center justify-center">

                    <span className="text-2xl">
                      ⏳
                    </span>

                  </div>

                </div>
              </div>

            </div>

            {/* Management cards */}
            <div className="mb-4">
              <h3 className="text-xl font-bold text-gray-900">
                Management
              </h3>

              <p className="text-gray-500 text-sm mt-1">
                Choose what you want to manage.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">

              {/* Products */}
              <button
                onClick={() => navigate('/admin/products')}
                className="bg-white rounded-xl shadow-sm p-6 text-left
                           hover:shadow-md transition border
                           border-gray-200"
              >
                <div className="text-3xl mb-4">
                  🛍️
                </div>

                <h4 className="text-lg font-bold text-gray-900">
                  Manage Products
                </h4>

                <p className="text-sm text-gray-500 mt-2">
                  Add, edit, deactivate products and manage stock.
                </p>

                <p className="text-blue-600 text-sm font-semibold mt-4">
                  Manage Products →
                </p>
              </button>

              {/* Orders */}
              <button
                onClick={() => navigate('/admin/orders')}
                className="bg-white rounded-xl shadow-sm p-6 text-left
                           hover:shadow-md transition border
                           border-gray-200"
              >
                <div className="text-3xl mb-4">
                  📦
                </div>

                <h4 className="text-lg font-bold text-gray-900">
                  Manage Orders
                </h4>

                <p className="text-sm text-gray-500 mt-2">
                  View customer orders and update their status.
                </p>

                <p className="text-blue-600 text-sm font-semibold mt-4">
                  Manage Orders →
                </p>
              </button>

              {/* Users */}
              <button
                onClick={() => navigate('/admin/users')}
                className="bg-white rounded-xl shadow-sm p-6 text-left
                           hover:shadow-md transition border
                           border-gray-200"
              >
                <div className="text-3xl mb-4">
                  👥
                </div>

                <h4 className="text-lg font-bold text-gray-900">
                  Manage Users
                </h4>

                <p className="text-sm text-gray-500 mt-2">
                  View registered users and manage user accounts.
                </p>

                <p className="text-blue-600 text-sm font-semibold mt-4">
                  Manage Users →
                </p>
              </button>

            </div>

            {/* Store link */}
            <div className="mt-8">
              <button
                onClick={() => navigate('/dashboard')}
                className="px-5 py-3 bg-blue-600 text-white
                           rounded-lg hover:bg-blue-700 transition
                           font-medium"
              >
                ← Back to Shop
              </button>
            </div>
          </>
        )}

      </main>

      {/* Footer */}
      <footer className="border-t bg-white mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6">
          <p className="text-center text-sm text-gray-500">
            © 2026 ShopHub Admin Panel
          </p>
        </div>
      </footer>

    </div>
  );
};

export default AdminDashboard;