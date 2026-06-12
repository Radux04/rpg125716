# The Forgotten Gate Wiki

Questa wiki descrive l'architettura, le funzionalita implementate e le scelte progettuali del progetto `rpg125716`.

Il progetto e una demo RPG desktop sviluppata in Java e JavaFX. La struttura del codice separa:

- presentazione JavaFX e navigazione scene;
- orchestrazione applicativa;
- modello di dominio;
- persistenza su file XML;
- eventi di gioco per estensioni trasversali, in particolare sugli achievement.

## Contenuti

- [Funzionalita Implementate](Funzionalita-Implementate.md)
- [Architettura e Responsabilita](Architettura-e-Responsabilita.md)
- [Classi e Interfacce](Classi-e-Interfacce.md)
- [Dati e Persistenza](Dati-e-Persistenza.md)
- [Estendibilita](Estendibilita.md)
- [Dichiarazione Uso AI](Dichiarazione-Uso-AI.md)

## Sintesi del progetto

La demo comprende:

- menu principale e schermata di caricamento slot;
- overview della run con statistiche, livello corrente e salvataggio;
- tre livelli giocabili;
- combattimento real-time semplificato su arena 2D;
- scelta dell'elemento della Pietra dell'Origine;
- superpotere della pietra sbloccato al livello 3;
- reward del secondo livello;
- boss fight finale e schermata "Demo completata";
- sistema di achievement con popup frontend;
- persistenza dei salvataggi e persistenza globale degli achievement.

## Organizzazione della documentazione

La wiki e organizzata per responsabilita:

- la pagina funzionale descrive cosa puo fare il gioco;
- la pagina architetturale descrive perche il codice e stato separato in quel modo;
- la pagina dei tipi elenca i componenti concreti della codebase;
- la pagina persistenza spiega struttura dati, file e regole di ripristino;
- la pagina estendibilita evidenzia i meccanismi predisposti per nuove funzionalita;
- la dichiarazione AI documenta uso e scopo degli strumenti di AI.

## Nota pratica

I file sono stati preparati in formato Markdown compatibile con GitHub Wiki. Se la wiki remota della repository non e ancora stata popolata, questi contenuti possono essere copiati nella repository `*.wiki.git` mantenendo gli stessi nomi file.
