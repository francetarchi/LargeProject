import json
from collections import OrderedDict

# Modifica il nome del campo 'region_1' in 'style' mantenendo l'ordine dei campi
def change_field_name(review, old_field_name, new_field_name):
    new_review = OrderedDict()
    for key, value in review.items():
        if key == old_field_name:
            new_review[new_field_name] = value
        else:
            new_review[key] = value
    return new_review

# Leggi il file JSON
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wines.json', encoding='utf-8') as f:
    data = json.load(f)

new_data = []
for wine in data:
    new_reviews = []
    for review in wine['reviews']:
        new_reviews.append(change_field_name(review, 'note', 'review_text'))
    wine['reviews'] = new_reviews
    new_data.append(wine)

# Salva il risultato nel file JSON
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wines.json', 'w', encoding='utf-8') as f:
    json.dump(new_data, f, ensure_ascii=False, indent=4)

print("Il file è stato modificato e salvato come 'wines.json'.")
