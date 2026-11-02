# ReceiptSheet — Smart Expense Tracker con AI & Google Sheets

Applicazione Android nativa creata con Jetpack Compose e Kotlin che permette di fotografare scontrini, estrarre istantaneamente i dati chiave (esercente, totale, valuta, data, categoria e dettagli) tramite Gemini AI multimodale, verificare i valori in un'anteprima interattiva e sincronizzare automaticamente le voci su un foglio Google Sheets, mantenendo un registro spese locale con statistiche mensili e grafici visivi.

---

## Decisioni Confermate e Revisione Utente

> [!IMPORTANT]
> Le seguenti scelte sono state confermate tramite la sessione di chiarimento iniziale e guidano l'architettura del progetto:

- **Sincronizzazione Google Sheets**: Collegamento diretto tramite autenticazione Google / OAuth e Google Sheets API per creare o aggiornare automaticamente il foglio di calcolo con righe formattate (`Data`, `Esercente`, `Categoria`, `Importo`, `Note`).
- **Verifica e Revisione Scontrino**: Flusso a 2 fasi con cattura/selezione foto -> estrazione automatica tramite Gemini AI -> schermata di anteprima interattiva con campi modificabili (esercente, importo, categoria, data) prima dell'invio.
- **Registro Spese Locale & Analisi**: Database locale Room per conservare la cronologia completa delle spese anche offline, con grafici di riepilogo mensile, filtro per periodo e suddivisione percentuale per categoria.
- **Supporto Fotocamera e Galleria**: Possibilità di scattare una foto al momento o selezionare un'immagine già salvata tramite Android Photo Picker nativo.

---

## 1. Panoramica & Concetto Chiave

### Cosa Fa l'Applicazione
L'app risolve il problema della registrazione manuale delle spese quotidiane. L'utente scatta una foto a uno scontrino (o ne seleziona uno dalla galleria): l'intelligenza artificiale estrae con precisione l'importo totale, il nome dell'attività, la data di emissione e assegna una categoria intelligente (es. *Alimentari*, *Ristorazione*, *Carburante*, *Utenze*, *Salute*, *Altro*). Dopo una rapida conferma visiva da parte dell'utente, la spesa viene salvata localmente e inviata all'istante a Google Sheets.

### Destinatari
- Liberi professionisti e lavoratori autonomi che devono rendicontare spese e scontrini.
- Famiglie e individui che gestiscono un budget mensile su Google Sheets e desiderano eliminare l'inserimento manuale.
- Chiunque voglia avere una visione chiara e ordinata delle proprie uscite senza perdere tempo a digitare scontrini stropicciati.

### Valore Principale
- **Zero noia da digitazione**: Da scontrino cartaceo a riga formattata su Google Sheets in meno di 5 secondi.
- **Accuratezza e controllo**: L'utente ha sempre l'ultima parola sui dati estratti prima della sincronizzazione.
- **Tracciamento integrato**: Statistiche mensili e andamento delle spese visibili direttamente nell'app anche senza aprire Google Sheets.

---

## 2. Esperienza Utente & Design Visivo

### Flussi Utente Principali
1. **Dashboard & Registro Spese**:
   - Card in evidenza con il totale spese del mese corrente e confronto visivo rispetto alle settimane precedenti.
   - Grafico a barre/torta interattivo con la distribuzione delle categorie.
   - Lista delle transazioni recenti con badge di stato sincronizzazione Google Sheets (Sincronizzato / In sospeso).
   - Floating Action Button (FAB) prominente "Scansiona Scontrino" e pulsante secondario "Aggiungi Manualmente".
2. **Cattura e Scansione Scontrino**:
   - Scelta tra scatto rapido da fotocamera o selezione da galleria (Photo Picker).
   - Animazione di scansione laser/radar con indicatore di caricamento ("Analisi AI in corso...").
3. **Schermata di Revisione & Conferma**:
   - Anteprima dell'immagine dello scontrino con zoom/pinch.
   - Campi precompilati dall'IA: Nome Esercente, Data, Importo Totale, Valuta, Categoria suggerita, Note opzionali.
   - Selettore a chip per cambiare rapidamente categoria con un tocco.
   - Pulsante primario "Salva & Sincronizza su Google Sheets" e pulsante "Salva solo in locale".
4. **Configurazione Google Sheets**:
   - Sezione Impostazioni per collegare l'account Google con un tocco.
   - Selezione del foglio di destinazione (creazione automatica di un nuovo foglio "Spese Personali" o inserimento dell'ID foglio esistente).

### Identità Visiva & Tema
- **Stile Estetico**: Modern Financial Editorial — pulito, affidabile, moderno con contrasti netti e finiture Material 3 di alto livello.
- **Tavolozza Colori**:
  - *Sfondo Principale*: Dark surface (`#12161A`) e Slate Light (`#F8FAFC`) con palette dinamica M3.
  - *Colore Primario*: Emerald Green profondo (`#00875A` / `#22C55E`) che evoca prosperità finanziaria, precisione e successo di sincronizzazione.
  - *Accento Secondario*: Vivid Teal (`#0D9488`) e Warm Amber (`#F59E0B`) per badge di categoria e avvisi di verifica.
  - *Superfici*: Card con elevazione tonale morbida e bordi arrotondati (16.dp).
- **Tipografia**:
  - Display & Titoli: Caratteri geometrici moderni, gerarchia chiara e numeri in evidenza per gli importi monetari.
  - Testo Corpo: Inter/Roboto scalabile, con etichette chiare e touch target minimi di 48dp.
- **Micro-interazioni & Feedback**:
  - Transizioni animate con `AnimatedVisibility` durante l'estrazione dati.
  - Haptic feedback leggero alla cattura e alla sincronizzazione completata.
  - Badge di sincronizzazione Google Sheets con animazione di spunta verde.

---

## 3. Decisioni di Prodotto & Trade-Off

| Decisione | Approccio Scelto | Motivazione & Vantaggi | Alternative Scartate |
| :--- | :--- | :--- | :--- |
| **Modello di Visione AI** | Gemini 3.8 Flash multimodale tramite Firebase AI Logic | Tempi di risposta rapidissimi (1-2s), eccellente comprensione di scontrini stropicciati e supporto di output JSON strutturato nativo (`responseSchema`). | OCR on-device ML Kit (troppo rigido per scontrini non standard e prive di comprensione semantica della categoria). |
| **Flusso di Sincronizzazione Sheets** | Google Sheets API con OAuth nativo (`set_up_oauth`) | Massima sicurezza, autorizzazioni ufficiali Google, nessuna configurazione di webhook complessi per l'utente comune. | Webhook serverless manuale (troppo ostico per l'utente finale che dovrebbe configurare script di terze parti). |
| **Persistenza Locale** | Room Database (SQLite) | Letture istantanee, funzionamento offline al 100%, query efficienti per aggregazioni mensili e raggruppamento categorie. | Solo cloud / memoria temporanea (perdita dati in assenza di connessione). |
| **Acquisizione Immagini** | Android Photo Picker + Camera Intent | Zero permessi invasivi di archiviazione per la galleria, esperienza utente conforme alle policy Google Play più recenti. | Permessi `READ_EXTERNAL_STORAGE` deprecati. |

---

## 4. Architettura Tecnica & Flusso Dati

### Schema del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                       Interfaccia Utente                    │
│   Dashboard (Grafici)  │  Scansione/Fotocamera  │  Revisione│
└──────────────┬───────────────────┬───────────────────┬──────┘
               │                   │                   │
               ▼                   ▼                   ▼
┌─────────────────────────────────────────────────────────────┐
│                    ReceiptExpenseViewModel                  │
│  - Gestione stato UI (Idle, Scanning, Review, Syncing)      │
│  - Calcolo statistiche mensili e aggregazione categorie     │
└──────────────┬───────────────────┬───────────────────┬──────┘
               │                   │                   │
      ┌────────▼────────┐ ┌────────▼────────┐ ┌────────▼────────┐
      │   Gemini AI     │ │   Room Database │ │  Google Sheets  │
      │  Vision Service │ │   Repository    │ │   Sync Service  │
      │ (Estrazione     │ │ (Spese locali,  │ │ (OAuth Google   │
      │  dati JSON)     │ │  cache offline) │ │  Workspace API) │
      └─────────────────┘ └─────────────────┘ └─────────────────┘
```

### Modello Dati Principale (`ExpenseItem`)
- `id`: Long (Chiave primaria autoincrementale)
- `merchant`: String (es. "Esselunga", "Eni Station", "Ristorante Bella Italia")
- `amount`: Double (Importo totale dello scontrino)
- `currency`: String (Default "EUR" / "€")
- `category`: ExpenseCategory (Enum: GROCERIES, RESTAURANTS, TRANSPORT, UTILITIES, SHOPPING, HEALTH, ENTERTAINMENT, OTHER)
- `date`: Long (Timestamp della spesa estratta dallo scontrino)
- `notes`: String? (Dettagli aggiuntivi o voci rilevate)
- `imageUri`: String? (URI locale della foto scansionata)
- `isSyncedToSheets`: Boolean (Flag di sincronizzazione)
- `sheetsRowId`: String? (Riferimento riga o ID aggiornamento)

### Sequenza di Elaborazione
1. **Cattura**: L'utente scatta la foto dello scontrino o ne sceglie una dalla galleria.
2. **Analisi Multimodale**: L'immagine viene ridimensionata ottimamente e inviata a `gemini-3.8-flash` con un prompt strutturato e `responseSchema` JSON che definisce esercente, totale, data, valuta e categoria.
3. **Revisione Utente**: L'app apre il foglio di revisione. L'utente può modificare o approvare i dati estratti con un tocco.
4. **Salvataggio Locale**: L'oggetto `ExpenseItem` viene memorizzato in Room.
5. **Esportazione su Google Sheets**: Se l'utente ha collegato il proprio account Google, viene eseguita la chiamata `appendRow` alle API di Google Sheets nel foglio selezionato (aggiungendo automaticamente intestazioni se è un nuovo foglio).
6. **Aggiornamento Dashboard**: I grafici di spesa e il budget mensile si aggiornano istantaneamente in tempo reale.
