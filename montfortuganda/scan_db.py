import os
import re

java_dir = 'src/main/java/com/erp/montfortuganda'
sql_dir = 'databse'

# 1. Parse SQL schemas to find tables and columns
tables = {}
for root, _, files in os.walk(sql_dir):
    for f in files:
        if f.endswith('.sql'):
            with open(os.path.join(root, f), 'r', encoding='utf-8', errors='ignore') as file:
                content = file.read()
                # find CREATE TABLE
                create_tables = re.findall(r'CREATE TABLE\s+`?(\w+)`?\s*\((.*?)\)\s*(?:ENGINE|;)', content, re.DOTALL | re.IGNORECASE)
                for table_name, columns_str in create_tables:
                    columns = re.findall(r'`(\w+)`\s+\w+', columns_str)
                    tables[table_name.lower()] = [c.lower() for c in columns]

# 2. Parse Java Entities to find @Table and @Column mappings
mismatches = []
for root, _, files in os.walk(java_dir):
    for f in files:
        if f.endswith('.java'):
            with open(os.path.join(root, f), 'r', encoding='utf-8', errors='ignore') as file:
                content = file.read()
                if '@Entity' in content:
                    table_match = re.search(r'@Table\s*\(\s*name\s*=\s*"([^"]+)"', content)
                    if table_match:
                        table_name = table_match.group(1).lower()
                        if table_name not in tables:
                            mismatches.append(f"Table '{table_name}' mapped in {f} DOES NOT EXIST in SQL schema.")
                            continue
                        
                        # Find all @Column
                        columns = re.findall(r'@Column\s*\([^)]*name\s*=\s*"([^"]+)"', content)
                        # Find all @JoinColumn
                        join_columns = re.findall(r'@JoinColumn\s*\([^)]*name\s*=\s*"([^"]+)"', content)
                        all_columns = columns + join_columns
                        
                        for col in all_columns:
                            col_lower = col.lower()
                            if col_lower not in tables[table_name]:
                                mismatches.append(f"Entity '{f}': Column '{col}' mapped but DOES NOT EXIST in table '{table_name}'.")

print("--- SCHEMA VERIFICATION RESULTS ---")
for m in sorted(set(mismatches)):
    print(m)
if not mismatches:
    print("All entity mappings correspond to actual DB columns.")
