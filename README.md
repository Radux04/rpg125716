# RPG 125716: The Forgotten Gate

Progetto Java per la modellazione delle entita principali di un semplice gioco RPG.

Il progetto contiene:

- `Player`, nel package `model.character`
- `ElementType`, enum per la specializzazione elementale del player
- `ElementalPower`, classe che rappresenta il superpotere legato all'elemento scelto
- `AchievementType`, enum per gli achievement della demo
- `Achievement`, stato persistente globale degli achievement
- package `event` con eventi di gioco e dispatcher leggero
- `Enemy`, nel package `model.enemy`
- nemici concreti: `Slime`, `Goblin`, `Skeleton`, `BossEnemy`
- `DemoLevel` e `DemoCampaign`, per la struttura dei 3 livelli della demo
- `GameStateLog`, `LevelState`, `EnemyState`, `SaveSlot`, `SaveSlotInfo`, `SaveRepository` e `AchievementRepository`, nel package `persistence`, per il sistema di salvataggio
- oggetti di gioco base: `Potion`, `Weapon`, `Armor`, `KeyItem`
- drop e reward concreti: `OriginStone`, `Helmet`, `BossSword`
- inventario cumulabile tramite `Inventory`
- servizi applicativi `CombatService`, `SaveService`, `LoadService`, `AchievementService` e `GameService`
- `GameLoadController` e `GameController`, nel package `controller`, per la gestione ad alto livello della sessione runtime

Il codice usa Lombok per generare automaticamente `getter`, `setter`, `toString`, `equals` e `hashCode`.

---

## Struttura Backend

### Player

La classe `Player` rappresenta il personaggio controllato dal giocatore.

Campi principali:

- `name`
- `level`
- `experience`
- `maxHp`
- `currentHp`
- `attack`
- `defense`
- `gold`
- `speed`
- `elementType`
- `elementalPower`
- `unlockedAchievements`
- `inventory`

Metodi principali:

- `chooseElement(ElementType elementType)`
- `attuneToOriginStone(ElementType elementType)`
- `takeDamage(int damage)`
- `heal(int amount)`
- `gainExperience(int amount)`
- `levelUp()`
- `isAlive()`
- `collectItem(Item item)`
- `removeItem(Item item)`
- `useItem(Item item)`
- `unlockAchievement(AchievementType achievementType)`
- `hasAchievement(AchievementType achievementType)`

`chooseElement(...)` puo essere usato una sola volta: il player sceglie un unico elemento e ottiene il relativo bonus statistico.

### ElementType

L'enum `ElementType` definisce l'elemento scelto dal player:

- `FIRE`
- `WATER`
- `WIND`
- `EARTH`

La scelta viene applicata tramite `chooseElement(...)`, che modifica le statistiche del player nel seguente modo:

- `FIRE`: `attack +8`, `speed +5`
- `WATER`: `maxHp +25`, `currentHp +25`, `speed +5`
- `WIND`: `speed +20`
- `EARTH`: `defense +5`, `attack +2`

Inoltre, `chooseElement(...)` assegna automaticamente anche un `ElementalPower` coerente con l'elemento selezionato.

### ElementalPower

La classe `ElementalPower` rappresenta il superpotere elementale del player.

Campi principali:

- `elementType`
- `name`
- `description`

Poteri associati:

- `FIRE`: `Fiamma Primordiale` -> aumenta il danno degli attacchi
- `WATER`: `Cura Fluente` -> permette al giocatore di recuperare punti vita
- `WIND`: `Passo del Vento` -> aumenta la possibilita di evitare gli attacchi
- `EARTH`: `Pelle di Roccia` -> aumenta la difesa del giocatore

Per ottenere il potere corretto a partire da un elemento e disponibile il metodo:

- `ElementalPower.fromElementType(ElementType elementType)`

### AchievementType

La demo contiene i seguenti achievement:

- `FIRST_KILL` -> nome mostrato: `Primo sangue`
- `BOSS_SLAYER` -> nome mostrato: `Boss Slayer`
- `COLLECTOR` -> nome mostrato: `Collezionista`
- `ORIGIN_STONE` -> nome mostrato: `Pietra dell'Origine`

Il primo viene sbloccato alla prima uccisione di un nemico.
Il secondo viene sbloccato quando il player sconfigge il suo primo boss.
Il terzo viene sbloccato quando il player arriva ad avere almeno 10 oggetti raccolti.
Il quarto viene sbloccato quando il player usa la `Pietra dell'Origine` per ottenere il proprio elemento.

### Achievement

La classe `Achievement` rappresenta lo stato persistente globale di un achievement.

Campi principali:

- `id`
- `title`
- `description`
- `unlocked`
- `unlockedAt`

Gli achievement non vivono piu solo dentro il `GameStateLog` della singola run: vengono anche salvati globalmente nel file `achievements.json`.
Questo evita di sbloccare di nuovo lo stesso achievement quando si inizia una nuova partita.

### Eventi di gioco

Il progetto espone un piccolo sistema di eventi nel package `event`, pensato per rendere il backend piu estendibile.

Componenti principali:

- `GameEvent`, interfaccia base
- `GameEventListener`, interfaccia per i listener
- `GameEventDispatcher`, dispatcher in-memory

Eventi attualmente modellati:

- `EnemyDefeatedEvent`
- `LevelCompletedEvent`
- `PlayerDiedEvent`
- `ItemCollectedEvent`
- `OriginStoneAttunedEvent`

Idea progettuale:

- i servizi e i controller emettono eventi quando succede qualcosa di rilevante
- `AchievementService` ascolta questi eventi e decide se sbloccare un achievement
- in questo modo aggiungere nuovi achievement non richiede di spargere logica in tutto il codice di gioco

### Enemy

`Enemy` e una classe astratta che rappresenta un nemico generico.

Campi principali:

- `name`
- `hp`
- `attack`
- `defense`
- `experienceReward`
- `goldReward`
- `detectionRange`
- `chasesPlayerWhenDetected`

Classi concrete disponibili:

- `Slime`
- `Goblin`
- `Skeleton`
- `BossEnemy`

Ogni nemico concreto inizializza valori predefiniti per statistiche e ricompense.
I nemici possono anche rilevare il player tramite `detectionRange` e decidere se inseguirlo con `shouldChasePlayer(int distanceFromPlayer)`.

### Item e Inventory

Il package `model.item` contiene gli oggetti raccoglibili dal player.

Classi principali:

- `Item`, classe astratta base per tutti gli oggetti
- `ItemType`, enum che identifica il tipo di oggetto
- `Inventory`, inventario del player con oggetti cumulabili
- `Potion`, oggetto consumabile che cura il player
- `Weapon`, oggetto non consumabile che aumenta l'attacco
- `Armor`, oggetto non consumabile che aumenta la difesa
- `KeyItem`, oggetto chiave non consumabile
- `OriginStone`, drop del primo livello
- `Helmet`, ricompensa del secondo livello con `+2` difesa
- `BossSword`, drop del boss finale con molto attacco aggiuntivo

L'inventario usa una struttura `Map<Item, Integer>`, quindi piu oggetti uguali vengono salvati come quantita.
Per il salvataggio XML, l'inventario viene esposto anche come lista di `InventoryEntryState`, cosi da evitare problemi di serializzazione con chiavi oggetto.

Metodi principali di `Inventory`:

- `addItem(Item item)`
- `removeItem(Item item)`
- `useItem(Player player, Item item)`
- `containsItem(Item item)`
- `getTotalItemCount()`
- `getItems()`

### DemoLevel e DemoCampaign

La progressione della demo e modellata tramite `DemoCampaign`, che contiene tre `DemoLevel` sequenziali.

Campi principali di `DemoLevel`:

- `number`
- `name`
- `description`
- `tutorial`
- `bossFight`
- `unlocksElementChoice`
- `endsDemoWithVictory`
- `enemies`
- `completionDrop`
- `rewardChoices`

Metodi principali di `DemoLevel`:

- `isCompleted()`
- `getRemainingEnemies()`
- `grantCompletionDrop(Player player)`
- `chooseReward(Player player, LevelRewardChoice rewardChoice)`

Le ricompense selezionabili del secondo livello sono rappresentate dall'enum `LevelRewardChoice`:

- `HEALING_POTION`
- `DEFENSE_HELMET`

### Struttura della demo

#### Livello 1

- 1 solo nemico
- serve da tutorial
- alla sconfitta del nemico droppa la `Pietra dell'Origine`
- dopo il drop il player sceglie uno tra `FIRE`, `WATER`, `WIND`, `EARTH`

#### Livello 2

- almeno 2 nemici
- i nemici inseguono il player quando entra nel loro raggio di rilevamento
- i nemici sconfitti danno XP tramite `CombatService`
- quando tutti i nemici sono sconfitti il player sceglie una ricompensa:
- `HEALING_POTION` -> una pozione curativa
- `DEFENSE_HELMET` -> un elmo con `+2` difesa

#### Livello 3

- bossfight finale
- il boss e piu forte dei nemici normali
- alla sconfitta droppa una `BossSword`
- la spada viene aggiunta all'inventario
- la demo termina con la vittoria

### CombatService

Il package `service` contiene la logica di combattimento.

Classi principali:

- `CombatService`
- `CombatResult`
- `CombatWinner`

`CombatService` gestisce gli attacchi tra player e nemico e l'uso degli oggetti in combattimento.
Non conosce JavaFX e non aggiorna direttamente la grafica: restituisce sempre oggetti risultato.
Gli attacchi nemici possono essere schivati in base alla `speed` del player, con una probabilita che cresce fino a un massimo del `35%`.
In questo modo `WIND` rende davvero operativo il potere `Passo del Vento`, dato che il bonus a `speed` aumenta anche la probabilita di evitare il colpo in arrivo.
Quando succede qualcosa di rilevante nel combattimento, `CombatService` emette eventi di gioco:

- `EnemyDefeatedEvent` quando il player sconfigge un nemico
- `PlayerDiedEvent` quando il player viene sconfitto

`AchievementService` ascolta questi eventi e puo sbloccare automaticamente `Primo sangue` e `Boss Slayer`.

Esempio:

```java
CombatService combatService = new CombatService();
CombatResult result = combatService.playerAttack(player, enemy);
```

Metodi principali:

- `playerAttack(Player player, Enemy enemy)`
- `enemyAttack(Enemy enemy, Player player)`
- `attack(Player player, Enemy enemy)`
- `attack(Enemy enemy, Player player)`
- `useItem(Player player, Item item)`
- `isCombatFinished()`
- `getWinner()`

### SaveService

Il package `service` contiene anche `SaveService`, che usa `SaveRepository` per gestire i salvataggi applicativi.

Metodi principali:

- `saveGame(GameStateLog gameStateLog, SaveSlot saveSlot)`
- `loadGame(SaveSlot saveSlot)`
- `deleteSave(SaveSlot saveSlot)`
- `getAvailableSlots()`
- `listSlots()`

### LoadService

Il package `service` contiene anche `LoadService`, che carica uno slot di salvataggio e ricostruisce gli oggetti runtime del gioco.

Restituisce un `LoadedGameSession`, che contiene:

- `saveSlot`
- `player`
- `campaign`
- `completedLevels`
- `sourceSave`
- `loadedAt`

Metodi principali:

- `loadFromSlot(SaveSlot saveSlot)`
- `requireLoadedSession(SaveSlot saveSlot)`

Durante il caricamento:

- viene ricostruito un `Player` runtime a partire dal `GameStateLog`
- viene ricostruita una `DemoCampaign`
- i livelli precedenti a quello corrente vengono marcati come completati
- il livello corrente viene ripristinato dal `LevelState` salvato
- gli achievement globali vengono sincronizzati con quelli presenti nel `Player` caricato

### CurrentGameState

La classe `CurrentGameState` rappresenta la vista runtime che il frontend puo leggere in modo diretto.

Campi principali:

- `saveSlot`
- `player`
- `campaign`
- `currentLevel`
- `completedLevels`
- `currentLevelStarted`
- `demoCompleted`

### AchievementService

Il package `service` contiene anche `AchievementService`, che gestisce la persistenza globale degli achievement.

Metodi principali:

- `unlockAchievement(String achievementId)`
- `unlockAchievement(AchievementType achievementType)`
- `unlockAchievement(Player player, AchievementType achievementType)`
- `onGameEvent(GameEvent gameEvent)`
- `isUnlocked(String achievementId)`
- `isUnlocked(AchievementType achievementType)`
- `getUnlockedAchievements()`
- `getAllAchievements()`
- `synchronizePlayerAchievements(Player player)`

Scelta progettuale:

- il file globale usato di default e `achievements.json`
- il servizio parte sempre dalle definizioni note in `AchievementType`
- se carichi un vecchio save con achievement solo locali nel `Player`, `synchronizePlayerAchievements(...)` li porta anche nel registro globale
- se invece esistono achievement globali gia sbloccati, questi vengono rimessi nel `Player` caricato o nella nuova run
- i nuovi achievement gameplay vengono sbloccati soprattutto tramite eventi, non con logica distribuita nei modelli

### GameService

`GameService` e la facciata principale pensata per il frontend JavaFX.
Incapsula il flusso di nuova partita, caricamento, salvataggio e avanzamento dei livelli sopra `GameController`.

Metodi principali:

- `newGame()`
- `loadGame(SaveSlot saveSlot)`
- `saveCurrentGame(SaveSlot saveSlot)`
- `getCurrentGameState()`
- `startLevel()`
- `completeCurrentLevel()`

Metodi di supporto esposti anche per il flusso della demo:

- `attuneCurrentPlayerToOriginStone(ElementType elementType)`
- `chooseCurrentLevelReward(LevelRewardChoice rewardChoice)`
- `listSaveSlots()`
- `deleteSave(SaveSlot saveSlot)`

Scelta progettuale:

- `newGame()` crea una sessione runtime nuova con un player di default e la `DemoCampaign`
- `getCurrentGameState()` restituisce un `CurrentGameState`, quindi il frontend non deve ricostruire manualmente lo stato leggendo i servizi interni
- `completeCurrentLevel()` valida che il livello sia davvero finito e blocca l'avanzamento se mancano ancora scelte obbligatorie, come elemento o reward

### GameLoadController

Il package `controller` contiene `GameLoadController`, un controller ad alto livello che usa `LoadService` e mantiene in memoria la sessione caricata corrente.

Metodi principali:

- `loadSelectedSlot(SaveSlot saveSlot)`
- `requireCurrentSession()`
- `getCurrentSession()`
- `clearCurrentSession()`

### GameController

Il package `controller` contiene anche `GameController`, che unifica il percorso completo:

- load da uno slot
- gestione della sessione runtime corrente
- costruzione di un nuovo `GameStateLog` a partire dal runtime
- save su uno slot scelto

Metodi principali:

- `listSaveSlots()`
- `loadGame(SaveSlot saveSlot)`
- `saveCurrentGame(SaveSlot saveSlot)`
- `attuneCurrentPlayerToOriginStone(ElementType elementType)`
- `collectItemForCurrentPlayer(Item item)`
- `claimCurrentLevelCompletionDrop()`
- `chooseCurrentLevelReward(LevelRewardChoice rewardChoice)`
- `publishCurrentLevelCompleted()`
- `publishCurrentPlayerDied()`
- `deleteSave(SaveSlot saveSlot)`
- `getAllAchievements()`
- `getCurrentSession()`
- `requireCurrentSession()`
- `clearCurrentSession()`

Quando salva la sessione corrente:

- deriva i livelli completati dallo stato reale della `DemoCampaign`
- costruisce un nuovo `GameStateLog`
- aggiorna la sessione corrente con il nuovo slot e il nuovo source save

Inoltre, `GameController` espone anche entry point che emettono eventi di raccolta item, completamento livello, morte del player e sintonia con la `Pietra dell'Origine`, cosi la UI o il game loop possono usare un backend gia pronto per logica estendibile basata su eventi.
`GameService` usa `GameController` come livello di orchestrazione sottostante, mentre il frontend dovrebbe preferire `GameService` come API principale.

### Persistence

Il package `persistence` contiene `GameStateLog`, `LevelState`, `EnemyState`, `InventoryEntryState`, `SaveSlot`, `SaveSlotInfo`, `SaveRepository`, `XmlSaveRepository` e `AchievementRepository`.

Il sistema supporta 3 slot fissi:

- `SLOT_1` -> `saves/slot-1.xml`
- `SLOT_2` -> `saves/slot-2.xml`
- `SLOT_3` -> `saves/slot-3.xml`

Campi salvati:

- `int saveVersion`
- `int slotId`
- `Player player`
- `int currentLevel`
- `List<String> completedLevels`
- `Inventory inventory`
- `LevelState currentLevelState`
- `LocalDateTime lastSavedAt`

Vincoli principali:

- `slotId` valido da `1` a `3`, per supportare fino a 3 slot di salvataggio
- `saveVersion` per mantenere compatibilita futura del formato di save
- il `GameStateLog` crea una snapshot dei dati passati, quindi modifiche successive a `Player`, `Inventory` o `completedLevels` non alterano il contenuto del salvataggio gia creato

`SaveRepository` definisce le operazioni base di persistenza:

- `save(GameStateLog gameStateLog, SaveSlot saveSlot)`
- `load(SaveSlot saveSlot)`
- `delete(SaveSlot saveSlot)`
- `exists(SaveSlot saveSlot)`
- `getAvailableSlots()`

`XmlSaveRepository` implementa il repository su file XML tramite Jackson XML.

`SaveSlotInfo` rappresenta i metadati utili per menu di load/save:

- `slot`
- `occupied`
- `playerName`
- `currentLevel`
- `lastSavedAt`

`LevelState` rappresenta il livello corrente nel modo in cui deve essere ricaricato:

- `levelNumber`
- `levelName`
- flag del livello come `tutorial`, `bossFight`, `unlocksElementChoice`, `endsDemoWithVictory`
- `restartFromBeginningOnLoad`
- `completionDropName`
- `rewardOptions`
- `enemyStates`

`EnemyState` rappresenta ogni nemico salvato per il restart del livello:

- `enemyType`
- `name`
- `startingHp`
- `attack`
- `defense`
- `experienceReward`
- `goldReward`
- `detectionRange`
- `chasesPlayerWhenDetected`

Scelta progettuale del save:

- se il player salva durante un combattimento o comunque durante un livello non ancora completato, al caricamento quel livello riparte dall'inizio
- per questo `currentLevelState` salva il punto di restart del livello, non il frame esatto del combattimento in corso
- se invece il livello corrente e gia completato, il save mantiene quello stato completato

Persistenza globale degli achievement:

- il file globale usato di default e `achievements.json`
- il file e separato dagli slot `saves/slot-1.xml`, `saves/slot-2.xml`, `saves/slot-3.xml`
- gli achievement quindi restano disponibili anche iniziando una nuova partita o caricando un'altra run

---

## Tecnologie usate

- Java 25
- Gradle
- Lombok
- Jackson XML
- Woodstox
- JUnit 5 per i test

---

## Prerequisiti

Per compilare il progetto e necessario usare una JVM compatibile con Gradle.

Requisiti consigliati:

- JDK 25, coerente con la configurazione del progetto
- Gradle Wrapper incluso nel repository

Il progetto usa una Java toolchain configurata nel `build.gradle.kts` e richiede quindi una JDK 25 disponibile sulla macchina.
Eventuali configurazioni locali come `org.gradle.java.home` non devono essere versionate nel repository:

- se vuoi usare un path JDK specifico, configuralo nel file locale `~/.gradle/gradle.properties`
- in alternativa, imposta `JAVA_HOME` sulla tua installazione di JDK 25

---

## Build del progetto

Da terminale, nella cartella principale del progetto:

```bash
./gradlew build
```

Su Windows:

```bash
gradlew.bat build
```

---

## Test

Per eseguire i test:

```bash
./gradlew test
```

Su Windows:

```bash
gradlew.bat test
```

---

## Esecuzione

Il progetto contiene una classe `Main` iniziale.

Quando sara configurato un task di esecuzione Gradle, il progetto potra essere avviato con:

```bash
./gradlew run
```

---

## Uso di strumenti di AI

Sono stati utilizzati strumenti di AI come supporto per:

- creare una prima struttura delle classi `Player` ed `Enemy`
- creare il servizio di combattimento separato dalla UI
- creare il sistema di item e inventario cumulabile
- configurare Lombok nel file Gradle
- modellare la progressione demo con livelli, achievement, drop e ricompense
- aggiornare la documentazione del progetto

Il codice prodotto e stato sempre letto, compreso, testato e adattato manualmente durante lo sviluppo.
