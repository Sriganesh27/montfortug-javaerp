import re

with open('src/main/resources/static/views/admin/add-employee.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Replace form-control with detail-input
html = html.replace('class="form-control w-100"', 'class="detail-input w-100"')

# Replace inline grid styles
html = html.replace('class="detail-grid mb-3" style="display: grid; grid-template-columns: 1fr 1fr 1fr 1fr; gap: 15px;"', 'class="emp-grid emp-grid-4 mb-3"')
html = html.replace('class="detail-grid mb-4" style="display: grid; grid-template-columns: 1fr 1fr 1fr 1fr; gap: 15px;"', 'class="emp-grid emp-grid-4 mb-4"')

html = html.replace('class="detail-grid mb-3" style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;"', 'class="emp-grid emp-grid-2 mb-3"')
html = html.replace('class="detail-grid mb-4" style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;"', 'class="emp-grid emp-grid-2 mb-4"')

html = html.replace('class="detail-grid mb-3" style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 15px;"', 'class="emp-grid emp-grid-3 mb-3"')
html = html.replace('class="detail-grid mb-4" style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 15px;"', 'class="emp-grid emp-grid-3 mb-4"')

# Replace inline flex style
html = html.replace('id="add-empNameContainer" style="display:flex; gap:10px;"', 'id="add-empNameContainer" class="emp-name-container"')

with open('src/main/resources/static/views/admin/add-employee.html', 'w', encoding='utf-8') as f:
    f.write(html)
