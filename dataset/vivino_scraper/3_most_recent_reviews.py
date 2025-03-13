# Funzione: Aggiunge le recensioni più recenti a ciascun vino nel file wines.json
import json

# Apro il file delle recensioni ordinate per data di creazione e id del vino crescenti
with open('json/reviews_sorted.json', 'r', encoding='utf-8') as f:
    reviews = json.load(f)

# Mi salvo le recensioni più recenti di tutti i vini in un dizionario
most_recent_reviews = {}

for review in reviews:
    wine_id = review['wine_id']
    if wine_id not in most_recent_reviews:
        most_recent_reviews[wine_id] = []
    # Aggiungo le recensioni ordinate, massimo 3 per vino
    most_recent_reviews[wine_id].append(review)
    most_recent_reviews[wine_id] = sorted(most_recent_reviews[wine_id], key=lambda x: x['created_at'], reverse=True)[:3]

# Apro il file wines.json e lo salvo in una variabile
with open('json/wines.json', 'r', encoding='utf-8') as f:
    wines = json.load(f)

# Aggiungo le recensioni più recenti a ciascun vino
for wine in wines:
    wine_id = wine['id']
    wine['reviews'] = most_recent_reviews.get(wine_id, [])  # Aggiungi solo se ci sono recensioni

# Scrivo il file aggiornato
with open('json/wines_with_reviews.json', 'w', encoding='utf-8') as f:
    json.dump(wines, f, ensure_ascii=False, indent=4)

print("Il file è stato aggiornato con le recensioni più recenti.")
