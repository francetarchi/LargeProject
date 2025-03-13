# Il codice apre un file con all'interno un JSON e scorre tutti gli elementi al suo interno contando quanti hanno un certo valore associato ad una certa chiave
# Per completezza, il codice conta anche quanti elementi ho in totale e calcola la percentuale di elementi con quel dato valore associato a quella data chiave.

import json

# Apro il file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wines.json', encoding='utf-8') as f:
    data = json.load(f)

# Chiudo il file
f.close()

# Inizializzo il contatore
counter = 0
mancanti = 0
# min_ = 1
# max_ = 4824

# Itero su ogni elemento dell'array
array = []
for wine in data:
    # # se la chiave esiste e il valore è diverso da null
    # if 'style' not in wine or wine['style'] is None or wine['style'] == []:
    #     array.append(wine)
    #     continue
    # if wine['style']['blurb'] is None or wine['style']['blurb'] == '':
    #     counter += 1
    
    if not wine.get("type_id"):
        mancanti += 1
        continue
    
    # if wine["type_id"] < min_:
    #     min_ = wine["type_id"]
    
    # if wine["type_id"] > max_:
    #     max_ = wine["type_id"]
    
    counter += 1

    trovato = False
    for i in range(0, len(array)):
        if len(array) == 0:
            break
        if wine["type_id"] == array[i]["TYPE_ID"]:
            trovato = True

            num_wines_this_type = len(array[i]) - 1
            if num_wines_this_type < 11:
                # aggiungo il nome del vino corrente al dizionario
                array[i][f"name_{num_wines_this_type + 1}"] = wine["name"]

            break
    
    if not trovato:
        array.append({ "TYPE_ID": wine["type_id"], "name_1": wine["name"] })

# Riordino l'array in base al TYPE_ID di ogni elemento
array.sort(key=lambda x: x["TYPE_ID"])

# Calcolo la percentuale di elementi presenti
percentage = counter / len(data) * 100

print(f"Numero di elementi totali: {len(data)}")
print(f"Numero di elementi con 'type_id' presente: {counter}")
print(f"Percentuale di elementi presenti: {percentage:.2f}%")

print(f"Numero di elementi senza 'type_id': {mancanti}")
# print(f"Array di elementi senza 'type_id': {array}")

# print(f"Min: {min_}")
# print(f"Max: {max_}")

# Stampo in formato JSON
print(json.dumps(array, indent=4, ensure_ascii=False))
