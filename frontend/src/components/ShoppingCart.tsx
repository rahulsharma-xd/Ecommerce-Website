import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { useState } from 'react';

const ShoppingCart = () => {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const { cart, loading, error, updateCartItem, removeCartItem } = useCart();

  const [updatingItemId, setUpdatingItemId] = useState<number | null>(null);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleHomeClick = () => {
    navigate('/dashboard');
  };

  const handleUpdateQuantity = async (
    itemId: number,
    newQuantity: number
  ) => {
    if (newQuantity < 1) {
      return;
    }

    try {
      setUpdatingItemId(itemId);
      await updateCartItem(itemId, newQuantity);
    } catch (err) {
      console.error('Failed to update quantity:', err);
    } finally {
      setUpdatingItemId(null);
    }
  };

  const handleRemoveItem = async (itemId: number) => {
    if (
      window.confirm(
        'Are you sure you want to remove this item from your cart?'
      )
    ) {
      try {
        setUpdatingItemId(itemId);
        await removeCartItem(itemId);
      } catch (err) {
        console.error('Failed to remove item:', err);
      } finally {
        setUpdatingItemId(null);
      }
    }
  };

  const handleProductClick = (productId: number) => {
    navigate(`/products/${productId}`);
  };

  if (loading && !cart) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>

          <p className="text-gray-600 font-medium">
            Loading cart...
          </p>
        </div>
      </div>
    );
  }

  const isEmpty = !cart || cart.items.length === 0;

  return (
    <div className="min-h-screen bg-gray-50">

      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 py-4">

          <div className="flex items-center justify-between">

            {/* Logo */}
            <button
              onClick={handleHomeClick}
              className="text-2xl font-bold text-blue-600 hover:text-blue-700 transition"
            >
              ShopHub
            </button>

            {/* Right side */}
            <div className="flex items-center gap-4">

              <button
                onClick={() => navigate('/orders')}
                className="hidden sm:block text-gray-700 hover:text-blue-600 font-medium transition"
              >
                Orders
              </button>

              {/* Cart */}
              <button
                onClick={() => navigate('/cart')}
                className="relative text-gray-700 hover:text-blue-600 text-2xl transition"
                aria-label="Shopping Cart"
              >
                🛒

                {cart && cart.totalItems > 0 && (
                  <span className="absolute -top-2 -right-3 bg-red-500 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center">
                    {cart.totalItems}
                  </span>
                )}
              </button>

              {/* Logout */}
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
      <main className="max-w-7xl mx-auto px-4 py-8">

        {/* Page Heading */}
        <div className="mb-8">

          <button
            onClick={handleHomeClick}
            className="text-blue-600 hover:text-blue-700 font-medium mb-4"
          >
            ← Continue Shopping
          </button>

          <h1 className="text-3xl md:text-4xl font-bold text-gray-900">
            Shopping Cart
          </h1>

          {!isEmpty && (
            <p className="text-gray-500 mt-2">
              {cart?.totalItems} item
              {cart?.totalItems !== 1 ? 's' : ''} in your cart
            </p>
          )}

        </div>

        {/* Error */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-5 py-4 rounded-xl">
            {error}
          </div>
        )}

        {/* Empty Cart */}
        {!loading && isEmpty && (
          <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-12 md:p-16 text-center">

            <div className="text-7xl mb-6">
              🛒
            </div>

            <h2 className="text-2xl font-bold text-gray-900 mb-3">
              Your cart is empty
            </h2>

            <p className="text-gray-500 mb-8 max-w-md mx-auto">
              Looks like you haven't added anything to your cart yet.
              Explore our products and find something you love.
            </p>

            <button
              onClick={handleHomeClick}
              className="px-8 py-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition font-semibold"
            >
              Start Shopping
            </button>

          </div>
        )}

        {/* Cart */}
        {cart && cart.items.length > 0 && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

            {/* Cart Items */}
            <div className="lg:col-span-2 space-y-5">

              {cart.items.map((item) => {

                const isUpdating = updatingItemId === item.id;

                return (
                  <div
                    key={item.id}
                    className="bg-white rounded-2xl border border-gray-200 shadow-sm hover:shadow-md transition p-5 md:p-6"
                  >

                    <div className="flex flex-col sm:flex-row gap-5">

                      {/* Product Image */}
                      <div
                        onClick={() =>
                          handleProductClick(item.productId)
                        }
                        className="w-full sm:w-36 h-48 sm:h-36 bg-gray-50 rounded-xl overflow-hidden flex-shrink-0 cursor-pointer hover:opacity-90 transition"
                      >

                        {item.productImageUrl ? (
                          <img
                            src={item.productImageUrl}
                            alt={item.productName}
                            className="w-full h-full object-cover"
                          />
                        ) : (
                          <div className="w-full h-full flex items-center justify-center text-5xl">
                            📦
                          </div>
                        )}

                      </div>

                      {/* Product Details */}
                      <div className="flex-1">

                        <div className="flex flex-col md:flex-row md:justify-between gap-3">

                          <div>

                            <button
                              onClick={() =>
                                handleProductClick(item.productId)
                              }
                              className="text-xl font-bold text-gray-900 hover:text-blue-600 text-left transition"
                            >
                              {item.productName}
                            </button>

                            <p className="text-gray-500 text-sm mt-2">
                              ${item.priceAtAddition.toFixed(2)} each
                            </p>

                          </div>

                          {/* Item Subtotal */}
                          <div className="md:text-right">

                            <p className="text-sm text-gray-500">
                              Subtotal
                            </p>

                            <p className="text-xl font-bold text-gray-900">
                              ${item.subtotal.toFixed(2)}
                            </p>

                          </div>

                        </div>

                        {/* Divider */}
                        <div className="border-t border-gray-100 my-5"></div>

                        {/* Quantity + Remove */}
                        <div className="flex flex-wrap items-center justify-between gap-4">

                          {/* Quantity */}
                          <div>

                            <p className="text-sm font-medium text-gray-600 mb-2">
                              Quantity
                            </p>

                            <div className="flex items-center border border-gray-300 rounded-lg overflow-hidden">

                              <button
                                onClick={() =>
                                  handleUpdateQuantity(
                                    item.id,
                                    item.quantity - 1
                                  )
                                }
                                disabled={
                                  item.quantity <= 1 ||
                                  isUpdating
                                }
                                className="w-10 h-10 bg-gray-50 hover:bg-gray-100 disabled:bg-gray-100 disabled:text-gray-300 text-lg font-bold transition"
                              >
                                −
                              </button>

                              <div className="w-12 h-10 flex items-center justify-center border-x border-gray-300 font-semibold text-gray-800">
                                {isUpdating ? '...' : item.quantity}
                              </div>

                              <button
                                onClick={() =>
                                  handleUpdateQuantity(
                                    item.id,
                                    item.quantity + 1
                                  )
                                }
                                disabled={isUpdating}
                                className="w-10 h-10 bg-gray-50 hover:bg-gray-100 disabled:bg-gray-100 disabled:text-gray-300 text-lg font-bold transition"
                              >
                                +
                              </button>

                            </div>

                          </div>

                          {/* Remove */}
                          <button
                            onClick={() =>
                              handleRemoveItem(item.id)
                            }
                            disabled={isUpdating}
                            className="text-red-600 hover:text-red-700 font-medium text-sm disabled:text-gray-400 transition"
                          >
                            {isUpdating
                              ? 'Updating...'
                              : 'Remove item'}
                          </button>

                        </div>

                      </div>

                    </div>

                  </div>
                );
              })}

            </div>

            {/* Cart Summary */}
            <div className="lg:col-span-1">

              <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-6 lg:sticky lg:top-24">

                <h2 className="text-xl font-bold text-gray-900 mb-6">
                  Order Summary
                </h2>

                {/* Items */}
                <div className="space-y-4">

                  <div className="flex justify-between text-gray-600">
                    <span>Items</span>
                    <span className="font-medium text-gray-900">
                      {cart.totalItems}
                    </span>
                  </div>

                  <div className="flex justify-between text-gray-600">
                    <span>Subtotal</span>
                    <span className="font-medium text-gray-900">
                      ${cart.totalPrice.toFixed(2)}
                    </span>
                  </div>

                  <div className="flex justify-between text-gray-600">
                    <span>Delivery</span>
                    <span className="text-green-600 font-medium">
                      Free
                    </span>
                  </div>

                </div>

                {/* Total */}
                <div className="border-t border-gray-200 mt-6 pt-6">

                  <div className="flex justify-between items-center">

                    <span className="text-lg font-bold text-gray-900">
                      Total
                    </span>

                    <span className="text-2xl font-bold text-blue-600">
                      ${cart.totalPrice.toFixed(2)}
                    </span>

                  </div>

                </div>

                {/* Checkout */}
                <button
                  onClick={() => navigate('/checkout')}
                  className="w-full mt-6 py-4 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition font-semibold text-lg"
                >
                  Proceed to Checkout
                </button>

                {/* Continue Shopping */}
                <button
                  onClick={handleHomeClick}
                  className="w-full mt-3 py-3 border border-gray-300 text-gray-700 rounded-xl hover:bg-gray-50 transition font-medium"
                >
                  Continue Shopping
                </button>

                {/* Secure Shopping */}
                <div className="mt-6 pt-5 border-t border-gray-100">

                  <div className="flex items-center gap-3 text-sm text-gray-500">
                    <span className="text-lg">🔒</span>
                    <span>Secure checkout</span>
                  </div>

                  <div className="flex items-center gap-3 text-sm text-gray-500 mt-3">
                    <span className="text-lg">🚚</span>
                    <span>Fast delivery available</span>
                  </div>

                </div>

              </div>

            </div>

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

export default ShoppingCart;