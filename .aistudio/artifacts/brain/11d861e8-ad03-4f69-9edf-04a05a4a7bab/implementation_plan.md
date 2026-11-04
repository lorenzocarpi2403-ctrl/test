# Piano di Evoluzione — Personal Finance & Smart Expense Tracker

Evoluzione dell'applicazione in una suite completa di gestione delle finanze personali: rimozione completa dell'integrazione Google Sheets in favore di un'architettura nativa al 100% che gestisce **Entrate**, **Spese con scansione scontrini AI**, **Abbonamenti ricorrenti** (Netflix, Disney+, Spotify, ecc.), **Conti multipli con saldi automatici** (Banca, Carte, Contanti, Risparmi) e una pagina dedicata con **Grafici e Statistiche Avanzate**.

---

## Decisioni Confermate e Revisione Utente

> [!IMPORTANT]
> Le seguenti scelte sono state confermate dall'utente e guidano l'architettura:

- **Rimozione Google Sheets**: Eliminazione del modulo Google Sheets, dei webhook, dei pulsanti di sincronizzazione e degli scope OAuth, semplificando l'esperienza utente e rendendo l'app 100% autonoma, veloce e privata.
- **Gestione Conti Multipli**: Tracciamento di molteplici conti e portafogli (es. *Conto Corrente*, *Revolut / Carta*, *Contanti*, *Fondo Risparmio*) con saldo iniziale configurabile e aggiornamento istantaneo del saldo per ogni entrata o uscita registrata.
- **Abbonamenti Ricorrenti**: Sezione dedicata per monitorare servizi come Netflix, Spotify, Disney+, iCloud, palestra, ecc. con calcolo automatico dell'impatto mensile complessivo, data del prossimo rinnovo e frequenza (mensile / annuale).
- **Entrate (Income Tracking)**: Modulo per registrare stipendi, bonus, bonifici, vendite e regali con categoria, conto di accredito e data.
- **Nuova Pagina Grafici & Analisi**: Schermata analitica dedicata con:
  - Confronto visivo Entrate vs Uscite e Risparmio Netto del mese corrente.
  - Grafico a ciambella interattivo delle categorie di spesa e categorie di entrata.
  - Panoramica dell'andamento finanziario complessivo e patrimonio netto totale.
- **Navigazione a Barra Inferiore**: 4 sezioni principali chiare e accessibili:
  1. **Transazioni** (Spese con scanner AI ed Entrate)
  2. **Conti** (Panoramica saldi, patrimonio netto e gestione conti)
  3. **Abbonamenti** (Servizi attivi, costo mensile e prossime scadenze)
  4. **Statistiche** (Grafici generali e analisi dettagliata del mese)

---

## 1. Panoramica delle Funzionalità

### 1.1 Entrate e Spese (Transazioni)
- Possibilità di registrare sia **Uscite** (tramite scansione scontrino Gemini AI o inserimento manuale rapido) sia **Entrate** (stipendio, investimenti, vendite, freelance).
- Ogni transazione specifica: importo, data, categoria, conto di riferimento e note.
- Filtri veloci per tipo (Tutte, Solo Uscite, Solo Entrate) e per categoria.

### 1.2 Monitoraggio Abbonamenti (Subscriptions)
- Schede personalizzate per servizi popolari con loghi/colori dedicati (Netflix, Spotify, Prime, Disney+, YouTube, Gym, iCloud, ecc.) o inserimento personalizzato.
- Calcolo automatico della spesa ricorrente mensile e annuale.
- Visualizzazione chiara dei giorni rimanenti al prossimo rinnovo con badge di stato.

### 1.3 Conti e Saldi (Accounts)
- Supporto per più conti con icone e colori distintivi (Banca principale, Carte prepagate, Risparmi, Portafoglio contanti).
- Saldo totale (Patrimonio Netto) calcolato in tempo reale sommando i saldi dei singoli conti.
- Quando si aggiunge un'uscita, il saldo del conto selezionato diminuisce; quando si aggiunge un'entrata, il saldo aumenta. Possibilità di effettuare giroconti/trasferimenti tra conti.

### 1.4 Schermata Statistiche & Analisi Finanziaria
- **Metriche del Mese**: Entrate Totali, Uscite Totali, Flusso di Cassa Netto (Risparmio) e % di risparmio rispetto alle entrate.
- **Grafico a Barre Comparativo Entrate vs Spese**: visualizzazione immediata dell'equilibrio finanziario del mese.
- **Grafico di Ripartizione Spese per Categoria**: visualizzazione a ciambella Canvas con percentuali per individuare dove si spende di più.
- **Riepilogo Abbonamenti**: incidenza fissa mensile sul budget totale.

---

## 2. Esperienza Utente & Design Visivo

### Identità Visiva M3
- **Stile**: Modern Financial Dashboard — raffinato, scuro o chiaro, con palette dinamica e accenti cromatici netti:
  - *Verde Smeraldo (`#10B981`)* per entrate e flussi positivi.
  - *Rosso Corallo / Ambra (`#EF4444` / `#F59E0B`)* per uscite e spese elevate.
  - *Indaco / Viola (`#6366F1`)* per abbonamenti e servizi ricorrenti.
  - *Blu Elettrico (`#3B82F6`)* per conti bancari e patrimonio netto.
- **NavigationBar Material 3**:
  - `Transazioni` (Icona lista / scontrino)
  - `Conti` (Icona portafoglio / banca)
  - `Abbonamenti` (Icona abbonamenti / streaming)
  - `Statistiche` (Icona grafici / analytics)

---

## 3. Decisioni Tecniche e Pulizia del Codice

| Aspetto | Azione Intrapresa | Motivazione |
| :--- | :--- | :--- |
| **Google Sheets** | Rimozione completa di `GoogleSheetsSyncService`, impostazioni Sheets, opzioni di sincronizzazione e revoca scope OAuth via `remove_oauth`. | L'utente non desidera più Google Sheets; l'app diventa 100% nativa e autonoma. |
| **Database Room** | Espansione dello schema Room con tabelle: `accounts`, `transactions` (con tipo IN / OUT), `subscriptions`. | Persistenza locale ultra-rapida, query reattive con `Flow` e integrità referenziale. |
| **Gemini AI** | Conservazione dello scanner scontrini per l'estrazione veloce delle uscite, indirizzando la spesa direttamente sul conto scelto. | Preserva il valore centrale della scansione intelligente senza digitazione manuale. |
| **Grafici Canvas** | Implementazione con Compose Canvas puro (zero dipendenze esterne pesanti), massima fluidità e supporto dark/light mode nativo. | Alte performance e interattività fluida. |

---

## 4. Architettura Tecnica & Schema Dati

```
┌─────────────────────────────────────────────────────────────┐
│                 Material 3 NavigationBar                     │
│  [Transazioni]   │   [Conti]   │  [Abbonamenti] │ [Statistiche]│
└─────────┬───────────────┬──────────────┬───────────────┬────┘
          │               │              │               │
          ▼               ▼              ▼               ▼
┌─────────────────────────────────────────────────────────────┐
│                    FinanceViewModel                         │
│  - Stato Patrimoniale: Saldo Totale, Conti & Saldi          │
│  - Transazioni: Entrate & Uscite (con Scanner AI)           │
│  - Abbonamenti: Spesa mensile ricorrente & Scadenze         │
│  - Statistiche Mensili: Totali, Risparmio, Breakdown Canvas │
└─────────┬──────────────────────────────┬────────────────────┘
          │                              │
          ▼                              ▼
┌──────────────────┐           ┌──────────────────┐
│  Gemini 3.5 AI   │           │  AppDatabase     │
│  (Scan Scontrini)│           │  (Room SQLite)   │
└──────────────────┘           └──────────────────┘
```

### Entità del Database Room
1. `AccountEntity`:
   - `id`: Long (PK)
   - `name`: String (es. "Conto Principale", "Carta Revolut", "Contanti")
   - `balance`: Double (saldo corrente)
   - `colorHex`: Long
   - `iconName`: String
2. `TransactionEntity`:
   - `id`: Long (PK)
   - `type`: String ("EXPENSE" o "INCOME")
   - `merchantOrTitle`: String
   - `amount`: Double
   - `category`: String
   - `date`: Long (timestamp)
   - `accountId`: Long (FK verso account)
   - `notes`: String
   - `imageUri`: String?
3. `SubscriptionEntity`:
   - `id`: Long (PK)
   - `name`: String (es. "Netflix", "Spotify", "Disney+", "Palestra")
   - `cost`: Double
   - `billingCycle`: String ("MONTHLY", "ANNUAL")
   - `nextRenewalDate`: Long
   - `category`: String
   - `colorHex`: Long
   - `isActive`: Boolean
