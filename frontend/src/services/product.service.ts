import api from './api';
import type { Product } from '../types/product.types';

/**
 * Product service for fetching product data
 */
class ProductService {
  /**
   * Fetch all products from the backend
   * @returns Promise with array of products
   */
  async getAllProducts(): Promise<Product[]> {
    const response = await api.get<Product[]>('/products');
    return response.data;
  }

  /**
   * Fetch a single product by ID
   * @param id - Product ID
   * @returns Promise with product data
   */
  async getProductById(id: number): Promise<Product> {
    const response = await api.get<Product>(`/products/${id}`);
    return response.data;
  }
}

export default new ProductService();
