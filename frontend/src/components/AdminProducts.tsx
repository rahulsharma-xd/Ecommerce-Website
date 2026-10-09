
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

interface AdminProduct {
  id: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  category: string;
  brand?: string;
  imageUrl?: string;
  active?: boolean;
}

interface ProductForm {
  name: string;
  description: string;
  price: string;
  stockQuantity: string;
  category: string;
  brand: string;
  imageUrl: string;
}

const emptyForm: ProductForm = {
  name: '',
  description: '',
  price: '',
  stockQuantity: '',
  category: '',
  brand: '',
  imageUrl: '',
};

const AdminProducts = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [products, setProducts] = useState<AdminProduct[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Add / Edit product states
  const [showAddForm, setShowAddForm] = useState(false);
  const [editingProductId, setEditingProductId] = useState<number | null>(
    null
  );

  const [form, setForm] = useState<ProductForm>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    fetchProducts();
  }, []);

  // ============================
  // FETCH PRODUCTS
  // ============================
  const fetchProducts = async () => {
    try {
      setLoading(true);
      setError('');

      const response = await api.get<AdminProduct[]>('/admin/products');

      setProducts(response.data);
    } catch (err: any) {
      console.error('Failed to load products:', err);

      if (err.response?.status === 403) {
        setError('You do not have permission to access this page.');
      } else {
        setError('Failed to load products. Please try again.');
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
  // FORM CHANGE
  // ============================
  const handleFormChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
    >
  ) => {
    const { name, value } = e.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));

    setFormError('');
  };

  // ============================
  // OPEN ADD FORM
  // ============================
  const handleOpenAddForm = () => {
    setEditingProductId(null);
    setForm(emptyForm);
    setFormError('');
    setSuccessMessage('');
    setShowAddForm(true);

    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  };

  // ============================
  // OPEN EDIT FORM
  // ============================
  const handleEditProduct = (product: AdminProduct) => {
    setEditingProductId(product.id);

    setForm({
      name: product.name || '',
      description: product.description || '',
      price: String(product.price ?? ''),
      stockQuantity: String(product.stockQuantity ?? ''),
      category: product.category || '',
      brand: product.brand || '',
      imageUrl: product.imageUrl || '',
    });

    setFormError('');
    setSuccessMessage('');
    setShowAddForm(true);

    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  };

  // ============================
  // ADD / UPDATE PRODUCT
  // ============================
  const handleSubmitProduct = async (e: React.FormEvent) => {
    e.preventDefault();

    setFormError('');
    setSuccessMessage('');

    // Product name validation
    if (!form.name.trim()) {
      setFormError('Product name is required.');
      return;
    }

    // Description validation
    if (!form.description.trim()) {
      setFormError('Product description is required.');
      return;
    }

    // Price validation
    if (!form.price || Number(form.price) <= 0) {
      setFormError('Please enter a valid price greater than 0.');
      return;
    }

    // Stock validation
    if (
      form.stockQuantity === '' ||
      Number(form.stockQuantity) < 0
    ) {
      setFormError('Please enter a valid stock quantity.');
      return;
    }

    // Category validation
    if (!form.category) {
      setFormError('Please select a category.');
      return;
    }

    try {
      setSaving(true);

      const productData = {
        name: form.name.trim(),
        description: form.description.trim(),
        price: Number(form.price),
        stockQuantity: Number(form.stockQuantity),
        category: form.category,
        brand: form.brand.trim(),
        imageUrl: form.imageUrl.trim(),
      };

      // EDIT EXISTING PRODUCT
      if (editingProductId !== null) {
        await api.put(
          `/admin/products/${editingProductId}`,
          productData
        );

        setSuccessMessage('Product updated successfully.');
      }

      // ADD NEW PRODUCT
      else {
        await api.post('/admin/products', productData);

        setSuccessMessage('Product added successfully.');
      }

      setForm(emptyForm);
      setEditingProductId(null);
      setShowAddForm(false);

      await fetchProducts();

    } catch (err: any) {
      console.error('Failed to save product:', err);

      if (err.response?.data?.message) {
        setFormError(err.response.data.message);
      } else if (err.response?.status === 403) {
        setFormError(
          editingProductId !== null
            ? 'You do not have permission to update products.'
            : 'You do not have permission to add products.'
        );
      } else {
        setFormError(
          editingProductId !== null
            ? 'Failed to update product. Please try again.'
            : 'Failed to add product. Please try again.'
        );
      }
    } finally {
      setSaving(false);
    }
  };

  // ============================
  // CANCEL ADD / EDIT
  // ============================
  const handleCancelForm = () => {
    setShowAddForm(false);
    setEditingProductId(null);
    setForm(emptyForm);
    setFormError('');
  };

  // ============================
  // DEACTIVATE PRODUCT
  // ============================
  const handleDeactivateProduct = async (
    product: AdminProduct
  ) => {
    const confirmed = window.confirm(
      `Are you sure you want to deactivate "${product.name}"?`
    );

    if (!confirmed) {
      return;
    }

    try {
      setError('');
      setSuccessMessage('');

      await api.delete(`/admin/products/${product.id}`);

      setSuccessMessage(
        `"${product.name}" has been deactivated successfully.`
      );

      await fetchProducts();

    } catch (err: any) {
      console.error(
        'Failed to deactivate product:',
        err
      );

      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.status === 403) {
        setError(
          'You do not have permission to deactivate products.'
        );
      } else {
        setError(
          'Failed to deactivate product. Please try again.'
        );
      }
    }
  };

  const activeProducts = products.filter(
    (product) => product.active !== false
  ).length;

  const isEditing = editingProductId !== null;

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

            {/* Admin information */}
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

      {/* ============================
          MAIN
      ============================ */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 py-8">

        {/* Back button */}
        <button
          onClick={() => navigate('/admin')}
          className="text-blue-600 hover:text-blue-700
                     font-medium mb-6"
        >
          ← Back to Admin Dashboard
        </button>

        {/* ============================
            PAGE HEADING
            ============================ */}
        <div className="flex flex-col sm:flex-row
                        sm:items-center
                        sm:justify-between
                        gap-4 mb-8">

          <div>

            <h2 className="text-3xl font-bold text-gray-900">
              Manage Products
            </h2>

            <p className="text-gray-500 mt-1">
              Add and manage products in your ShopHub store.
            </p>

          </div>

          <button
            onClick={handleOpenAddForm}
            className="px-5 py-3 bg-blue-600 text-white rounded-lg
                       hover:bg-blue-700 transition font-medium"
          >
            + Add Product
          </button>

        </div>

        {/* ============================
            SUCCESS MESSAGE
        ============================ */}
        {successMessage && (
          <div className="mb-6 bg-green-50 border border-green-200
                          text-green-700 rounded-xl p-4">

            <p className="font-medium">
              ✓ {successMessage}
            </p>

          </div>
        )}

        {/* ============================
            ADD / EDIT FORM
        ============================ */}
        {showAddForm && (
          <div className="bg-white rounded-xl shadow-sm mb-8">

            {/* Form Header */}
            <div className="px-6 py-5 border-b">

              <h3 className="text-xl font-bold text-gray-900">
                {isEditing
                  ? 'Edit Product'
                  : 'Add New Product'}
              </h3>

              <p className="text-sm text-gray-500 mt-1">
                {isEditing
                  ? 'Update the details of this product.'
                  : 'Enter the details of the new product.'}
              </p>

            </div>

            {/* Form */}
            <form
              onSubmit={handleSubmitProduct}
              className="p-6"
            >

              {/* Form Error */}
              {formError && (
                <div className="mb-6 bg-red-50 border border-red-200
                                text-red-700 rounded-lg p-4">

                  <p className="font-medium">
                    {formError}
                  </p>

                </div>
              )}

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

                {/* Product Name */}
                <div>

                  <label
                    htmlFor="name"
                    className="block text-sm font-semibold
                               text-gray-700 mb-2"
                  >
                    Product Name *
                  </label>

                  <input
                    id="name"
                    name="name"
                    type="text"
                    value={form.name}
                    onChange={handleFormChange}
                    placeholder="e.g. Wireless Mouse"
                    className="w-full px-4 py-3 border
                               border-gray-300 rounded-lg
                               focus:outline-none
                               focus:ring-2 focus:ring-blue-500"
                    required
                  />

                </div>

                {/* Category */}
                <div>

                  <label
                    htmlFor="category"
                    className="block text-sm font-semibold
                               text-gray-700 mb-2"
                  >
                    Category *
                  </label>

                  <select
                    id="category"
                    name="category"
                    value={form.category}
                    onChange={handleFormChange}
                    className="w-full px-4 py-3 border
                               border-gray-300 rounded-lg
                               bg-white focus:outline-none
                               focus:ring-2 focus:ring-blue-500"
                    required
                  >

                    <option value="">
                      Select category
                    </option>

                    <option value="Electronics">
                      Electronics
                    </option>

                    <option value="Clothing">
                      Clothing
                    </option>

                    <option value="Books">
                      Books
                    </option>

                    <option value="Home & Kitchen">
                      Home & Kitchen
                    </option>

                    <option value="Beauty">
                      Beauty
                    </option>

                    <option value="Sports">
                      Sports
                    </option>

                    <option value="Grocery">
                      Grocery
                    </option>

                    <option value="Accessories">
                      Accessories
                    </option>

                  </select>

                </div>

                {/* Price */}
                <div>

                  <label
                    htmlFor="price"
                    className="block text-sm font-semibold
                               text-gray-700 mb-2"
                  >
                    Price *
                  </label>

                  <input
                    id="price"
                    name="price"
                    type="number"
                    min="0.01"
                    step="0.01"
                    value={form.price}
                    onChange={handleFormChange}
                    placeholder="e.g. 299.99"
                    className="w-full px-4 py-3 border
                               border-gray-300 rounded-lg
                               focus:outline-none
                               focus:ring-2 focus:ring-blue-500"
                    required
                  />

                </div>

                {/* Stock */}
                <div>

                  <label
                    htmlFor="stockQuantity"
                    className="block text-sm font-semibold
                               text-gray-700 mb-2"
                  >
                    Stock Quantity *
                  </label>

                  <input
                    id="stockQuantity"
                    name="stockQuantity"
                    type="number"
                    min="0"
                    step="1"
                    value={form.stockQuantity}
                    onChange={handleFormChange}
                    placeholder="e.g. 50"
                    className="w-full px-4 py-3 border
                               border-gray-300 rounded-lg
                               focus:outline-none
                               focus:ring-2 focus:ring-blue-500"
                    required
                  />

                </div>

                {/* Brand */}
                <div>

                  <label
                    htmlFor="brand"
                    className="block text-sm font-semibold
                               text-gray-700 mb-2"
                  >
                    Brand
                  </label>

                  <input
                    id="brand"
                    name="brand"
                    type="text"
                    value={form.brand}
                    onChange={handleFormChange}
                    placeholder="e.g. Logitech"
                    className="w-full px-4 py-3 border
                               border-gray-300 rounded-lg
                               focus:outline-none
                               focus:ring-2 focus:ring-blue-500"
                  />

                </div>

                {/* Image URL */}
                <div>

                  <label
                    htmlFor="imageUrl"
                    className="block text-sm font-semibold
                               text-gray-700 mb-2"
                  >
                    Image URL
                  </label>

                  <input
                    id="imageUrl"
                    name="imageUrl"
                    type="url"
                    value={form.imageUrl}
                    onChange={handleFormChange}
                    placeholder="https://example.com/image.jpg"
                    className="w-full px-4 py-3 border
                               border-gray-300 rounded-lg
                               focus:outline-none
                               focus:ring-2 focus:ring-blue-500"
                  />

                </div>

              </div>

              {/* Description */}
              <div className="mt-6">

                <label
                  htmlFor="description"
                  className="block text-sm font-semibold
                             text-gray-700 mb-2"
                >
                  Description *
                </label>

                <textarea
                  id="description"
                  name="description"
                  rows={4}
                  value={form.description}
                  onChange={handleFormChange}
                  placeholder="Describe the product..."
                  className="w-full px-4 py-3 border
                             border-gray-300 rounded-lg
                             resize-none focus:outline-none
                             focus:ring-2 focus:ring-blue-500"
                  required
                />

              </div>

              {/* Form Buttons */}
              <div className="flex flex-col sm:flex-row
                              justify-end gap-3 mt-6">

                <button
                  type="button"
                  onClick={handleCancelForm}
                  disabled={saving}
                  className="px-5 py-3 border border-gray-300
                             text-gray-700 rounded-lg
                             hover:bg-gray-50 transition
                             font-medium disabled:opacity-50"
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  disabled={saving}
                  className="px-5 py-3 bg-blue-600 text-white
                             rounded-lg hover:bg-blue-700
                             transition font-medium
                             disabled:bg-blue-400
                             disabled:cursor-not-allowed"
                >
                  {saving
                    ? isEditing
                      ? 'Updating Product...'
                      : 'Adding Product...'
                    : isEditing
                      ? 'Update Product'
                      : 'Add Product'}
                </button>

              </div>

            </form>

          </div>
        )}

        {/* ============================
            LOADING
        ============================ */}
        {loading && (
          <div className="bg-white rounded-xl shadow-sm p-10 text-center">

            <div className="animate-spin rounded-full h-10 w-10
                            border-b-2 border-blue-600
                            mx-auto mb-4">
            </div>

            <p className="text-gray-600">
              Loading products...
            </p>

          </div>
        )}

        {/* ============================
            ERROR
        ============================ */}
        {!loading && error && (
          <div className="bg-red-50 border border-red-200
                          text-red-700 rounded-xl p-5">

            <p className="font-semibold">
              {error}
            </p>

            <button
              onClick={fetchProducts}
              className="mt-3 text-sm font-medium underline"
            >
              Try Again
            </button>

          </div>
        )}

        {/* ============================
            PRODUCTS
        ============================ */}
        {!loading && !error && (
          <div className="bg-white rounded-xl shadow-sm overflow-hidden">

            {/* Products Header */}
            <div className="px-6 py-5 border-b">

              <div className="flex flex-col sm:flex-row
                              sm:items-center
                              sm:justify-between gap-3">

                <div>

                  <h3 className="text-lg font-bold text-gray-900">
                    All Products
                  </h3>

                  <p className="text-sm text-gray-500 mt-1">
                    {products.length} total product
                    {products.length !== 1 ? 's' : ''} •{' '}
                    {activeProducts} active
                  </p>

                </div>

                <button
                  onClick={fetchProducts}
                  className="px-4 py-2 border border-gray-300
                             rounded-lg text-sm font-medium
                             text-gray-700 hover:bg-gray-50
                             transition"
                >
                  Refresh
                </button>

              </div>

            </div>

            {/* ============================
                EMPTY STATE
            ============================ */}
            {products.length === 0 && (
              <div className="p-12 text-center">

                <div className="text-5xl mb-4">
                  🛍️
                </div>

                <h3 className="text-xl font-bold text-gray-900">
                  No products found
                </h3>

                <p className="text-gray-500 mt-2">
                  Add your first product to the store.
                </p>

              </div>
            )}

            {/* ============================
                DESKTOP TABLE
            ============================ */}
            {products.length > 0 && (
              <div className="hidden md:block overflow-x-auto">

                <table className="w-full">

                  <thead className="bg-gray-50 border-b">

                    <tr>

                      <th
                        className="text-left px-6 py-4
                                   text-xs font-semibold
                                   text-gray-500 uppercase"
                      >
                        Product
                      </th>

                      <th
                        className="text-left px-6 py-4
                                   text-xs font-semibold
                                   text-gray-500 uppercase"
                      >
                        Category
                      </th>

                      <th
                        className="text-left px-6 py-4
                                   text-xs font-semibold
                                   text-gray-500 uppercase"
                      >
                        Price
                      </th>

                      <th
                        className="text-left px-6 py-4
                                   text-xs font-semibold
                                   text-gray-500 uppercase"
                      >
                        Stock
                      </th>

                      <th
                        className="text-left px-6 py-4
                                   text-xs font-semibold
                                   text-gray-500 uppercase"
                      >
                        Status
                      </th>

                      <th
                        className="text-left px-6 py-4
                                   text-xs font-semibold
                                   text-gray-500 uppercase"
                      >
                        Actions
                      </th>

                    </tr>

                  </thead>

                  <tbody className="divide-y">

                    {products.map((product) => (

                      <tr
                        key={product.id}
                        className="hover:bg-gray-50 transition"
                      >

                        {/* Product */}
                        <td className="px-6 py-4">

                          <div className="flex items-center gap-4">

                            {product.imageUrl ? (
                              <img
                                src={product.imageUrl}
                                alt={product.name}
                                className="w-14 h-14 object-cover
                                           rounded-lg border"
                              />
                            ) : (
                              <div className="w-14 h-14 rounded-lg
                                              bg-gray-100 flex
                                              items-center
                                              justify-center">
                                🛍️
                              </div>
                            )}

                            <div>

                              <p className="font-semibold
                                            text-gray-900">
                                {product.name}
                              </p>

                              {product.brand && (
                                <p className="text-sm text-gray-500">
                                  {product.brand}
                                </p>
                              )}

                            </div>

                          </div>

                        </td>

                        {/* Category */}
                        <td className="px-6 py-4">

                          <span className="px-3 py-1 rounded-full
                                           bg-blue-50 text-blue-700
                                           text-sm font-medium">
                            {product.category || 'Uncategorized'}
                          </span>

                        </td>

                        {/* Price */}
                        <td className="px-6 py-4">

                          <span className="font-semibold
                                           text-gray-900">
                            ${Number(product.price).toFixed(2)}
                          </span>

                        </td>

                        {/* Stock */}
                        <td className="px-6 py-4">

                          <span
                            className={
                              product.stockQuantity > 0
                                ? 'text-green-600 font-medium'
                                : 'text-red-600 font-medium'
                            }
                          >
                            {product.stockQuantity}
                          </span>

                        </td>

                        {/* Status */}
                        <td className="px-6 py-4">

                          {product.active !== false ? (
                            <span className="px-3 py-1 rounded-full
                                             bg-green-50 text-green-700
                                             text-sm font-medium">
                              Active
                            </span>
                          ) : (
                            <span className="px-3 py-1 rounded-full
                                             bg-gray-100 text-gray-600
                                             text-sm font-medium">
                              Inactive
                            </span>
                          )}

                        </td>

                        {/* Actions */}
                        <td className="px-6 py-4">

                          <div className="flex gap-2">

                            {/* Edit */}
                            <button
                              onClick={() =>
                                handleEditProduct(product)
                              }
                              className="px-3 py-2 rounded-lg
                                         bg-blue-50 text-blue-700
                                         hover:bg-blue-100
                                         text-sm font-medium
                                         transition"
                            >
                              Edit
                            </button>

                            {/* Deactivate */}
                            <button
                              onClick={() =>
                                handleDeactivateProduct(product)
                              }
                              disabled={product.active === false}
                              className="px-3 py-2 rounded-lg
                                         bg-red-50 text-red-700
                                         hover:bg-red-100
                                         text-sm font-medium
                                         transition
                                         disabled:opacity-50
                                         disabled:cursor-not-allowed"
                            >
                              {product.active === false
                                ? 'Inactive'
                                : 'Deactivate'}
                            </button>

                          </div>

                        </td>

                      </tr>

                    ))}

                  </tbody>

                </table>

              </div>
            )}

            {/* ============================
                MOBILE CARDS
            ============================ */}
            {products.length > 0 && (
              <div className="md:hidden divide-y">

                {products.map((product) => (

                  <div
                    key={product.id}
                    className="p-5"
                  >

                    <div className="flex gap-4">

                      {/* Image */}
                      {product.imageUrl ? (
                        <img
                          src={product.imageUrl}
                          alt={product.name}
                          className="w-20 h-20 object-cover
                                     rounded-lg border"
                        />
                      ) : (
                        <div className="w-20 h-20 rounded-lg
                                        bg-gray-100 flex
                                        items-center
                                        justify-center">
                          🛍️
                        </div>
                      )}

                      {/* Product Info */}
                      <div className="flex-1">

                        <h4 className="font-bold text-gray-900">
                          {product.name}
                        </h4>

                        {product.brand && (
                          <p className="text-sm text-gray-500 mt-1">
                            {product.brand}
                          </p>
                        )}

                        <p className="text-blue-600 font-bold mt-2">
                          ${Number(product.price).toFixed(2)}
                        </p>

                        <p className="text-sm text-gray-500 mt-1">
                          Stock: {product.stockQuantity}
                        </p>

                        <p className="text-sm text-gray-500 mt-1">
                          Category: {product.category}
                        </p>

                      </div>

                    </div>

                    {/* Mobile Bottom */}
                    <div className="flex items-center
                                    justify-between mt-4">

                      {/* Status */}
                      {product.active !== false ? (
                        <span className="px-3 py-1 rounded-full
                                         bg-green-50 text-green-700
                                         text-sm font-medium">
                          Active
                        </span>
                      ) : (
                        <span className="px-3 py-1 rounded-full
                                         bg-gray-100 text-gray-600
                                         text-sm font-medium">
                          Inactive
                        </span>
                      )}

                      {/* Actions */}
                      <div className="flex gap-2">

                        {/* Edit */}
                        <button
                          onClick={() =>
                            handleEditProduct(product)
                          }
                          className="px-3 py-2 rounded-lg
                                     bg-blue-50 text-blue-700
                                     text-sm font-medium
                                     hover:bg-blue-100
                                     transition"
                        >
                          Edit
                        </button>

                        {/* Deactivate */}
                        <button
                          onClick={() =>
                            handleDeactivateProduct(product)
                          }
                          disabled={product.active === false}
                          className="px-3 py-2 rounded-lg
                                     bg-red-50 text-red-700
                                     text-sm font-medium
                                     hover:bg-red-100
                                     transition
                                     disabled:opacity-50
                                     disabled:cursor-not-allowed"
                        >
                          {product.active === false
                            ? 'Inactive'
                            : 'Deactivate'}
                        </button>

                      </div>

                    </div>

                  </div>

                ))}

              </div>
            )}

          </div>
        )}

      </main>

      {/* ============================
          FOOTER
      ============================ */}
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

export default AdminProducts;


