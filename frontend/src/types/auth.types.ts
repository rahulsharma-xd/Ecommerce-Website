// Login request payload
export interface LoginRequest {
  email: string;
  password: string;
}

// Login response from backend
export interface LoginResponse {
  id: number;
  username: string;
  email: string;
  role: string;
  message: string;
  token: string;
}

// User data stored in context
export interface User {
  id: number;
  username: string;
  email: string;
  role: string;
}
