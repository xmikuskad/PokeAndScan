# Technical Design

**Product:** PokeAndScan
**Status:** Agreed MVP direction
**Date:** 2026-09-21

## Technical goals

- Make the parser deterministic, explainable, and testable.
- Keep capture source separate from scan logic.
- Preserve evidence for every uncertain or user-corrected field.
- Keep processing offline and memory-bounded.
- Leave a clean path from MP4 import to live capture.
- Avoid unnecessary full-frame analysis; expose progress and support background processing with durable checkpoints.
- Report determinate progress for recorded sources from decoded source position; report live-session metrics without inventing a total percentage.
- MP4 processing must use an Android execution path that can continue while the app is backgrounded and the screen is locked where platform rules permit, with durable checkpoints for interruption recovery.
- Run user-initiated MP4 processing in a dedicated foreground service started while PokeAndScan is foregrounded. Persist checkpoints; after interruption, mark the scan incomplete and do not automatically restart or resume processing. Continue only after an explicit user action.
- Supply the foreground-service notification required by Android. Do not request `POST_NOTIFICATIONS` solely for MP4 processing; keep in-app progress as the primary progress surface and explain that Android may expose service status through Task Manager instead of the notification drawer when permission is denied.
- Recovery is user-triggered rather than automatic: load the last checkpoint and offer Continue, Finish partial snapshot, or Discard.
- Enforce a single active capture/processing job in MVP. Snapshot reads, edits, review, and exports may continue for already finalized snapshots, but a second source job cannot start until the current job is finalized or discarded.

## Platform and technology choices

- Android application, Kotlin-first.
- Preserve the scaffold's `applicationId` and Kotlin namespace `com.falconsocka.pokeandscan` as the permanent app identity; `falconsocka` is the author's chosen namespace. Do not change it after public publication.
- Minimum supported Android version is Android 10 / API 29; the project targets the current Android SDK configured by the repository.
- Jetpack Compose for UI.
- Coroutines and Flow for asynchronous processing and progress.
- Room/SQLite for local persistence.
- Android Storage Access Framework for MP4 selection and exports.
- MP4 input supports `ACTION_OPEN_DOCUMENT`-style picker selection and a share/import intent that receives a content URI.
- Android MediaExtractor + MediaCodec for sequential video decoding.
- Android MediaProjection for the live source delivered in the second implementation stage of MVP.
- ML Kit Text Recognition v2 bundled Latin model for MVP OCR.
- Kotlin-native image geometry for the MVP appraisal-bar measurements; a small isolated OpenCV or equivalent component is a conditional fallback if fixture results justify it.
- No required backend, account, network, or cloud AI dependency.
- UI strings must be localized for Slovak (`sk`) and English (`en`). First launch selects `sk` only for a Slovak system; every other system language selects `en`. Persisted settings expose only these two explicit locales.

The dependency set should stay small until a real parser problem requires a larger library. The first implementation may use fewer Gradle modules than the eventual logical architecture.

### Approved OCR and computer-vision baseline

- Use the bundled ML Kit Text Recognition v2 Latin model for OCR of visible Pokémon names and CP.
- Implement appraisal-bar geometry in Kotlin using profile-owned normalized regions; do not add OpenCV to the baseline solely for these measurements.
- Keep OCR and image-analysis components behind narrow interfaces so an implementation can be replaced without changing capture sources, review, persistence, or export.
- All recognition remains on-device and offline. Cloud OCR or uploading captured game screens is not an acceptable fallback.
- Do not add an AI-model fallback to MVP, either on-device or cloud. If the approved OCR/CV baseline and any evidence-justified non-AI implementation still fail the quality gate, preserve uncertainty as Unknown/review rather than infer a value.

If fixture tests show unsatisfactory results, improve the current approach in this order:

1. Verify the reference profile, orientation normalization, stable-frame selection, and normalized ROI placement.
2. Improve crop quality and narrowly scoped image preprocessing, then retest against the same labeled fixtures.
3. For text recognition that still misses its quality gate, benchmark another bundled, on-device OCR engine behind the OCR interface. Adopt it only if it improves the labeled fixture and real-device pilot results without weakening offline/privacy requirements.
4. For appraisal-bar measurement that still misses its quality gate, benchmark an isolated computer-vision implementation, including OpenCV if appropriate, behind the image-analysis interface. Do not spread library-specific types or coordinates into domain or parser contracts.

No fallback may turn uncertain input into a confident value. Any replacement must pass the existing golden-fixture, replay, and real-device pilot gates, including verification of automatically exported non-Unknown values against ground truth. Candidate libraries and their licensing, APK size, device performance, and maintenance status are evaluated when a measured failure justifies that comparison; they are not preselected now.

## Supported MVP profile

The first supported profile is versioned and explicit:

~~~
profile-en-portrait-pixel9proxl-v1
device: Pixel 9 Pro XL
orientation: portrait
language: English
display/font settings: Android defaults
source: Android MP4 screen recording or live capture
~~~

The app locale is selected independently from the source profile. The source profile remains English Pokémon GO UI even when the PokeAndScan interface is Slovak.

This verified device profile defines MVP support only. Other Android devices may be install-compatible, but must not be represented as verified for scanning until an additional profile has been tested. Post-MVP compatibility expansion concerns additional Android phone models running Pokémon GO; supporting other game/apps is not in scope.

### Provisional post-MVP device-profile strategy

The current proposal is to expand verified phone support through versioned, tested device profiles. A profile may be shared by similar phone models only after tests confirm equivalent screen geometry and parser reliability; resolution alone is not sufficient evidence. This strategy is provisional, not a commitment to maintain a per-model profile system indefinitely.

Before broadening support, validate the approach on additional phones and review the effort to create, test, and maintain profiles. If profiles require many model-specific exceptions or become burdensome to keep current, simplify or abandon this strategy and narrow the verified-device list rather than accumulating brittle overrides. Do not claim untested devices as verified.

The parser must use normalized coordinates and profile-owned ROIs. Raw pixel rectangles must not be scattered through parser classes.

The canonical species/form dictionary is bundled and versioned with the application. A parser result stores the dictionary version used for normalization. Updating support for new species/forms is an application update, not a network dependency.

## Logical architecture

~~~
Capture source
  → FrameSource
  → frame sampler
  → orientation normalizer
  → screen classifier
  → stable-state selector
  → traversal-end detector
  → appraisal parser
  → canonical normalization
  → temporal deduplicator
  → evidence/review creation
  → Room repositories
  → snapshot/export
~~~

The parser must not know whether frames came from MP4 or MediaProjection.

## Capture abstraction

~~~
interface FrameSource {
    val metadata: FrameSourceMetadata
    suspend fun frames(): Flow<VideoFrame>
}
~~~

First MVP implementation:

~~~
RecordedVideoFrameSource
~~~

Second MVP implementation stage:

~~~
MediaProjectionFrameSource
~~~

One ScanSession has exactly one source. Switching from live to MP4 creates a new session.

## Core domain model

~~~
ScanSession
ScanPass
PokemonCandidate
PokemonRecord
FieldEvidence
ReviewIssue
Snapshot
~~~

The model must distinguish:

- the original parser observation;
- the canonical/current value;
- the user-confirmed correction;
- the evidence crop and source frame;
- the independent snapshot that owns the record.

Suggested MVP fields:

~~~
PokemonRecord
  localId
  snapshotId
  sequenceIndex
  speciesId?
  formId?
  cp?
  ivAttack?
  ivDefense?
  ivStamina?
  ivPercent?
  snapshotName
  provenance
  recordStatus
~~~

ivPercent is derived from the three integer IV values and is never an independent manually entered source field.

`snapshotName` is optional input from the user. When absent, the application generates and persists a date-based name with a prefix localized to the app language at creation (`Sken` or `Scan`), such as `Sken 20.12.2023` or `Scan 20.12.2023`. It does not infer the name from Pokémon GO filters or tags. Later app-language changes do not modify the stored name.

## Field confidence and evidence

Every extracted field carries internally:

~~~
value
confidence 0..1
method
source frame
ROI when available
raw value/evidence
~~~

Evidence retention policy:

- `READY` records retain one minimal representative appraisal crop;
- `NEEDS_REVIEW`, `PARTIAL`, nickname, and parser-warning fields retain relevant field crops;
- user corrections retain the original crop;
- missed appraisal events retain only a transition crop;
- full source video and non-game frames are never copied into evidence storage.

Unknown is a deliberate value. A low-confidence field creates a ReviewIssue; it does not become a guessed boolean or text value.

Manual review creates a USER_CONFIRMED value and preserves the original evidence.

For parser-derived fields, retain both the original observation and the current user-confirmed value so Review can offer `Reset to original detection`. For manually added records, current values are user-owned with no parser baseline. Exclusion/restoration operates at record level and is separate from field correction.

Record editing is available from snapshot detail for any included record. Editing reuses review validation and recalculates record status/derived IV percentage without deleting the original parser observation or evidence.

A manually added record has `provenance = MANUALLY_ADDED`, no parser observation, and no required source crop. Its editable values use the same canonical species and numeric validation rules as review corrections. Export must preserve the provenance marker.

Manual-add validation requires `speciesId`; `cp`, `ivAttack`, `ivDefense`, and `ivStamina` may be null/Unknown. CP, when present, is a non-negative integer, and each IV, when present, is an integer from 0 through 15. The form cannot submit without a species.

When created from `MissedAppraisalScreen`, the new record inherits the event's sequence position/time anchor and keeps a link to the warning. This supports deterministic ordering and prevents an added record from looking like an unrelated observation.

`recordStatus = EXCLUDED` is reversible. Excluded records retain their evidence and audit metadata but are filtered out of normal CSV/JSON exports. The status must not be implemented as physical deletion.

Record status semantics:

- `READY`: no unresolved issue remains and every MVP field has a known value;
- `NEEDS_REVIEW`: parser uncertainty or an unresolved review issue remains;
- `PARTIAL`: the user intentionally saved the record with one or more Unknown fields and no unresolved review issue remains. See the business rule for the user-triggered transition from `NEEDS_REVIEW`;
- `EXCLUDED`: user-excluded and omitted from normal exports.

`INCOMPLETE` is a session/snapshot status and must not be conflated with `recordStatus`.

Snapshot lifecycle is `SETUP`, `PROCESSING`, `INCOMPLETE`, or `COMPLETE`. `SETUP` reserves a resumable session identity before capture and does not count as an active job. Positive detection of the end of the intended live traversal automatically closes capture and finalizes the session as `COMPLETE` with `INTENDED_RANGE` scope. A user Stop before that detection finalizes the saved result as `COMPLETE` with `PARTIAL` scope after processing. An unexpected interruption leaves it `INCOMPLETE` until resume or explicit partial finalization. Discard removes the incomplete session and its local data, not a retained lifecycle state.

Store scope completeness separately as `INTENDED_RANGE` or `PARTIAL`. A positively detected live traversal end produces `INTENDED_RANGE`; an earlier user Stop produces `PARTIAL`. Finishing an MP4 proves file completion but does not itself establish `INTENDED_RANGE`. Store warning counts/types and record-status counts separately. Do not introduce combined states such as `COMPLETE_WITH_WARNINGS` or `INCOMPLETE_NEEDS_REVIEW`; derive those UI labels from the orthogonal fields.

Review queue ordering is deterministic: primary order is scan sequence position/time anchor, then field priority `species/form`, `cp`, `ivAttack`, `ivDefense`, `ivStamina`. A skipped issue remains unresolved and is re-queued after the current pass.

Successful live traversal completion is automatic after a positive end signal; it closes capture, drains remaining work, and finalizes `COMPLETE` with `INTENDED_RANGE` scope. `Stop` closes capture early and finalizes a `PARTIAL` scope result after processing. Neither path needs a separate completion confirmation. Inactivity, an unrecognized screen, and a missing next stable appraisal state are not end signals. There is no independent pause state; checkpointing and `INCOMPLETE` recovery handle unexpected interruptions and later continuation.

## Appraisal pipeline

1. Decode a frame and normalize orientation.
2. Classify the screen as UNKNOWN or APPRAISAL.
3. Ignore motion/transition frames.
4. Wait for a stable appraisal state.
5. OCR the species and CP ROIs.
6. Normalize species/form against the bundled English canonical dictionary.
7. Measure the three appraisal bars geometrically and quantize each IV to 0..15.
8. Select a frame-quality winner from the stable window.
9. Emit one candidate with evidence and confidence.
10. Use transition evidence and content fingerprints to suppress repeated frames.

The traversal contract for the MVP is continuous in-screen navigation. The user opens Appraise on the first Pokémon and then navigates to subsequent Pokémon inside Pokémon GO without returning to the storage list after each record. A stable appraisal state following a meaningful navigation transition is eligible to emit one candidate; transition frames and repeated stable frames are ignored.

Automatic live completion requires a positive, profile-specific end-of-traversal signal validated against real reference-device footage. Capture and label examples of the last Pokémon, normal pauses, failed/too-fast navigation, and non-game screens before implementing the detector. If a reliable signal cannot be demonstrated, do not substitute a timeout or unchanged screen; raise the missing signal as an MVP blocker for product review while keeping Stop and partial results functional.

The first version must prefer a review issue over a fabricated record. A missing species does not invalidate trustworthy CP/IV values.

If the visible name appears to be a nickname or cannot be normalized to the canonical dictionary, preserve the raw text as field evidence, set species/form to Unknown, and create a review issue. Do not reject the whole candidate when CP/IV extraction is trustworthy.

If a meaningful navigation transition is detected but no stable appraisal state follows within the allowed settling window, emit a `MissedAppraisalScreen` warning with a timestamp and available evidence. Do not emit a `PokemonCandidate` for that event. The warning remains linked to the session and can guide manual addition or a later user-initiated rescan. Any reprocessing after finalization requires the user to select the source MP4 again because the app has released access.

## Temporal identity and deduplication

The system counts stable content states after meaningful navigation, not source frames.

- Same stable state without a transition: duplicate frame.
- Visible transition followed by a new stable state: new individual candidate, even if all values match.
- Alphabetical ordering is validation evidence only.
- A sequence anomaly lowers confidence or creates a warning; it never deletes a record.

## Persistence and storage

- Room stores sessions, snapshots, candidates, records, evidence metadata, and review issues.
- Continuing New scan or leaving a modified setup for the Library reserves one independent snapshot/session identity in `SETUP` and persists its ordinary display name, selected source, and intended scan scope. An unfinished filtered-subset description may remain blank until the user continues. `SETUP` is resumable and does not count as an active capture/processing job. The intended scope type is whole collection or a user-entered filtered-subset description; PokeAndScan never reads the game's filter/tag name.
- Starting capture/processing atomically transitions one setup to `PROCESSING` only when no other `PROCESSING` or recoverable `INCOMPLETE` job exists. Finalized snapshots stay available while that single-job gate blocks another start.
- Persisted setup fields are added through a Room migration so earlier local snapshots keep their identity and data.
- Evidence crops are stored in app-private storage and referenced by evidence metadata.
- Android cloud Auto Backup and device-to-device transfer must exclude all PokeAndScan app data in MVP, including databases, preferences, and evidence files. Set `allowBackup` explicitly and configure both legacy full-backup rules and Android 12+ cloud/device-transfer extraction rules so behavior is not left to platform defaults. Verify behavior on the reference device and supported Android range.
- The selected MP4 URI is processed without copying the entire source video into the app by default.
- Persist URI access only while processing or while the scan is incomplete and may need recovery. Release it when the scan is finalized or discarded; if access is lost earlier, mark the source unavailable and ask the user to select the recording again.
- Any reprocessing after finalization requires the user to select the source MP4 again. Releasing URI access never deletes or modifies the user's original file.
- Import accepts an MP4 URI with a decodable video track; audio tracks are ignored.
- The decoder may use any codec supported by the target Android device. Decode failure produces an actionable unsupported-recording error.
- A preflight validates orientation, dimensions/aspect ratio, and profile compatibility before full processing. Profile-specific tolerance determines whether a deviation is rejected or allowed with a warning.
- Profile-specific preflight thresholds are calibration data, not implementation guesses. Establish them against labeled reference-profile fixtures before implementing `Supported`, `Supported with deviations`, and `Unsupported` classifications. Until then, do not claim an unverified layout is Supported; report only facts the app can verify and leave the classification gate open.
- Current fixture availability, missing evidence, and calibration status are recorded in [MP4 reference-profile calibration](calibration/mp4-profile-calibration.md).
- Deleting a snapshot deletes its records, review issues, and evidence crops together.
- Older snapshots remain independent and selectable for export.
- Results are checkpointed during processing so an interrupted session can resume from the last safe position or be finalized as a partial snapshot.
- Discarding an incomplete session is an explicit user action and removes its local records/evidence according to the same atomic deletion rule as a snapshot.
- A completed snapshot is not locked by export. Edits create a new current state while prior field observations and user-change history remain retained; existing exported files are external point-in-time artifacts.
- Renaming a snapshot updates only its display name and future export filename derivation; `snapshotId`, records, and existing exported files are unchanged.
- MVP does not implement a separate delete-all-data command. Per-snapshot deletion is supported; full app-data clearing is delegated to Android system settings.

## Live capture requirements

The live source is the second implementation stage of MVP and must:

- use explicit user consent;
- run in a properly declared media-projection foreground service;
- show a persistent notification;
- provide a Stop scan notification action;
- use a bounded processing queue;
- survive ordinary interruption as a partial session where possible;
- never inject input into Pokémon GO;
- never require an overlay for recognition.

MediaProjection frames are not assumed to belong to Pokémon GO. The screen classifier must route launcher, other-app, system, and permission-dialog frames to a non-game/unknown state. These frames do not create candidates; live capture may continue until the user stops it.

Non-game frames must be discarded after in-memory classification. They must not be written to evidence storage, logs, crash payloads, or diagnostics bundles. A session may retain only an event type and timestamp for the warning.

On Android versions with runtime notification permission, request `POST_NOTIFICATIONS` at the first live-scan start, after the user has chosen the live source. If denied, continue MediaProjection capture when platform requirements permit it, but surface the degraded control path: stopping from PokeAndScan or Android Task Manager instead of the notification drawer.

## MP4 background execution

The user starts MP4 processing from the foreground app. A dedicated foreground service owns the immediate processing run, publishes required ongoing service status, and updates durable checkpoints. Do not use WorkManager as the MVP processing-run owner. Use `dataSync` for local file processing on Android 10–14 and the dedicated `mediaProcessing` type on Android 15/API 35 and newer. This API mapping is approved for MVP; implementation must still validate manifest declarations and runtime behavior against the target SDK and Android versions.

The service is not an automatic-resume mechanism. On interruption or process loss, reconcile any orphaned `PROCESSING` session from persisted state to `INCOMPLETE` and wait for the user to choose Continue, Finish partial snapshot, or Discard. The in-app progress surface remains primary. Do not prompt for notification permission solely to process an MP4; if notification permission is denied, explain the system's available service-status/stop surface while preserving processing and in-app controls where Android permits.

Platform basis: Android classifies local file processing as a `dataSync` use case and provides `mediaProcessing` for time-consuming media work beginning with Android 15. An FGS must include a notification; on Android 13+ the runtime notification permission is not required to start the service, although denied notifications are not shown in the notification drawer and service status remains available in Task Manager. See [Android foreground-service types](https://developer.android.com/develop/background-work/services/fgs/service-types) and [notification runtime permission](https://developer.android.com/develop/ui/compose/notifications/notification-permission).

## Export contract

MVP exports:

- UTF-8 CSV for practical spreadsheet use;
- versioned JSON for structured interchange.

User-facing exports contain final values, record status, provenance, and stable local IDs. Raw confidence, OCR, ROI coordinates, parser methods, and crops belong to a later optional diagnostics bundle, not the normal export. Internal confidence remains available for review prioritization and parser diagnostics.

Unknown representation is format-specific and explicit:

- CSV uses the literal `UNKNOWN` so an unknown value is not confused with an accidentally empty cell.
- JSON uses `null`.
- Both formats include `recordStatus` and `provenance` for each record.
- Export field names, canonical species/form names, and technical enum values are always English and are independent of the selected UI locale.
- Capture and import process video frames only. Audio tracks are ignored, and live capture does not request microphone or system-audio access.

Minimum record columns/fields are `localId`, `sequenceIndex`, `species`, `form`, `cp`, `ivAttack`, `ivDefense`, `ivStamina`, `ivPercent`, `recordStatus`, and `provenance`. Excluded records are omitted from normal exports.

JSON is the complete interchange format and includes snapshot metadata (`snapshotLifecycle`, `scopeCompleteness`, warning counts, and missed-appraisal events). CSV is intentionally row-oriented and contains Pokémon records only; the export flow must warn before producing CSV when unresolved warnings or gaps remain.

Every JSON export has a top-level integer `schemaVersion`; MVP emits `1`. Increment this value for an incompatible change to field shape or meaning. Compatible additions that consumers may ignore do not increment it. CSV does not include a version column; its ordered header is the documented format contract.

Wall-clock timestamps in JSON use ISO 8601 UTC. Source positions are separate integer `sourceOffsetMs` values measured from the beginning of the MP4 or live-capture session. Derive MP4 offsets from the decoded source presentation timeline and live offsets from elapsed capture time; do not use device wall-clock time for event ordering. Records and missed-appraisal events retain their source offset when available.

The export filename is derived from `snapshotName` and receives `.csv` or `.json`. The filename sanitizer removes or replaces invalid filesystem characters. The Android save/share flow must request overwrite confirmation rather than silently replacing an existing file.

`ivPercent = (ivAttack + ivDefense + ivStamina) / 45 * 100` is derived only when all three IVs are known. Otherwise `ivPercent` is Unknown (`UNKNOWN` in CSV, `null` in JSON). Export formatting uses one decimal place.

## Testing strategy

Required before MVP completion:

- unit tests for species normalization and numeric validation;
- golden image tests for appraisal screen detection and IV geometry;
- video replay tests for stable-state selection and deduplication;
- Room and export round-trip tests;
- interruption/progress tests;
- checkpoint and resume tests for interrupted MP4 processing and incomplete-session recovery;
- background-processing and progress-reporting tests;
- fixtures for clean, transitional, ambiguous, and unsupported frames;
- fixtures for too-fast navigation with a missed stable appraisal state;
- reference-device replay fixtures proving automatic end detection and rejecting pauses, failed navigation, and other-app screens as false ends;
- a real pilot of at least 100 Pokémon on the reference device, aiming for 200 to cover a longer traversal;
- a live-capture pilot using the same parser contract as MP4 import.

The quality gate prioritizes absence of silent wrong values over fill rate. A field may remain Unknown or require review; it must not be exported as a confident value unless it agrees with manually verified ground truth.

## Intentionally deferred implementation and roadmap items

These are not unresolved MVP product choices; each is deferred until the stated evidence or scope makes it actionable.

### Evidence-dependent MVP follow-up

- Select an OCR or computer-vision fallback only if labeled fixture and reference-device results show that the approved baseline misses its quality gate. Until then, keep the baseline and manual review/Unknown behavior.
- Set precise processing-throughput targets after the real pilot. Exact duration targets are intentionally not being specified during this design session.

### Post-MVP validation or roadmap candidates

- Reassess whether versioned device profiles are maintainable after testing additional Android phone models; simplify or abandon the strategy if it proves burdensome.
- Nickname and status-flag detectors, as already placed in the reliability-and-coverage phase.
- Moves/items/metadata pass matching, XLSX, and full snapshot backup/restore formats, as later exporter candidates rather than MVP commitments.
- An optional on-device AI approach may be reconsidered after MVP as a separate feature; it is not an MVP recognition fallback or a committed roadmap item.
