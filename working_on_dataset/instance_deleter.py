# Il codice apre un file con all'interno un JSON e scorre tutti gli elementi al suo interno eliminando una chiave (e il valore ad essa associato).

import json

# Apro il file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wines.json', encoding='utf-8') as f:
    data = json.load(f)

# Chiudo il file
f.close()

# Inizializzo il contatore dei valori eliminati
counter = 0
# counter_1 = 0
elementi_mancanti = 0
# elementi_mancanti_1 = 0

# Itero su ogni elemento dell'array
for wine in data:
    # if 'wines' not in wine or wine['wines'] is None or wine['wines'] == []:
    #     print("--- WRN: chiave 'wines' assente.")
    #     elementi_mancanti += 1
    #     continue

    # se la chiave esiste e il valore è diverso da null
    if not wine.get("reviews"):
        elementi_mancanti += 1
        continue

    wine.pop("reviews", None)
    counter += 1
    
    # if 'taster_twitter_handle' not in wine:
    #     print("--- WRN: chiave 'taster_twitter_handle' assente.")
    #     elementi_mancanti_1 += 1
    #     continue
    # wine.pop('taster_twitter_handle', None)
    # counter_1 += 1

# Calcolo la percentuale dei valori eliminati correttamente
percentage = counter / (len(data) - elementi_mancanti) * 100
# percentage_1 = counter / (len(data) - elementi_mancanti_1) * 100

print(f"Numero di elementi totali: {len(data)}")
print(f"Numero di chiavi 'reviews' mancanti: {elementi_mancanti}")
# print(f"Numero di chiavi 'taster_twitter_handle' mancanti: {elementi_mancanti_1}")
print(f"Numero di chiavi 'reviews' eliminate: {counter}")
# print(f"Numero di chiavi 'taster_twitter_handle' eliminate: {counter_1}")
print(f"Percentuale: {percentage:.2f}%")
# print(f"Percentuale_1: {percentage_1:.2f}%")

# Apro il file in scrittura
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wines_new.json', 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=4)

# Chiudo il file
f.close()
