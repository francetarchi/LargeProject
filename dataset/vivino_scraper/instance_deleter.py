# Il codice apre un file con all'interno un JSON e scorre tutti gli elementi al suo interno eliminando una chiave (e il valore ad essa associato).

import json

# Apro il file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\scraped_data_from_vivino\scraped_wines\scraped_wines.json', encoding='utf-8') as f:
    data = json.load(f)

# Chiudo il file
f.close()

# Inizializzo il contatore dei valori eliminati
counter = 0
style_mancanti = 0

# Itero su ogni elemento dell'array
for wine in data:
    if 'style' not in wine or wine['style'] is None:
        print("--- WRN: chiave 'style' assente.")
        style_mancanti += 1
        continue
    if 'background_image' not in wine['style']:
        print("--- WRN: chiave 'background_image' assente.")
        continue

    value = wine['style'].pop('background_image', None)
    counter += 1

# Calcolo la percentuale dei valori eliminati correttamente
percentage = counter / (len(data) - style_mancanti) * 100

print(f"Numero di elementi totali: {len(data)}")
print(f"Numero di chiavi 'style' mancanti: {style_mancanti}")
print(f"Numero di chiavi 'background_image' eliminate: {counter}")
print(f"Percentuale_bi: {percentage:.2f}%")

# Apro il file in scrittura
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\scraped_data_from_vivino\scraped_wines\scraped_wines.json', 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=4)

# Chiudo il file
f.close()
