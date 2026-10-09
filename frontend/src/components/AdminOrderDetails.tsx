import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

interface OrderItem {
  id?: number;
  productId?: number;
  productName?: string;
  productImageUrl?: string;
  imageUrl?: string;
  price?: number;
  quantity?: number;
  subtotal?: number;
}

interface AdminOrder {
  id: number;
  orderNumber?: string;
  status: string;
  totalAmount: number;
  createdAt?: string;
  updatedAt?: string;

  username?: string;
  userEmail?: string;
  customerName?: string;
  customerEmail?: string;

  items?: OrderItem[];
  totalItems?: number;
  totalQuantity?: number;
}

const ORDER_STATUSES = [
  'PENDING',
  'PROCESSING',
  'SHIPPED',
  'DELIVERED',
  'CANCELLED',
];

const AdminOrderDetails = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const { id } = useParams();

  const [order, setOrder] = useState<AdminOrder | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [updating, setUpdating] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  // ========================================
  // FETCH ORDER
  // ========================================
  const fetchOrder = async () => {
    try {
      setLoading(true);
      setError('');

      const response = await api.get<AdminOrder>(
        `/admin/orders/${id}`
      );

      setOrder(response.data);
    } catch (err: any) {
      console.error(
        'Failed to load order:',
        err
      );

      if (err.response?.status === 403) {
        setError(
          'You do not have permission to view this order.'
        );
      } else if (err.response?.status === 404) {
        setError('Order not found.');
      } else {
        setError(
          'Failed to load order details. Please try again.'
        );
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (id) {
      fetchOrder();
    }
  }, [id]);

  // ========================================
  // LOGOUT
  // ========================================
  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // ========================================
  // STATUS COLORS
  // ========================================
  const getStatusClasses = (status: string) => {
    switch (status) {
      case 'PENDING':
        return 'bg-yellow-50 text-yellow-700';

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

  // ========================================
  // UPDATE STATUS
  // ========================================
  const handleStatusChange = async (
    newStatus: string
  ) => {
    if (!order) {
      return;
    }

    try {
      setUpdating(true);
      setError('');
      setSuccessMessage('');

      await api.put(
        `/admin/orders/${order.id}/status`,
        {
          status: newStatus,
        }
      );

      setOrder({
        ...order,
        status: newStatus,
      });

      setSuccessMessage(
        `Order status updated to ${newStatus}.`
      );

    } catch (err: any) {
      console.error(
        'Failed to update order status:',
        err
      );

      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.status === 403) {
        setError(
          'You do not have permission to update this order.'
        );
      } else {
        setError(
          'Failed to update order status. Please try again.'
        );
      }
    } finally {
      setUpdating(false);
    }
  };

  // ========================================
  // HELPERS
  // ========================================
  const getCustomerName = () => {
    if (!order) {
      return 'Customer';
    }

    return (
      order.customerName ||
      order.username ||
      'Customer'
    );
  };

  const getCustomerEmail = () => {
    if (!order) {
      return '—';
    }

    return (
      order.customerEmail ||
      order.userEmail ||
      '—'
    );
  };

  const getProductCount = () => {
    if (!order) {
      return 0;
    }

    if (order.totalItems !== undefined) {
      return order.totalItems;
    }

    return order.items?.length || 0;
  };

  const getTotalQuantity = () => {
    if (!order) {
      return 0;
    }

    if (order.totalQuantity !== undefined) {
      return order.totalQuantity;
    }

    return (
      order.items?.reduce(
        (total, item) =>
          total + Number(item.quantity || 0),
        0
      ) || 0
    );
  };

  const getItemImage = (item: OrderItem) => {
    return (
      item.productImageUrl ||
      item.imageUrl ||
      ''
    );
  };

  const getItemSubtotal = (item: OrderItem) => {
    if (item.subtotal !== undefined) {
      return Number(item.subtotal);
    }

    return (
      Number(item.price || 0) *
      Number(item.quantity || 0)
    );
  };

  // ========================================
  // LOADING
  // ========================================
  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100">

        <header className="bg-white border-b shadow-sm">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 py-4">

            <h1 className="text-2xl font-bold text-gray-900">
              Shop<span className="text-blue-600">Hub</span>
            </h1>

            <p className="text-sm text-gray-500">
              Admin Panel
            </p>

          </div>
        </header>

        <main className="max-w-7xl mx-auto px-4 sm:px-6 py-16">

          <div className="bg-white rounded-xl shadow-sm p-12 text-center">

            <div
              className="animate-spin rounded-full
                         h-10 w-10 border-b-2
                         border-blue-600
                         mx-auto mb-4"
            />

            <p className="text-gray-600">
              Loading order details...
            </p>

          </div>

        </main>

      </div>
    );
  }

  // ========================================
  // ERROR
  // ========================================
  if (error && !order) {
    return (
      <div className="min-h-screen bg-gray-100">

        <header className="bg-white border-b shadow-sm">

          <div className="max-w-7xl mx-auto px-4 sm:px-6 py-4">

            <div className="flex items-center justify-between">

              <div>

                <h1 className="text-2xl font-bold text-gray-900">
                  Shop<span className="text-blue-600">Hub</span>
                </h1>

                <p className="text-sm text-gray-500">
                  Admin Panel
                </p>

              </div>

              <button
                onClick={handleLogout}
                className="px-4 py-2 bg-red-600
                           text-white rounded-lg
                           hover:bg-red-700
                           transition font-medium"
              >
                Logout
              </button>

            </div>

          </div>

        </header>

        <main className="max-w-7xl mx-auto px-4 sm:px-6 py-8">

          <button
            onClick={() => navigate('/admin/orders')}
            className="text-blue-600
                       hover:text-blue-700
                       font-medium mb-6"
          >
            ← Back to Manage Orders
          </button>

          <div className="bg-red-50
                          border border-red-200
                          text-red-700
                          rounded-xl p-6">

            <p className="font-semibold">
              {error}
            </p>

            <button
              onClick={fetchOrder}
              className="mt-3 text-sm
                         font-medium underline"
            >
              Try Again
            </button>

          </div>

        </main>

      </div>
    );
  }

  if (!order) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-100">

      {/* ========================================
          HEADER
      ======================================== */}
      <header
        className="bg-white border-b shadow-sm
                   sticky top-0 z-20"
      >

        <div className="max-w-7xl mx-auto
                        px-4 sm:px-6 py-4">

          <div className="flex items-center
                          justify-between">

            <div>

              <h1 className="text-2xl font-bold
                             text-gray-900">
                Shop<span className="text-blue-600">
                  Hub
                </span>
              </h1>

              <p className="text-sm text-gray-500">
                Admin Panel
              </p>

            </div>

            <div className="flex items-center gap-4">

              <div className="hidden sm:block text-right">

                <p className="text-sm font-semibold
                              text-gray-800">
                  {user?.username}
                </p>

                <p className="text-xs text-gray-500">
                  Administrator
                </p>

              </div>

              <button
                onClick={handleLogout}
                className="px-4 py-2 bg-red-600
                           text-white rounded-lg
                           hover:bg-red-700
                           transition font-medium"
              >
                Logout
              </button>

            </div>

          </div>

        </div>

      </header>

      {/* ========================================
          MAIN
      ======================================== */}
      <main className="max-w-7xl mx-auto
                       px-4 sm:px-6 py-8">

        {/* Back */}
        <button
          onClick={() => navigate('/admin/orders')}
          className="text-blue-600
                     hover:text-blue-700
                     font-medium mb-6"
        >
          ← Back to Manage Orders
        </button>

        {/* Heading */}
        <div className="mb-8">

          <p className="text-sm text-gray-500 mb-2">
            Order Details
          </p>

          <div className="flex flex-col
                          lg:flex-row
                          lg:items-center
                          lg:justify-between
                          gap-4">

            <div>

              <h2 className="text-3xl font-bold
                             text-gray-900">
                {order.orderNumber ||
                  `Order #${order.id}`}
              </h2>

              {order.createdAt && (
                <p className="text-gray-500 mt-2">
                  Placed on{' '}
                  {new Date(
                    order.createdAt
                  ).toLocaleDateString(
                    'en-IN',
                    {
                      day: '2-digit',
                      month: 'long',
                      year: 'numeric',
                    }
                  )}{' '}
                  at{' '}
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

            </div>

            {/* Status */}
            <div className="flex flex-col
                            sm:flex-row
                            sm:items-center
                            gap-3">

              <span
                className={`px-4 py-2 rounded-full
                            text-sm font-semibold
                            ${getStatusClasses(
                              order.status
                            )}`}
              >
                {order.status}
              </span>

              <select
                value={order.status}
                disabled={updating}
                onChange={(e) =>
                  handleStatusChange(
                    e.target.value
                  )
                }
                className="px-4 py-3
                           bg-white
                           border border-gray-300
                           rounded-lg
                           text-sm font-medium
                           focus:outline-none
                           focus:ring-2
                           focus:ring-blue-500
                           disabled:opacity-50"
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

          </div>

        </div>

        {/* Success */}
        {successMessage && (
          <div className="mb-6 bg-green-50
                          border border-green-200
                          text-green-700
                          rounded-xl p-4">

            <p className="font-medium">
              ✓ {successMessage}
            </p>

          </div>
        )}

        {/* Error */}
        {error && (
          <div className="mb-6 bg-red-50
                          border border-red-200
                          text-red-700
                          rounded-xl p-4">

            <p className="font-medium">
              {error}
            </p>

          </div>
        )}

        {/* ========================================
            CUSTOMER + ORDER SUMMARY
        ======================================== */}
        <div className="grid grid-cols-1
                        lg:grid-cols-3
                        gap-6 mb-8">

          {/* Customer */}
          <div className="bg-white rounded-xl
                          shadow-sm p-6">

            <div className="flex items-center
                            gap-3 mb-5">

              <div className="w-11 h-11
                              rounded-lg
                              bg-blue-100
                              flex items-center
                              justify-center">

                <span className="text-xl">
                  👤
                </span>

              </div>

              <div>

                <h3 className="font-bold
                               text-gray-900">
                  Customer
                </h3>

                <p className="text-xs
                              text-gray-500">
                  Customer information
                </p>

              </div>

            </div>

            <div className="space-y-3">

              <div>

                <p className="text-xs
                              text-gray-500">
                  Name
                </p>

                <p className="font-medium
                              text-gray-900">
                  {getCustomerName()}
                </p>

              </div>

              <div>

                <p className="text-xs
                              text-gray-500">
                  Email
                </p>

                <p className="font-medium
                              text-gray-900
                              break-all">
                  {getCustomerEmail()}
                </p>

              </div>

            </div>

          </div>

          {/* Products */}
          <div className="bg-white rounded-xl
                          shadow-sm p-6">

            <div className="flex items-center
                            gap-3 mb-5">

              <div className="w-11 h-11
                              rounded-lg
                              bg-purple-100
                              flex items-center
                              justify-center">

                <span className="text-xl">
                  📦
                </span>

              </div>

              <div>

                <h3 className="font-bold
                               text-gray-900">
                  Products
                </h3>

                <p className="text-xs
                              text-gray-500">
                  Items in this order
                </p>

              </div>

            </div>

            <div className="space-y-3">

              <div className="flex
                              justify-between">

                <span className="text-gray-500">
                  Products
                </span>

                <span className="font-semibold
                                 text-gray-900">
                  {getProductCount()}
                </span>

              </div>

              <div className="flex
                              justify-between">

                <span className="text-gray-500">
                  Total Quantity
                </span>

                <span className="font-semibold
                                 text-gray-900">
                  {getTotalQuantity()}
                </span>

              </div>

            </div>

          </div>

          {/* Total */}
          <div className="bg-white rounded-xl
                          shadow-sm p-6">

            <div className="flex items-center
                            gap-3 mb-5">

              <div className="w-11 h-11
                              rounded-lg
                              bg-green-100
                              flex items-center
                              justify-center">

                <span className="text-xl">
                  💰
                </span>

              </div>

              <div>

                <h3 className="font-bold
                               text-gray-900">
                  Order Total
                </h3>

                <p className="text-xs
                              text-gray-500">
                  Total payable amount
                </p>

              </div>

            </div>

            <p className="text-3xl font-bold
                          text-gray-900">

              $
              {Number(
                order.totalAmount
              ).toFixed(2)}

            </p>

          </div>

        </div>

        {/* ========================================
            ORDER ITEMS
        ======================================== */}
        <div className="bg-white rounded-xl
                        shadow-sm overflow-hidden">

          <div className="px-6 py-5 border-b">

            <h3 className="text-xl font-bold
                           text-gray-900">
              Ordered Products
            </h3>

            <p className="text-sm text-gray-500 mt-1">
              Products included in this order.
            </p>

          </div>

          {order.items &&
          order.items.length > 0 ? (

            <div className="divide-y">

              {order.items.map(
                (item, index) => {

                  const image =
                    getItemImage(item);

                  const quantity =
                    Number(
                      item.quantity || 0
                    );

                  const price =
                    Number(
                      item.price || 0
                    );

                  const subtotal =
                    getItemSubtotal(item);

                  return (
                    <div
                      key={
                        item.id ||
                        item.productId ||
                        index
                      }
                      className="p-6"
                    >

                      <div className="flex
                                      flex-col
                                      sm:flex-row
                                      gap-5">

                        {/* Image */}
                        <div
                          className="w-24 h-24
                                     rounded-lg
                                     bg-gray-100
                                     flex-shrink-0
                                     overflow-hidden"
                        >

                          {image ? (
                            <img
                              src={image}
                              alt={
                                item.productName ||
                                'Product'
                              }
                              className="w-full
                                         h-full
                                         object-cover"
                            />
                          ) : (
                            <div
                              className="w-full
                                         h-full
                                         flex
                                         items-center
                                         justify-center"
                            >
                              <span className="text-3xl">
                                🛍️
                              </span>
                            </div>
                          )}

                        </div>

                        {/* Product */}
                        <div className="flex-1">

                          <h4 className="text-lg
                                         font-bold
                                         text-gray-900">
                            {item.productName ||
                              'Product'}
                          </h4>

                          <p className="text-sm
                                        text-gray-500
                                        mt-1">
                            Product ID:{' '}
                            {item.productId ||
                              '—'}
                          </p>

                          <div className="flex
                                          flex-wrap
                                          gap-6 mt-4">

                            <div>

                              <p className="text-xs
                                            text-gray-500">
                                Unit Price
                              </p>

                              <p className="font-semibold
                                            text-gray-900">
                                $
                                {price.toFixed(2)}
                              </p>

                            </div>

                            <div>

                              <p className="text-xs
                                            text-gray-500">
                                Quantity
                              </p>

                              <p className="font-semibold
                                            text-gray-900">
                                {quantity}
                              </p>

                            </div>

                            <div>

                              <p className="text-xs
                                            text-gray-500">
                                Subtotal
                              </p>

                              <p className="font-bold
                                            text-gray-900">
                                $
                                {subtotal.toFixed(2)}
                              </p>

                            </div>

                          </div>

                        </div>

                      </div>

                    </div>
                  );
                }
              )}

            </div>

          ) : (

            <div className="p-10 text-center">

              <p className="text-gray-500">
                No product information available
                for this order.
              </p>

            </div>

          )}

          {/* ========================================
              ORDER TOTAL FOOTER
          ======================================== */}
          <div className="border-t bg-gray-50
                          px-6 py-6">

            <div className="max-w-md ml-auto">

              <div className="flex
                              justify-between
                              text-gray-600 mb-3">

                <span>
                  Total Products
                </span>

                <span>
                  {getProductCount()}
                </span>

              </div>

              <div className="flex
                              justify-between
                              text-gray-600 mb-4">

                <span>
                  Total Quantity
                </span>

                <span>
                  {getTotalQuantity()}
                </span>

              </div>

              <div className="border-t
                              pt-4 flex
                              justify-between">

                <span className="text-lg
                                 font-bold
                                 text-gray-900">
                  Total Amount
                </span>

                <span className="text-2xl
                                 font-bold
                                 text-blue-600">
                  $
                  {Number(
                    order.totalAmount
                  ).toFixed(2)}
                </span>

              </div>

            </div>

          </div>

        </div>

        {/* ========================================
            BOTTOM ACTIONS
        ======================================== */}
        <div className="flex flex-col
                        sm:flex-row
                        gap-3 mt-8">

          <button
            onClick={() =>
              navigate('/admin/orders')
            }
            className="px-5 py-3
                       bg-white
                       border border-gray-300
                       text-gray-700
                       rounded-lg
                       hover:bg-gray-50
                       transition
                       font-medium"
          >
            ← Back to Orders
          </button>

          <button
            onClick={() =>
              navigate('/admin')
            }
            className="px-5 py-3
                       bg-blue-600
                       text-white
                       rounded-lg
                       hover:bg-blue-700
                       transition
                       font-medium"
          >
            Admin Dashboard
          </button>

        </div>

      </main>

      {/* ========================================
          FOOTER
      ======================================== */}
      <footer className="border-t bg-white mt-12">

        <div className="max-w-7xl mx-auto
                        px-4 sm:px-6 py-6">

          <p className="text-center
                        text-sm text-gray-500">
            © 2026 ShopHub Admin Panel
          </p>

        </div>

      </footer>

    </div>
  );
};

export default AdminOrderDetails;