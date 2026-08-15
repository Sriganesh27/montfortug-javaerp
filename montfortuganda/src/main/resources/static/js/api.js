// ==========================================
// api.js - Core API Communication Layer
// ==========================================

const API_BASE_URL = '/api';
const inFlightGetRequests = new Map();

const ERP_MUTATION_EVENT = 'erp:data-mutated';


/*
 * These search endpoints use POST only because their filters are JSON.
 * They are reads, not mutations, so they must never trigger ERP auto-sync.
 */
function isReadOnlyPostEndpoint(endpoint) {
    const normalized = String(endpoint || '')
        .trim()
        .split('?')[0]
        .replace(/\/+$/, '');

    return normalized === '/students/search'
        || normalized === '/branchadmin/employees/search';
}

function getApiLoadingMessage(
        method,
        endpoint
) {
    const normalizedMethod =
        String(method || '').toUpperCase();

    switch (normalizedMethod) {
        case 'POST':
            return 'Saving...';
        case 'PUT':
            return 'Updating...';
        case 'PATCH':
            return 'Updating...';
        case 'DELETE':
            return 'Deleting...';
        default:
            return 'Processing...';
    }
}

async function runApiForeground(
        method,
        endpoint,
        operation,
        options = {}
) {
    const {
        silent = false,
        loadingMessage = null
    } = options || {};

    if (
        silent
        || typeof window.erpRunForegroundOperation
            !== 'function'
    ) {
        return operation();
    }

    return window.erpRunForegroundOperation(
        loadingMessage
        || getApiLoadingMessage(
            method,
            endpoint
        ),
        operation
    );
}

function notifyErpMutation(method, endpoint, responseData) {
    document.dispatchEvent(
        new CustomEvent(ERP_MUTATION_EVENT, {
            detail: {
                method: String(method || '').toUpperCase(),
                endpoint: String(endpoint || ''),
                responseData,
                occurredAt: Date.now()
            }
        })
    );
}

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
async function apiGet(
        endpoint,
        options = {}
) {
    const normalizedEndpoint = String(endpoint || '').trim();

    if (inFlightGetRequests.has(normalizedEndpoint)) {
        return inFlightGetRequests.get(normalizedEndpoint);
    }

    const request = runApiForeground(
        'GET',
        normalizedEndpoint,
        async () => {
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
        },
        {
            ...options,
            /*
             * GET is silent unless a caller explicitly requests foreground
             * loading. This keeps polling/search/live-sync invisible.
             */
            silent:
                options?.foreground !== true
        }
    );

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
async function apiPost(
        endpoint,
        data,
        options = {}
) {
    const readOnlyPost =
        isReadOnlyPostEndpoint(endpoint);

    return runApiForeground(
        'POST',
        endpoint,
        async () => {
            const response = await fetch(
                `${API_BASE_URL}${endpoint}`,
                {
                    method: 'POST',
                    headers: getAuthHeaders(),
                    body: JSON.stringify(data),
                    credentials: 'include'
                }
            );

            const result =
                await handleResponse(response);

            if (!readOnlyPost) {
                notifyErpMutation(
                    'POST',
                    endpoint,
                    result
                );
            }

            return result;
        },
        {
            ...options,
            silent:
                options?.silent === true
                || readOnlyPost
        }
    );
}

/**
 * Standard PUT Request (JSON)
 */
async function apiPut(
        endpoint,
        data,
        options = {}
) {
    return runApiForeground(
        'PUT',
        endpoint,
        async () => {
            const response = await fetch(
                `${API_BASE_URL}${endpoint}`,
                {
                    method: 'PUT',
                    headers: getAuthHeaders(),
                    body: JSON.stringify(data),
                    credentials: 'include'
                }
            );

            const result =
                await handleResponse(response);

            notifyErpMutation(
                'PUT',
                endpoint,
                result
            );

            return result;
        },
        options
    );
}

/**
 * Standard DELETE Request
 *
 * The optional data argument supports endpoints that accept a JSON body.
 * Employee deactivation calls this method without a body.
 */
async function apiDelete(
        endpoint,
        data = undefined,
        options = {}
) {
    return runApiForeground(
        'DELETE',
        endpoint,
        async () => {
            const requestOptions = {
                method: 'DELETE',
                headers: getAuthHeaders(),
                credentials: 'include'
            };

            if (data !== undefined) {
                requestOptions.body =
                    JSON.stringify(data);
            }

            const response = await fetch(
                `${API_BASE_URL}${endpoint}`,
                requestOptions
            );

            const result =
                await handleResponse(response);

            notifyErpMutation(
                'DELETE',
                endpoint,
                result
            );

            return result;
        },
        options
    );
}


/**
 * MULTIPART Request (For File Uploads like Photos/Documents)
 */
async function apiMultipart(
        endpoint,
        method,
        formData,
        options = {}
) {
    return runApiForeground(
        method,
        endpoint,
        async () => {
            const response = await fetch(
                `${API_BASE_URL}${endpoint}`,
                {
                    method,
                    headers:
                        getAuthHeaders(true),
                    body: formData,
                    credentials: 'include'
                }
            );

            const result =
                await handleResponse(response);

            notifyErpMutation(
                method,
                endpoint,
                result
            );

            return result;
        },
        {
            loadingMessage:
                'Uploading...',
            ...options
        }
    );
}


/**
 * Standard PATCH Request (JSON)
 */
async function apiPatch(
        endpoint,
        data,
        options = {}
) {
    return runApiForeground(
        'PATCH',
        endpoint,
        async () => {
            const response = await fetch(
                `${API_BASE_URL}${endpoint}`,
                {
                    method: 'PATCH',
                    headers: getAuthHeaders(),
                    body: JSON.stringify(data),
                    credentials: 'include'
                }
            );

            const result =
                await handleResponse(response);

            notifyErpMutation(
                'PATCH',
                endpoint,
                result
            );

            return result;
        },
        options
    );
}
