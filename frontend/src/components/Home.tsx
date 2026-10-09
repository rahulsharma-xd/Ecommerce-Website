import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { useWishlist } from '../context/WishlistContext';
import productService from '../services/product.service';
import type { Product } from '../types/product.types';

const Home = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchText, setSearchText] = useState('');
  const [addingProductId, setAddingProductId] = useState<number | null>(null);
  const [successMessage, setSuccessMessage] = useState('');
  const [sortOption, setSortOption] = useState('default');

  const { logout } = useAuth();
  const { totalItems, addToCart } = useCart();
  const { isInWishlist, toggleWishlist } = useWishlist();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const selectedCategory = searchParams.get('category');

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const data = await productService.getAllProducts();
        setProducts(data);
      } catch (error) {
        console.error('Error fetching products:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const categories = [
    'Electronics',
    'Clothing',
    'Books',
    'Home & Kitchen',
    'Beauty',
    'Sports',
    'Grocery',
    'Accessories',
  ];

  const handleCategoryClick = (category: string) => {
    setSearchText('');
    setSortOption('default');

    navigate(
      `/dashboard?category=${encodeURIComponent(category)}`
    );
  };

  const handleAllProducts = () => {
    setSearchText('');
    setSortOption('default');
    navigate('/dashboard');
  };

  const handleAddToCart = async (
    event: React.MouseEvent<HTMLButtonElement>,
    product: Product
  ) => {
    event.stopPropagation();

    if (product.stockQuantity <= 0) {
      return;
    }

    try {
      setAddingProductId(product.id);
      setSuccessMessage('');

      await addToCart(product.id, 1);

      setSuccessMessage(`${product.name} added to cart!`);

      setTimeout(() => {
        setSuccessMessage('');
      }, 2500);
    } catch (error) {
      console.error('Failed to add product to cart:', error);
    } finally {
      setAddingProductId(null);
    }
  };

  const filteredProducts = useMemo(() => {
    const search = searchText.toLowerCase().trim();

    let result = products.filter((product) => {
      const matchesSearch =
        !search ||
        product.name.toLowerCase().includes(search) ||
        product.description.toLowerCase().includes(search) ||
        product.category.toLowerCase().includes(search) ||
        (product.brand &&
          product.brand.toLowerCase().includes(search));

      const matchesCategory =
        !selectedCategory ||
        product.category.toLowerCase() ===
          selectedCategory.toLowerCase();

      return matchesSearch && matchesCategory;
    });

    if (sortOption === 'price-low') {
      result = [...result].sort(
        (a, b) => a.price - b.price
      );
    }

    if (sortOption === 'price-high') {
      result = [...result].sort(
        (a, b) => b.price - a.price
      );
    }

    if (sortOption === 'name') {
      result = [...result].sort((a, b) =>
        a.name.localeCompare(b.name)
      );
    }

    return result;
  }, [products, searchText, selectedCategory, sortOption]);

  const clearFilters = () => {
    setSearchText('');
    setSortOption('default');
    navigate('/dashboard');
  };

  const hasActiveFilters =
    Boolean(selectedCategory) ||
    Boolean(searchText.trim()) ||
    sortOption !== 'default';

  return (
    <div className="min-h-screen bg-gray-50">

      {/* Success message */}
      {successMessage && (
        <div className="fixed top-24 right-5 z-[100] bg-green-600 text-white px-5 py-3 rounded-lg shadow-lg">
          ✓ {successMessage}
        </div>
      )}

      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-50">

        <div className="max-w-7xl mx-auto px-4 py-4">

          <div className="flex items-center justify-between gap-6">

            {/* Logo */}
            <button
              onClick={handleAllProducts}
              className="text-2xl font-bold text-blue-600"
            >
              ShopHub
            </button>

            {/* Search */}
            <div className="hidden md:flex flex-1 max-w-xl relative">

              <input
                type="text"
                value={searchText}
                onChange={(e) =>
                  setSearchText(e.target.value)
                }
                placeholder="Search for products..."
                className="w-full px-5 py-3 pr-12 border border-gray-300 rounded-full focus:outline-none focus:ring-2 focus:ring-blue-500"
              />

              {searchText && (
                <button
                  onClick={() => setSearchText('')}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-700"
                >
                  ✕
                </button>
              )}

            </div>

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
                className="relative text-gray-700 hover:text-blue-600 text-xl"
                aria-label="Shopping Cart"
              >
                🛒

                {totalItems > 0 && (
                  <span className="absolute -top-3 -right-3 bg-red-500 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center">
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

          {/* Mobile search */}
          <div className="md:hidden mt-4 relative">

            <input
              type="text"
              value={searchText}
              onChange={(e) =>
                setSearchText(e.target.value)
              }
              placeholder="Search for products..."
              className="w-full px-5 py-3 pr-12 border border-gray-300 rounded-full focus:outline-none focus:ring-2 focus:ring-blue-500"
            />

            {searchText && (
              <button
                onClick={() => setSearchText('')}
                className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-700"
              >
                ✕
              </button>
            )}

          </div>

        </div>

        {/* Categories */}
        <div className="border-t border-gray-100">

          <div className="max-w-7xl mx-auto px-4">

            <div className="flex gap-6 overflow-x-auto py-3">

              <button
                onClick={handleAllProducts}
                className={
                  !selectedCategory
                    ? 'text-blue-600 font-medium whitespace-nowrap'
                    : 'text-gray-600 hover:text-blue-600 whitespace-nowrap'
                }
              >
                All Products
              </button>

              {categories.map((category) => (
                <button
                  key={category}
                  onClick={() =>
                    handleCategoryClick(category)
                  }
                  className={
                    selectedCategory?.toLowerCase() ===
                    category.toLowerCase()
                      ? 'text-blue-600 font-medium whitespace-nowrap'
                      : 'text-gray-600 hover:text-blue-600 whitespace-nowrap'
                  }
                >
                  {category}
                </button>
              ))}

            </div>

          </div>

        </div>

      </header>

      {/* Hero */}
      {!selectedCategory && !searchText && (
        <section className="bg-gradient-to-r from-blue-600 to-indigo-700 text-white">

          <div className="max-w-7xl mx-auto px-4 py-20">

            <div className="max-w-2xl">

              <p className="text-blue-100 font-medium mb-3">
                YOUR EVERYDAY SHOPPING DESTINATION
              </p>

              <h1 className="text-4xl md:text-6xl font-bold leading-tight mb-6">
                Everything you need,
                <br />
                all in one place.
              </h1>

              <p className="text-lg text-blue-100 mb-8">
                Explore electronics, fashion, books, home essentials,
                beauty, sports products and more.
              </p>

              <button
                onClick={handleAllProducts}
                className="px-7 py-3 bg-white text-blue-600 font-semibold rounded-lg hover:bg-gray-100 transition"
              >
                Shop Now
              </button>

            </div>

          </div>

        </section>
      )}

      {/* Categories */}
      {!selectedCategory && !searchText && (
        <section className="max-w-7xl mx-auto px-4 py-12">

          <div className="flex items-center justify-between mb-6">

            <h2 className="text-2xl font-bold text-gray-900">
              Shop by Category
            </h2>

          </div>

          <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-8 gap-4">

            {categories.map((category) => (
              <button
                key={category}
                onClick={() =>
                  handleCategoryClick(category)
                }
                className="bg-white border border-gray-200 rounded-xl p-5 text-center hover:shadow-lg hover:border-blue-300 transition"
              >

                <div className="text-4xl mb-3">
                  {category === 'Electronics' && '💻'}
                  {category === 'Clothing' && '👕'}
                  {category === 'Books' && '📚'}
                  {category === 'Home & Kitchen' && '🏠'}
                  {category === 'Beauty' && '💄'}
                  {category === 'Sports' && '⚽'}
                  {category === 'Grocery' && '🛒'}
                  {category === 'Accessories' && '⌚'}
                </div>

                <h3 className="font-semibold text-gray-800 text-sm">
                  {category}
                </h3>

              </button>
            ))}

          </div>

        </section>
      )}

      {/* Products */}
      <section className="max-w-7xl mx-auto px-4 py-12">

        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 mb-6">

          <div>

            <h2 className="text-2xl font-bold text-gray-900">

              {searchText
                ? `Search Results for "${searchText}"`
                : selectedCategory
                ? `${selectedCategory} Products`
                : 'Featured Products'}

            </h2>

            <p className="text-gray-500 mt-1">

              {searchText
                ? `${filteredProducts.length} product(s) found`
                : selectedCategory
                ? `Explore our ${selectedCategory.toLowerCase()} collection`
                : 'Discover some of our popular products'}

            </p>

          </div>

          {/* Sorting and clear filters */}
          <div className="flex flex-col sm:flex-row gap-3">

            <select
              value={sortOption}
              onChange={(e) =>
                setSortOption(e.target.value)
              }
              className="px-4 py-2.5 border border-gray-300 rounded-lg bg-white text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="default">
                Sort Products
              </option>

              <option value="price-low">
                Price: Low to High
              </option>

              <option value="price-high">
                Price: High to Low
              </option>

              <option value="name">
                Name: A to Z
              </option>
            </select>

            {hasActiveFilters && (
              <button
                onClick={clearFilters}
                className="px-4 py-2.5 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-100 transition font-medium"
              >
                Clear Filters
              </button>
            )}

          </div>

        </div>

        {/* Loading */}
        {loading ? (

          <div className="text-center py-12 text-gray-500">
            Loading products...
          </div>

        ) : filteredProducts.length === 0 ? (

          <div className="text-center py-16">

            <div className="text-6xl mb-4">
              🔍
            </div>

            <h3 className="text-xl font-semibold text-gray-800 mb-2">
              No products found
            </h3>

            <p className="text-gray-500 mb-6">
              Try changing your search or filters.
            </p>

            <button
              onClick={clearFilters}
              className="px-5 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              Clear Filters
            </button>

          </div>

        ) : (

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">

            {(selectedCategory || searchText || sortOption !== 'default'
              ? filteredProducts
              : filteredProducts.slice(0, 8)
            ).map((product) => (

              <div
                key={product.id}
                onClick={() =>
                  navigate(`/products/${product.id}`)
                }
                className="bg-white rounded-xl border border-gray-200 overflow-hidden cursor-pointer hover:shadow-xl transition"
              >

                {/* Product image + Wishlist */}
<div className="h-56 bg-gray-100 flex items-center justify-center overflow-hidden relative">

  {product.imageUrl ? (
    <img
      src={product.imageUrl}
      alt={product.name}
      className="w-full h-full object-cover"
    />
  ) : (
    <div className="text-gray-400 text-5xl">
      📦
    </div>
  )}

  {/* Wishlist button */}
  <button
    type="button"
    onClick={(event) => {
      event.stopPropagation();
      toggleWishlist(product.id);
    }}
    className="absolute top-3 right-3 w-10 h-10 bg-white rounded-full shadow-md flex items-center justify-center text-xl hover:scale-110 transition"
    aria-label={
      isInWishlist(product.id)
        ? 'Remove from wishlist'
        : 'Add to wishlist'
    }
  >
    {isInWishlist(product.id) ? '❤️' : '♡'}
  </button>

</div>

                {/* Product information */}
                <div className="p-5">

                  <p className="text-sm text-blue-600 font-medium mb-1">
                    {product.category}
                  </p>

                  <h3 className="font-semibold text-gray-900 text-lg mb-2 truncate">
                    {product.name}
                  </h3>

                  <p className="text-gray-500 text-sm line-clamp-2 mb-4">
                    {product.description}
                  </p>

                  {/* Price and stock */}
                  <div className="flex items-center justify-between mb-4">

                    <span className="text-xl font-bold text-gray-900">
                      ${product.price.toFixed(2)}
                    </span>

                    <span
                      className={
                        product.stockQuantity > 0
                          ? 'text-sm text-green-600 font-medium'
                          : 'text-sm text-red-600 font-medium'
                      }
                    >
                      {product.stockQuantity > 0
                        ? `${product.stockQuantity} left`
                        : 'Out of stock'}
                    </span>

                  </div>

                  {/* Add to cart */}
                  <button
                    onClick={(event) =>
                      handleAddToCart(event, product)
                    }
                    disabled={
                      product.stockQuantity <= 0 ||
                      addingProductId === product.id
                    }
                    className={`w-full py-3 rounded-lg font-semibold transition ${
                      product.stockQuantity <= 0
                        ? 'bg-gray-200 text-gray-500 cursor-not-allowed'
                        : addingProductId === product.id
                        ? 'bg-blue-400 text-white cursor-wait'
                        : 'bg-blue-600 text-white hover:bg-blue-700'
                    }`}
                  >
                    {addingProductId === product.id
                      ? 'Adding...'
                      : product.stockQuantity <= 0
                      ? 'Out of Stock'
                      : '🛒 Add to Cart'}
                  </button>

                </div>

              </div>

            ))}

          </div>

        )}

      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-gray-300">

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

export default Home;