const DepartmentRepository = {
    findAll: (params) => Api.get(buildQuery(API_ENDPOINTS.DEPARTMENTS, params)),
    save: (id, payload) => id ? Api.put(`${API_ENDPOINTS.DEPARTMENTS}/${id}`, payload) : Api.post(API_ENDPOINTS.DEPARTMENTS, payload),
    delete: (id) => Api.delete(`${API_ENDPOINTS.DEPARTMENTS}/${id}`),
    getActive: () => Api.get(buildQuery(API_ENDPOINTS.DEPARTMENTS, { active: true, size: 500, sortBy: 'NAME', direction: 'ASC' }))
};
