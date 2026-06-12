# Estendibilita

## Obiettivo

Il progetto e stato organizzato per rendere l'aggiunta di nuove funzionalita il piu possibile locale, evitando modifiche diffuse in tutto il codice.

## Meccanismi gia disponibili

### 1. Event system

Il primo meccanismo di estensione e il sistema eventi:

- `GameEvent`
- `GameEventListener`
- `GameEventDispatcher`

Vantaggio:

- nuove reazioni a eventi di gioco possono essere aggiunte come listener senza cambiare il flusso principale.

Caso gia presente:

- `AchievementService` ascolta eventi come sconfitta nemico, raccolta item e completamento livello.

Estensioni future possibili:

- statistiche di run;
- telemetria locale;
- quest secondarie;
- tutorial contestuali;
- notifiche frontend aggiuntive.

### 2. Repository abstraction

La persistenza e astratta tramite `SaveRepository`.

Questo permette di introdurre:

- una persistenza JSON;
- un backend database;
- una persistenza cloud o remota;
- salvataggi cifrati.

Per farlo basta fornire una nuova implementazione del repository e iniettarla in `SaveService`.

### 3. Modellazione a enum e tipi dedicati

Alcune famiglie di varianti sono modellate esplicitamente:

- `ElementType`
- `ItemType`
- `LevelRewardChoice`
- `AchievementType`
- `SaveSlot`
- `CombatWinner`

Questo semplifica l'aggiunta di nuove opzioni controllate e mantiene i punti di switch chiari e rintracciabili.

### 4. Separazione frontend / backend

Il frontend non contiene la persistenza e non implementa direttamente le regole di gioco. Questo rende possibile:

- aggiungere nuove scene senza modificare il dominio;
- cambiare il comportamento di gioco senza toccare FXML e CSS;
- riusare `GameService` con un frontend diverso.

### 5. DTO di stato per il frontend

`CurrentGameState` e `CombatTurnResult` sono oggetti pensati per il dialogo fra logica e UI.

Questo consente di:

- esporre nuove informazioni al frontend con impatto localizzato;
- evitare che i controller debbano scavare dentro piu classi del modello.

### 6. Configurazione della campagna

La campagna demo e centralizzata in `DemoCampaign`.

Per aggiungere un nuovo livello si puo:

1. creare una nuova factory di livello;
2. aggiungerla alla lista della campagna;
3. definire nemici, drop e reward del nuovo livello;
4. aggiornare eventuali messaggi o transizioni lato UI.

## Come integrare nuove funzionalita

### Nuovo nemico

Passi tipici:

1. creare una nuova sottoclasse di `Enemy`;
2. assegnare statistiche, reward e detection range;
3. registrare lo sprite frontend, se necessario;
4. aggiungerlo a un `DemoLevel` o a una nuova campagna.

### Nuovo oggetto

Passi tipici:

1. creare una sottoclasse di `Item`, `Weapon`, `Armor` o `KeyItem`;
2. implementare `use(Player player)`;
3. aggiornare `InventoryEntryState.toItem()` se l'oggetto richiede ricostruzione specifica;
4. aggiungerlo come drop, reward o item di inventario.

### Nuovo achievement

Passi tipici:

1. aggiungere la voce a `AchievementType`;
2. decidere l'evento di sblocco;
3. aggiornare `AchievementService` per reagire a quell'evento;
4. se serve, introdurre un nuovo `GameEvent`.

Il popup frontend gia esiste e non richiede nuova logica generale: mostra qualsiasi achievement nuovo rilevato nello stato del player.

### Nuovo evento di gioco

Passi tipici:

1. creare un nuovo tipo che implementa `GameEvent`;
2. dispatcharlo dal punto corretto;
3. registrare uno o piu listener.

Questo pattern evita accoppiamenti diretti con `CombatService`, `GameController` o `GameViewController`.

### Nuovo potere della pietra

Passi tipici:

1. estendere il modello se l'elemento cambia;
2. aggiornare la logica di `Player` per bonus e sblocco;
3. aggiornare `GameViewController` per effetto, HUD e input;
4. aggiornare eventuale feedback visivo del player.

### Nuova scena frontend

Passi tipici:

1. creare il file FXML;
2. creare il controller JavaFX dedicato;
3. registrare il percorso in `SceneNavigator`;
4. aggiungere eventuale stile CSS.

## Punti di forza per evoluzioni future

- eventi di dominio gia presenti;
- persistenza astratta;
- DTO di frontiera fra servizi e UI;
- campagna centralizzata;
- inventario serializzabile tramite entry DTO;
- scene JavaFX separate per responsabilita.

## Limiti attuali da considerare

La struttura e estendibile, ma alcune aggiunte richiedono comunque aggiornamenti coordinati:

- nuovi item speciali richiedono update in `InventoryEntryState`;
- nuovi nemici con sprite dedicato richiedono mappatura anche nel frontend;
- i poteri della pietra sono oggi molto incapsulati in `GameViewController`, quindi un'espansione ampia suggerirebbe una futura estrazione in componenti dedicati.

Questi limiti non bloccano l'estensione, ma indicano i punti dove la crescita del progetto potrebbe suggerire un refactoring ulteriore.
