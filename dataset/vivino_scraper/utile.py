import json

# Apro i file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\scraped_data_from_vivino\scraped_wines\scraped_wines.json', encoding='utf-8') as f_wines:
    wines = json.load(f_wines)
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\scraped_data_from_vivino\scraped_vintages\scraped_vintages.json', encoding='utf-8') as f_vintages:
    vintages = json.load(f_vintages)

f_wines.close()
f_vintages.close()

for wine in wines:
    this_wine_vintages = []
    for vintage in vintages:
        if wine['id'] == vintage['wine']:
            vintage.pop('id', None)
            vintage.pop('name', None)
            vintage.pop('wine', None)
            this_wine_vintages.append(vintage)

            # Elimino dall'array vintages quella vintage
            vintages.remove(vintage)

    wine['vintages'] = this_wine_vintages

    # Riordino
    this_wine_reviews = wine.pop('reviews', None)
    wine['reviews'] = this_wine_reviews

    # Percentuale di avanzamento
    print(f"Avanzamento: {wines.index(wine) / len(wines) * 100:.2f}%")

# Salvo il risultato
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\scraped_data_from_vivino\scraped_wines\scraped_wines.json', 'w', encoding='utf-8') as f:
    json.dump(wines, f, ensure_ascii=False, indent=4)

print("Il file è stato modificato e salvato come 'scraped_wines.json'.")
