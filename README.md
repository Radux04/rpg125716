# RPG 125716: The Forgotten Gate

Progetto Java per la modellazione delle entita principali di un semplice gioco RPG.

Il progetto contiene:

- `Player`, nel package `model.character`
- `ElementType`, enum per la specializzazione elementale del player
- `ElementalPower`, classe che rappresenta il superpotere legato all'elemento scelto
- `AchievementType`, enum per gli achievement della demo
- `Enemy`, nel package `model.enemy`
- nemici concreti: `Slime`, `Goblin`, `Skeleton`, `BossEnemy`
- `DemoLevel` e `DemoCampaign`, per la struttura dei 3 livelli della demo
- `GameStateLog`, `LevelState` e `EnemyState`, nel package `persistence`, per rappresentare tutto cio che va salvato
- oggetti di gioco base: `Potion`, `Weapon`, `Armor`, `KeyItem`
- drop e reward concreti: `OriginStone`, `Helmet`, `BossSword`
- inventario cumulabile tramite `Inventory`
- servizio di combattimento tramite `CombatService`

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

- `FIRST_KILL` -> nome mostrato: `first kill`
- `ORIGIN_STONE` -> nome mostrato: `Pietra dell'Origine`

Il primo viene sbloccato alla prima uccisione di un nemico.
Il secondo viene sbloccato quando il player usa la `Pietra dell'Origine` per ottenere il proprio elemento.

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

Metodi principali di `Inventory`:

- `addItem(Item item)`
- `removeItem(Item item)`
- `useItem(Player player, Item item)`
- `containsItem(Item item)`
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
Quando il player sconfigge il suo primo nemico, sblocca automaticamente l'achievement `first kill`.

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

### Persistence

Il package `persistence` contiene `GameStateLog`, `LevelState` e `EnemyState`, che modellano i dati di salvataggio.

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

---

## Tecnologie usate

- Java 25
- Gradle
- Lombok
- JUnit 5 per i test

---

## Prerequisiti

Per compilare il progetto e necessario usare una JVM compatibile con Gradle.

Requisiti consigliati:

- JDK 25, coerente con la configurazione del progetto
- Gradle Wrapper incluso nel repository

Il progetto forza Gradle a usare il JDK 25 configurato in `gradle.properties`.

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
