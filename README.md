# RPG 125716: The Forgotten Gate

Progetto Java per la modellazione delle entita principali di un semplice gioco RPG.

Il progetto contiene:

- `Player`, nel package `model.character`
- `Enemy`, nel package `model.enemy`
- nemici concreti: `Slime`, `Goblin`, `Skeleton`, `BossEnemy`
- oggetti di gioco: `Potion`, `Weapon`, `Armor`, `KeyItem`
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
- `inventory`

Metodi principali:

- `takeDamage(int damage)`
- `heal(int amount)`
- `gainExperience(int amount)`
- `levelUp()`
- `isAlive()`
- `collectItem(Item item)`
- `removeItem(Item item)`
- `useItem(Item item)`

### Enemy

`Enemy` e una classe astratta che rappresenta un nemico generico.

Campi principali:

- `name`
- `hp`
- `attack`
- `defense`
- `experienceReward`
- `goldReward`

Classi concrete disponibili:

- `Slime`
- `Goblin`
- `Skeleton`
- `BossEnemy`

Ogni nemico concreto inizializza valori predefiniti per statistiche e ricompense.

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

L'inventario usa una struttura `Map<Item, Integer>`, quindi piu oggetti uguali vengono salvati come quantita.

Metodi principali di `Inventory`:

- `addItem(Item item)`
- `removeItem(Item item)`
- `useItem(Player player, Item item)`
- `getItems()`

### CombatService

Il package `service` contiene la logica di combattimento.

Classi principali:

- `CombatService`
- `CombatResult`
- `CombatWinner`

`CombatService` gestisce gli attacchi tra player e nemico e l'uso degli oggetti in combattimento.
Non conosce JavaFX e non aggiorna direttamente la grafica: restituisce sempre oggetti risultato.

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
- aggiornare la documentazione del progetto

Il codice prodotto e stato sempre letto, compreso, testato e adattato manualmente durante lo sviluppo.
