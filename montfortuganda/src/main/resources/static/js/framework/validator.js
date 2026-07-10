const Validator = {
    validate: (payload, rules) => {
        for (let field in rules) {
            const rule = rules[field];
            const value = payload[field];

            if (rule.required && (value === undefined || value === null || value === '')) {
                return rule.message || `${field} is required`;
            }
        }
        return null; // Null means valid
    }
};