
import json

# Apro il file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wines.json', encoding='utf-8') as f:
    wines = json.load(f)
f.close()

styles = []
for wine in wines:
    if 'style' not in wine or wine['style'] is None or wine['style'] == []:
        continue

    this_style = dict()
    this_style['id'] = wine['style']['id']
    this_style['count'] = 1

    if len(styles) == 0:
        styles.append(this_style)
        continue

    yet_present = False
    for style in styles:
        if style['id'] == this_style['id']:
            yet_present = True
            style['count'] += 1
            break
    
    if not yet_present:
        styles.append(this_style)

print(f"styles: {len(styles)}")
print(f"styles: {styles}")

for wine in wines:
    if 'style' not in wine or wine['style'] is None or wine['style'] == []:
        continue

    for style in styles:
        if style['id'] == wine['style']['id']:
            wine['style']['statistics'] = style['count']
            break

# Salvo il file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wines_1.json', 'w', encoding='utf-8') as f:
    json.dump(wines, f, ensure_ascii=False, indent=4)
f.close()
