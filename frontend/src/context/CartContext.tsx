import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { useAuth } from './AuthContext';
import cartService from '../services/cart.service';
import type { Cart } from '../types/cart.types';

// Define the shape of our context
interface CartContextType {
  cart: Cart | null;
  loading: boolean;
  error: string | null;
  totalItems: number;
  addToCart: (productId: number, quantity: number) => Promise<void>;
  updateCartItem: (itemId: number, quantity: number) => Promise<void>;
  removeCartItem: (itemId: number) => Promise<void>;
  refreshCart: () => Promise<void>;
}

// Create context with undefined default value
const CartContext = createContext<CartContextType | undefined>(undefined);

// Provider props
interface CartProviderProps {
  children: ReactNode;
}

/**
 * CartProvider component that wraps the app and provides cart state
 */
export const CartProvider = ({ children }: CartProviderProps) => {
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const { isAuthenticated } = useAuth();

  // Auto-fetch cart when user is authenticated
  useEffect(() => {
    if (isAuthenticated) {
      refreshCart();
    } else {
      setCart(null);
    }
  }, [isAuthenticated]);

  /**
   * Refresh cart from server
   */
  const refreshCart = async (): Promise<void> => {
    try {
      setLoading(true);
      setError(null);
      const cartData = await cartService.getCart();
      setCart(cartData);
    } catch (err: any) {
      console.error('Failed to fetch cart:', err);
      setError('Failed to load cart. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Add item to cart
   */
  const addToCart = async (productId: number, quantity: number): Promise<void> => {
    try {
      setLoading(true);
      setError(null);
      const updatedCart = await cartService.addToCart(productId, quantity);
      setCart(updatedCart);
    } catch (err: any) {
      console.error('Failed to add to cart:', err);
      setError(err.response?.data?.message || 'Failed to add item to cart.');
      throw err; // Re-throw so component can handle it
    } finally {
      setLoading(false);
    }
  };

  /**
   * Update cart item quantity
   */
  const updateCartItem = async (itemId: number, quantity: number): Promise<void> => {
    try {
      setLoading(true);
      setError(null);
      const updatedCart = await cartService.updateCartItem(itemId, quantity);
      setCart(updatedCart);
    } catch (err: any) {
      console.error('Failed to update cart item:', err);
      setError(err.response?.data?.message || 'Failed to update cart item.');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  /**
   * Remove item from cart by setting quantity to 0
   */
  const removeCartItem = async (itemId: number): Promise<void> => {
    try {
      setLoading(true);
      setError(null);
      const updatedCart = await cartService.updateCartItem(itemId, 0);
      setCart(updatedCart);
    } catch (err: any) {
      console.error('Failed to remove cart item:', err);
      setError(err.response?.data?.message || 'Failed to remove item from cart.');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Calculate total items
  const totalItems = cart?.totalItems || 0;

  const value: CartContextType = {
    cart,
    loading,
    error,
    totalItems,
    addToCart,
    updateCartItem,
    removeCartItem,
    refreshCart,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};

/**
 * Custom hook to use the cart context
 * @throws Error if used outside of CartProvider
 */
export const useCart = (): CartContextType => {
  const context = useContext(CartContext);
  if (context === undefined) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};
