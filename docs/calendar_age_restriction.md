# Restrict Calendar Dates (Age Limit)

We can make the calendar even smarter! By passing a custom configuration to our `createErpCalendar` function, we can physically disable all dates in the calendar that would make the employee younger than 18.

### Update `employees.js`

Open your `src/main/resources/static/js/modules/employees.js`.

#### 1. In the `initAddEmployeeView()` function:

Find where we initialize the calendars (the part you just updated a moment ago):
```javascript
        // IMPORTANT FIX: Initialize calendars AFTER cloning the form
        if (typeof createErpCalendar === 'function') {
            createErpCalendar('#add-empDob');
            createErpCalendar('#add-empJoiningDate');
        }
```

Change it to:
```javascript
        // IMPORTANT FIX: Initialize calendars AFTER cloning the form
        if (typeof createErpCalendar === 'function') {
            const today = new Date();
            // Calculate exactly 18 years ago (Change 18 to 20 if needed!)
            const maxDobDate = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());

            createErpCalendar('#add-empDob', {
                maxDate: maxDobDate,
                defaultDate: maxDobDate // Opens the calendar starting 18 years ago
            });
            
            createErpCalendar('#add-empJoiningDate');
        }
```

#### 2. In the `initEmployeesView()` function (for the Edit Screen):

Find the calendar initialization at the very top of `initEmployeesView()`:
```javascript
    // Initialize calendar if available
    if (typeof createErpCalendar === 'function') {
        createErpCalendar('#edit-empDob');
    }
```

Change it to:
```javascript
    // Initialize calendar if available
    if (typeof createErpCalendar === 'function') {
        const today = new Date();
        const maxDobDate = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());
        
        createErpCalendar('#edit-empDob', {
            maxDate: maxDobDate
        });
    }
```

Save the file and do a **Hard Refresh**. Now, when you click the Date of Birth field, the calendar will automatically open at the year 2008 (or whatever is 18 years ago), and everything after that date will be grayed out and unclickable!
