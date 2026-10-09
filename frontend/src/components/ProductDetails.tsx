import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import productService from '../services/product.service';
import type { Product } from '../types/product.types';

const ProductDetails = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const { logout } = useAuth();
  const { addToCart, totalItems } = useCart();

  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [quantity, setQuantity] = useState(1);
  const [addingToCart, setAddingToCart] = useState(false);
  const [buyingNow, setBuyingNow] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      fetchProduct(Number(id));
    }
  }, [id]);

  const fetchProduct = async (productId: number) => {
    try {
      setLoading(true);
      const data = await productService.getProductById(productId);

      setProduct(data);
      setError(null);
      setQuantity(1);
    } catch (err) {
      console.error('Error fetching product:', err);
      setError('Failed to load product. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = async () => {
    if (!product || product.stockQuantity <= 0) {
      return;
    }

    try {
      setAddingToCart(true);
      setError(null);

      await addToCart(product.id, quantity);

      setSuccessMessage(
        `${quantity} ${quantity === 1 ? 'item' : 'items'} added to your cart!`
      );

      setTimeout(() => {
        setSuccessMessage(null);
      }, 3000);
    } catch (err: any) {
      console.error('Failed to add to cart:', err);

      setError(
        err.response?.data?.message ||
          'Failed to add product to cart. Please try again.'
      );
    } finally {
      setAddingToCart(false);
    }
  };

  const handleBuyNow = async () => {
    if (!product || product.stockQuantity <= 0) {
      return;
    }

    try {
      setBuyingNow(true);
      setError(null);

      await addToCart(product.id, quantity);

      navigate('/cart');
    } catch (err: any) {
      console.error('Failed to buy product:', err);

      setError(
        err.response?.data?.message ||
          'Unable to continue with purchase. Please try again.'
      );

      setBuyingNow(false);
    }
  };

  const handleQuantityChange = (newQuantity: number) => {
    if (!product) {
      return;
    }

    if (newQuantity >= 1 && newQuantity <= product.stockQuantity) {
      setQuantity(newQuantity);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleBackToDashboard = () => {
    if (product?.category) {
      navigate(
        `/dashboard?category=${encodeURIComponent(product.category)}`
      );
    } else {
      navigate('/dashboard');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>

          <p className="text-gray-600 font-medium">
            Loading product...
          </p>
        </div>
      </div>
    );
  }

  if (error && !product) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-6">
        <div className="bg-white rounded-2xl shadow-lg p-8 max-w-md w-full text-center">

          <div className="text-6xl mb-4">
            😕
          </div>

          <h2 className="text-2xl font-bold text-gray-900 mb-3">
            Product Not Found
          </h2>

          <p className="text-red-600 mb-6">
            {error}
          </p>

          <button
            onClick={() => navigate('/dashboard')}
            className="w-full px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-semibold"
          >
            Back to Shop
          </button>

        </div>
      </div>
    );
  }

  if (!product) {
    return null;
  }

  const isOutOfStock = product.stockQuantity <= 0;

  return (
    <div className="min-h-screen bg-gray-50">

      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-50">

        <div className="max-w-7xl mx-auto px-4 py-4">

          <div className="flex items-center justify-between">

            {/* Logo */}
            <button
              onClick={() => navigate('/dashboard')}
              className="text-2xl font-bold text-blue-600"
            >
              ShopHub
            </button>

            {/* Right side */}
            <div className="flex items-center gap-4">

              <button
                onClick={() => navigate('/orders')}
                className="hidden sm:block text-gray-700 hover:text-blue-600 font-medium"
              >
                Orders
              </button>

              <button
                onClick={() => navigate('/cart')}
                className="relative text-gray-700 hover:text-blue-600 text-2xl"
                aria-label="Shopping Cart"
              >
                🛒

                {totalItems > 0 && (
                  <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center">
                    {totalItems}
                  </span>
                )}
              </button>

              <button
                onClick={handleLogout}
                className="hidden sm:block px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-700"
              >
                Logout
              </button>

            </div>

          </div>

        </div>

      </header>

      {/* Main */}
      <main className="max-w-7xl mx-auto px-4 py-8">

        {/* Breadcrumb */}
        <div className="flex items-center gap-2 text-sm text-gray-500 mb-6">

          <button
            onClick={() => navigate('/dashboard')}
            className="hover:text-blue-600"
          >
            Home
          </button>

          <span>›</span>

          <button
            onClick={handleBackToDashboard}
            className="hover:text-blue-600"
          >
            {product.category}
          </button>

          <span>›</span>

          <span className="text-gray-800 font-medium truncate">
            {product.name}
          </span>

        </div>

        {/* Success */}
        {successMessage && (
          <div className="mb-6 bg-green-50 border border-green-200 text-green-700 px-5 py-4 rounded-xl flex items-center gap-3">
            <span className="text-xl">✓</span>
            <span className="font-medium">
              {successMessage}
            </span>
          </div>
        )}

        {/* Error */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-5 py-4 rounded-xl">
            {error}
          </div>
        )}

        {/* Product */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-0">

            {/* Product Image */}
            <div className="bg-gray-50 p-8 lg:p-12 flex items-center justify-center min-h-[450px]">

              <div className="w-full h-full max-h-[500px] flex items-center justify-center">

                {product.imageUrl ? (
                  <img
                    src={product.imageUrl}
                    alt={product.name}
                    className="max-w-full max-h-[480px] object-contain rounded-xl"
                  />
                ) : (
                  <div className="text-gray-300 text-8xl">
                    📦
                  </div>
                )}

              </div>

            </div>

            {/* Product Information */}
            <div className="p-8 lg:p-12">

              {/* Category */}
              <div className="mb-4">

                <span className="inline-block px-3 py-1 bg-blue-50 text-blue-600 rounded-full text-sm font-semibold">
                  {product.category}
                </span>

              </div>

              {/* Product name */}
              <h1 className="text-3xl lg:text-4xl font-bold text-gray-900 mb-4">
                {product.name}
              </h1>

              {/* Brand */}
              {product.brand && (
                <p className="text-gray-500 mb-6">
                  Brand:{' '}
                  <span className="font-semibold text-gray-800">
                    {product.brand}
                  </span>
                </p>
              )}

              {/* Rating */}
              <div className="flex items-center gap-2 mb-6">

                <div className="flex text-yellow-400 text-lg">
                  ★★★★★
                </div>

                <span className="text-sm text-gray-500">
                  Product Rating
                </span>

              </div>

              {/* Price */}
              <div className="border-y border-gray-200 py-6 mb-6">

                <div className="flex items-center gap-3">

                  <span className="text-4xl font-bold text-gray-900">
                    ${product.price.toFixed(2)}
                  </span>

                </div>

                <p className="text-sm text-gray-500 mt-2">
                  Inclusive of applicable taxes
                </p>

              </div>

              {/* Description */}
              <div className="mb-8">

                <h2 className="text-lg font-semibold text-gray-900 mb-3">
                  About this product
                </h2>

                <p className="text-gray-600 leading-relaxed">
                  {product.description}
                </p>

              </div>

              {/* Stock */}
              <div className="mb-6">

                {isOutOfStock ? (
                  <div className="flex items-center gap-2 text-red-600 font-semibold">
                    <span>●</span>
                    Out of stock
                  </div>
                ) : product.stockQuantity <= 10 ? (
                  <div className="flex items-center gap-2 text-orange-600 font-semibold">
                    <span>●</span>
                    Only {product.stockQuantity} left in stock
                  </div>
                ) : (
                  <div className="flex items-center gap-2 text-green-600 font-semibold">
                    <span>●</span>
                    In stock
                  </div>
                )}

              </div>

              {!isOutOfStock && (
                <>
                  {/* Quantity */}
                  <div className="mb-6">

                    <label className="block text-sm font-semibold text-gray-800 mb-3">
                      Quantity
                    </label>

                    <div className="flex items-center w-fit border border-gray-300 rounded-lg overflow-hidden">

                      <button
                        onClick={() =>
                          handleQuantityChange(quantity - 1)
                        }
                        disabled={quantity <= 1}
                        className="w-11 h-11 bg-gray-50 hover:bg-gray-100 disabled:text-gray-300 text-lg font-bold"
                      >
                        −
                      </button>

                      <div className="w-14 h-11 flex items-center justify-center border-x border-gray-300 font-semibold">
                        {quantity}
                      </div>

                      <button
                        onClick={() =>
                          handleQuantityChange(quantity + 1)
                        }
                        disabled={
                          quantity >= product.stockQuantity
                        }
                        className="w-11 h-11 bg-gray-50 hover:bg-gray-100 disabled:text-gray-300 text-lg font-bold"
                      >
                        +
                      </button>

                    </div>

                  </div>

                  {/* Buttons */}
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">

                    <button
                      onClick={handleAddToCart}
                      disabled={addingToCart || buyingNow}
                      className="py-4 bg-blue-600 text-white rounded-xl hover:bg-blue-700 disabled:bg-blue-300 font-semibold transition"
                    >
                      {addingToCart
                        ? 'Adding...'
                        : '🛒 Add to Cart'}
                    </button>

                    <button
                      onClick={handleBuyNow}
                      disabled={addingToCart || buyingNow}
                      className="py-4 bg-orange-500 text-white rounded-xl hover:bg-orange-600 disabled:bg-orange-300 font-semibold transition"
                    >
                      {buyingNow
                        ? 'Processing...'
                        : '⚡ Buy Now'}
                    </button>

                  </div>

                </>
              )}

              {/* Delivery information */}
              <div className="mt-8 border-t border-gray-200 pt-6 space-y-4">

                <div className="flex items-start gap-3">

                  <span className="text-xl">
                    🚚
                  </span>

                  <div>
                    <p className="font-semibold text-gray-800">
                      Fast Delivery
                    </p>

                    <p className="text-sm text-gray-500">
                      Delivery available to your location
                    </p>
                  </div>

                </div>

                <div className="flex items-start gap-3">

                  <span className="text-xl">
                    ↩️
                  </span>

                  <div>
                    <p className="font-semibold text-gray-800">
                      Easy Returns
                    </p>

                    <p className="text-sm text-gray-500">
                      Hassle-free return experience
                    </p>
                  </div>

                </div>

                <div className="flex items-start gap-3">

                  <span className="text-xl">
                    🔒
                  </span>

                  <div>
                    <p className="font-semibold text-gray-800">
                      Secure Shopping
                    </p>

                    <p className="text-sm text-gray-500">
                      Your purchase is securely processed
                    </p>
                  </div>

                </div>

              </div>

            </div>

          </div>

        </div>

        {/* Back to shopping */}
        <div className="mt-8">

          <button
            onClick={handleBackToDashboard}
            className="text-blue-600 hover:text-blue-700 font-semibold"
          >
            ← Continue Shopping
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

export default ProductDetails;