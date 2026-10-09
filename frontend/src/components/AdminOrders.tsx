import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  productImageUrl?: string;
  price: number;
  quantity: number;
  subtotal: number;
}

interface AdminOrder {
  id: number;
  orderNumber: string;
  status: string;
  totalAmount: number;
  createdAt: string;
  updatedAt?: string;
  items?: OrderItem[];
  totalItems?: number;
  totalQuantity?: number;
  userId?: number;
  username?: string;
  userEmail?: string;
}

const ORDER_STATUSES = [
  'PENDING',
  'PROCESSING',
  'SHIPPED',
  'DELIVERED',
  'CANCELLED',
];

const AdminOrders = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [orders, setOrders] = useState<AdminOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [updatingOrderId, setUpdatingOrderId] = useState<number | null>(
    null
  );
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    fetchOrders();
  }, []);

  // ============================
  // FETCH ORDERS
  // ============================
  const fetchOrders = async () => {
    try {
      setLoading(true);
      setError('');

      const response = await api.get<AdminOrder[]>('/admin/orders');

      setOrders(response.data);
    } catch (err: any) {
      console.error('Failed to load orders:', err);

      if (err.response?.status === 403) {
        setError(
          'You do not have permission to access orders.'
        );
      } else {
        setError(
          'Failed to load orders. Please try again.'
        );
      }
    } finally {
      setLoading(false);
    }
  };

  // ============================
  // LOGOUT
  // ============================
  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // ============================
  // STATUS COLOR
  // ============================
  const getStatusClasses = (status: string) => {
    switch (status) {
      case 'PENDING':
        return 'bg-yellow-50 text-yellow-700';

      case 'CONFIRMED':
        return 'bg-blue-50 text-blue-700';

      case 'PROCESSING':
        return 'bg-purple-50 text-purple-700';

      case 'SHIPPED':
        return 'bg-indigo-50 text-indigo-700';

      case 'DELIVERED':
        return 'bg-green-50 text-green-700';

      case 'CANCELLED':
        return 'bg-red-50 text-red-700';

      default:
        return 'bg-gray-100 text-gray-700';
    }
  };

  // ============================
  // UPDATE ORDER STATUS
  // ============================
  const handleStatusChange = async (
    orderId: number,
    newStatus: string
  ) => {
    try {
      setUpdatingOrderId(orderId);
      setError('');
      setSuccessMessage('');

      await api.put(
        `/admin/orders/${orderId}/status`,
        {
          status: newStatus,
        }
      );

      setSuccessMessage(
        `Order status updated to ${newStatus}.`
      );

      await fetchOrders();

    } catch (err: any) {
      console.error(
        'Failed to update order status:',
        err
      );

      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.status === 403) {
        setError(
          'You do not have permission to update order status.'
        );
      } else {
        setError(
          'Failed to update order status. Please try again.'
        );
      }
    } finally {
      setUpdatingOrderId(null);
    }
  };

  // ============================
  // VIEW ORDER DETAILS
  // ============================
  const handleViewOrder = (orderId: number) => {
    navigate(`/admin/orders/${orderId}`);
  };

  const pendingCount = orders.filter(
    (order) => order.status === 'PENDING'
  ).length;

 

  const processingCount = orders.filter(
    (order) => order.status === 'PROCESSING'
  ).length;

  const deliveredCount = orders.filter(
    (order) => order.status === 'DELIVERED'
  ).length;

  return (
    <div className="min-h-screen bg-gray-100">

      {/* ============================
          HEADER
      ============================ */}
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

            {/* Admin */}
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
                className="px-4 py-2 bg-red-600 text-white
                           rounded-lg hover:bg-red-700
                           transition font-medium"
              >
                Logout
              </button>

            </div>

          </div>

        </div>

      </header>

      {/* ============================
          MAIN
      ============================ */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 py-8">

        {/* Back */}
        <button
          onClick={() => navigate('/admin')}
          className="text-blue-600 hover:text-blue-700
                     font-medium mb-6"
        >
          ← Back to Admin Dashboard
        </button>

        {/* Heading */}
        <div className="flex flex-col sm:flex-row
                        sm:items-center
                        sm:justify-between
                        gap-4 mb-8">

          <div>

            <h2 className="text-3xl font-bold text-gray-900">
              Manage Orders
            </h2>

            <p className="text-gray-500 mt-1">
              View customer orders and manage order status.
            </p>

          </div>

          <button
            onClick={fetchOrders}
            disabled={loading}
            className="px-5 py-3 border border-gray-300
                       bg-white text-gray-700 rounded-lg
                       hover:bg-gray-50 transition
                       font-medium disabled:opacity-50"
          >
            ↻ Refresh Orders
          </button>

        </div>

        {/* ============================
            SUCCESS MESSAGE
        ============================ */}
        {successMessage && (
          <div className="mb-6 bg-green-50
                          border border-green-200
                          text-green-700 rounded-xl p-4">

            <p className="font-medium">
              ✓ {successMessage}
            </p>

          </div>
        )}

        {/* ============================
            ERROR MESSAGE
        ============================ */}
        {error && (
          <div className="mb-6 bg-red-50
                          border border-red-200
                          text-red-700 rounded-xl p-4">

            <p className="font-medium">
              {error}
            </p>

            <button
              onClick={fetchOrders}
              className="mt-2 text-sm font-medium underline"
            >
              Try Again
            </button>

          </div>
        )}

        {/* ============================
            SUMMARY CARDS
        ============================ */}
        {!loading && !error && (
          <div className="grid grid-cols-2
                          md:grid-cols-4 gap-4 mb-8">

            {/* Total */}
            <div className="bg-white rounded-xl
                            shadow-sm p-5">

              <p className="text-sm text-gray-500">
                Total Orders
              </p>

              <p className="text-3xl font-bold
                            text-gray-900 mt-2">
                {orders.length}
              </p>

            </div>

            {/* Pending */}
            <div className="bg-white rounded-xl
                            shadow-sm p-5">

              <p className="text-sm text-gray-500">
                Pending
              </p>

              <p className="text-3xl font-bold
                            text-yellow-600 mt-2">
                {pendingCount}
              </p>

            </div>

            {/* Processing */}
            <div className="bg-white rounded-xl
                            shadow-sm p-5">

              <p className="text-sm text-gray-500">
                Processing
              </p>

              <p className="text-3xl font-bold
                            text-purple-600 mt-2">
                {processingCount}
              </p>

            </div>

            {/* Delivered */}
            <div className="bg-white rounded-xl
                            shadow-sm p-5">

              <p className="text-sm text-gray-500">
                Delivered
              </p>

              <p className="text-3xl font-bold
                            text-green-600 mt-2">
                {deliveredCount}
              </p>

            </div>

          </div>
        )}

        {/* ============================
            LOADING
        ============================ */}
        {loading && (
          <div className="bg-white rounded-xl
                          shadow-sm p-12 text-center">

            <div className="animate-spin rounded-full
                            h-10 w-10 border-b-2
                            border-blue-600 mx-auto mb-4">
            </div>

            <p className="text-gray-600">
              Loading orders...
            </p>

          </div>
        )}

        {/* ============================
            EMPTY STATE
        ============================ */}
        {!loading && !error && orders.length === 0 && (
          <div className="bg-white rounded-xl
                          shadow-sm p-12 text-center">

            <div className="text-5xl mb-4">
              📦
            </div>

            <h3 className="text-xl font-bold text-gray-900">
              No orders found
            </h3>

            <p className="text-gray-500 mt-2">
              Customer orders will appear here.
            </p>

          </div>
        )}

        {/* ============================
            DESKTOP ORDERS TABLE
        ============================ */}
        {!loading && !error && orders.length > 0 && (
          <div className="bg-white rounded-xl
                          shadow-sm overflow-hidden">

            <div className="hidden md:block overflow-x-auto">

              <table className="w-full">

                <thead className="bg-gray-50 border-b">

                  <tr>

                    <th className="text-left px-6 py-4
                                   text-xs font-semibold
                                   text-gray-500 uppercase">
                      Order
                    </th>

                    <th className="text-left px-6 py-4
                                   text-xs font-semibold
                                   text-gray-500 uppercase">
                      Customer
                    </th>

                    <th className="text-left px-6 py-4
                                   text-xs font-semibold
                                   text-gray-500 uppercase">
                      Date
                    </th>

                    <th className="text-left px-6 py-4
                                   text-xs font-semibold
                                   text-gray-500 uppercase">
                      Amount
                    </th>

                    <th className="text-left px-6 py-4
                                   text-xs font-semibold
                                   text-gray-500 uppercase">
                      Status
                    </th>

                    <th className="text-left px-6 py-4
                                   text-xs font-semibold
                                   text-gray-500 uppercase">
                      Actions
                    </th>

                  </tr>

                </thead>

                <tbody className="divide-y">

                  {orders.map((order) => (

                    <tr
                      key={order.id}
                      className="hover:bg-gray-50 transition"
                    >

                      {/* Order */}
                      <td className="px-6 py-5">

                        <p className="font-bold text-gray-900">
                          {order.orderNumber ||
                            `Order #${order.id}`}
                        </p>

                        <p className="text-sm text-gray-500 mt-1">
                          {order.totalQuantity ??
                            order.totalItems ??
                            order.items?.reduce(
                              (sum, item) =>
                                sum + item.quantity,
                              0
                            ) ??
                            0}{' '}
                          items
                        </p>

                      </td>

                      {/* Customer */}
                      <td className="px-6 py-5">

                        <p className="font-medium text-gray-900">
                          {order.username ||
                            'Customer'}
                        </p>

                        {order.userEmail && (
                          <p className="text-sm text-gray-500 mt-1">
                            {order.userEmail}
                          </p>
                        )}

                      </td>

                      {/* Date */}
                      <td className="px-6 py-5">

                        <p className="text-sm text-gray-700">
                          {order.createdAt
                            ? new Date(
                                order.createdAt
                              ).toLocaleDateString(
                                'en-IN',
                                {
                                  day: '2-digit',
                                  month: 'short',
                                  year: 'numeric',
                                }
                              )
                            : '—'}
                        </p>

                        {order.createdAt && (
                          <p className="text-xs text-gray-500 mt-1">
                            {new Date(
                              order.createdAt
                            ).toLocaleTimeString(
                              'en-IN',
                              {
                                hour: '2-digit',
                                minute: '2-digit',
                              }
                            )}
                          </p>
                        )}

                      </td>

                      {/* Amount */}
                      <td className="px-6 py-5">

                        <p className="font-bold text-gray-900">
                          $
                          {Number(
                            order.totalAmount
                          ).toFixed(2)}
                        </p>

                      </td>

                      {/* Status */}
                      <td className="px-6 py-5">

                        <select
                          value={order.status}
                          disabled={
                            updatingOrderId === order.id
                          }
                          onChange={(e) =>
                            handleStatusChange(
                              order.id,
                              e.target.value
                            )
                          }
                          className={`px-3 py-2 rounded-lg
                                      text-sm font-medium
                                      border-0
                                      focus:ring-2
                                      focus:ring-blue-500
                                      ${getStatusClasses(
                                        order.status
                                      )}`}
                        >

                          {ORDER_STATUSES.map(
                            (status) => (
                              <option
                                key={status}
                                value={status}
                              >
                                {status}
                              </option>
                            )
                          )}

                        </select>

                        {updatingOrderId ===
                          order.id && (
                          <p className="text-xs
                                        text-gray-500
                                        mt-1">
                            Updating...
                          </p>
                        )}

                      </td>

                      {/* Actions */}
                      <td className="px-6 py-5">

                        <button
                          onClick={() =>
                            handleViewOrder(order.id)
                          }
                          className="px-4 py-2
                                     bg-blue-50
                                     text-blue-700
                                     rounded-lg
                                     hover:bg-blue-100
                                     text-sm
                                     font-medium
                                     transition"
                        >
                          View Details
                        </button>

                      </td>

                    </tr>

                  ))}

                </tbody>

              </table>

            </div>

            {/* ============================
                MOBILE ORDER CARDS
            ============================ */}
            <div className="md:hidden divide-y">

              {orders.map((order) => (

                <div
                  key={order.id}
                  className="p-5"
                >

                  {/* Top */}
                  <div className="flex items-start
                                  justify-between gap-3">

                    <div>

                      <p className="font-bold
                                    text-gray-900">
                        {order.orderNumber ||
                          `Order #${order.id}`}
                      </p>

                      <p className="text-sm
                                    text-gray-500 mt-1">
                        {order.createdAt
                          ? new Date(
                              order.createdAt
                            ).toLocaleDateString(
                              'en-IN',
                              {
                                day: '2-digit',
                                month: 'short',
                                year: 'numeric',
                              }
                            )
                          : '—'}
                      </p>

                    </div>

                    <span
                      className={`px-3 py-1
                                  rounded-full
                                  text-xs
                                  font-semibold
                                  ${getStatusClasses(
                                    order.status
                                  )}`}
                    >
                      {order.status}
                    </span>

                  </div>

                  {/* Customer */}
                  <div className="mt-4">

                    <p className="text-sm
                                  text-gray-500">
                      Customer
                    </p>

                    <p className="font-medium
                                  text-gray-900">
                      {order.username ||
                        'Customer'}
                    </p>

                    {order.userEmail && (
                      <p className="text-sm
                                    text-gray-500">
                        {order.userEmail}
                      </p>
                    )}

                  </div>

                  {/* Amount */}
                  <div className="flex items-center
                                  justify-between mt-4">

                    <div>

                      <p className="text-sm
                                    text-gray-500">
                        Total Amount
                      </p>

                      <p className="text-xl
                                    font-bold
                                    text-gray-900">
                        $
                        {Number(
                          order.totalAmount
                        ).toFixed(2)}
                      </p>

                    </div>

                    <div className="text-right">

                      <p className="text-sm
                                    text-gray-500">
                        Items
                      </p>

                      <p className="font-semibold
                                    text-gray-900">
                        {order.totalQuantity ??
                          order.totalItems ??
                          order.items?.reduce(
                            (sum, item) =>
                              sum + item.quantity,
                            0
                          ) ??
                          0}
                      </p>

                    </div>

                  </div>

                  {/* Status */}
                  <div className="mt-4">

                    <label className="block text-sm
                                       font-medium
                                       text-gray-700 mb-2">
                      Update Status
                    </label>

                    <select
                      value={order.status}
                      disabled={
                        updatingOrderId === order.id
                      }
                      onChange={(e) =>
                        handleStatusChange(
                          order.id,
                          e.target.value
                        )
                      }
                      className={`w-full px-4 py-3
                                  rounded-lg
                                  border-0
                                  text-sm
                                  font-medium
                                  focus:ring-2
                                  focus:ring-blue-500
                                  ${getStatusClasses(
                                    order.status
                                  )}`}
                    >

                      {ORDER_STATUSES.map(
                        (status) => (
                          <option
                            key={status}
                            value={status}
                          >
                            {status}
                          </option>
                        )
                      )}

                    </select>

                  </div>

                  {/* View Details */}
                  <button
                    onClick={() =>
                      handleViewOrder(order.id)
                    }
                    className="w-full mt-4
                               px-4 py-3
                               bg-blue-600
                               text-white
                               rounded-lg
                               hover:bg-blue-700
                               transition
                               font-medium"
                  >
                    View Order Details
                  </button>

                </div>

              ))}

            </div>

          </div>
        )}

      </main>

      {/* ============================
          FOOTER
      ============================ */}
      <footer className="border-t bg-white mt-12">

        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6">

          <p className="text-center
                        text-sm text-gray-500">
            © 2026 ShopHub Admin Panel
          </p>

        </div>

      </footer>

    </div>
  );
};

export default AdminOrders;
