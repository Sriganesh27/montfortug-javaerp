const Events = (function() {
    const topics = {};
    return {
        on: (topic, listener) => {
            if (!topics[topic]) topics[topic] = [];
            topics[topic].push(listener);
        },
        off: (topic, listener) => {
            if (!topics[topic]) return;
            // If a specific listener is passed, remove it
            if (listener) {
                topics[topic] = topics[topic].filter(l => l !== listener);
            } else {
                // Otherwise clear all listeners for the topic
                topics[topic] = [];
            }
        },
        emit: (topic, data) => {
            if (!topics[topic]) return;
            topics[topic].forEach(listener => listener(data));
        }
    };
})();