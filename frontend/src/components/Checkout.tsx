import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import orderService from '../services/order.service';
import type { CheckoutRequest } from '../types/order.types';

const Checkout = () => {
  const navigate = useNavigate();

  const { logout } = useAuth();
  const { cart, loading: cartLoading } = useCart();

  const [formData, setFormData] = useState<CheckoutRequest>({
    cardNumber: '',
    cardHolderName: '',
    expiryDate: '',
    cvv: '',
  });

  const [errors, setErrors] = useState<
    Partial<Record<keyof CheckoutRequest, string>>
  >({});

  const [isProcessing, setIsProcessing] = useState(false);
  const [apiError, setApiError] = useState<string | null>(null);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    if (errors[name as keyof CheckoutRequest]) {
      setErrors((prev) => ({
        ...prev,
        [name]: undefined,
      }));
    }

    setApiError(null);
  };

  const fillExampleCardDetails = () => {
    setFormData({
      cardNumber: '4532015112830366',
      cardHolderName: 'John Doe',
      expiryDate: '12/28',
      cvv: '123',
    });

    setErrors({});
    setApiError(null);
  };

  const validateForm = (): boolean => {
    const newErrors: Partial<
      Record<keyof CheckoutRequest, string>
    > = {};

    // Card number
    if (!formData.cardNumber.trim()) {
      newErrors.cardNumber = 'Card number is required';
    } else if (
      !/^\d{16}$/.test(
        formData.cardNumber.replace(/\s/g, '')
      )
    ) {
      newErrors.cardNumber = 'Card number must be 16 digits';
    }

    // Cardholder name
    if (!formData.cardHolderName.trim()) {
      newErrors.cardHolderName =
        'Cardholder name is required';
    }

    // Expiry date
    if (!formData.expiryDate.trim()) {
      newErrors.expiryDate = 'Expiry date is required';
    } else if (
      !/^\d{2}\/\d{2}$/.test(formData.expiryDate)
    ) {
      newErrors.expiryDate = 'Format must be MM/YY';
    } else {
      const [month, year] = formData.expiryDate
        .split('/')
        .map(Number);

      const currentDate = new Date();
      const currentYear =
        currentDate.getFullYear() % 100;
      const currentMonth =
        currentDate.getMonth() + 1;

      if (month < 1 || month > 12) {
        newErrors.expiryDate = 'Invalid month';
      } else if (
        year < currentYear ||
        (year === currentYear &&
          month < currentMonth)
      ) {
        newErrors.expiryDate = 'Card is expired';
      }
    }

    // CVV
    if (!formData.cvv.trim()) {
      newErrors.cvv = 'CVV is required';
    } else if (!/^\d{3,4}$/.test(formData.cvv)) {
      newErrors.cvv = 'CVV must be 3 or 4 digits';
    }

    setErrors(newErrors);

    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
    e: React.FormEvent
  ) => {
    e.preventDefault();

    setApiError(null);

    if (!validateForm()) {
      return;
    }

    setIsProcessing(true);

    try {
      const order = await orderService.checkout(
        formData
      );

      // Clear sensitive payment data
      setFormData({
        cardNumber: '',
        cardHolderName: '',
        expiryDate: '',
        cvv: '',
      });

      navigate(`/orders/${order.id}`, {
        state: {
          orderConfirmed: true,
          orderNumber: order.orderNumber,
        },
      });
    } catch (error: any) {
      console.error('Checkout failed:', error);

      if (error.response?.status === 409) {
        setApiError(
          error.response.data?.message ||
            'Insufficient stock for one or more items.'
        );
      } else if (error.response?.status === 400) {
        setApiError(
          error.response.data?.message ||
            'Invalid payment details.'
        );
      } else if (error.response?.data?.message) {
        setApiError(error.response.data.message);
      } else {
        setApiError(
          'Checkout failed. Please try again.'
        );
      }
    } finally {
      setIsProcessing(false);
    }
  };

  if (cartLoading || !cart) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>

          <p className="text-gray-600 font-medium">
            Loading checkout...
          </p>
        </div>
      </div>
    );
  }

  if (cart.items.length === 0) {
    navigate('/cart');
    return null;
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
              Checkout
            </h1>

            {/* Right side */}
            <div className="flex items-center gap-4">

              <button
                onClick={() => navigate('/cart')}
                className="text-gray-700 hover:text-blue-600 font-medium"
              >
                ← Cart
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
      <main className="max-w-7xl mx-auto px-4 py-8">

        {/* Heading */}
        <div className="mb-8">

          <button
            onClick={() => navigate('/cart')}
            className="text-blue-600 hover:text-blue-700 font-medium mb-4"
          >
            ← Back to Cart
          </button>

          <h2 className="text-3xl md:text-4xl font-bold text-gray-900">
            Secure Checkout
          </h2>

          <p className="text-gray-500 mt-2">
            Complete your payment to place your order.
          </p>

        </div>

        {/* API Error */}
        {apiError && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-5 py-4 rounded-xl">
            <p className="font-medium">
              {apiError}
            </p>
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

          {/* Payment */}
          <div className="lg:col-span-2">

            <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-6 md:p-8">

              {/* Section heading */}
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">

                <div>

                  <h3 className="text-2xl font-bold text-gray-900">
                    Payment Information
                  </h3>

                  <p className="text-gray-500 text-sm mt-1">
                    Enter your card details below.
                  </p>

                </div>

                <button
                  type="button"
                  onClick={fillExampleCardDetails}
                  className="px-4 py-2 border border-blue-200 text-blue-600 bg-blue-50 rounded-lg hover:bg-blue-100 transition font-medium text-sm"
                >
                  Fill Example
                </button>

              </div>

              <form onSubmit={handleSubmit}>

                {/* Card Number */}
                <div className="mb-6">

                  <label
                    htmlFor="cardNumber"
                    className="block text-sm font-semibold text-gray-800 mb-2"
                  >
                    Card Number
                  </label>

                  <div className="relative">

                    <input
                      type="text"
                      id="cardNumber"
                      name="cardNumber"
                      value={formData.cardNumber}
                      onChange={handleInputChange}
                      placeholder="1234 5678 9012 3456"
                      maxLength={16}
                      inputMode="numeric"
                      autoComplete="cc-number"
                      className={`w-full px-4 py-3 border ${
                        errors.cardNumber
                          ? 'border-red-500'
                          : 'border-gray-300'
                      } rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition`}
                    />

                  </div>

                  {errors.cardNumber && (
                    <p className="mt-2 text-sm text-red-600">
                      {errors.cardNumber}
                    </p>
                  )}

                </div>

                {/* Cardholder */}
                <div className="mb-6">

                  <label
                    htmlFor="cardHolderName"
                    className="block text-sm font-semibold text-gray-800 mb-2"
                  >
                    Cardholder Name
                  </label>

                  <input
                    type="text"
                    id="cardHolderName"
                    name="cardHolderName"
                    value={formData.cardHolderName}
                    onChange={handleInputChange}
                    placeholder="John Doe"
                    autoComplete="cc-name"
                    className={`w-full px-4 py-3 border ${
                      errors.cardHolderName
                        ? 'border-red-500'
                        : 'border-gray-300'
                    } rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition`}
                  />

                  {errors.cardHolderName && (
                    <p className="mt-2 text-sm text-red-600">
                      {errors.cardHolderName}
                    </p>
                  )}

                </div>

                {/* Expiry + CVV */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 mb-8">

                  <div>

                    <label
                      htmlFor="expiryDate"
                      className="block text-sm font-semibold text-gray-800 mb-2"
                    >
                      Expiry Date
                    </label>

                    <input
                      type="text"
                      id="expiryDate"
                      name="expiryDate"
                      value={formData.expiryDate}
                      onChange={handleInputChange}
                      placeholder="MM/YY"
                      maxLength={5}
                      inputMode="numeric"
                      autoComplete="cc-exp"
                      className={`w-full px-4 py-3 border ${
                        errors.expiryDate
                          ? 'border-red-500'
                          : 'border-gray-300'
                      } rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition`}
                    />

                    {errors.expiryDate && (
                      <p className="mt-2 text-sm text-red-600">
                        {errors.expiryDate}
                      </p>
                    )}

                  </div>

                  <div>

                    <label
                      htmlFor="cvv"
                      className="block text-sm font-semibold text-gray-800 mb-2"
                    >
                      CVV
                    </label>

                    <input
                      type="password"
                      id="cvv"
                      name="cvv"
                      value={formData.cvv}
                      onChange={handleInputChange}
                      placeholder="123"
                      maxLength={4}
                      inputMode="numeric"
                      autoComplete="cc-csc"
                      className={`w-full px-4 py-3 border ${
                        errors.cvv
                          ? 'border-red-500'
                          : 'border-gray-300'
                      } rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition`}
                    />

                    {errors.cvv && (
                      <p className="mt-2 text-sm text-red-600">
                        {errors.cvv}
                      </p>
                    )}

                  </div>

                </div>

                {/* Security */}
                <div className="mb-6 p-4 bg-blue-50 border border-blue-100 rounded-xl">

                  <div className="flex items-start gap-3">

                    <span className="text-xl">
                      🔒
                    </span>

                    <div>

                      <p className="font-semibold text-blue-900">
                        Secure Payment
                      </p>

                      <p className="text-sm text-blue-700 mt-1">
                        Your payment details are transmitted
                        securely for this demo checkout.
                      </p>

                    </div>

                  </div>

                </div>

                {/* Submit */}
                <button
                  type="submit"
                  disabled={isProcessing}
                  className="w-full py-4 bg-blue-600 text-white rounded-xl hover:bg-blue-700 disabled:bg-blue-300 disabled:cursor-not-allowed transition font-semibold text-lg"
                >
                  {isProcessing ? (
                    <span className="flex items-center justify-center">

                      <span className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-3"></span>

                      Processing Order...

                    </span>
                  ) : (
                    `Place Order - $${cart.totalPrice.toFixed(2)}`
                  )}
                </button>

              </form>

            </div>

          </div>

          {/* Order Summary */}
          <div className="lg:col-span-1">

            <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-6 lg:sticky lg:top-24">

              <h3 className="text-xl font-bold text-gray-900 mb-6">
                Order Summary
              </h3>

              {/* Items */}
              <div className="space-y-5 max-h-[420px] overflow-y-auto pr-1">

                {cart.items.map((item) => (

                  <div
                    key={item.id}
                    className="flex gap-3"
                  >

                    {/* Image */}
                    <div className="w-16 h-16 bg-gray-50 rounded-lg overflow-hidden flex-shrink-0">

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

                    {/* Details */}
                    <div className="flex-1 min-w-0">

                      <p className="font-semibold text-gray-800 text-sm truncate">
                        {item.productName}
                      </p>

                      <p className="text-gray-500 text-sm mt-1">
                        Qty: {item.quantity}
                      </p>

                      <p className="font-semibold text-blue-600 text-sm mt-1">
                        ${item.subtotal.toFixed(2)}
                      </p>

                    </div>

                  </div>

                ))}

              </div>

              {/* Totals */}
              <div className="border-t border-gray-200 mt-6 pt-5 space-y-4">

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
              <div className="border-t border-gray-200 mt-5 pt-5">

                <div className="flex justify-between items-center">

                  <span className="text-lg font-bold text-gray-900">
                    Total
                  </span>

                  <span className="text-2xl font-bold text-blue-600">
                    ${cart.totalPrice.toFixed(2)}
                  </span>

                </div>

              </div>

              {/* Trust information */}
              <div className="mt-6 space-y-3">

                <div className="flex items-center gap-3 text-sm text-gray-500">
                  <span>🔒</span>
                  <span>Secure checkout</span>
                </div>

                <div className="flex items-center gap-3 text-sm text-gray-500">
                  <span>🚚</span>
                  <span>Fast delivery</span>
                </div>

                <div className="flex items-center gap-3 text-sm text-gray-500">
                  <span>↩️</span>
                  <span>Easy returns</span>
                </div>

              </div>

            </div>

          </div>

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

export default Checkout;