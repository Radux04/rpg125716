# Dati e Persistenza

## Obiettivo

Il sistema di persistenza deve supportare due bisogni distinti:

- salvataggio della run corrente;
- mantenimento globale degli achievement, indipendente dagli slot.

Per questo il progetto usa due canali separati:

- file save della partita;
- file globale degli achievement.

## Organizzazione dei dati principali

### Dati runtime

Lo stato di gioco in memoria ruota attorno a:

- `Player`
- `DemoCampaign`
- `DemoLevel`
- `Inventory`
- `LoadedGameSession`
- `CurrentGameState`

`LoadedGameSession` rappresenta la sessione completa caricata in backend.
`CurrentGameState` e lo snapshot esposto al frontend.

### Dati persistiti

Per la serializzazione si usano DTO dedicati:

- `GameStateLog`
- `LevelState`
- `EnemyState`
- `InventoryEntryState`
- `Achievement`
- `AchievementCatalog`

Questi tipi esistono per serializzare lo stato senza dipendere dal rendering o da strutture transitorie del game loop.

## File usati

### Salvataggi

I salvataggi sono contenuti nella cartella `saves/`:

- `saves/slot-1.xml`
- `saves/slot-2.xml`
- `saves/slot-3.xml`

Gli slot sono definiti da `SaveSlot`.

### Achievement globali

Gli achievement globali sono salvati in:

- `achievements.xml`

Il file e separato dai save di run.

## Come viene salvata una partita

Il percorso e il seguente:

1. Il frontend invoca `GameService.saveCurrentGame(...)`.
2. `GameService` delega a `GameController`.
3. `GameController` costruisce un nuovo `GameStateLog` a partire dalla sessione corrente.
4. `SaveService` delega a `SaveRepository`.
5. `XmlSaveRepository` serializza il log in XML.

## Struttura logica del save

`GameStateLog` salva:

- versione del save;
- slot id;
- copia del `Player`;
- numero del livello corrente;
- livelli completati;
- inventario;
- stato serializzato del livello corrente;
- timestamp ultimo salvataggio.

## Come viene garantita la coerenza

### Snapshot difensivi

`GameStateLog` copia i dati ricevuti invece di mantenere riferimenti diretti. Questo evita che modifiche successive alla sessione runtime cambino il contenuto dello snapshot.

### Validazione

I DTO di persistenza validano i campi principali:

- `slotId` valido;
- `saveVersion` positiva;
- `currentLevel` coerente con `LevelState`;
- nomi e liste non null dove richiesto;
- HP e dati dei nemici non negativi dove applicabile.

### Sincronizzazione inventario

`GameStateLog` e `XmlSaveRepository` richiamano la sincronizzazione dell'inventario per riallineare `Player` e `Inventory` dopo il caricamento o prima del riuso dei dati salvati.

## Persistenza dell'inventario

L'inventario runtime usa una `Map<Item, Integer>`. Questa struttura non e ideale per l'XML quando la chiave e un oggetto concreto. Per questo il progetto introduce `InventoryEntryState`.

Ogni entry salva:

- classe logica dell'item;
- tipo dell'item;
- nome e descrizione;
- quantita;
- eventuali bonus specifici.

Durante il load:

- `setEntries(...)` ricostruisce gli `Item`;
- gli item speciali come `BossSword`, `Helmet` e `OriginStone` vengono ricreati con i loro tipi corretti.

## Persistenza del livello corrente

`LevelState` salva:

- metadati del livello;
- flag di gameplay;
- drop e reward;
- elenco dei `EnemyState`;
- stato di completamento e claim;
- indicazione `restartFromBeginningOnLoad`.

`EnemyState` salva per ogni nemico:

- tipo;
- nome;
- HP di partenza al restore;
- attacco;
- difesa;
- reward XP/oro;
- detection range;
- flag inseguimento.

## Strategia di caricamento

Il caricamento segue questa logica:

1. `LoadService` legge il `GameStateLog`.
2. Viene creato un `Player` runtime copiando il player salvato.
3. Gli achievement globali vengono sincronizzati sul player.
4. Si ricostruisce una `DemoCampaign` nuova.
5. I livelli precedenti al corrente vengono marcati come completati.
6. Il livello corrente viene ripristinato usando `LevelState`.

## Regola sul restart del livello

Il progetto non salva lo stato frame-by-frame della battaglia in corso.

Scelta adottata:

- se il livello e gia completato, il salvataggio conserva lo stato completato;
- se il livello non e completato, il salvataggio memorizza una snapshot di restart del livello.

Questo rende il caricamento:

- semplice da mantenere;
- prevedibile;
- indipendente dai dettagli del game loop JavaFX.

## Persistenza degli achievement

Gli achievement usano `AchievementRepository`, separato dai save di run.

Flusso:

1. un evento di gioco raggiunge `AchievementService`;
2. il servizio carica la vista merge degli achievement noti;
3. se uno sblocco e nuovo, aggiorna `achievements.xml`;
4. il player corrente riceve anche l'achievement in memoria.

Questo garantisce:

- riuso degli achievement tra piu run;
- persistenza globale indipendente dagli slot;
- sincronizzazione anche quando si carica un vecchio salvataggio.

## Interfacce e adattatori

La persistenza e stata resa estendibile tramite:

- `SaveRepository` come contratto;
- `XmlSaveRepository` come implementazione concreta attuale.

In questo modo il formato di storage puo cambiare senza riscrivere `SaveService` o `GameService`.
