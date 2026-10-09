/**
 * Order type definitions matching backend DTOs
 */

// Order status constants
export const OrderStatus = {
  PENDING: 'PENDING',
  PROCESSING: 'PROCESSING',
  SHIPPED: 'SHIPPED',
  DELIVERED: 'DELIVERED',
  CANCELLED: 'CANCELLED',
} as const;

// Order status type
export type OrderStatus =
  (typeof OrderStatus)[keyof typeof OrderStatus];

// Checkout request with payment card details
export interface CheckoutRequest {
  cardNumber: string;
  cardHolderName: string;
  expiryDate: string;
  cvv: string;
}

// Order item in an order (product snapshot at time of purchase)
export interface OrderItemDTO {
  id: number;
  productId: number;
  productName: string;
  productImageUrl: string;
  quantity: number;
  priceAtPurchase: number;
  subtotal: number;
}

// Complete order response
export interface OrderResponse {
  id: number;
  orderNumber: string;
  status: OrderStatus;
  totalAmount: number;
  orderedAt: string;
  updatedAt: string;
  items: OrderItemDTO[];
}

// Admin request to update order status
export interface UpdateOrderStatusRequest {
  status: OrderStatus;
}
