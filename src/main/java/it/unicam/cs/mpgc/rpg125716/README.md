# RPG 125716: The Forgotten Gate

Progetto Java per la modellazione delle entita principali di un semplice gioco RPG.

Il progetto contiene:

- `Player`, nel package `model.character`
- `Enemy`, nel package `model.enemy`
- nemici concreti: `Slime`, `Goblin`, `Skeleton`, `BossEnemy`

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
- configurare Lombok nel file Gradle
- aggiornare la documentazione del progetto

Il codice prodotto è stato sempre letto, compreso, testato e adattato manualmente durante lo sviluppo.
