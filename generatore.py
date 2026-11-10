import random, csv
from faker import Faker
from faker_ecommerce import EcommerceProvider
from datetime import datetime, timedelta

fake = Faker('it_IT')
fake.add_provider(EcommerceProvider)
random.seed(42)

N_ORDINI = 1500
# CATEGORIE = ['Elettronica','Abbigliamento','Casa','Sport', 'Libri','Giocattoli','Alimentari','Automotive']
CITTA = ['Roma','Milano','Napoli','Torino','Bologna', 'Palermo','Firenze','Venezia','Genova','Bari']
STATI = ['pending','spedito','consegnato','annullato']
BASE = datetime.now() - timedelta(days=730)
righe = []

# Ordini
for i in range(1, N_ORDINI + 1):
    # cid    = random.randint(1, N_CLIENTI) pid    = random.randint(1, N_PRODOTTI)
    qty     = random.randint(1, 10)
    totale  = round(random.uniform(1, 50), 2)
    data    = (BASE + timedelta(seconds=random.randint(0, 730*86400))).strftime('%Y-%m-%d %H:%M:%S')
    stato   = random.choice(STATI)

    # Prodotti
    # stock   = random.randint(0, 40)
    catch = fake.product_name()
    categ = fake.product_category()
    prezzo  = round(random.uniform(0.99, 5.99), 2)

    # Clienti
    nome    = fake.name()
    cognome = fake.last_name()
    email   = f"{cognome}.{i%40}@mail.it"
    citta   = random.choice(CITTA)
    data    = (BASE + timedelta(days=random.randint(0,730))).strftime('%Y-%m-%d')

    # Inserimento Ordini
    with open(f'Ordine #{i} {cognome} {catch}.txt', 'w', encoding='utf-8') as f:
        f.write(f"=== Ordine #{i} ===\n")
        righe.append(f"Quantità: {qty},\nTotale: {totale}€,\nData: '{data}',\nStato: '{stato}'")

        f.write(',\n'.join(righe) + ',\n')
        righe = []
        print(f"  Ordini: {i:,}/{N_ORDINI:,}")

        # Inserimento Prodotti
        righe.append(f"Nome Prodotto: {catch},\nCategoria: '{categ}',\nPrezzo: {prezzo}€")
        f.write(',\n'.join(righe) + ',\n')
        righe = []

        # Inserimento Clienti
        righe.append(f"Nome Cliente: {nome},\nCognome: {cognome},\nEmail: {email},\nCittà: '{citta}'")
        f.write(',\n'.join(righe) + ';\n\n')
        righe = []
        # print(f"  Clienti: {i:,}/{N_CLIENTI:,}")

print("File generati!")
