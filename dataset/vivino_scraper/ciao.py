# File che apre tutti i file delle aziende vinicole (un file per regione) e li unisce al file wineries.json, dove ci sono già altre aziende vinicole.

import json
import os

# Apro il file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wineries.json', encoding='utf-8') as f:
    data = json.load(f)
f.close()

# Apro tutti i file delle aziende vinicole
next_id = len(data) + 1
for filename in os.listdir(r'C:\Users\franc\Desktop\Uni\0_Progetti\LargeProject\dataset\aziende_vinicole\\'):
    with open(r'C:\Users\franc\Desktop\Uni\0_Progetti\LargeProject\dataset\aziende_vinicole\\' + filename, encoding='utf-8') as f:
        # Aggiungo un id ad ogni elemento del file appena aperto come primo campo del singolo elemento
        print(f"\n\n\n\n\n\nfilename: {filename}")
        this_region_data = json.load(f)
        for i in range(len(this_region_data)):
            # Inserisco l'id come primo campo
            print(f"\n\n\n\n\n\n**this_region_data[i]: {this_region_data[i]}")
            this_region_data[i] = {'id': next_id, **this_region_data[i]}
            print(f"\n\n\n\n\n\n**this_region_data[i]: {this_region_data[i]}")
            next_id += 1
        
        data.extend(this_region_data)
    f.close()

# Scrivo il file
with open(r'C:\Users\franc\OneDrive - University of Pisa\Documenti\_Progetti magistrale\Large\datasets\wineries_1.json', 'w', encoding='utf-8') as f:
    json.dump(data, f, indent=4, ensure_ascii=False)
f.close()

print('Done')
