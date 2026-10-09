import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      await login({ email, password });

      setSuccess(true);

      // Clear form
      setEmail('');
      setPassword('');

      // Redirect based on user role
      setTimeout(() => {
        const storedUser = localStorage.getItem('user');

        if (storedUser) {
          const userData = JSON.parse(storedUser);

          if (userData.role === 'ADMIN') {
            navigate('/admin');
          } else {
            navigate('/dashboard');
          }
        } else {
          navigate('/dashboard');
        }
      }, 500);

    } catch (err: any) {

      if (err.response?.data?.message) {
        setError(err.response.data.message);

      } else if (err.response?.status === 401) {
        setError('Invalid email or password');

      } else if (err.message) {
        setError(err.message);

      } else {
        setError('Login failed. Please try again.');
      }

    } finally {
      setLoading(false);
    }
  };

  const fillUserCredentials = () => {
    setEmail('userDataInit@example.com');
    setPassword('user123');
  };

  const fillAdminCredentials = () => {
    setEmail('adminDataInit@example.com');
    setPassword('admin123');
  };

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">

      <div className="bg-white w-96 h-auto p-8 rounded-lg shadow-lg border-2 border-black">

        <h2 className="text-2xl font-bold text-center mb-6 text-gray-800">
          Welcome Back
        </h2>

        <form onSubmit={handleSubmit} className="space-y-4">

          {/* Email */}
          <div>
            <label
              htmlFor="email"
              className="block text-sm font-medium text-gray-700 mb-2"
            >
              Email
            </label>

            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                setError(null);
                setSuccess(false);
              }}
              className="w-full px-4 py-2 border border-gray-300 rounded-md
                         focus:outline-none focus:ring-2 focus:ring-blue-500
                         focus:border-transparent"
              placeholder="Enter your email"
              required
            />
          </div>

          {/* Password */}
          <div>
            <label
              htmlFor="password"
              className="block text-sm font-medium text-gray-700 mb-2"
            >
              Password
            </label>

            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                setError(null);
                setSuccess(false);
              }}
              className="w-full px-4 py-2 border border-gray-300 rounded-md
                         focus:outline-none focus:ring-2 focus:ring-blue-500
                         focus:border-transparent"
              placeholder="Enter your password"
              required
            />
          </div>

          {/* Login Button */}
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 text-white py-2 rounded-md
                       hover:bg-blue-700 transition-colors duration-200
                       font-medium disabled:bg-blue-400
                       disabled:cursor-not-allowed"
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>

        </form>

        {/* Error */}
        {error && (
          <div className="mt-4 p-3 bg-red-100 border border-red-400
                          text-red-700 rounded-md text-sm">
            {error}
          </div>
        )}

        {/* Success */}
        {success && (
          <div className="mt-4 p-3 bg-green-100 border border-green-400
                          text-green-700 rounded-md text-sm">
            Login successful! Welcome back.
          </div>
        )}

        {/* Quick Fill Buttons */}
        <div className="mt-6 pt-6 border-t border-gray-200">

          <p className="text-xs text-gray-500 text-center mb-3">
            Quick fill for testing:
          </p>

          <div className="flex gap-2">

            <button
              type="button"
              onClick={fillUserCredentials}
              className="flex-1 bg-gray-200 text-gray-700 py-2 px-4
                         rounded-md hover:bg-gray-300
                         transition-colors duration-200
                         text-sm font-medium"
            >
              Fill User
            </button>

            <button
              type="button"
              onClick={fillAdminCredentials}
              className="flex-1 bg-gray-200 text-gray-700 py-2 px-4
                         rounded-md hover:bg-gray-300
                         transition-colors duration-200
                         text-sm font-medium"
            >
              Fill Admin
            </button>

          </div>

        </div>

      </div>

    </div>
  );
};

export default Login;
