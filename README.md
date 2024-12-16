# JavaDrive
Simple drive using java

# Documentazione

## Come usare:

1. La classe `Packet` deve essere comune a client e server.
2. Avere un server MySQL attivo all'indirizzo impostato in `database.url` (vedi `server/config.properties`).
3. Un database esistente chiamato di default **"utenti"**.

### Struttura del database:

- **`<nome_id>`**: `INT` (univoco/primary key) con `AUTO_INCREMENT`  
  _Id identificatore_
- **`username`**: `VARCHAR(<lunghezza nome massima>)`  
  _Nome non univoco_
- **`hash`**: `VARCHAR(64)`  
  _Password dopo SHA256 + salt_

#### Esempio:

![ALT](example.png "esempio")

---

## Codici stato client:

| Codice | Significato | Note |
|--------|-------------|------|
| 0      | Il server è pronto a trasmettere | Ricevuto solo all'inizio dello scambio dei messaggi |
| 1      | Il client è stato disconnesso per troppi tentativi | |
| 2      | Il client ha fatto il login | |
| 3      | Il client ha registrato un nuovo utente | |
| 4      | Il client ha provato a fare il login ma la combinazione username/password è sbagliata | |
| 5      | Il server non può più accettare connessioni | Causa: congestione |
| 6      | Il server sta chiudendo | |
| 7      | Il client sta aspettando i nomi dei file precedentemente caricati | |
| 8      | Il server sta aspettando i dati del file | |
| 99     | Conferma file ricevuto | Inviato da entrambi per confermare la ricezione del file e indicare il prossimo |