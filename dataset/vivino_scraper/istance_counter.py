# Il codice apre un file con all'interno un JSON e scorre tutti gli elementi al suo interno contando quanti hanno un certo valore associato ad una certa chiave
# Per completezza, il codice conta anche quanti elementi ho in totale e calcola la percentuale di elementi con quel dato valore associato a quella data chiave.

import json

# Apro il file
with open('scraped_data/scraped_wines.json', encoding='utf-8') as f:
    data = json.load(f)

# Chiudo il file
f.close()

# Inizializzo il contatore
counter = 0

# Itero su ogni elemento dell'array
array = []
# print(f"{data['wines'][0]['style']['grapes']}")
for wine in data['wines']:
    if wine['style'] is None:
        print("--- WRN: chiave 'style' assente.")
        counter += 1
        continue

    if wine['style']['grapes'] is None:
        print("--- WRN: chiave 'grapes' assente.")
        counter += 1
        continue
    
    for grape in wine['style']['grapes']:
        if 'parent_grape_id' not in grape:
            print("--- WRN: chiave 'parent_grape_id' assente.")
            exit()
        if grape['parent_grape_id'] is None:
            counter += 1
        else:
            array.append(grape['parent_grape_id'])

# Calcolo la percentuale
percentage = counter / len(data['wines']) * 100

print(f"Numero di elementi totali: {len(data['wines'])}")
print(f"Numero di elementi con 'parent_grape_id' uguale a null: {counter}")
print(f"Percentuale: {percentage:.2f}%")
print(f"Array di elementi con 'parent_grape_id' diverso da null: {array}")
