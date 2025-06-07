import json
from neo4j import GraphDatabase
from datetime import datetime

# === CONFIGURAZIONE ===
NEO4J_URI = "bolt://localhost:7687"
NEO4J_USER = "neo4j"
NEO4J_PASSWORD = "wineadvisor"

USERS_FILE = r"C:\Users\Marta\Documents\MARTA\MAGISTRALE\dataset\users.json"
WINERIES_FILE = r"C:\Users\Marta\Documents\MARTA\MAGISTRALE\dataset\wineries.json"
FOLLOWS_FILE = r"C:\Users\Marta\Documents\MARTA\MAGISTRALE\dataset\user_follows.json"

# === CONNESSIONE ===
driver = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_USER, NEO4J_PASSWORD))

# === FUNZIONI ===

def create_user(tx, user):
    tx.run("""
        MERGE (u:User {username: $username})
        SET u.email = $email,
            u.thumbnail = $thumbnail,
            u.picture = $picture
    """, username=user["login"]["username"],
         email=user["email"],
         thumbnail=user["picture"]["thumbnail"],
         picture=user["picture"]["large"])

def create_winery(tx, winery):
    tx.run("""
        MERGE (w:Winery {username: $username})
        SET w.name = $name,
            w.thumbnail = $thumbnail
    """, username=winery["login"]["username"],
         name=winery["name"],
         thumbnail=winery["picture"]["thumbnail"])

def create_review(tx, review):
    tx.run("""
        MERGE (r:Review {id: $id})
        SET r.text = $text,
            r.rating = $rating,
            r.created_at = datetime($created_at)
    """, id=review["review_id"],
         text=review["text"],
         rating=review["rating"],
         created_at=parse_date(review["created_at"]["$date"]))

def create_user_review_relation(tx, review):
    tx.run("""
        MATCH (u:User {username: $username})
        MATCH (r:Review {id: $review_id})
        MERGE (u)-[:WROTE]->(r)
    """, username=review["user_id"]["username"],
         review_id=review["review_id"])

def create_user_follow_user(tx, follower, followed):
    tx.run("""
        MATCH (a:User {username: $follower})
        MATCH (b:User {username: $followed})
        MERGE (a)-[:FOLLOWS]->(b)
    """, follower=follower, followed=followed)

def create_user_follow_winery(tx, user, winery_username):
    tx.run("""
        MATCH (u:User {username: $user})
        MATCH (w:Winery {username: $winery})
        MERGE (u)-[:FOLLOWS]->(w)
    """, user=user, winery=winery_username)

def parse_date(datestring):
    return datetime.fromisoformat(datestring.replace("Z", "+00:00")).isoformat()

# === MAIN ===

with driver.session() as session:

    # Carica wineries
    with open(WINERIES_FILE, "r", encoding="utf-8") as f:
        wineries = json.load(f)
        if isinstance(wineries, dict):
            wineries = [wineries]
        for winery in wineries:
            session.execute_write(create_winery, winery)

    # Carica users
    with open(USERS_FILE, "r", encoding="utf-8") as f:
        users = json.load(f)
        total = len(users)

    for i, user in enumerate(users[:20], 1):  # ← cambia a [:total] per tutti
        percent = (i / total) * 100
        print(f"👤 User {i}/{total} ({percent:.1f}%)...", end="\r")

        session.execute_write(create_user, user)

        reviews = sorted(user.get("reviews", []), key=lambda r: r["created_at"]["$date"], reverse=True)[:3]
        for review in reviews:
            session.execute_write(create_review, review)
            session.execute_write(create_user_review_relation, review)

    # Carica relazioni di follow
    with open(FOLLOWS_FILE, "r", encoding="utf-8") as f:
        follow_data = json.load(f)

    for i, entry in enumerate(follow_data[:20], 1):  # ← cambia a [:len(follow_data)] per tutti
        username = entry["username"]
        for followed_user in entry.get("follows", []):
            session.execute_write(create_user_follow_user, username, followed_user)
        for followed_winery in entry.get("follows_wineries", []):
            session.execute_write(create_user_follow_winery, username, followed_winery)

driver.close()
print("\n Popolamento completato.")
