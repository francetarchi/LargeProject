import json
from collections import OrderedDict

# Funzione per aggiungere un nuovo campo in una posizione specifica
def add_field(data, new_field_name, new_field_value, position):
    new_data = []
    for item in data:
        new_item = OrderedDict()
        for idx, (key, value) in enumerate(item.items()):
            if idx == position:
                new_item[new_field_name] = new_field_value
            new_item[key] = value
        if position >= len(item):
            new_item[new_field_name] = new_field_value
        new_data.append(new_item)
    return new_data

# Leggi il file JSON
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\scraped_data_from_vivino\scraped_wines\scraped_wines.json', encoding='utf-8') as f:
    data = json.load(f)

# Aggiungi il nuovo campo 'new_field' con valore 'new_value' alla posizione desiderata (ad esempio, posizione 2)
new_data = add_field(data, 'description', None, 2) # La posizione è 0-based

# Salva il risultato nel file JSON
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\scraped_data_from_vivino\scraped_wines\scraped_wines.json', 'w', encoding='utf-8') as f:
    json.dump(new_data, f, ensure_ascii=False, indent=4)

print("Il file è stato modificato e salvato come 'scraped_wines.json'.")
