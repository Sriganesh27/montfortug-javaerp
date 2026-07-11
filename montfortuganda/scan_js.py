import os
import re

js_dir = 'src/main/resources/static/js'
html_dir = 'src/main/resources/templates'
java_dir = 'src/main/java/com/erp/montfortuganda'

# Read all HTML content
html_content = ""
for root, _, files in os.walk(html_dir):
    for f in files:
        if f.endswith('.html'):
            with open(os.path.join(root, f), 'r', encoding='utf-8', errors='ignore') as file:
                html_content += file.read() + "\n"

# Scan JS for getElementById
missing_ids = []
for root, _, files in os.walk(js_dir):
    for f in files:
        if f.endswith('.js'):
            with open(os.path.join(root, f), 'r', encoding='utf-8', errors='ignore') as file:
                js_content = file.read()
                # Find all getElementById calls
                matches = re.findall(r"getElementById\(['\"]([^'\"]+)['\"]\)", js_content)
                for dom_id in matches:
                    # Check if ID exists in HTML
                    if f'id="{dom_id}"' not in html_content and f"id='{dom_id}'" not in html_content:
                        missing_ids.append(f"Missing DOM Element: '{dom_id}' requested in {f}")

for m in set(missing_ids):
    print(m)
