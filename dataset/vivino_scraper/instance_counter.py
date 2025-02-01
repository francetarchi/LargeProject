# Il codice apre un file con all'interno un JSON e scorre tutti gli elementi al suo interno contando quanti hanno un certo valore associato ad una certa chiave
# Per completezza, il codice conta anche quanti elementi ho in totale e calcola la percentuale di elementi con quel dato valore associato a quella data chiave.

import json

# Apro il file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wineries_1.json', encoding='utf-8') as f:
    data = json.load(f)

# Chiudo il file
f.close()

# Inizializzo il contatore
counter = 0

# Itero su ogni elemento dell'array
array = []
for winery in data:
    # se la chiave esiste e il valore è diverso da null
    if 'facebook' not in winery:
        array.append(winery)
        continue
    if winery['facebook'] is None or winery['facebook'] == '':
        counter += 1

# Calcolo la percentuale
percentage = counter / len(data) * 100

print(f"Numero di elementi totali: {len(data)}")
print(f"Numero di elementi con 'facebook' uguale a null: {counter}")
print(f"Percentuale: {percentage:.2f}%")
# print(f"Array di elementi senza 'facebook': {array}")
print(f"Numero di elementi senza 'facebook': {len(array)}")
