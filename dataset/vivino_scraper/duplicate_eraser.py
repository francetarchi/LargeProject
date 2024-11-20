# Codice che preleva da un file un array di documenti json ordinati per id e rimuove tutti i duplicati (id uguale).

import json

# Apro il file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\scraped_data_from_vivino\scraped_wines\scraped_wines.json', encoding='utf-8') as f:
    docs = json.load(f)

# Chiudo il file
f.close()

new_docs = []
n_docs = len(docs)
dup = 0

print(f"\nNumero di elementi iniziali: {n_docs}")

# Rimuovo i duplicati
for i in range(0, n_docs, 1):
    print(f"\r    --> Avanzamento: {i/(n_docs-1)*100:.2f}%", end='')
    
    if i != 0 and docs[i]['id'] == docs[i-1]['id']:
        dup += 1
        continue
    new_docs.append(docs[i])
print('\n')

# Salvo il file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\scraped_data_from_vivino\scraped_wines\scraped_wines.json', 'w', encoding='utf-8') as f:
    json.dump(new_docs, f, ensure_ascii=False, indent=4)

# Chiudo il file
f.close()

print(f"--- Duplicati rimossi ---\n")

print(f"Numero di duplicati rimossi: {dup} ({dup/n_docs*100:.2f}%)")
print(f"Numero di elementi rimasti: {len(new_docs)}\n")
