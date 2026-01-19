/**
 * API client for AtSea shop backend
 */
import _products from './products.json';

const API_BASE = '/api';

const getAuthHeaders = () => {
  const accessToken = localStorage.getItem('accessToken');
  return accessToken ? { 'Authorization': `Bearer ${accessToken}` } : {};
};

export default {
  getProducts: async (cb, timeout) => {
    try {
      const headers = getAuthHeaders();
      const response = await fetch(`${API_BASE}/product/`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...headers,
        },
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const products = await response.json();
      cb(products);
    } catch (error) {
      console.error('Failed to fetch products:', error);
      // Removed fallback to mock data to ensure API is used
      cb([]);
    }
  },

  buyProducts: (payload, cb, timeout) => setTimeout(() => cb(), timeout || 100)
}
