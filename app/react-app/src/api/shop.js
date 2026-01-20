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

  getCart: async (cb) => {
    try {
      console.log('Fetching cart contents...');
      const response = await fetch(`${API_BASE}/cart/`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeaders(),
        },
      });
      
      if (!response.ok) {
        console.error('Fetch cart failed with status:', response.status);
        cb([]);
        return;
      }
      
      const cart = await response.json();
      console.log('Cart fetched successfully:', cart);
      cb(cart);
    } catch (error) {
      console.error('Failed to fetch cart (network error):', error);
      cb([]);
    }
  },

  addToCart: async (product, quantity, cb) => {
    try {
      console.log('Sending Add to Cart request for product:', product.productId);
      const response = await fetch(`${API_BASE}/cart/`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeaders(),
        },
        body: JSON.stringify({
          productId: product.productId,
          name: product.name,
          price: product.price,
          quantity: quantity,
          image: product.image
        }),
      });
      
      if (!response.ok) {
        console.error('Add to cart failed with status:', response.status);
        const errorText = await response.text();
        console.error('Error details:', errorText);
      } else {
        console.log('Successfully added to cart');
        cb();
      }
    } catch (error) {
      console.error('Failed to add to cart (network error):', error);
    }
  },

  removeFromCart: async (productId, cb) => {
    try {
      await fetch(`${API_BASE}/cart/${productId}`, {
        method: 'DELETE',
        headers: {
          ...getAuthHeaders(),
        },
      });
      cb();
    } catch (error) {
      console.error('Failed to remove from cart:', error);
    }
  },

  clearCart: async (cb) => {
    try {
      await fetch(`${API_BASE}/cart/`, {
        method: 'DELETE',
        headers: {
          ...getAuthHeaders(),
        },
      });
    } catch (error) {
      console.error('Failed to clear cart:', error);
      throw error;
    } finally {
      if (typeof cb === 'function') {
        cb();
      }
    }
  },

  buyProducts: (payload, cb, timeout) => setTimeout(() => cb(), timeout || 100)
}
