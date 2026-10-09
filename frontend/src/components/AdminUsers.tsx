import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

interface AdminUser {
  id: number;
  username: string;
  email: string;
  role: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

const AdminUsers = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [users, setUsers] = useState<AdminUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [deletingUserId, setDeletingUserId] = useState<number | null>(null);

  // ========================================
  // FETCH USERS
  // ========================================
  const fetchUsers = async () => {
    try {
      setLoading(true);
      setError('');

      const response = await api.get<AdminUser[]>(
        '/admin/users'
      );

      setUsers(response.data);
    } catch (err: any) {
      console.error('Failed to load users:', err);

      if (err.response?.status === 403) {
        setError(
          'You do not have permission to access users.'
        );
      } else {
        setError(
          'Failed to load users. Please try again.'
        );
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  // ========================================
  // LOGOUT
  // ========================================
  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // ========================================
  // DELETE USER
  // ========================================
  const handleDeleteUser = async (selectedUser: AdminUser) => {
    if (selectedUser.id === user?.id) {
      setError('You cannot delete your own admin account.');
      return;
    }

    const confirmed = window.confirm(
      `Are you sure you want to delete "${selectedUser.username}"?`
    );

    if (!confirmed) {
      return;
    }

    try {
      setDeletingUserId(selectedUser.id);
      setError('');
      setSuccessMessage('');

      await api.delete(
        `/admin/users/${selectedUser.id}`
      );

      setSuccessMessage(
        `User "${selectedUser.username}" deleted successfully.`
      );

      await fetchUsers();
    } catch (err: any) {
      console.error(
        'Failed to delete user:',
        err
      );

      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.status === 403) {
        setError(
          'You do not have permission to delete users.'
        );
      } else {
        setError(
          'Failed to delete user. Please try again.'
        );
      }
    } finally {
      setDeletingUserId(null);
    }
  };

  // ========================================
  // STATISTICS
  // ========================================
  const adminCount = users.filter(
    (currentUser) => currentUser.role === 'ADMIN'
  ).length;

  const customerCount = users.filter(
    (currentUser) => currentUser.role !== 'ADMIN'
  ).length;

  const activeCount = users.filter(
    (currentUser) => currentUser.active !== false
  ).length;

  return (
    <div className="min-h-screen bg-gray-100">

      {/* ========================================
          HEADER
      ======================================== */}
      <header className="bg-white border-b shadow-sm sticky top-0 z-20">

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

      {/* ========================================
          MAIN
      ======================================== */}
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
              Manage Users
            </h2>

            <p className="text-gray-500 mt-1">
              View and manage registered ShopHub users.
            </p>

          </div>

          <button
            onClick={fetchUsers}
            disabled={loading}
            className="px-5 py-3 bg-white
                       border border-gray-300
                       text-gray-700 rounded-lg
                       hover:bg-gray-50
                       transition font-medium
                       disabled:opacity-50"
          >
            ↻ Refresh Users
          </button>

        </div>

        {/* ========================================
            SUCCESS MESSAGE
        ======================================== */}
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

        {/* ========================================
            ERROR MESSAGE
        ======================================== */}
        {error && (
          <div className="mb-6 bg-red-50
                          border border-red-200
                          text-red-700
                          rounded-xl p-4">

            <p className="font-medium">
              {error}
            </p>

            <button
              onClick={fetchUsers}
              className="mt-2 text-sm
                         font-medium underline"
            >
              Try Again
            </button>

          </div>
        )}

        {/* ========================================
            SUMMARY CARDS
        ======================================== */}
        {!loading && !error && (
          <div className="grid grid-cols-2
                          md:grid-cols-4 gap-4 mb-8">

            {/* Total */}
            <div className="bg-white rounded-xl
                            shadow-sm p-5">

              <p className="text-sm text-gray-500">
                Total Users
              </p>

              <p className="text-3xl font-bold
                            text-gray-900 mt-2">
                {users.length}
              </p>

            </div>

            {/* Customers */}
            <div className="bg-white rounded-xl
                            shadow-sm p-5">

              <p className="text-sm text-gray-500">
                Customers
              </p>

              <p className="text-3xl font-bold
                            text-blue-600 mt-2">
                {customerCount}
              </p>

            </div>

            {/* Admins */}
            <div className="bg-white rounded-xl
                            shadow-sm p-5">

              <p className="text-sm text-gray-500">
                Administrators
              </p>

              <p className="text-3xl font-bold
                            text-purple-600 mt-2">
                {adminCount}
              </p>

            </div>

            {/* Active */}
            <div className="bg-white rounded-xl
                            shadow-sm p-5">

              <p className="text-sm text-gray-500">
                Active Accounts
              </p>

              <p className="text-3xl font-bold
                            text-green-600 mt-2">
                {activeCount}
              </p>

            </div>

          </div>
        )}

        {/* ========================================
            LOADING
        ======================================== */}
        {loading && (
          <div className="bg-white rounded-xl
                          shadow-sm p-12 text-center">

            <div
              className="animate-spin rounded-full
                         h-10 w-10 border-b-2
                         border-blue-600
                         mx-auto mb-4"
            />

            <p className="text-gray-600">
              Loading users...
            </p>

          </div>
        )}

        {/* ========================================
            USERS
        ======================================== */}
        {!loading && !error && (
          <div className="bg-white rounded-xl
                          shadow-sm overflow-hidden">

            {/* Header */}
            <div className="px-6 py-5 border-b">

              <h3 className="text-lg font-bold
                             text-gray-900">
                All Users
              </h3>

              <p className="text-sm text-gray-500 mt-1">
                {users.length} registered account
                {users.length !== 1 ? 's' : ''}
              </p>

            </div>

            {/* Empty */}
            {users.length === 0 && (
              <div className="p-12 text-center">

                <div className="text-5xl mb-4">
                  👥
                </div>

                <h3 className="text-xl font-bold
                               text-gray-900">
                  No users found
                </h3>

                <p className="text-gray-500 mt-2">
                  Registered users will appear here.
                </p>

              </div>
            )}

            {/* ========================================
                DESKTOP TABLE
            ======================================== */}
            {users.length > 0 && (
              <div className="hidden md:block overflow-x-auto">

                <table className="w-full">

                  <thead className="bg-gray-50 border-b">

                    <tr>

                      <th className="text-left px-6 py-4
                                     text-xs font-semibold
                                     text-gray-500 uppercase">
                        User
                      </th>

                      <th className="text-left px-6 py-4
                                     text-xs font-semibold
                                     text-gray-500 uppercase">
                        Email
                      </th>

                      <th className="text-left px-6 py-4
                                     text-xs font-semibold
                                     text-gray-500 uppercase">
                        Role
                      </th>

                      <th className="text-left px-6 py-4
                                     text-xs font-semibold
                                     text-gray-500 uppercase">
                        Status
                      </th>

                      <th className="text-left px-6 py-4
                                     text-xs font-semibold
                                     text-gray-500 uppercase">
                        Registered
                      </th>

                      <th className="text-left px-6 py-4
                                     text-xs font-semibold
                                     text-gray-500 uppercase">
                        Actions
                      </th>

                    </tr>

                  </thead>

                  <tbody className="divide-y">

                    {users.map((currentUser) => (

                      <tr
                        key={currentUser.id}
                        className="hover:bg-gray-50 transition"
                      >

                        {/* User */}
                        <td className="px-6 py-5">

                          <div className="flex items-center gap-3">

                            <div
                              className="w-10 h-10 rounded-full
                                         bg-blue-100
                                         flex items-center
                                         justify-center"
                            >
                              <span className="text-lg">
                                👤
                              </span>
                            </div>

                            <div>

                              <p className="font-semibold
                                            text-gray-900">
                                {currentUser.username}
                              </p>

                              <p className="text-xs
                                            text-gray-500">
                                ID: {currentUser.id}
                              </p>

                            </div>

                          </div>

                        </td>

                        {/* Email */}
                        <td className="px-6 py-5">

                          <p className="text-sm
                                        text-gray-700">
                            {currentUser.email}
                          </p>

                        </td>

                        {/* Role */}
                        <td className="px-6 py-5">

                          {currentUser.role === 'ADMIN' ? (
                            <span
                              className="px-3 py-1 rounded-full
                                         bg-purple-50
                                         text-purple-700
                                         text-sm font-medium"
                            >
                              ADMIN
                            </span>
                          ) : (
                            <span
                              className="px-3 py-1 rounded-full
                                         bg-blue-50
                                         text-blue-700
                                         text-sm font-medium"
                            >
                              USER
                            </span>
                          )}

                        </td>

                        {/* Status */}
                        <td className="px-6 py-5">

                          {currentUser.active !== false ? (
                            <span
                              className="px-3 py-1 rounded-full
                                         bg-green-50
                                         text-green-700
                                         text-sm font-medium"
                            >
                              Active
                            </span>
                          ) : (
                            <span
                              className="px-3 py-1 rounded-full
                                         bg-gray-100
                                         text-gray-600
                                         text-sm font-medium"
                            >
                              Inactive
                            </span>
                          )}

                        </td>

                        {/* Registered */}
                        <td className="px-6 py-5">

                          {currentUser.createdAt ? (
                            <div>

                              <p className="text-sm
                                            text-gray-700">
                                {new Date(
                                  currentUser.createdAt
                                ).toLocaleDateString(
                                  'en-IN',
                                  {
                                    day: '2-digit',
                                    month: 'short',
                                    year: 'numeric',
                                  }
                                )}
                              </p>

                              <p className="text-xs
                                            text-gray-500 mt-1">
                                {new Date(
                                  currentUser.createdAt
                                ).toLocaleTimeString(
                                  'en-IN',
                                  {
                                    hour: '2-digit',
                                    minute: '2-digit',
                                  }
                                )}
                              </p>

                            </div>
                          ) : (
                            <span className="text-gray-400">
                              —
                            </span>
                          )}

                        </td>

                        {/* Actions */}
                        <td className="px-6 py-5">

                          {currentUser.id === user?.id ? (

                            <span className="text-sm
                                             text-gray-400">
                              Current account
                            </span>

                          ) : (

                            <button
                              onClick={() =>
                                handleDeleteUser(
                                  currentUser
                                )
                              }
                              disabled={
                                deletingUserId ===
                                currentUser.id
                              }
                              className="px-3 py-2
                                         rounded-lg
                                         bg-red-50
                                         text-red-700
                                         hover:bg-red-100
                                         text-sm
                                         font-medium
                                         transition
                                         disabled:opacity-50
                                         disabled:cursor-not-allowed"
                            >
                              {deletingUserId ===
                              currentUser.id
                                ? 'Deleting...'
                                : 'Delete'}
                            </button>

                          )}

                        </td>

                      </tr>

                    ))}

                  </tbody>

                </table>

              </div>
            )}

            {/* ========================================
                MOBILE CARDS
            ======================================== */}
            {users.length > 0 && (
              <div className="md:hidden divide-y">

                {users.map((currentUser) => (

                  <div
                    key={currentUser.id}
                    className="p-5"
                  >

                    <div className="flex
                                    items-start
                                    justify-between
                                    gap-4">

                      <div className="flex gap-3">

                        <div
                          className="w-11 h-11
                                     rounded-full
                                     bg-blue-100
                                     flex items-center
                                     justify-center
                                     flex-shrink-0"
                        >
                          <span className="text-lg">
                            👤
                          </span>
                        </div>

                        <div>

                          <h4 className="font-bold
                                         text-gray-900">
                            {currentUser.username}
                          </h4>

                          <p className="text-sm
                                        text-gray-500
                                        mt-1 break-all">
                            {currentUser.email}
                          </p>

                          <p className="text-xs
                                        text-gray-400 mt-1">
                            User ID: {currentUser.id}
                          </p>

                        </div>

                      </div>

                      {currentUser.role === 'ADMIN' ? (
                        <span
                          className="px-2 py-1
                                     rounded-full
                                     bg-purple-50
                                     text-purple-700
                                     text-xs font-medium"
                        >
                          ADMIN
                        </span>
                      ) : (
                        <span
                          className="px-2 py-1
                                     rounded-full
                                     bg-blue-50
                                     text-blue-700
                                     text-xs font-medium"
                        >
                          USER
                        </span>
                      )}

                    </div>

                    <div className="mt-4 grid
                                    grid-cols-2 gap-4">

                      <div>

                        <p className="text-xs
                                      text-gray-500">
                          Status
                        </p>

                        <p className="mt-1">

                          {currentUser.active !== false ? (
                            <span
                              className="text-sm
                                         font-medium
                                         text-green-600"
                            >
                              Active
                            </span>
                          ) : (
                            <span
                              className="text-sm
                                         font-medium
                                         text-gray-500"
                            >
                              Inactive
                            </span>
                          )}

                        </p>

                      </div>

                      <div>

                        <p className="text-xs
                                      text-gray-500">
                          Registered
                        </p>

                        <p className="text-sm
                                      text-gray-700 mt-1">

                          {currentUser.createdAt
                            ? new Date(
                                currentUser.createdAt
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

                    </div>

                    {currentUser.id === user?.id ? (

                      <div
                        className="mt-4 px-4 py-3
                                   bg-gray-50
                                   rounded-lg
                                   text-sm
                                   text-gray-500
                                   text-center"
                      >
                        This is your current admin account.
                      </div>

                    ) : (

                      <button
                        onClick={() =>
                          handleDeleteUser(
                            currentUser
                          )
                        }
                        disabled={
                          deletingUserId ===
                          currentUser.id
                        }
                        className="w-full mt-4
                                   px-4 py-3
                                   bg-red-50
                                   text-red-700
                                   rounded-lg
                                   hover:bg-red-100
                                   transition
                                   font-medium
                                   disabled:opacity-50"
                      >
                        {deletingUserId ===
                        currentUser.id
                          ? 'Deleting User...'
                          : 'Delete User'}
                      </button>

                    )}

                  </div>

                ))}

              </div>
            )}

          </div>
        )}

      </main>

      {/* ========================================
          FOOTER
      ======================================== */}
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

export default AdminUsers;