import api from './api';
import type {
  OrderResponse,
  CheckoutRequest,
  OrderStatus,
  UpdateOrderStatusRequest
} from '../types/order.types';

/**
 * Order service for managing order operations
 */
class OrderService {
  /**
   * Process checkout with cart items
   * Deducts stock, clears cart, sends confirmation email
   * @param checkoutRequest - Payment card details
   * @returns Promise with order response including order number
   */
  async checkout(checkoutRequest: CheckoutRequest): Promise<OrderResponse> {
    const response = await api.post<OrderResponse>('/orders/checkout', checkoutRequest);
    return response.data;
  }

  /**
   * Get all orders for authenticated user
   * Orders sorted by most recent first
   * @returns Promise with array of user's orders
   */
  async getUserOrders(): Promise<OrderResponse[]> {
    const response = await api.get<OrderResponse[]>('/orders');
    return response.data;
  }

  /**
   * Get specific order details for authenticated user
   * Validates order belongs to user
   * @param orderId - Order ID
   * @returns Promise with order details
   */
  async getOrderById(orderId: number): Promise<OrderResponse> {
    const response = await api.get<OrderResponse>(`/orders/${orderId}`);
    return response.data;
  }

  // ==================== ADMIN METHODS ====================

  /**
   * Get all orders in the system (admin only)
   * No ownership restrictions
   * @returns Promise with all orders
   */
  async getAllOrders(): Promise<OrderResponse[]> {
    const response = await api.get<OrderResponse[]>('/admin/orders');
    return response.data;
  }

  /**
   * Get any order by ID without ownership check (admin only)
   * @param orderId - Order ID
   * @returns Promise with order details
   */
  async getOrderByIdAdmin(orderId: number): Promise<OrderResponse> {
    const response = await api.get<OrderResponse>(`/admin/orders/${orderId}`);
    return response.data;
  }

  /**
   * Update order status (admin only)
   * Supports status progression: PENDING → PROCESSING → SHIPPED → DELIVERED
   * @param orderId - Order ID
   * @param status - New order status
   * @returns Promise with updated order
   */
  async updateOrderStatus(orderId: number, status: OrderStatus): Promise<OrderResponse> {
    const request: UpdateOrderStatusRequest = { status };
    const response = await api.put<OrderResponse>(`/admin/orders/${orderId}/status`, request);
    return response.data;
  }
}

export default new OrderService();
