import api from './api';
import type { Cart, AddToCartRequest, UpdateCartItemRequest } from '../types/cart.types';

/**
 * Cart service for managing shopping cart operations
 */
class CartService {
  /**
   * Get current user's cart
   * Auto-creates cart if user doesn't have one
   * @returns Promise with cart data
   */
  async getCart(): Promise<Cart> {
    const response = await api.get<Cart>('/cart');
    return response.data;
  }

  /**
   * Add item to cart
   * If product already in cart, increases quantity
   * @param productId - Product ID to add
   * @param quantity - Quantity to add
   * @returns Promise with updated cart
   */
  async addToCart(productId: number, quantity: number): Promise<Cart> {
    const request: AddToCartRequest = { productId, quantity };
    const response = await api.post<Cart>('/cart/items', request);
    return response.data;
  }

  /**
   * Update cart item quantity
   * @param itemId - Cart item ID
   * @param quantity - New quantity
   * @returns Promise with updated cart
   */
  async updateCartItem(itemId: number, quantity: number): Promise<Cart> {
    const request: UpdateCartItemRequest = { quantity };
    const response = await api.put<Cart>(`/cart/items/${itemId}`, request);
    return response.data;
  }
}

export default new CartService();
