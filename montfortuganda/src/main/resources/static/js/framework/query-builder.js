function buildQuery(endpoint, params = {}) {
    const query = [];
    for (let key in params) {
        if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
            query.push(`${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`);
        }
    }
    return query.length > 0 ? `${endpoint}?${query.join('&')}` : endpoint;
}