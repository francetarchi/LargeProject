# Ordinare le recensioni per id del vino e data creazione (crescente)

import json

# Apro i file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\reviews.json', encoding='utf-8') as f:
    reviews = json.load(f)
f.close()

with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\users.json', encoding='utf-8') as f:
    users = json.load(f)
f.close()

reviews_sorted = sorted(reviews, key=lambda x: (x['user_id']['username'], x['created_at']))

# scrivo il file reviews_sorted.json
with open('json/reviews_sorted.json', 'w', encoding='utf-8') as file:
    json.dump(reviews_sorted, file, ensure_ascii=False, indent=4)

print('File reviews_sorted.json creato con successo')
