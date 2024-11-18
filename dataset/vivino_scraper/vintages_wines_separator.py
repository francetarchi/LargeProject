# Il codice apre un file con all'interno un JSON e scorre tutti gli elementi al suo interno eliminando una chiave (e il valore ad essa associato).

import json

# Apro il file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\scraped_data_from_vivino\scraped_data_from80.json', encoding='utf-8') as f:
    data = json.load(f)

# Chiudo il file
f.close()

# Stampo i dati del file completo
print(f"data.keys(): {data.keys()}")
print(f"len(data['vintages']): {len(data['vintages'])}")
print(f"len(data['wines']): {len(data['wines'])}")

# Salvo gli array in due file separati
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\scraped_data_from_vivino\scraped_vintages_from80.json', 'w', encoding='utf-8') as f:
    # Scrivo i dati modificati
    json.dump(data['vintages'], f)
f.close()

with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\scraped_data_from_vivino\scraped_wines_from80.json', 'w', encoding='utf-8') as f:
    # Scrivo i dati modificati
    json.dump(data['wines'], f)
f.close()

print("\n --- Separation done! --- \n")

# Stampo il numero di elementi nei due file separati per controllare che siano corretti
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\scraped_data_from_vivino\scraped_vintages_from80.json', encoding='utf-8') as f:
    vintages = json.load(f)
f.close()
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\scraped_data_from_vivino\scraped_wines_from80.json', encoding='utf-8') as f:
    wines = json.load(f)
f.close()

print(f"len(vintages): {len(vintages)}")
print(f"len(wines): {len(wines)}")
