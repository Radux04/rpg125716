# Funzionalita Implementate

## 1. Avvio e navigazione

L'applicazione avvia una finestra desktop JavaFX a dimensione fissa e usa un navigatore centralizzato per cambiare scena senza duplicare logica di bootstrap.

Funzionalita presenti:

- apertura del menu principale;
- accesso alla schermata di caricamento slot;
- apertura dell'overview della run;
- ingresso nel livello attivo;
- schermata finale della demo.

## 2. Gestione delle run

Dal frontend l'utente puo:

- iniziare una nuova run;
- caricare una run da uno dei tre slot;
- salvare la run corrente;
- rientrare in un livello gia avviato;
- tornare al menu dal riepilogo o dalla schermata finale.

L'overview mostra:

- nome player;
- HP correnti e massimi;
- attacco, difesa, velocita;
- elemento selezionato;
- livello corrente;
- numero livelli completati;
- stato del livello;
- eventuali flag del livello, ad esempio tutorial o boss fight.

## 3. Struttura della demo

La campagna e composta da tre livelli sequenziali.

### Livello 1 - Esterno della fortezza

Contiene:

- 1 nemico;
- drop della Pietra dell'Origine;
- overlay di scelta dell'elemento.

Quando il livello viene completato:

- il player raccoglie la Pietra dell'Origine;
- sceglie uno tra `FIRE`, `WATER`, `WIND`, `EARTH`;
- l'elemento applica bonus statistici permanenti;
- il gioco consente l'avanzamento al livello 2.

### Livello 2 - Sale del Forgotten Gate

Contiene:

- 2 nemici attivi in arena;
- inseguimento dei nemici entro il raggio di rilevamento;
- reward obbligatoria a fine livello.

Quando entrambi i nemici vengono sconfitti:

- il livello viene marcato come completato;
- compare un overlay di reward;
- il player deve scegliere tra:
  - `Pozione curativa`
  - `Elmo con +2 difesa`

Solo dopo la scelta della reward il livello puo essere concluso.

### Livello 3 - Boss Fight

Contiene:

- boss finale;
- drop della `BossSword`;
- conclusione della demo.

Dopo la raccolta della spada:

- la demo viene chiusa;
- viene mostrata una transizione visiva;
- compare la schermata finale "Demo completata".

## 4. Combat system

Il combattimento e gestito come scambio di turni logici incapsulato nel game loop del frontend.

Caratteristiche:

- attacco player contro nemico selezionato;
- possibile contrattacco del nemico;
- danno minimo garantito;
- ricompensa in esperienza e oro alla sconfitta del nemico;
- probabilita di schivata del player basata sulla velocita;
- tracking dello stato `combat finished` e del vincitore.

## 5. Arena di gioco

Il livello giocabile presenta:

- arena 2D con board renderizzata via JavaFX;
- posizionamento di player, nemici, item e porta;
- ostacoli scenici e collisioni;
- interazione tramite tastiera;
- HUD laterale e contestuale.

Comandi principali:

- `WASD` o frecce per il movimento;
- `Space` o `Invio` per attaccare;
- `E` per interagire con porta o oggetto;
- `M` per inventario;
- `R` per il potere speciale della pietra, se disponibile.

## 6. Inventario e oggetti

Il player possiede un inventario cumulabile.

Oggetti presenti nel progetto:

- `Potion`
- `Weapon`
- `Armor`
- `OriginStone`
- `Helmet`
- `BossSword`

Funzionalita implementate:

- raccolta item;
- uso di item consumabili;
- uso di equipaggiamenti che modificano statistiche;
- serializzazione dell'inventario per il save/load.

## 7. Elementi e progressione

Il player puo scegliere un solo elemento.

Elementi disponibili:

- `FIRE`
- `WATER`
- `WIND`
- `EARTH`

Effetti principali attuali:

- `FIRE`: bonus statistico offensivo e attacco speciale a distanza con palla di fuoco;
- `WATER`: bonus difensivo e fase temporanea con forte knockback e schivata alternata;
- `WIND`: bonus velocita, schivata elevata e tornado temporaneo che blocca/danneggia;
- `EARTH`: bonus difesa/attacco e superpotere di cura totale.

Il superpotere della pietra:

- si sblocca al passaggio al livello 3;
- si attiva con `R`;
- si ricarica dopo `5` hit inflitti oppure `3` colpi subiti.

## 8. Frontend delle scelte contestuali

Sono presenti overlay dedicati per:

- inventario;
- scelta elemento;
- scelta reward del livello 2.

La scelta dell'elemento usa quattro pulsanti circolari in sovraimpressione, uno per ciascun elemento.

## 9. Feedback visivo

Il frontend include:

- aura del player colorata in base all'elemento;
- HUD del potere della pietra;
- indicatori dello stato arena;
- transizioni tra scene;
- popup achievement sbloccato;
- schermata finale demo.

## 10. Achievement

Gli achievement attualmente gestiti sono:

- `Primo Sangue` - sconfiggi il primo nemico;
- `Portatore della Pietra` - raccogli la Pietra Elementale;
- `Guardiano sconfitto` - sconfiggi il boss;
- `Demo completata` - finisci la demo.

Quando il backend sblocca un nuovo achievement:

- il player viene aggiornato;
- il registro globale viene persistito;
- il frontend mostra un popup compatto sopra il titolo del livello.

## 11. Persistenza utente

La demo supporta:

- 3 slot di salvataggio;
- caricamento dello slot selezionato;
- ricostruzione della campagna;
- sincronizzazione degli achievement globali.

Il formato dei save e XML. Gli achievement globali sono salvati in un file XML separato.
