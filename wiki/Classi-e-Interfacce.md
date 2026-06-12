# Classi e Interfacce

Questa pagina elenca i tipi principali della repository con la responsabilita associata a ciascuno.

Nota: oltre a classi e interfacce, il progetto usa anche `enum` e `record`. Sono inclusi per completezza perche partecipano alla struttura applicativa.

## Root package

| Tipo | Responsabilita |
| --- | --- |
| `Main` | Entry point dell'applicazione e avvio di JavaFX. |
| `FrontendApplication` | Bootstrap della finestra desktop, creazione di `GameService` e `SceneNavigator`. |

## Package `controller`

| Tipo | Responsabilita |
| --- | --- |
| `GameController` | Orchestrazione di sessione runtime, save/load, raccolta item, reward e pubblicazione eventi di gioco. |
| `GameLoadController` | Controller minimale dedicato al solo caricamento di una sessione da slot. |

## Package `event`

| Tipo | Responsabilita |
| --- | --- |
| `GameEvent` | Interfaccia marker base per gli eventi di gioco. |
| `GameEventListener` | Contratto per i listener che reagiscono agli eventi di gioco. |
| `GameEventDispatcher` | Registro in-memory dei listener e dispatcher sincrono degli eventi. |
| `EnemyDefeatedEvent` | Evento emesso quando un nemico viene sconfitto dal player. |
| `ItemCollectedEvent` | Evento emesso quando il player raccoglie o riceve un item. |
| `LevelCompletedEvent` | Evento emesso al completamento di un livello, incluso il flag di demo completata. |
| `OriginStoneAttunedEvent` | Evento emesso quando il player si sintonizza con la Pietra dell'Origine. |
| `PlayerDiedEvent` | Evento emesso quando il player viene sconfitto. |

## Package `frontend.controller.game`

| Tipo | Responsabilita |
| --- | --- |
| `GameOverviewController` | Controller del riepilogo run, gestione save e accesso al livello corrente. |
| `GameViewController` | Controller principale della scena di gioco: input, loop, arena, HUD, overlay, poteri della pietra e popup achievement rilevati dallo stato. |
| `DemoCompletedController` | Controller della schermata finale con ritorno al menu o uscita. |

## Package `frontend.controller.menu`

| Tipo | Responsabilita |
| --- | --- |
| `MainMenuController` | Controller del menu principale e dei flussi iniziali di navigazione. |
| `LoadSlotsController` | Controller della schermata di caricamento e visualizzazione dei tre slot. |

## Package `frontend.scene`

| Tipo | Responsabilita |
| --- | --- |
| `SceneNavigator` | Caricamento FXML, instanziazione controller, gestione scene, transizioni e popup globali. |

## Package `model.character`

| Tipo | Responsabilita |
| --- | --- |
| `Player` | Entita principale del gioco: statistiche, inventario, elemento, progressione, schivata, achievement locali e stato del superpotere della pietra. |
| `ElementType` | Enum dei quattro elementi selezionabili dal player. |
| `ElementalPower` | Value object descrittivo del potere associato all'elemento scelto. |

## Package `model.enemy`

| Tipo | Responsabilita |
| --- | --- |
| `Enemy` | Classe astratta base dei nemici, con statistiche, ricompense e logica di detection/chase. |
| `Slime` | Nemico base del primo livello. |
| `Goblin` | Nemico del secondo livello con ruolo offensivo/intermedio. |
| `Skeleton` | Nemico del secondo livello con variante di statistiche rispetto al goblin. |
| `BossEnemy` | Nemico boss del livello finale. |

## Package `model.item`

| Tipo | Responsabilita |
| --- | --- |
| `Item` | Classe astratta base per tutti gli oggetti usabili o raccoglibili. |
| `ItemType` | Enum che classifica gli item per famiglia funzionale. |
| `Inventory` | Collezione cumulabile di item del player, con supporto alla serializzazione tramite entry DTO. |
| `Potion` | Item consumabile che ripristina HP. |
| `Weapon` | Item non consumabile che aumenta l'attacco quando usato. |
| `Armor` | Item non consumabile che aumenta la difesa quando usato. |
| `KeyItem` | Item chiave non consumabile usato per oggetti di progressione. |
| `OriginStone` | Key item del primo livello che abilita la scelta dell'elemento. |
| `Helmet` | Ricompensa speciale del livello 2 con bonus difensivo. |
| `BossSword` | Reward finale del boss con forte bonus offensivo. |

## Package `model.level`

| Tipo | Responsabilita |
| --- | --- |
| `DemoCampaign` | Definizione della campagna demo, ordine dei livelli e factory dei livelli fresh. |
| `DemoLevel` | Modello del singolo livello con nemici, drop, reward e flag di gameplay. |
| `LevelRewardChoice` | Enum delle reward selezionabili nel livello 2. |

## Package `model.progression`

| Tipo | Responsabilita |
| --- | --- |
| `Achievement` | Stato persistente di un achievement globale, con titolo, descrizione e data di sblocco. |
| `AchievementType` | Catalogo degli achievement supportati dalla demo e loro metadati. |

## Package `persistence`

| Tipo | Responsabilita |
| --- | --- |
| `SaveRepository` | Interfaccia del backend di persistenza dei salvataggi. |
| `XmlSaveRepository` | Implementazione concreta della persistenza dei save su file XML. |
| `SaveSlot` | Enum dei tre slot di salvataggio e del relativo mapping a file. |
| `SaveSlotInfo` | DTO leggero per mostrare nel frontend lo stato dei singoli slot. |
| `GameStateLog` | Snapshot serializzabile di una run: player, livello, inventario, progressi e timestamp. |
| `LevelState` | DTO serializzabile del livello corrente, incluse reward, drop, flag e nemici. |
| `EnemyState` | DTO serializzabile del singolo nemico all'interno del livello. |
| `InventoryEntryState` | DTO serializzabile di uno stack di item, usato per ricostruire l'inventario da XML. |
| `AchievementCatalog` | Wrapper XML della collezione di achievement globali. |
| `AchievementRepository` | Repository dedicato alla persistenza separata degli achievement globali. |

## Package `service`

| Tipo | Responsabilita |
| --- | --- |
| `GameService` | Facciata applicativa principale verso il frontend: nuova partita, load/save, livelli, reward, elemento e combattimento. |
| `SaveService` | Servizio applicativo che usa `SaveRepository` per save, load, delete e lista slot. |
| `LoadService` | Ricostruzione di una `LoadedGameSession` a partire da un `GameStateLog`. |
| `LoadedGameSession` | Aggregato runtime della sessione caricata: player, campagna, livelli completati e save sorgente. |
| `CurrentGameState` | Snapshot leggibile dal frontend dello stato corrente della run. |
| `CombatService` | Regole di combattimento, uso item in combat context e dispatch degli eventi correlati. |
| `CombatResult` | Value object del risultato di una singola azione di combattimento. |
| `CombatTurnResult` | Aggregato dei risultati del turno player/enemy piu stato aggiornato del gioco. |
| `CombatWinner` | Enum dello stato finale del combattimento. |
| `AchievementService` | Gestione degli achievement globali, sincronizzazione con il player e reazione agli eventi di gioco. |

## Responsabilita trasversali rilevanti

### Frontend

La responsabilita di UI e confinata a:

- controller JavaFX;
- FXML;
- CSS;
- `SceneNavigator`.

Il modello e i servizi non dipendono da JavaFX.

### Persistenza

La responsabilita del formato file e confinata a:

- DTO del package `persistence`;
- `SaveRepository` e `XmlSaveRepository`;
- `AchievementRepository`.

### Regole di gameplay

La responsabilita delle regole e suddivisa in modo mirato:

- `Player` per progressione, schivata e stato del potere pietra;
- `CombatService` per risoluzione di attacchi e scontri;
- `DemoLevel` e `DemoCampaign` per flusso dei livelli;
- `GameService` per vincoli applicativi di avanzamento.

### Estensioni

La responsabilita di reazioni trasversali e delegata all'event system, che permette di aggiungere comportamento senza moltiplicare dipendenze dirette fra i componenti principali.
