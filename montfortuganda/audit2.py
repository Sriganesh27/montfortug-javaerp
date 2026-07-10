import os
import re
import glob

# 1. Parse SQL files to get DB schema
db_schema = {}
sql_dir = r'D:\Java\montforterp\ERP-Java\databse'

for filename in os.listdir(sql_dir):
    if not filename.endswith('.sql'): continue
    
    with open(os.path.join(sql_dir, filename), 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()
    
    matches = re.finditer(r'CREATE TABLE ([^]+) \((.*?)\) ENGINE=', content, re.DOTALL)
    for match in matches:
        table_name = match.group(1).lower()
        columns_text = match.group(2)
        
        columns = set()
        for line in columns_text.split('\n'):
            line = line.strip()
            if line.startswith(''):
                col_name = line.split('')[1].lower()
                columns.add(col_name)
        
        db_schema[table_name] = columns

# 2. Parse Java Entity files
entity_schema = {}
java_dir = r'D:\Java\montforterp\ERP-Java\montfortuganda\src\main\java\com\erp\montfortuganda'

def camel_to_snake(name):
    s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
    return re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).lower()

# Add global auditable fields
base_fields = {'created_at', 'updated_at', 'created_by', 'updated_by', 'version'}

for filepath in glob.glob(os.path.join(java_dir, '**', '*.java'), recursive=True):
    with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()
        
    if '@Entity' not in content: continue
    
    # Extract table name
    table_match = re.search(r'@Table\([^)]*name\s*=\s*"([^"]+)"', content)
    if not table_match: 
        class_match = re.search(r'public class (\w+)', content)
        if class_match:
            table_name = camel_to_snake(class_match.group(1))
        else:
            continue
    else:
        table_name = table_match.group(1).lower()
        
    columns = set(base_fields)
    
    # Proper regex for @Column and @JoinColumn that ignores whitespace and matches name="value"
    # Find all annotations
    annotations = re.finditer(r'@(?:Column|JoinColumn)\s*\(([^)]+)\)', content)
    for ann in annotations:
        attrs = ann.group(1)
        # Look for name="something" or name = "something"
        name_match = re.search(r'name\s*=\s*"([^"]+)"', attrs)
        if name_match:
            columns.add(name_match.group(1).lower())
            
    # Also grab basic fields without annotations (where JPA just uses the snake case of the field)
    # We do a line-by-line parsing looking for fields
    lines = content.split('\n')
    for line in lines:
        line = line.strip()
        if line.startswith('private ') or line.startswith('protected '):
            if '@Transient' in content: # Too hard to map exact lines, skip
                pass 
            # Very basic extraction: private Type fieldName;
            field_match = re.search(r'(?:private|protected)\s+(?!List|Set|Collection)([\w<>\[\]]+)\s+(\w+)\s*[;=]', line)
            if field_match:
                type_name = field_match.group(1)
                field_name = field_match.group(2)
                if type_name in ['String', 'Integer', 'Long', 'Boolean', 'LocalDateTime', 'LocalDate', 'Double', 'Float', 'BigDecimal', 'Status', 'Gender', 'Role', 'byte[]', 'LocalDate', 'LocalTime', 'UUID']:
                    columns.add(camel_to_snake(field_name))
                
    entity_schema[table_name] = {
        'filepath': filepath,
        'columns': columns
    }

# 3. Compare and Report
print("=================== REFINED DB vs ENTITY AUDIT REPORT ===================")
for table_name, db_cols in db_schema.items():
    if table_name not in entity_schema:
        print(f"\n[WARNING] Table '{table_name}' exists in DB but no corresponding @Entity found!")
        continue
        
    entity = entity_schema[table_name]
    entity_cols = entity['columns']
    
    # We don't care about foreign keys missing in java since our regex might have missed them
    # But we DO care about basic fields.
    # To reduce noise, we only report missing DB columns if they don't end in _id
    missing_in_entity = {col for col in db_cols - entity_cols if not col.endswith('_id')}
    
    # To reduce noise in "Extra in Java", we filter out fk_ names and _id (which are usually auto-resolved foreign keys)
    missing_in_db = {col for col in entity_cols - db_cols if not col.startswith('fk_') and not col.endswith('_id')}
    
    if missing_in_entity or missing_in_db:
        print(f"\n--- TABLE: {table_name} ---")
        if missing_in_entity:
            print(f"  In DB, Missing in Java: {', '.join(missing_in_entity)}")
        if missing_in_db:
            print(f"  In Java, Missing in DB: {', '.join(missing_in_db)}")
            
print("\n=========================================================================")
