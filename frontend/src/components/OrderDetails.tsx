import { useEffect, useState } from 'react';
import {
  useLocation,
  useNavigate,
  useParams,
} from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import orderService from '../services/order.service';
import { OrderStatus } from '../types/order.types';
import type { OrderResponse } from '../types/order.types';

const OrderDetails = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const { logout } = useAuth();

  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showSuccessMessage, setShowSuccessMessage] =
    useState(false);

  const orderConfirmed = location.state?.orderConfirmed;
  const orderNumber = location.state?.orderNumber;

  useEffect(() => {
    if (orderConfirmed) {
      setShowSuccessMessage(true);

      const timer = setTimeout(() => {
        setShowSuccessMessage(false);
      }, 5000);

      return () => clearTimeout(timer);
    }
  }, [orderConfirmed]);

  useEffect(() => {
    if (id) {
      loadOrder(parseInt(id));
    }
  }, [id]);

  const loadOrder = async (orderId: number) => {
    try {
      setLoading(true);
      setError(null);

      const orderData =
        await orderService.getOrderById(orderId);

      setOrder(orderData);
    } catch (err: any) {
      console.error('Failed to load order:', err);

      if (err.response?.status === 404) {
        setError(
          'Order not found or you do not have permission to view it.'
        );
      } else {
        setError(
          err.response?.data?.message ||
            'Failed to load order. Please try again.'
        );
      }
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getStatusStyles = (
    status: OrderStatus
  ): string => {
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

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">

          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>

          <p className="text-gray-600 font-medium">
            Loading order details...
          </p>

        </div>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="min-h-screen bg-gray-50">

        {/* Header */}
        <header className="bg-white shadow-sm sticky top-0 z-50">
          <div className="max-w-7xl mx-auto px-4 py-4">

            <div className="flex items-center justify-between">

              <button
                onClick={() => navigate('/dashboard')}
                className="text-2xl font-bold text-blue-600 hover:text-blue-700 transition"
              >
                ShopHub
              </button>

              <h1 className="hidden sm:block text-xl font-bold text-gray-900">
                Order Details
              </h1>

              <button
                onClick={handleLogout}
                className="hidden sm:block px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-700 transition"
              >
                Logout
              </button>

            </div>

          </div>
        </header>

        {/* Error */}
        <main className="max-w-2xl mx-auto px-4 py-16">

          <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-10 text-center">

            <div className="text-6xl mb-5">
              😕
            </div>

            <h2 className="text-2xl font-bold text-gray-900 mb-3">
              Order Not Found
            </h2>

            <p className="text-red-600 mb-8">
              {error || 'Order not found.'}
            </p>

            <button
              onClick={() => navigate('/orders')}
              className="px-7 py-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition font-semibold"
            >
              Back to Orders
            </button>

          </div>

        </main>

      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">

      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-50">

        <div className="max-w-7xl mx-auto px-4 py-4">

          <div className="flex items-center justify-between">

            {/* Logo */}
            <button
              onClick={() => navigate('/dashboard')}
              className="text-2xl font-bold text-blue-600 hover:text-blue-700 transition"
            >
              ShopHub
            </button>

            {/* Page title */}
            <h1 className="hidden sm:block text-xl font-bold text-gray-900">
              Order Details
            </h1>

            {/* Right side */}
            <div className="flex items-center gap-4">

              <button
                onClick={() => navigate('/orders')}
                className="text-gray-700 hover:text-blue-600 font-medium transition"
              >
                Orders
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

        {/* Success Message */}
        {showSuccessMessage && orderConfirmed && (
          <div className="mb-6 bg-green-50 border border-green-200 rounded-2xl p-5">

            <div className="flex items-start gap-4">

              <div className="w-10 h-10 rounded-full bg-green-100 text-green-600 flex items-center justify-center text-xl font-bold flex-shrink-0">
                ✓
              </div>

              <div>

                <h2 className="font-bold text-green-800 text-lg">
                  Order Placed Successfully!
                </h2>

                <p className="text-green-700 text-sm mt-1">
                  Your order {orderNumber} has been
                  confirmed.
                </p>

              </div>

            </div>

          </div>
        )}

        {/* Back */}
        <button
          onClick={() => navigate('/orders')}
          className="text-blue-600 hover:text-blue-700 font-medium mb-5"
        >
          ← Back to Orders
        </button>

        {/* Order Header */}
        <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-6 md:p-8 mb-6">

          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-5">

            <div>

              <p className="text-sm text-gray-500 mb-1">
                Order Number
              </p>

              <h2 className="text-2xl md:text-3xl font-bold text-gray-900">
                #{order.orderNumber}
              </h2>

              <p className="text-gray-500 text-sm mt-3">
                Placed on {formatDate(order.orderedAt)}
              </p>

            </div>

            <div
              className={`inline-flex items-center gap-2 px-4 py-2 rounded-full border font-semibold w-fit ${getStatusStyles(
                order.status
              )}`}
            >
              <span>
                {getStatusIcon(order.status)}
              </span>

              <span>
                {order.status}
              </span>
            </div>

          </div>

          {/* Updated */}
          <div className="mt-6 pt-5 border-t border-gray-100">

            <p className="text-sm text-gray-500">
              Last updated
            </p>

            <p className="text-sm font-medium text-gray-800 mt-1">
              {formatDate(order.updatedAt)}
            </p>

          </div>

        </div>

        {/* Order Items */}
        <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-6 md:p-8 mb-6">

          <div className="flex items-center justify-between mb-6">

            <div>

              <h3 className="text-xl font-bold text-gray-900">
                Order Items
              </h3>

              <p className="text-sm text-gray-500 mt-1">
                {order.items.length} product
                {order.items.length !== 1 ? 's' : ''}
              </p>

            </div>

          </div>

          <div className="space-y-5">

            {order.items.map((item) => (

              <div
                key={item.id}
                className="flex flex-col sm:flex-row gap-5 pb-5 border-b border-gray-100 last:border-b-0 last:pb-0"
              >

                {/* Image */}
                <div
                  onClick={() =>
                    navigate(`/products/${item.productId}`)
                  }
                  className="w-full sm:w-28 h-28 bg-gray-50 rounded-xl overflow-hidden flex-shrink-0 cursor-pointer hover:opacity-90 transition"
                >

                  {item.productImageUrl ? (
                    <img
                      src={item.productImageUrl}
                      alt={item.productName}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-4xl">
                      📦
                    </div>
                  )}

                </div>

                {/* Information */}
                <div className="flex-1">

                  <button
                    onClick={() =>
                      navigate(`/products/${item.productId}`)
                    }
                    className="text-lg font-bold text-gray-900 hover:text-blue-600 text-left transition"
                  >
                    {item.productName}
                  </button>

                  <div className="mt-3 grid grid-cols-2 sm:grid-cols-3 gap-4">

                    <div>
                      <p className="text-xs text-gray-500">
                        Price
                      </p>

                      <p className="font-semibold text-gray-800 mt-1">
                        ${item.priceAtPurchase.toFixed(2)}
                      </p>
                    </div>

                    <div>
                      <p className="text-xs text-gray-500">
                        Quantity
                      </p>

                      <p className="font-semibold text-gray-800 mt-1">
                        {item.quantity}
                      </p>
                    </div>

                    <div>
                      <p className="text-xs text-gray-500">
                        Subtotal
                      </p>

                      <p className="font-bold text-blue-600 mt-1">
                        ${item.subtotal.toFixed(2)}
                      </p>
                    </div>

                  </div>

                </div>

              </div>

            ))}

          </div>

        </div>

        {/* Bottom Section */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

          {/* Order Summary */}
          <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-6">

            <h3 className="text-xl font-bold text-gray-900 mb-6">
              Order Summary
            </h3>

            <div className="space-y-4">

              <div className="flex justify-between text-gray-600">
                <span>Total Products</span>
                <span className="font-medium text-gray-900">
                  {order.items.length}
                </span>
              </div>

              <div className="flex justify-between text-gray-600">
                <span>Total Quantity</span>
                <span className="font-medium text-gray-900">
                  {order.items.reduce(
                    (total, item) =>
                      total + item.quantity,
                    0
                  )}
                </span>
              </div>

              <div className="border-t border-gray-200 pt-4">

                <div className="flex justify-between items-center">

                  <span className="text-lg font-bold text-gray-900">
                    Total Amount
                  </span>

                  <span className="text-2xl font-bold text-blue-600">
                    ${order.totalAmount.toFixed(2)}
                  </span>

                </div>

              </div>

            </div>

          </div>

          {/* Order Information */}
          <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-6">

            <h3 className="text-xl font-bold text-gray-900 mb-6">
              Order Information
            </h3>

            <div className="space-y-4">

              <div className="flex items-start gap-3">

                <span className="text-xl">
                  🔒
                </span>

                <div>
                  <p className="font-semibold text-gray-800">
                    Secure Order
                  </p>

                  <p className="text-sm text-gray-500 mt-1">
                    Your order information is securely
                    stored.
                  </p>
                </div>

              </div>

              <div className="flex items-start gap-3">

                <span className="text-xl">
                  📦
                </span>

                <div>
                  <p className="font-semibold text-gray-800">
                    Order Status
                  </p>

                  <p className="text-sm text-gray-500 mt-1">
                    Current status: {order.status}
                  </p>
                </div>

              </div>

              <div className="flex items-start gap-3">

                <span className="text-xl">
                  🚚
                </span>

                <div>
                  <p className="font-semibold text-gray-800">
                    Delivery
                  </p>

                  <p className="text-sm text-gray-500 mt-1">
                    Delivery information will be
                    updated with your order status.
                  </p>
                </div>

              </div>

            </div>

          </div>

        </div>

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row gap-3 mt-8">

          <button
            onClick={() => navigate('/orders')}
            className="flex-1 py-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition font-semibold"
          >
            View All Orders
          </button>

          <button
            onClick={() => navigate('/dashboard')}
            className="flex-1 py-3 border border-gray-300 text-gray-700 rounded-xl hover:bg-white transition font-semibold"
          >
            Continue Shopping
          </button>

        </div>

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

export default OrderDetails;