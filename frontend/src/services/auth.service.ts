import api from './api';
import type { LoginRequest, LoginResponse } from '../types/auth.types';

/**
 * Authentication service for user login/logout operations
 */
class AuthService {
  /**
   * Login user with email and password
   * @param credentials - User login credentials
   * @returns Promise with login response data
   */
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await api.post<LoginResponse>('/auth/login', credentials);
    return response.data;
  }

  /**
   * Logout user by clearing stored data
   */
  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  /**
   * Check if user is authenticated
   * @returns true if token exists in localStorage
   */
  isAuthenticated(): boolean {
    return !!localStorage.getItem('token');
  }

  /**
   * Get stored auth token
   * @returns token string or null
   */
  getToken(): string | null {
    return localStorage.getItem('token');
  }
}

export default new AuthService();
