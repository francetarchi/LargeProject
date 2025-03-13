# Codice che preleva da un file un array di documenti json ordinati per id e rimuove tutti i duplicati (id uguale).

import json

# Apro il file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wineries_1.json', 'r', encoding='utf-8') as f:
    docs = json.load(f)
f.close()

with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wines.json', 'r', encoding='utf-8') as f:
    wines = json.load(f)
f.close()

new_docs = []
n_docs = len(docs)
dup = 0

print(f"\nNumero di elementi iniziali: {n_docs}")

# Rimuovo i duplicati
skip_next = False
for i in range(0, n_docs, 1):
    print(f"\r    --> Avanzamento: {i/(n_docs-1)*100:.2f}%", end='')

    if skip_next:
        skip_next = False
        continue
    
    if i != n_docs-1 and docs[i]['name'] == docs[i+1]['name']:
        bad_id = None
        good_id = None
        if 'telephone' in docs[i]:
            skip_next = True
            dup += 1

            bad_id = docs[i+1]['id']
            good_id = docs[i]['id']
        else:
            bad_id = docs[i]['id']
            good_id = docs[i+1]['id']
        
        # modifico l'id nel file wines.json
        for wine in wines:
            if 'winery' in wine and wine['winery']['id'] == bad_id:
                wine['winery']['id'] = good_id
        
        if not skip_next:
            continue
    
    new_docs.append(docs[i])
    
print('\n')

# Salvo i file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wines_1.json', 'w', encoding='utf-8') as f:
    json.dump(wines, f, ensure_ascii=False, indent=4)
f.close()
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wineries_1.json', 'w', encoding='utf-8') as f:
    json.dump(new_docs, f, ensure_ascii=False, indent=4)
f.close()

print(f"--- Duplicati rimossi ---\n")

print(f"Numero di duplicati rimossi: {dup} ({dup/n_docs*100:.2f}%)")
print(f"Numero di elementi rimasti: {len(new_docs)}\n")
