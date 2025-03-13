# Funzione: Aggiunge le recensioni più recenti a ciascun vino nel file wines.json
import json

# Apro i file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\reviews.json', encoding='utf-8') as f:
    reviews = json.load(f)
f.close()

with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wines.json', encoding='utf-8') as f:
    wines = json.load(f)
f.close()

reviews_sorted = sorted(reviews, key=lambda x: (x['wine_id']['id'], x['created_at']))

# Mi salvo le recensioni più recenti PER OGNI ANNATA in un dizionario
last_reviews_per_vintage = {}

for review in reviews_sorted:
    vintage_id = str(review['wine_id']['id']) + '-' + str(review['wine_id']['year'])

    # Controllo se l'annata è già presente nel dizionario
    if vintage_id not in last_reviews_per_vintage:
        # Se non è presente, la aggiungo (con una lista vuota)
        last_reviews_per_vintage[vintage_id] = []
    
    # Aggiungo le recensioni ordinate, massimo 3 per utente
    last_reviews_per_vintage[vintage_id].append(review)
    last_reviews_per_vintage[vintage_id] = sorted(last_reviews_per_vintage[vintage_id], key=lambda x: x['created_at'], reverse=True)[:3]

# Aggiungo le recensioni più recenti a ciascuna annata di ciascun vino
for wine in wines:
    wine_id = wine["id"]
    for vintage in wine["vintages"]:
        vintage_id = str(wine_id) + '-' + str(vintage["year"])
        vintage["reviews"] = last_reviews_per_vintage.get(vintage_id, [])  # Aggiungi solo se ci sono recensioni

# Scrivo il file aggiornato
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wines_new.json', 'w', encoding='utf-8') as f:
    json.dump(wines, f, ensure_ascii=False, indent=4)
f.close()

print("Il file è stato aggiornato con le recensioni più recenti.")
