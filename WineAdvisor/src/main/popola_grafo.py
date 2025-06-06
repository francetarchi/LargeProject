import json
from neo4j import GraphDatabase
from datetime import datetime

# === CONFIGURAZIONE ===
NEO4J_URI = "bolt://localhost:7687"
NEO4J_USER = "neo4j"
NEO4J_PASSWORD = "wineadvisor"

USERS_FILE = r"C:\Users\Marta\Documents\MARTA\MAGISTRALE\dataset\users.json"
WINERIES_FILE = r"C:\Users\Marta\Documents\MARTA\MAGISTRALE\dataset\wineries.json"
WINES_FILE = r"C:\Users\Marta\Documents\MARTA\MAGISTRALE\dataset\wines.json"

# Carica mappa id vino → username winery
with open(WINES_FILE, "r", encoding="utf-8") as f:
    wines_data = json.load(f)
    wine_to_winery = {
        wine["_id"]: wine["winery"]["username"]
        for wine in wines_data
        if "winery" in wine and "username" in wine["winery"]
    }

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

def create_wine(tx, wine, winery_username=None):
    tx.run("""
        MERGE (w:Wine {id: $id})
        SET w.name = $name,
            w.year = $year,
            w.image = $image
    """, id=wine["id"],
         name=wine["name"],
         year=wine["year"],
         image=wine["image"])
    
    if winery_username:
        tx.run("""
            MATCH (w:Wine {id: $wine_id})
            MATCH (wy:Winery {username: $winery_username})
            MERGE (w)-[:PRODUCED_BY]->(wy)
        """, wine_id=wine["id"], winery_username=winery_username)

def create_review(tx, review):
    tx.run("""
        MERGE (r:Review {id: $id})
        SET r.text = $text,
            r.rating = $rating,
            r.created_at = datetime($created_at),
            r.likes_count = $likes,
            r.dislikes_count = $dislikes
    """, id=review["review_id"],
         text=review["text"],
         rating=review["rating"],
         created_at=parse_date(review["created_at"]["$date"]),
         likes=review["likes_count"],
         dislikes=review["dislikes_count"])

def create_relationships(tx, review):
    username = review["user_id"]["username"]
    wine_id = review["wine_id"]["id"]
    review_id = review["review_id"]
    
    tx.run("""
        MATCH (u:User {username: $username})
        MATCH (r:Review {id: $review_id})
        MERGE (u)-[:WROTE]->(r)
    """, username=username, review_id=review_id)

    tx.run("""
        MATCH (r:Review {id: $review_id})
        MATCH (w:Wine {id: $wine_id})
        MERGE (r)-[:REVIEWS]->(w)
    """, review_id=review_id, wine_id=wine_id)

def create_likes_dislikes(tx, user):
    for liked_id in user.get("likes", []):
        tx.run("""
            MATCH (u:User {username: $username})
            MATCH (r:Review {id: $review_id})
            MERGE (u)-[:LIKES]->(r)
        """, username=user["login"]["username"], review_id=liked_id)
    
    for disliked_id in user.get("dislikes", []):
        tx.run("""
            MATCH (u:User {username: $username})
            MATCH (r:Review {id: $review_id})
            MERGE (u)-[:DISLIKES]->(r)
        """, username=user["login"]["username"], review_id=disliked_id)

def create_favorites(tx, user):
    for fav in user.get("wine_favorites", []):
        tx.run("""
            MERGE (w:Wine {id: $id})
            SET w.name = $name, w.image = $image
            WITH w
            MATCH (u:User {username: $username})
            MERGE (u)-[:FAVORITED]->(w)
        """, id=fav["id"],
             name=fav["name"],
             image=fav["image"],
             username=user["login"]["username"])

def parse_date(datestring):
    return datetime.fromisoformat(datestring.replace("Z", "+00:00")).isoformat()

# === MAIN ===

with driver.session() as session:
    # Carica wineries
    with open(WINERIES_FILE, "r", encoding="utf-8") as f:
        winery_data = json.load(f)
        if isinstance(winery_data, dict):  # supporta anche un solo oggetto
            winery_data = [winery_data]
        for winery in winery_data:
            session.execute_write(create_winery, winery)

    # Carica utenti
    with open(USERS_FILE, "r", encoding="utf-8") as f:
        users = json.load(f)
        total = len(users)
    for i, user in enumerate(users[:20], 1):
        percent = (i / total) * 100
        print(f"👤 Utente {i}/{total} ({percent:.1f}%)...", end="\r")  # \r sovrascrive la riga

        session.execute_write(create_user, user)

        reviews = sorted(user.get("reviews", []), key=lambda r: r["created_at"]["$date"], reverse=True)[:3]
        for review in reviews:
            wine_id = review["wine_id"]["id"]
            winery_username = wine_to_winery.get(wine_id)

            session.execute_write(create_wine, review["wine_id"], winery_username)
            session.execute_write(create_review, review)
            session.execute_write(create_relationships, review)

        session.execute_write(create_likes_dislikes, user)
        session.execute_write(create_favorites, user)

driver.close()
print("Popolamento completato.")
import json
from neo4j import GraphDatabase
from datetime import datetime

# === CONFIGURAZIONE ===
NEO4J_URI = "bolt://localhost:7687"
NEO4J_USER = "neo4j"
NEO4J_PASSWORD = "wineadvisor"

USERS_FILE = r"C:\Users\Marta\Documents\MARTA\MAGISTRALE\dataset\users.json"
WINERIES_FILE = r"C:\Users\Marta\Documents\MARTA\MAGISTRALE\dataset\wineries.json"

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

def parse_date(datestring):
    return datetime.fromisoformat(datestring.replace("Z", "+00:00")).isoformat()

# === MAIN ===

with driver.session() as session:
    # Carica wineries
    with open(WINERIES_FILE, "r", encoding="utf-8") as f:
        winery_data = json.load(f)
        if isinstance(winery_data, dict):
            winery_data = [winery_data]
        for winery in winery_data:
            session.execute_write(create_winery, winery)

    # Carica utenti
    with open(USERS_FILE, "r", encoding="utf-8") as f:
        users = json.load(f)
        total = len(users)

    for i, user in enumerate(users[:20], 1):  # ← Modifica a [:total] per tutti
        percent = (i / total) * 100
        print(f"👤 Utente {i}/{total} ({percent:.1f}%)...", end="\r")

        session.execute_write(create_user, user)

        reviews = sorted(user.get("reviews", []), key=lambda r: r["created_at"]["$date"], reverse=True)[:3]
        for review in reviews:
            session.execute_write(create_review, review)
            session.execute_write(create_user_review_relation, review)

driver.close()
print("\n✅ Popolamento completato.")
