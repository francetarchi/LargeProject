# Il codice apre in lettura un file contenente un JSON e ordina tutti gli elementi in base all'id (crescente).
# Poi salva i dati ordinati nello stesso file.

import json

# Apro il file in lettura
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wineries.json', 'r', encoding='utf-8') as f:
    data = json.load(f)
f.close()

# Stampo il numero di elementi nel file da ordinare per controllare che siano corretti
print(f"\nlen(data): {len(data)}")

# Ordino i dati in base all'id
data.sort(key=lambda x: x['id'])

# Apro il file in scrittura
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wineries.json', 'w', encoding='utf-8') as f:
    # Scrivo i dati ordinati nel file
    json.dump(data, f, ensure_ascii=False, indent=4)
f.close()

print("\n --- Order done! --- \n")

# Stampo il numero di elementi nel file ordinato per controllare che siano corretti
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wineries.json', 'r', encoding='utf-8') as f:
    docs = json.load(f)
f.close()

print(f"len(docs): {len(docs)}\n")
print(f"Primi cento id ordinati: {[doc['id'] for doc in docs[:100]]}\n")
