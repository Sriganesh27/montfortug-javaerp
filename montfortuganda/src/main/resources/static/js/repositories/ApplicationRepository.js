const ApplicationRepository = {
    findAll: (params) => {
        return Api.get(buildQuery('/admission/branch/applications', params));
    },
    save: (id, payload) => id ? Api.put('/admission/branch/applications/' + id, payload) : Api.post('/admission/branch/applications', payload),
    delete: (id) => Api.delete('/admission/branch/applications/' + id)
};