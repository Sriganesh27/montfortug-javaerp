// ==========================================
// api.js - Core API Communication Layer
// ==========================================

const API_BASE_URL = '/api';
const inFlightGetRequests = new Map();

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

// Flag to prevent multiple session timeout modals
let isSessionTimeoutShown = false;

/**
 * Handle API responses globally
 */
async function handleResponse(response) {
    if (!response.ok) {
        // Handle 401 Unauthorized globally
        if (response.status === 401) {
            localStorage.removeItem('user_role');
            localStorage.removeItem('username');
            localStorage.removeItem('user_branch');
            localStorage.removeItem('school_id');
            localStorage.removeItem('branch_id');
            localStorage.removeItem('permissions');

            if (!isSessionTimeoutShown) {
                isSessionTimeoutShown = true;
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
            }
            throw new Error("Session expired. Please log in again.");
        }
        const errorData = await response.json().catch(() => null);
        let errorMessage = errorData?.message || `HTTP Error: ${response.status}`;

        // Extract Spring Boot validation field errors if they exist
        if (errorData?.errors && typeof errorData.errors === 'object') {
            const fieldErrors = Object.entries(errorData.errors)
                .map(([field, msg]) => `${field}: ${msg}`)
                .join('\n');
            errorMessage += `\n\n${fieldErrors}`;
        }

        const apiError = new Error(errorMessage);

        apiError.name = 'ApiError';
        apiError.status = response.status;
        apiError.data = errorData;
        apiError.response = {
            status: response.status,
            statusText: response.statusText,
            data: errorData
        };

        throw apiError;
    }

    let text = await response.text();
    let json;
    try {
        json = text ? JSON.parse(text) : null;
    } catch(e) {
        // Not JSON, return raw text directly
        return text;
    }

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
    const normalizedEndpoint = String(endpoint || '').trim();

    if (inFlightGetRequests.has(normalizedEndpoint)) {
        return inFlightGetRequests.get(normalizedEndpoint);
    }

    const request = (async () => {
        const response = await fetch(
            `${API_BASE_URL}${normalizedEndpoint}`,
            {
                method: 'GET',
                headers: getAuthHeaders(),
                credentials: 'include',
                cache: 'no-store'
            }
        );

        return handleResponse(response);
    })();

    inFlightGetRequests.set(normalizedEndpoint, request);

    try {
        return await request;
    } finally {
        if (inFlightGetRequests.get(normalizedEndpoint) === request) {
            inFlightGetRequests.delete(normalizedEndpoint);
        }
    }
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
 * Standard DELETE Request
 *
 * The optional data argument supports endpoints that accept a JSON body.
 * Employee deactivation calls this method without a body.
 */
async function apiDelete(endpoint, data = undefined) {
    const requestOptions = {
        method: 'DELETE',
        headers: getAuthHeaders(),
        credentials: 'include'
    };

    if (data !== undefined) {
        requestOptions.body = JSON.stringify(data);
    }

    const response = await fetch(
        `${API_BASE_URL}${endpoint}`,
        requestOptions
    );

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
