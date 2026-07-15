# Fix Blank Add Employee Page

The view is loading perfectly without errors, but it is invisible because there is a `hidden` CSS class on the main wrapper inside `add-employee.html`!

### Fix:
Open your `src/main/resources/static/views/admin/add-employee.html` file.

Look at the very first line. It currently says:
```html
<div id="ba-add-employee-view" class="fade-in hidden">
```

Change it to remove the `hidden` class, so it looks exactly like this:
```html
<div id="ba-add-employee-view" class="fade-in">
```

Save the file and refresh your browser. The "Add Employee" screen will appear immediately!
