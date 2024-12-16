import json
import time
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.options import Options
from selenium.common.exceptions import NoSuchElementException
import random

# Configurazione del browser
options = Options()
options.add_experimental_option("excludeSwitches", ["enable-automation"])
options.add_experimental_option('useAutomationExtension', False)
options.add_argument("--disable-blink-features")
options.add_argument("--disable-blink-features=AutomationControlled")
options.add_argument("--start-maximized")

# Inizializzo il driver
driver = webdriver.Chrome(options=options)

# Nascondo l'indicatore webdriver
driver.execute_cdp_cmd("Page.addScriptToEvaluateOnNewDocument", {
    "source": """
        Object.defineProperty(navigator, 'webdriver', {
            get: () => undefined
        })
    """
})

# URL di base
base_url = "https://www.bereilvino.it/database/cantine/regione/"
current_region = "piemonte"

# URL con regione
url = base_url + current_region + "/"

# Lista per salvare i dati
aziende_vinicole = []

# Funzione per estrarre i dati da una pagina
def estrai_dati_pagina():
    aziende = driver.find_elements(By.CSS_SELECTOR, ".item-details")
    for azienda in aziende:
        nome = azienda.find_element(By.CSS_SELECTOR, ":first-child").find_element(By.CSS_SELECTOR, ":first-child").text
        dettagli = azienda.find_element(By.CSS_SELECTOR, ".td-post-text-content").find_elements(By.CSS_SELECTOR, ".winery")
        indirizzo = []
        tel = ""
        email = ""
        web = ""
        face = ""
        ig = ""

        for dettaglio in dettagli:
            text = dettaglio.text.strip()
            if text.startswith("Telefono:"):
                tel = text.replace("Telefono: ", "").strip()
                continue
            if text.startswith("Fax:"):
                continue
            if text.startswith("website"):
                web = dettaglio.find_element(By.CSS_SELECTOR, "a").get_attribute("href")
                continue
            if text.startswith("email"):
                email = dettaglio.find_element(By.CSS_SELECTOR, "a").get_attribute("href").replace("mailto:", "")
                continue
            if text.startswith("eshop"):
                continue
            if text.startswith("facebook"):
                face = dettaglio.find_element(By.CSS_SELECTOR, "a").get_attribute("href")
                continue
            if text.startswith("twitter"):
                continue
            if text.startswith("instagram"):
                ig = dettaglio.find_element(By.CSS_SELECTOR, "a").get_attribute("href")
                continue
            indirizzo.append(text)
        
        cap = indirizzo[1].split(" ")[0]
        indirizzo[1] = indirizzo[1].replace(cap, "").strip()
        città = indirizzo[1].split("(")[0].strip()
        prov = indirizzo[1].split("(")[1].strip()
        prov = prov.replace(")", "")

        aziende_vinicole.append({
            "nome": nome,
            "indirizzo": indirizzo[0],
            "città": città,
            "cap": cap,
            "provincia": prov,
            "regione": "Toscana",
            "stato": "Italia",
            "telefono": tel,
            "email": email,
            "website": web,
            "facebook": face,
            "instagram": ig,
        })

try:
    # Apro il sito
    driver.get(url)
    time.sleep(random.randint(1, 5))

    # Trovo il numero totale di pagine
    try:
        page_nav = driver.find_element(By.CSS_SELECTOR, ".page-nav")
        str = page_nav.find_element(By.CSS_SELECTOR, ".pages").text
        total_pages = int(str.split(" ")[-1])
    except NoSuchElementException:
        total_pages = 1
    print(f"Trovate {total_pages} pagine totali.")

    # Scorro attraverso tutte le pagine
    for page in range(1, total_pages + 1):
        print(f"Processando pagina {page} di {total_pages}")
        if page > 1:
            url += f"page/{page}/"
            driver.get(url)
            url = url.replace(f"page/{page}/", "")
        time.sleep(random.randint(1, 5))
        estrai_dati_pagina()

    # Salvo i dati in un file JSON
    path = f"./dataset/aziende_vinicole/{current_region}.json"
    with open(path, "w", encoding="utf-8") as f:
        json.dump(aziende_vinicole, f, ensure_ascii=False, indent=4)

finally:
    driver.quit()
