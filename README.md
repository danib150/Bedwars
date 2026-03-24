# 🎮 Bedwars Plugin - Documentazione

## 📌 Descrizione

Questo plugin implementa una modalità **Bedwars** per server **Minecraft Bukkit/Spigot**.

🔧 **Nota:**
Questo plugin deriva dal progetto originale di **Wild Adventure**, ma è stato **ristrutturato e aggiornato**.

In particolare, questa versione include un **recode del sistema NPC**, con uso di:

* **Bukkit API**
* **NMS**
* supporto opzionale agli **hologram**

ed è stato rimosso **HolographicMobs**, perché:

* deprecato
* non necessario per questo plugin
* sostituibile con una gestione più pulita tramite villager + hologram separati

---

## 📦 Dipendenze

Questo plugin richiede le seguenti dipendenze:

* **WildCommons** → [https://github.com/danib150/WildCommons](https://github.com/danib150/WildCommons)
* **Boosters** → [https://github.com/danib150/Boosters](https://github.com/danib150/Boosters)

---

## 🧱 Struttura generale

La logica delle cartelle e della configurazione segue lo stile già usato in **SurvivalGames**.

Le arene vengono configurate tramite comandi amministrativi e salvate nei file del plugin.

---

## 🗺️ Setup arene

Le arene vengono create e modificate tramite il comando:

```text
/arena
```

Ogni admin può caricare in memoria una sola arena alla volta in fase di modifica.

---

## 📜 Comandi `/arena`

### Informazioni generali

```text
/arena
```

Mostra la lista dei sottocomandi disponibili.

```text
/arena info
```

Mostra le informazioni dell’arena attualmente caricata.

```text
/arena info -v
```

Mostra anche il volume dell’arena.

```text
/arena info -v -r
```

Mostra il volume dell’arena rimpiazzando visivamente i blocchi.

---

### Creazione e caricamento arena

```text
/arena new <nome>
```

Crea una nuova arena.

```text
/arena load <nome>
```

Carica un’arena esistente in modalità modifica.

```text
/arena unload
```

Scarica l’arena corrente senza salvare le modifiche.

```text
/arena save
```

Salva l’arena caricata.

---

### Impostazioni base arena

```text
/arena playersPerTeam <numero>
```

Imposta il numero massimo di giocatori per team.

```text
/arena protection <spawn|generator|villager|boss> <raggio>
```

Imposta il raggio di protezione per:

* `spawn`
* `generator`
* `villager`
* `boss`

---

### Volume arena

```text
/arena loc1
```

Imposta il primo angolo della region.

```text
/arena loc2
```

Imposta il secondo angolo della region.

---

### Posizioni globali

```text
/arena lobby
```

Imposta la lobby dell’arena.

```text
/arena spectatorSpawn
```

Imposta lo spawn degli spettatori.

```text
/arena boss
```

Imposta la posizione del boss.

```text
/arena sign
```

Imposta il cartello dell’arena guardando un cartello a muro.

---

### Spawner risorse

```text
/arena addSpawner <tipo>
```

Aggiunge uno spawner del tipo specificato guardando il blocco corretto.

```text
/arena removeSpawner <tipo>
```

Rimuove uno spawner del tipo specificato dal blocco guardato.

I tipi validi dipendono da `ResourceType` del plugin.

---

### Team

```text
/arena addTeam <team>
```

Aggiunge un team all’arena.

```text
/arena removeTeam <team>
```

Rimuove un team e la relativa configurazione.

---

### Configurazione team

```text
/arena spawn <team>
```

Imposta lo spawn del team.

```text
/arena bed <team>
```

Imposta il letto del team guardando un letto valido.

```text
/arena villager <team> <item|team>
```

Imposta la posizione del villager del team.

* `item` → shop oggetti
* `team` → shop potenziamenti/team

---

## ✅ Ordine consigliato per creare un’arena

Esempio pratico:

```text
/arena new NomeArena
/arena playersPerTeam 2
/arena loc1
/arena loc2
/arena lobby
/arena spectatorSpawn
/arena boss
/arena sign
```

Poi per ogni team:

```text
/arena addTeam red
/arena spawn red
/arena bed red
/arena villager red item
/arena villager red team
```

Aggiungere eventuali spawner:

```text
/arena addSpawner iron
/arena addSpawner gold
/arena addSpawner diamond
/arena addSpawner emerald
```

Infine:

```text
/arena save
```

---

## 🧍 NPC Shop

Il plugin usa villager come NPC shop.

Questa versione è stata riscritta per usare:

* villager spawnati runtime
* gestione con Bukkit API + NMS
* supporto opzionale a hologram sopra gli NPC
* rimozione di HolographicMobs

Gli NPC vengono gestiti come entità temporanee dell’arena e rimossi durante il cleanup/reset.

---

## 🏆 Setup Podium

Il plugin include un comando separato per configurare i podi/classifiche:

```text
/podium
```

Serve per impostare:

* teste dei giocatori in classifica
* cartelli del podio

Tipi supportati:

* `finalkills`
* `wins`

---

## 📜 Comandi `/podium`

```text
/podium finalkills head <1-3>
```

Imposta la testa del classificato per la classifica final kills.

```text
/podium finalkills sign <1-3>
```

Imposta il cartello del classificato per la classifica final kills.

```text
/podium wins head <1-3>
```

Imposta la testa del classificato per la classifica wins.

```text
/podium wins sign <1-3>
```

Imposta il cartello del classificato per la classifica wins.

### Esempi

```text
/podium finalkills head 1
/podium finalkills sign 1
/podium wins head 2
/podium wins sign 2
```

⚠️ Per usare questi comandi bisogna guardare il blocco corretto:

* `head` → una testa
* `sign` → un cartello a muro

---

## ⚠️ Errori comuni

* Arena non caricata prima di usare i comandi
* Team non aggiunto prima di configurare spawn/letto/villager
* Letto incompleto o selezionato dal lato sbagliato
* Cartello non a muro
* Spawner aggiunto guardando il blocco sbagliato
* Arena salvata senza tutti i campi obbligatori
* Volume arena non impostato (`loc1` / `loc2`)
* Boss, lobby o spectator spawn mancanti

---

## 🔧 Note tecniche

* Il plugin è pensato per una configurazione manuale tramite comandi admin.
* Le entità runtime dell’arena vengono ricreate e distrutte durante i reset.
* Gli hologram creati via API non sono persistenti e vengono ricreati dal plugin quando necessario.
* Gli NPC shop non dipendono più da HolographicMobs.

---

## 👨‍💻 Crediti

* Progetto originale: **Wild Adventure**
* Fork / recode: **danib150**
* Dipendenze:

  * [https://github.com/danib150/WildCommons](https://github.com/danib150/WildCommons)
  * [https://github.com/danib150/Boosters](https://github.com/danib150/Boosters)
