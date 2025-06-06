import json
import random

# === Percorsi ===
USERS_FILE = r"C:\Users\Marta\Documents\MARTA\MAGISTRALE\dataset\users.json"
WINERIES_FILE = r"C:\Users\Marta\Documents\MARTA\MAGISTRALE\dataset\wineries.json"
OUTPUT_FILE = r"C:\Users\Marta\Documents\MARTA\MAGISTRALE\dataset\user_follows.json"

# === Carica utenti ===
with open(USERS_FILE, "r", encoding="utf-8") as f:
    users = json.load(f)
    usernames = [user["login"]["username"] for user in users]

# === Carica wineries ===
with open(WINERIES_FILE, "r", encoding="utf-8") as f:
    wineries_data = json.load(f)
    # Supporta sia lista che singolo oggetto
    if isinstance(wineries_data, dict):
        wineries_data = [wineries_data]
    winery_usernames = [w["login"]["username"] for w in wineries_data]

# === Costruisci la lista finale ===
follow_data = []

for username in usernames:
    # Follows verso altri user
    num_user_follows = random.randint(0, 300)
    other_users = [u for u in usernames if u != username]
    follows = random.sample(other_users, min(num_user_follows, len(other_users)))

    # Follows verso wineries
    num_winery_follows = random.randint(0, 20)
    follows_wineries = random.sample(winery_usernames, min(num_winery_follows, len(winery_usernames)))

    follow_data.append({
        "username": username,
        "follows": follows,
        "follows_wineries": follows_wineries
    })

# === Scrivi su file JSON ===
with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    json.dump(follow_data, f, indent=4, ensure_ascii=False)

print(f"✅ File generato: {OUTPUT_FILE}")
