window.ApiClient = (function () {
  const BASE_URL = '';

  async function request(url, options = {}) {
    const resp = await fetch(BASE_URL + url, {
      headers: {
        'Content-Type': 'application/json',
        ...(options.headers || {}),
      },
      ...options,
    });

    if (!resp.ok) {
      const text = await resp.text().catch(() => '');
      throw new Error(`HTTP ${resp.status} for ${url}: ${text}`);
    }

    if (resp.status === 204) return null;
    const ct = resp.headers.get('Content-Type') || '';
    if (ct.includes('application/json')) {
      return resp.json();
    }
    return resp.text();
  }

  const Products = {
    async search(params) {
      const q = new URLSearchParams();
      if (params.category) q.append('category', params.category);
      if (params.minPrice != null) q.append('minPrice', params.minPrice);
      if (params.maxPrice != null) q.append('maxPrice', params.maxPrice);
      q.append('page', params.page ?? 0);
      q.append('size', params.size ?? 10);
      q.append('sort', params.sort || 'id,asc');
      q.append('useNative', params.useNative ? 'true' : 'false');

      return request(`/api/products/search?${q.toString()}`);
    },

    async create(payload) {
      return request('/api/products', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
    },
  };

  const Categories = {
    async getAll() {
      return request('/api/categories');
    },
  };

  const Stores = {
    async getAll() {
      return request('/api/stores');
    },
  };

  const PriceHistory = {
    async getByProduct(productId, options = {}) {
      const q = new URLSearchParams();
      if (options.optimized) q.append('optimized', 'true');
      return request(`/api/price-history/product/${productId}?${q.toString()}`);
    },
  };

  return {
    Products,
    Categories,
    Stores,
    PriceHistory,
  };
})();