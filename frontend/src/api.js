// In production (Vercel), VITE_API_URL = https://your-render-url.onrender.com
// In development, falls back to /api (proxied to localhost:8080 by Vite)
const API_BASE = import.meta.env.VITE_API_URL
  ? `${import.meta.env.VITE_API_URL}/api`
  : '/api';

// Helper to get auth header
const getAuthHeader = () => {
  const token = localStorage.getItem('token');
  return token ? { 'Authorization': `Bearer ${token}` } : {};
};

// Generic request helper
async function request(url, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...getAuthHeader(),
    ...options.headers,
  };

  const response = await fetch(`${API_BASE}${url}`, {
    ...options,
    headers,
  });

  if (response.status === 204) {
    return null;
  }

  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.message || response.statusText || 'API error');
  }
  return data;
}

export const api = {
  // Auth API
  register: async (username, email, password, role) => {
    return request('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, email, password, role }),
    });
  },

  login: async (email, password) => {
    const data = await request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    if (data.token) {
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify({
        username: data.username,
        email: data.email,
        role: data.role,
      }));
    }
    return data;
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  getCurrentUser: () => {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },

  // Vehicles API
  getVehicles: async () => {
    return request('/vehicles');
  },

  searchVehicles: async ({ make, model, category, minPrice, maxPrice }) => {
    const query = new URLSearchParams();
    if (make) query.append('make', make);
    if (model) query.append('model', model);
    if (category) query.append('category', category);
    if (minPrice) query.append('minPrice', minPrice);
    if (maxPrice) query.append('maxPrice', maxPrice);

    return request(`/vehicles/search?${query.toString()}`);
  },

  addVehicle: async (vehicleData) => {
    return request('/vehicles', {
      method: 'POST',
      body: JSON.stringify(vehicleData),
    });
  },

  updateVehicle: async (id, vehicleData) => {
    return request(`/vehicles/${id}`, {
      method: 'PUT',
      body: JSON.stringify(vehicleData),
    });
  },

  deleteVehicle: async (id) => {
    return request(`/vehicles/${id}`, {
      method: 'DELETE',
    });
  },

  purchaseVehicle: async (id) => {
    return request(`/vehicles/${id}/purchase`, {
      method: 'POST',
    });
  },

  restockVehicle: async (id, quantity) => {
    return request(`/vehicles/${id}/restock`, {
      method: 'POST',
      body: JSON.stringify({ quantity: parseInt(quantity, 10) }),
    });
  },
};
