# Il codice apre un file con all'interno un JSON e scorre tutti gli elementi al suo interno: per ogni elemento, se trova la chiave specificata, ne salva il contenuto in un array.
# Memorizza l'array in un file JSON omonimo alla chiave estratta.

import json

# Apro il file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wines.json', encoding='utf-8') as f:
    data = json.load(f)
f.close()

# Inizializzo il contatore dei valori estratti
counter = 0
elementi_mancanti = 0

# Inizializzo l'array per memorizzare i valori estratti
wineries = []
for wine in data:
    # Controllo se la chiave è presente
    if 'winery' not in wine:
        elementi_mancanti += 1
        continue

    # Estraggo il valore
    current_winery = wine['winery']

    # Memorizzo il valore
    wineries.append(current_winery)
    counter += 1

# Salvo l'array in un file JSON
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wineries.json', 'w', encoding='utf-8') as f:
    json.dump(wineries, f, ensure_ascii=False, indent=4)
f.close()

print("------------------ Estrazione completata. ------------------")
print(f"Elementi totali: {len(data)}")
print(f"Elementi con chiave mancante: {elementi_mancanti}")
print(f"Elementi con chiave presente: {len(data) - elementi_mancanti}")
print(f"Estrazioni completate (sul totale degli elementi con chiave presente): {counter / (len(data) - elementi_mancanti) * 100}%")
