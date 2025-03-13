# Ordinare le recensioni per id del vino e data creazione (crescente)

import json

# leggo il file reviews.json
with open('json/reviews.json', 'r', encoding='utf-8') as file:
    reviews = json.load(file)

reviews_sorted = sorted(reviews, key=lambda x: (x['wine_id'] , x['created_at']))

# scrivo il file reviews_sorted.json
with open('json/reviews_sorted.json', 'w', encoding='utf-8') as file:
    json.dump(reviews_sorted, file, ensure_ascii=False, indent=4)

print('File reviews_sorted.json creato con successo')
