const API_ENDPOINTS = {
};

const Api = (function() {
    function getHeaders() {
        const token = localStorage.getItem('jwtToken');
        return {
            'Content-Type': 'application/json',
            ...(token ? { 'Authorization': `Bearer ${token}` } : {})
        };
    }

    async function request(endpoint, options = {}) {
        const url = `/api${endpoint}`;
        try {
            const response = await fetch(url, {
                ...options,
                headers: getHeaders(),
                credentials: 'include' // <--- REQUIRED for session cookies!
            });

            if (response.status === 401) throw new Error('UnauthorizedError');
            if (response.status === 403) throw new Error('ForbiddenError');

            let data;
            try { data = await response.json(); } catch(e) { data = null; }

            if (!response.ok) throw new Error(data?.message || 'Server Error');

            // MAGIC FIX: Wrap raw JSON in a "data" property so the UI tables don't crash reading res.data
            if (data !== null && typeof data === 'object' && !('data' in data)) {
                return {
                    data: data,
                    message: data.message || "Success"
                };
            }
            return data;
        } catch (error) {
            if (error.message === 'UnauthorizedError') {
                window.location.href = '/login?expired=true';
            }
            throw error;
        }
    }

    return {
        get: (endpoint) => request(endpoint, { method: 'GET' }),
        post: (endpoint, body) => request(endpoint, { method: 'POST', body: JSON.stringify(body) }),
        put: (endpoint, body) => request(endpoint, { method: 'PUT', body: JSON.stringify(body) }),
        delete: (endpoint) => request(endpoint, { method: 'DELETE' })
    };
})();