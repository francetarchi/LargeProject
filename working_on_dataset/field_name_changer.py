# Il codice apre un file con all'interno un JSON e scorre tutti gli elementi al suo interno: per ogni elemento modifica la chiave di un campo, sostituendola con un'altra.
# Mantiene l'ordine dei campi.

import json
from collections import OrderedDict

# Modifica il nome del campo 'region_1' in 'style' mantenendo l'ordine dei campi
def change_field_name(element, old_field_name, new_field_name):
    new_element = OrderedDict()
    for key, value in element.items():
        if key == old_field_name:
            new_element[new_field_name] = value
        else:
            new_element[key] = value
    return new_element

# Leggi il file JSON
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wines.json', encoding='utf-8') as f:
    data = json.load(f)
f.close()

new_data = []
for wine in data:
    if 'style' not in wine or wine['style'] is None or wine['style'] == []:
        new_data.append(wine)
        continue

    new_wine = wine
    new_style = change_field_name(wine['style'], 'statistics', 'wines_count')
    new_wine['style'] = new_style
    new_data.append(new_wine)

# Salva il risultato nel file JSON
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wines_1.json', 'w', encoding='utf-8') as f:
    json.dump(new_data, f, ensure_ascii=False, indent=4)
f.close()

print("Il file è stato modificato e salvato come 'wines_1.json'.")
