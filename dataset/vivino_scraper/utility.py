# File di utilità da usare come appoggio per scrivere ed eseguire codice ce serve per fare operazioni a caso una volta e basta.

import json
import random

# Apro i file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wines.json', encoding='utf-8') as f:
    wines = json.load(f)
f.close()

with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\users.json', encoding='utf-8') as f:
    users = json.load(f)
f.close()

with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\reviews.json', encoding='utf-8') as f:
    reviews = json.load(f)
f.close()

for review in reviews:
    ##### UTENTI #####
    if not review.get("user_id"):
        continue
    user_index = review["user_id"] - 1

    # Creo un nuovo documento
    user_info = {
        "username": users[user_index]["login"]["username"],
        "thumbnail": users[user_index]["picture"]["thumbnail"]
    }

    # Lo inserisco nella review corrente al campo user_id
    review["user_id"] = user_info


    ##### VINI #####
    if not review.get("wine_id"):
        continue
    wine_index = review["wine_id"] - 1

    # Scelgo randomicamente un'annata fra quelle disponibili per quel vino
    print(f"wine index: {wine_index}")
    if not wines[wine_index].get("vintages") or len(wines[wine_index]["vintages"]) == 0:
        wine_year = None
    else:
        w = random.choice(wines[wine_index]["vintages"])
        wine_year = w["year"]
        wine_image = w["image"]

    # Creo un nuovo documento
    wine_info = {
        "id": wines[wine_index]["id"],
        "name": wines[wine_index]["name"],
        "year": wine_year,
        "image": wine_image
    }

    # Lo inserisco nella review corrente al campo wine_id
    review["wine_id"] = wine_info

# Salvo le modifiche in un nuovo file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\reviews_new.json', 'w', encoding='utf-8') as f:
    json.dump(reviews, f, ensure_ascii=False, indent=4)
f.close()
