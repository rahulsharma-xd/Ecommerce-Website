// Product entity
export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
  stockQuantity: number;
  category: string;
  brand?: string;
  active?: boolean;
}

// Product list response
export interface ProductListResponse {
  products: Product[];
  totalCount: number;
}
