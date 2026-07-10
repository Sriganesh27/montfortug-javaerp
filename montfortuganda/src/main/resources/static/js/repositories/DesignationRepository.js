const DesignationRepository = {
    findAll: (params) => Api.get(buildQuery(API_ENDPOINTS.DESIGNATIONS, params)),
    save: (id, payload) => id ? Api.put(`${API_ENDPOINTS.DESIGNATIONS}/${id}`, payload) : Api.post(API_ENDPOINTS.DESIGNATIONS, payload),
    delete: (id) => Api.delete(`${API_ENDPOINTS.DESIGNATIONS}/${id}`)
};