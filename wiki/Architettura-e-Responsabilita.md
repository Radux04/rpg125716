# Architettura e Responsabilita

## Obiettivo architetturale

L'architettura e stata costruita per separare il piu possibile:

- logica di dominio;
- regole applicative;
- persistenza;
- rendering e interazione JavaFX;
- logiche trasversali basate su eventi.

Questa separazione riduce l'accoppiamento e rende piu semplice aggiungere livelli, oggetti, achievement, nuove UI o nuovi backend di persistenza.

## Vista a livelli

| Livello | Responsabilita principale |
| --- | --- |
| Bootstrap | Avvio dell'applicazione e creazione della finestra JavaFX. |
| Frontend | Rendering scene, input utente, overlay, HUD, transizioni e popup. |
| Application services | Orchestrazione di sessioni, combattimento, save/load e progressione. |
| Controller applicativi | Coordinamento di sessione runtime, eventi e accesso ai servizi. |
| Dominio | Stato e regole di player, nemici, oggetti, livelli e achievement. |
| Eventi | Disaccoppiamento fra eventi di gioco e reazioni trasversali. |
| Persistenza | Snapshot dei dati e accesso a file XML. |

## Responsabilita individuate

### 1. Bootstrap

Responsabilita:

- avviare JavaFX;
- costruire `GameService` e `SceneNavigator`;
- fissare dimensioni e proprieta della finestra.

Questa responsabilita e confinata in `Main` e `FrontendApplication`.

### 2. Navigazione scene

Responsabilita:

- caricare le scene FXML;
- istanziare i controller con le dipendenze corrette;
- centralizzare i cambi scena;
- applicare transizioni e popup condivisi.

Questa responsabilita e concentrata in `SceneNavigator`.

### 3. Controller frontend

Responsabilita:

- leggere input utente;
- invocare i servizi applicativi;
- aggiornare la scena in base allo stato runtime;
- non contenere regole di persistenza o logica di dominio riutilizzabile.

Questa scelta evita di trasformare i controller JavaFX in contenitori di business logic.

### 4. Orchestrazione applicativa

Responsabilita:

- gestire il ciclo della run;
- validare i passaggi di livello;
- convertire il backend in stato leggibile dal frontend;
- mantenere compatta l'API usata dalla UI.

Questa responsabilita e distribuita fra `GameService`, `GameController`, `LoadService`, `SaveService` e `AchievementService`.

### 5. Dominio

Responsabilita:

- rappresentare le entita del gioco;
- contenere regole stabili e locali, ad esempio XP, level up, uso item e progressione;
- restare indipendente da JavaFX e dal formato dei file.

Il modello include player, nemici, oggetti, livelli e progression system.

### 6. Persistenza

Responsabilita:

- trasformare lo stato runtime in snapshot serializzabili;
- caricare e validare dati salvati;
- isolare il formato fisico dei file dalle regole di gioco.

Questo strato usa DTO espliciti come `GameStateLog`, `LevelState`, `EnemyState` e `InventoryEntryState`.

### 7. Estensioni trasversali

Responsabilita:

- reagire a eventi di gameplay senza accoppiare direttamente ogni servizio;
- consentire nuove funzionalita come achievement, analytics o effetti futuri.

Questa responsabilita e supportata da `GameEvent`, `GameEventDispatcher` e `GameEventListener`.

## Scelte progettuali rilevanti

### Facciata applicativa verso il frontend

Il frontend non interroga direttamente repository o controller di basso livello. Usa soprattutto `GameService`, che espone metodi ad alto livello:

- nuova partita;
- caricamento e salvataggio;
- avvio e completamento livello;
- attacco;
- uso item;
- scelta elemento;
- scelta reward.

Questo riduce il numero di punti di contatto fra UI e backend.

### Stato runtime come DTO dedicato

`CurrentGameState` esiste per dare alla UI uno snapshot coerente e leggibile della sessione corrente. In questo modo il frontend non deve ricostruire informazioni pescando da piu oggetti interni.

### Persistenza per snapshot e non per frame

Il gioco non salva il frame preciso della battaglia. Se il livello non e completato, il caricamento riparte dall'inizio del livello corrente. La scelta e intenzionale:

- semplifica il modello di persistenza;
- evita di serializzare dettagli transitori del game loop;
- mantiene il caricamento deterministico.

### Achievement disaccoppiati dal core gameplay

Gli achievement non sono sparsi nel codice del combattimento o della UI. Le cause di sblocco generano eventi e `AchievementService` decide se aggiornare il registro globale e il player.

### Frontend dichiarativo

FXML e CSS sono separati dalla logica Java. Questo favorisce:

- modifica della UI senza cambiare la logica applicativa;
- overlay specializzati;
- tema condiviso;
- scene finali o transizioni riutilizzabili.

### Benefici della struttura attuale

La ripartizione delle responsabilita consente di:

- estendere la campagna senza rifare il sistema di save/load;
- aggiungere achievement senza invadere i controller;
- sostituire il backend di persistenza implementando un'interfaccia;
- evolvere il frontend mantenendo invariata l'API di gioco;
- testare in modo separato servizi, controller e persistenza.
