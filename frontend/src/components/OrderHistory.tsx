import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import orderService from '../services/order.service';
import { OrderStatus } from '../types/order.types';
import type { OrderResponse } from '../types/order.types';

const OrderHistory = () => {
  const navigate = useNavigate();
  const { logout } = useAuth();

  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadOrders();
  }, []);

  const loadOrders = async () => {
    try {
      setLoading(true);
      setError(null);

      const userOrders = await orderService.getUserOrders();
      setOrders(userOrders);
    } catch (err: any) {
      console.error('Failed to load orders:', err);

      setError(
        err.response?.data?.message ||
          'Failed to load orders. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getStatusStyles = (status: OrderStatus): string => {
    switch (status) {
      case OrderStatus.PENDING:
        return 'bg-yellow-50 text-yellow-700 border-yellow-200';

      case OrderStatus.PROCESSING:
        return 'bg-blue-50 text-blue-700 border-blue-200';

      case OrderStatus.SHIPPED:
        return 'bg-purple-50 text-purple-700 border-purple-200';

      case OrderStatus.DELIVERED:
        return 'bg-green-50 text-green-700 border-green-200';

      case OrderStatus.CANCELLED:
        return 'bg-red-50 text-red-700 border-red-200';

      default:
        return 'bg-gray-50 text-gray-700 border-gray-200';
    }
  };

  const getStatusIcon = (status: OrderStatus) => {
    switch (status) {
      case OrderStatus.PENDING:
        return '⏳';

      case OrderStatus.PROCESSING:
        return '⚙️';

      case OrderStatus.SHIPPED:
        return '🚚';

      case OrderStatus.DELIVERED:
        return '✓';

      case OrderStatus.CANCELLED:
        return '✕';

      default:
        return '•';
    }
  };

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);

    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getTotalQuantity = (order: OrderResponse): number => {
    return order.items.reduce(
      (total, item) => total + item.quantity,
      0
    );
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">

          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>

          <p className="text-gray-600 font-medium">
            Loading your orders...
          </p>

        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">

      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-50">

        <div className="max-w-7xl mx-auto px-4 py-4">

          <div className="flex items-center justify-between">

            {/* ShopHub Logo */}
            <button
              onClick={() => navigate('/dashboard')}
              className="text-2xl font-bold text-blue-600 hover:text-blue-700 transition"
            >
              ShopHub
            </button>

            {/* Page Title */}
            <h1 className="hidden sm:block text-xl font-bold text-gray-900">
              My Orders
            </h1>

            {/* Right Side */}
            <div className="flex items-center gap-4">

              <button
                onClick={() => navigate('/dashboard')}
                className="text-gray-700 hover:text-blue-600 font-medium transition"
              >
                Shop
              </button>

              <button
                onClick={handleLogout}
                className="hidden sm:block px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-700 transition"
              >
                Logout
              </button>

            </div>

          </div>

        </div>

      </header>

      {/* Main */}
      <main className="max-w-5xl mx-auto px-4 py-8">

        {/* Page Heading */}
        <div className="mb-8">

          <button
            onClick={() => navigate('/dashboard')}
            className="text-blue-600 hover:text-blue-700 font-medium mb-4"
          >
            ← Continue Shopping
          </button>

          <h2 className="text-3xl md:text-4xl font-bold text-gray-900">
            My Orders
          </h2>

          <p className="text-gray-500 mt-2">
            View and track your recent orders.
          </p>

        </div>

        {/* Error */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 rounded-2xl p-5">

            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">

              <div>

                <p className="font-semibold text-red-800">
                  Unable to load orders
                </p>

                <p className="text-sm text-red-700 mt-1">
                  {error}
                </p>

              </div>

              <button
                onClick={loadOrders}
                className="px-5 py-2.5 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-medium"
              >
                Try Again
              </button>

            </div>

          </div>
        )}

        {/* Empty State */}
        {!error && orders.length === 0 && (
          <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-10 md:p-16 text-center">

            <div className="w-20 h-20 bg-blue-50 rounded-full flex items-center justify-center mx-auto mb-6 text-4xl">
              📦
            </div>

            <h2 className="text-2xl font-bold text-gray-900 mb-3">
              No orders yet
            </h2>

            <p className="text-gray-500 max-w-md mx-auto mb-8">
              You haven't placed any orders yet.
              Start shopping and your orders will appear here.
            </p>

            <button
              onClick={() => navigate('/dashboard')}
              className="px-8 py-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition font-semibold"
            >
              Start Shopping
            </button>

          </div>
        )}

        {/* Orders */}
        {orders.length > 0 && (
          <div className="space-y-5">

            {/* Order Count */}
            <div className="flex items-center justify-between">

              <p className="text-gray-600">
                <span className="font-semibold text-gray-900">
                  {orders.length}
                </span>{' '}
                order{orders.length !== 1 ? 's' : ''} found
              </p>

              <button
                onClick={loadOrders}
                className="text-sm text-blue-600 hover:text-blue-700 font-medium"
              >
                ↻ Refresh
              </button>

            </div>

            {orders.map((order) => (

              <div
                key={order.id}
                className="bg-white rounded-2xl border border-gray-200 shadow-sm hover:shadow-md transition p-5 md:p-6"
              >

                {/* Top Section */}
                <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-5">

                  <div>

                    <div className="flex flex-wrap items-center gap-3">

                      <h3 className="text-xl font-bold text-gray-900">
                        #{order.orderNumber}
                      </h3>

                      <span
                        className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full border text-sm font-semibold ${getStatusStyles(
                          order.status
                        )}`}
                      >
                        <span>
                          {getStatusIcon(order.status)}
                        </span>

                        {order.status}
                      </span>

                    </div>

                    <p className="text-sm text-gray-500 mt-2">
                      Placed on {formatDate(order.orderedAt)}
                    </p>

                  </div>

                  {/* Total */}
                  <div className="md:text-right">

                    <p className="text-sm text-gray-500">
                      Total Amount
                    </p>

                    <p className="text-2xl font-bold text-blue-600 mt-1">
                      ${order.totalAmount.toFixed(2)}
                    </p>

                  </div>

                </div>

                {/* Divider */}
                <div className="border-t border-gray-100 my-5"></div>

                {/* Order Information */}
                <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-5">

                  {/* Product Preview */}
                  <div className="flex items-center gap-3">

                    <div className="flex">

                      {order.items.slice(0, 4).map((item, index) => (

                        <div
                          key={item.id}
                          className={`w-16 h-16 bg-gray-50 rounded-xl overflow-hidden border border-gray-200 ${
                            index > 0 ? '-ml-2' : ''
                          }`}
                        >

                          {item.productImageUrl ? (
                            <img
                              src={item.productImageUrl}
                              alt={item.productName}
                              className="w-full h-full object-cover"
                            />
                          ) : (
                            <div className="w-full h-full flex items-center justify-center text-2xl">
                              📦
                            </div>
                          )}

                        </div>

                      ))}

                    </div>

                    {order.items.length > 4 && (
                      <span className="text-sm text-gray-500">
                        +{order.items.length - 4} more
                      </span>
                    )}

                  </div>

                  {/* Details */}
                  <div className="flex flex-wrap gap-6 text-sm">

                    <div>

                      <p className="text-gray-500">
                        Products
                      </p>

                      <p className="font-semibold text-gray-900 mt-1">
                        {order.items.length}
                      </p>

                    </div>

                    <div>

                      <p className="text-gray-500">
                        Quantity
                      </p>

                      <p className="font-semibold text-gray-900 mt-1">
                        {getTotalQuantity(order)}
                      </p>

                    </div>

                    <div>

                      <p className="text-gray-500">
                        Status
                      </p>

                      <p className="font-semibold text-gray-900 mt-1">
                        {order.status}
                      </p>

                    </div>

                  </div>

                  {/* View Button */}
                  <button
                    onClick={() =>
                      navigate(`/orders/${order.id}`)
                    }
                    className="w-full lg:w-auto px-6 py-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition font-semibold"
                  >
                    View Details →
                  </button>

                </div>

              </div>

            ))}

          </div>
        )}

      </main>

      {/* Footer */}
      <footer className="bg-gray-900 text-gray-300 mt-12">

        <div className="max-w-7xl mx-auto px-4 py-8 text-center">

          <p className="font-semibold text-white text-lg mb-2">
            ShopHub
          </p>

          <p className="text-sm">
            Your everyday shopping destination.
          </p>

          <p className="text-xs text-gray-500 mt-4">
            © 2026 ShopHub. All rights reserved.
          </p>

        </div>

      </footer>

    </div>
  );
};

export default OrderHistory;