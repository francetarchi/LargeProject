# Il codice apre un file con all'interno un JSON e scorre tutti gli elementi al suo interno eliminando una chiave (e il valore ad essa associato).

import json

# Apro il file
with open('scraped_data/scraped_vintages.json', encoding='utf-8') as f:
    data = json.load(f)

# Chiudo il file
f.close()

# Inizializzo il contatore dei valori eliminati
counter = 0

# Itero su ogni elemento dell'array
for vintage in data['vintages']:
    if 'grapes' not in vintage:
        print("--- WRN: chiave 'grapes' assente.")
        exit()
    vintage.pop('grapes', None)
    counter += 1

# Calcolo la percentuale dei valori eliminati correttamente
percentage = counter / len(data['vintages']) * 100

print(f"Numero di elementi totali: {len(data['vintages'])}")
print(f"Numero di chiavi 'grapes' eliminate: {counter}")
print(f"Percentuale: {percentage:.2f}%")

# Apro il file in scrittura
with open('scraped_data/scraped_vintages.json', 'w') as f:
    # Scrivo i dati modificati
    json.dump(data, f)

# Chiudo il file
f.close()
