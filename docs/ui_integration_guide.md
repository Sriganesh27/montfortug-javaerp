# UI Integration Guide: Module-Based Sidebar

I have reviewed your `layout.js`, `dashboard.html`, and `sidebar.html`. 

You have a very smart routing system! However, because `layout.js` uses `.innerHTML` to inject views, **it will not execute `<script>` tags that are inside the HTML views**. 

Here is exactly how to integrate the new modules securely based on your existing frontend architecture:

### 1. Add to Module Sidebar (`sidebar.html`)
Open `src/main/resources/static/components/sidebar.html`. Under the Branch Admin section (right below the Admissions Module), add this new "Master Data" module:

```html
        <!-- MASTER DATA MODULE -->
        <li data-role="BRANCH_ADMIN" class="has-dropdown">
            <a href="#" class="dropdown-toggle" data-tooltip="Master Data">
                <div class="dropdown-label">
                    <i class="bi bi-diagram-3-fill"></i> <span>Master Data</span>
                </div>
            </a>
            <ul class="sidebar-dropdown">
                <li data-role="BRANCH_ADMIN"><a href="/departments.html">Departments</a></li>
                <li data-role="BRANCH_ADMIN"><a href="/designations.html">Designations</a></li>
            </ul>
        </li>
```
*(Notice we use `data-role="BRANCH_ADMIN"` so your `layout.js` will automatically enforce security and hide it from unauthorized roles!)*

### 2. Load the Javascript (`dashboard.html`)
Because the router won't execute scripts inside the view HTML, you must load the Javascript files globally in your `dashboard.html`.

Open `src/main/resources/static/dashboard.html` and add these two lines under `<!-- 4. Load Business Logic Modules -->`:

```html
<!-- 4. Load Business Logic Modules -->
<script src="/js/modules/applications.js"></script>
<script src="/js/departments.js"></script>
<script src="/js/designations.js"></script>
```

> [!TIP]
> If you copied the previous `departments.html` and `designations.html` files from my last message, make sure you **delete** the `<script src="...">` tags at the very bottom of those files, as they are no longer needed and won't work in the router anyway!
