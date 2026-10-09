/**
 * Cart type definitions matching backend DTOs
 */

// Cart item in the cart
export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  productImageUrl: string;
  priceAtAddition: number;
  quantity: number;
  subtotal: number;
}

// Complete cart with all items
export interface Cart {
  id: number;
  items: CartItem[];
  totalItems: number;
  totalPrice: number;
}

// Request to add item to cart
export interface AddToCartRequest {
  productId: number;
  quantity: number;
}

// Request to update cart item quantity
export interface UpdateCartItemRequest {
  quantity: number;
}
