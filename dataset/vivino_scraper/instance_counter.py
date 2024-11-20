# Il codice apre un file con all'interno un JSON e scorre tutti gli elementi al suo interno contando quanti hanno un certo valore associato ad una certa chiave
# Per completezza, il codice conta anche quanti elementi ho in totale e calcola la percentuale di elementi con quel dato valore associato a quella data chiave.

import json

# Apro il file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\scraped_data_from_vivino\scraped_wines\scraped_wines.json', encoding='utf-8') as f:
    data = json.load(f)

# Chiudo il file
f.close()

# Inizializzo il contatore
counter = 0

# Itero su ogni elemento dell'array
array = []
for wine in data:
    if 'style' not in wine or wine['style'] is None:
        print("--- WRN: chiave 'style' assente.")
        continue
    if 'image' not in wine['style']:
        print("--- WRN: chiave 'image' assente.")
        continue

    if wine['style']['image'] is None:
        counter += 1
    else:
        array.append(wine['style']['image'])

# Calcolo la percentuale
percentage = counter / len(data) * 100

print(f"Numero di elementi totali: {len(data)}")
print(f"Numero di elementi con 'image' uguale a null: {counter}")
print(f"Percentuale: {percentage:.2f}%")
print(f"Array di elementi con 'image' diverso da null: {array}")
print(f"Numero di elementi con 'image' diverso da null: {len(array)}")
