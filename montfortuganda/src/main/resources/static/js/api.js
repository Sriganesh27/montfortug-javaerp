// ==========================================
// api.js - Core API Communication Layer
// ==========================================

const API_BASE_URL = '/api';

/**
 * Helper to get authorization headers
 */
function getAuthHeaders(isMultipart = false) {
    const headers = {};

    // Do NOT set Content-Type for multipart/form-data. The browser must set it automatically.
    if (!isMultipart) {
        headers['Content-Type'] = 'application/json';
    }

    return headers;
}

/**
 * Handle API responses globally
 */
async function handleResponse(response) {
    if (!response.ok) {
        // Handle 401 Unauthorized globally
        if (response.status === 401) {
            if (typeof window.showSessionTimeoutModal === 'function') {
                window.showSessionTimeoutModal({
                    title: "Session Expired",
                    message: "Your secure dashboard session has expired.",
                    buttonText: "Login Again",
                    redirectUrl: "/login.html"
                });
            } else {
                window.location.href = '/login.html';
            }
            throw new Error("Session expired. Please log in again.");
        }
        const errorData = await response.json().catch(() => null);
        const errorMessage = errorData?.message || `HTTP Error: ${response.status}`;
        throw new Error(errorMessage);
    }

    const json = await response.json();

    // MAGIC FIX: If the backend returns raw JSON without a "data" property,
    // we automatically wrap it so superadmin.js never throws 'undefined' again!
    if (json !== null && typeof json === 'object' && !('data' in json)) {
        return {
            data: json,
            message: json.message || "Success"
        };
    }

    return json;
}

/**
 * Standard GET Request
 */
async function apiGet(endpoint) {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'GET',
        headers: getAuthHeaders(),
        credentials: 'include' // <--- FORCES COOKIE TO BE SENT
    });
    return handleResponse(response);
}

/**
 * Standard POST Request (JSON)
 */
async function apiPost(endpoint, data) {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(data),
        credentials: 'include' // <--- FORCES COOKIE TO BE SENT
    });
    return handleResponse(response);
}

/**
 * Standard PUT Request (JSON)
 */
async function apiPut(endpoint, data) {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify(data),
        credentials: 'include' // <--- FORCES COOKIE TO BE SENT
    });
    return handleResponse(response);
}


/**
 * MULTIPART Request (For File Uploads like Photos/Documents)
 */
async function apiMultipart(endpoint, method, formData) {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: method, // POST or PUT
        headers: getAuthHeaders(true),
        body: formData,
        credentials: 'include' // <--- FORCES COOKIE TO BE SENT
    });
    return handleResponse(response);
}