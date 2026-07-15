# JS GUIDELINES

*Status: Active*

This document serves as the enterprise reference for JS GUIDELINES.

## 1. Modularity
*   Use Immediately Invoked Function Expressions (IIFE) for modularity to avoid polluting the global namespace.
    ```javascript
    const UserModule = (function() {
        // Private variables
        let table;
        return {
            init: function() { ... }
        };
    })();
    ```

## 2. DOM Loading & Events
*   **DO NOT** use inline HTML event handlers (e.g., `onclick="doSomething()"`).
*   Always attach listeners via JavaScript.
*   Always wait for the DOM to load before initializing scripts:
    ```javascript
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', UserModule.init);
    } else {
        UserModule.init();
    }
    ```
*   Use Event Delegation for dynamically generated elements:
    ```javascript
    tableContainer.addEventListener('click', (e) => {
        if (e.target.closest('.delete-btn')) { ... }
    });
    ```

## 3. Asynchronous Operations
*   Use `async/await` syntax exclusively for promises.
*   Always wrap API calls in `try...catch` blocks to handle network-level failures gracefully.