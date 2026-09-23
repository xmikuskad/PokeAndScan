# Pokémon GO Exporter Domain

This context defines the domain language for converting user-visible Pokémon GO screens into local, reviewable exports. It distinguishes an individual Pokémon observation from a collection snapshot and from the evidence used to support extracted fields.

This is a glossary. Product rules, UI details, and implementation contracts live in the documents routed by [AGENTS.md](./AGENTS.md).

## Scanning

**Scan session**:
A single user-initiated run that produces one independent collection result. A session may use live screen capture or an imported screen recording.

**Imported source video**:
An MP4 selected by the user and processed in place rather than copied into PokeAndScan. The app keeps access only while processing or while the scan remains incomplete for recovery, then releases it when the scan is finalized or discarded. Reprocessing later requires the user to select the source again.

**Local-only scan data**:
MVP snapshots, review history, and evidence remain in PokeAndScan's app storage and are not included in Android cloud backup or device-to-device backup. The user may explicitly export CSV or JSON; those files are separate from app-managed snapshot history and evidence.

**Snapshot**:
The collection result produced by one scan session. Snapshots remain separate; a later scan does not silently merge into or overwrite an earlier snapshot.

**Snapshot library**:
The app's home view listing independent snapshots and any active or recoverable job. It is an organizational view, not a merged master collection; each snapshot remains separately viewable, editable, and exportable. Cards use names, dates, source/status symbols, lifecycle/scope labels, and compact quality counts rather than source-video or screenshot thumbnails.

User-facing Slovak labels use `sken`/`skeny` and English labels use `scan`/`scans` for this concept. `Snapshot` remains the internal/domain term and is not the normal UI noun.

**Snapshot name**:
An optional user-provided label for identifying a snapshot later. If omitted, the application generates a date-based name with a localized prefix, such as `Sken 20.12.2023` or `Scan 20.12.2023`, using the app language at creation time. The generated string is persisted as the snapshot's name and does not change if the app language later changes. The name describes the user's intent and is not a claim that the application knows which in-game filter or tag was used.

Snapshots remain locally editable after processing and export. An export is a point-in-time representation; later corrections change the snapshot but never silently rewrite an existing exported file. Renaming a snapshot changes its library label and default filename for future exports, without changing its identity, data, or existing exported files.

**Scan pass**:
A planned traversal of Pokémon GO screens focused on one family of fields. The first MVP has an appraisal pass for species, CP, and IVs.

**Scan scope**:
The intended subset of the in-game collection for one scan. It may be the whole collection or a user-prepared filtered subset, such as Pokémon with a chosen tag. PokeAndScan records the visible results but does not interpret or persist the game's filter/tag definition as a separate domain object in the MVP.

**Reference profile**:
The only verified MVP setup for layout and parser support: Pixel 9 Pro XL, portrait orientation, English Pokémon GO UI, and default Android display and font settings. This verification scope applies to MVP only; installation on other Android devices does not imply verified scanning support. Later expansion will verify additional Android phone models, always for Pokémon GO. A reference requirement is not presented as detected fact unless the app can genuinely verify it.

**Interrupted recovery**:
The explicit decision point shown after a capture or processing job stops unexpectedly. It gives the user Continue, Finish partial snapshot, or Discard using the last durable checkpoint; it never resumes automatically.

**Appraisal traversal**:
The user's continuous movement through the collection during an appraisal pass: open the first Pokémon, open Appraise, move to the next Pokémon using the game's in-screen navigation, and wait for each appraisal screen to settle before continuing. The user does not return to the storage list between every Pokémon.

**Stable appraisal state**:
The settled, user-visible appraisal screen for one individual Pokémon after a navigation transition. Transient animation or transition frames are not a stable appraisal state and do not produce a record.

**Candidate**:
A potential individual Pokémon record derived from one stable, navigated-to Pokémon screen. A candidate may remain incomplete or require review.

## Pokémon data

**Individual Pokémon**:
One specific Pokémon in the user's in-game collection. Two individuals with identical visible attributes are still separate records when the user navigated to them separately.

**Canonical species**:
The normalized Pokémon identity used in exports and record values after interpreting visible screen text. It may include a base species and a clearly named in-game form, such as an Alolan or Galarian form. Canonical species/form names remain official English names even when the app UI is Slovak. A nickname is not a canonical species name. Costume, shiny, and event visual variants are not separate MVP fields.

**Nickname**:
A user-assigned name displayed in place of, or alongside, the species name. Nicknames are outside the first MVP but are planned for a later version.

If a nickname prevents reliable species recognition, the record may still exist when other appraisal fields are trustworthy. Species remains Unknown until the user selects a canonical species during review.

## Evidence and review

**Field evidence**:
The retained source information supporting one extracted field, including the original observation and its relevant screen evidence. User correction does not erase the original field evidence.

**Review issue**:
An explicit unresolved or low-confidence field or record that requires a user decision. A review issue must remain visible rather than being silently guessed or discarded.

**Missed appraisal screen**:
A navigation event for which no stable appraisal state was captured. It is a warning about a possible missing individual Pokémon, not a fabricated Pokémon record. The event retains its position relative to the scan source and available transition evidence; it does not create a Pokémon record by itself.

**Source offset**:
The elapsed position in the selected scan source, measured in milliseconds from the beginning of the MP4 or live capture. It locates records and warnings in the capture timeline and is distinct from a wall-clock date/time.

**User-confirmed value**:
A value selected or entered by the user during review. It is the export value while the original parser observation remains available as field evidence.

For a parser-derived field, the user-confirmed value can be reset to the original parser observation. A manually added record has no parser observation to restore.

**Manually added record**:
A new individual Pokémon record created directly by the user because the scan did not produce a usable record. It has no parser observation and is explicitly marked as manually added in review and export provenance.

For a manually added record, canonical species is required. CP and each IV may remain Unknown, but an entirely empty record is not valid.

When created from a Missed appraisal screen warning, a manually added record inherits that warning's position and time in the scan sequence. It fills the identified gap without becoming an unlinked free-standing record.

**Unknown**:
A deliberate state meaning that the application does not know a value reliably. Unknown is distinct from a confidently detected negative value and must not be filled by guessing.


**Excluded record**:
A captured or manually added record that the user has intentionally removed from the normal snapshot result, for example because it is a duplicate or not a Pokémon. Exclusion is immediate but reversible: the record and evidence remain recoverable, ordinary exports omit it, and the UI offers Undo/Restore rather than a confirmation dialog.

**Record status**:
The current usability state of an individual Pokémon record: `READY` when no unresolved issue or Unknown field remains; `NEEDS_REVIEW` while a parser-derived issue still needs a user decision; `PARTIAL` when the user intentionally keeps known data while leaving one or more fields Unknown; or `EXCLUDED` when the user removes it from the normal result. In Review, **Save as partial** sets every remaining unresolved field in that record to Unknown and closes those issues, while preserving original parser observations and evidence. A manually added record with required species and optional Unknown numeric fields is saved directly as `PARTIAL`. `INCOMPLETE` describes a scan/snapshot, not an individual record.

Record status is presented to users as `Ready`, `Needs review`, `Partial`, or `Excluded`, with an icon, text label, and semantic color. `Missed appraisal` is a warning event rather than a record status, and `Confirmed by you` describes provenance rather than adding another status.

`Missed appraisal` uses the amber warning semantics because it signals a possible scan gap; red is reserved for actual failures and destructive actions.

**Incomplete scan**:
A scan session interrupted unexpectedly before normal finalization. Its persisted results remain available, and the user may resume it, finish it as a partial snapshot, or discard it explicitly. A user Stop finalizes the saved live result with `PARTIAL` scope instead.

**Snapshot lifecycle**:
The small lifecycle of a scan result: `PROCESSING` while capture/processing is active, `INCOMPLETE` after an unexpected or not-yet-finalized interruption, and `COMPLETE` after normal processing finishes or the user finalizes a recovered result. A complete snapshot may have unverified scope or warnings. Discarding removes the incomplete result instead of keeping a long-lived discarded state.

**Scope completeness**:
Whether a finalized snapshot reached a positively detected end of the intended traversal (`INTENDED_RANGE`) or was stopped before that point (`PARTIAL`). Scope completeness is separate from lifecycle and data quality.

**Snapshot quality summary**:
The derived picture of unresolved review issues, missed appraisal screens, partial records, and other warnings. It is represented by counts and warning types rather than a growing combined status enum.

**Scan summary**:
The post-capture overview that presents the snapshot identity, lifecycle label, scope completeness, record counts, and warning counts. Its primary action is Review issues when unresolved issues remain, otherwise Export; it does not imply perfect completeness from a single success count.

**Normal export**:
A CSV or JSON export containing every record except those explicitly marked Excluded. Ready, Needs review, and Partial records remain included, with Unknown values represented according to the selected format; MVP does not provide an implicit only-ready export mode.

**Live-capture notification state**:
The permission state controlling whether PokeAndScan can show its live-scan foreground-service notification in the notification drawer. It is requested only when the user first starts a live scan; denying it does not deny screen capture, but removes the drawer-based Stop action.

**Non-game screen**:
A captured screen state that is not a supported Pokémon GO appraisal screen, such as the launcher, another app, or a system surface. It is ignored for Pokémon records and may produce a user-facing warning while live capture continues.

Non-game screen content is transient only and is not retained as evidence. The session may retain an anonymized event timestamp without retaining the image.
