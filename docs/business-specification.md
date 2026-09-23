# Business Specification

**Product:** PokeAndScan
**Status:** Agreed MVP direction
**Date:** 2026-09-21

## Product definition

PokeAndScan is an independent Android companion that reads user-visible Pokémon GO appraisal screens and turns them into a local, reviewable export. It observes pixels from a user-controlled screen capture; it does not integrate with the game process or private services.

The preferred workflow is live screen capture. Importing an Android MP4 screen recording is the fallback and the first implementation path because it is deterministic and replayable.

## Target users

### MVP

The MVP is for the project owner and a small group of technically comfortable Android users who can follow a preparation checklist and correct uncertain values.

### Future public version

Version B is a Google Play-ready Android application for broader users. It will expand device/profile coverage and add more scan passes only after the appraisal workflow is reliable.

## User problem

Users with large Pokémon collections want a structured export of CP and IV data without manually transcribing every Pokémon. A scan can be imperfect, so the product must expose uncertainty and let the user correct it.

## MVP promise

> Prepare Pokémon GO once, scan the appraisal screens, review the few uncertain fields, and export a trustworthy local snapshot.

## MVP scope

Included:

- Android application only;
- Android 10 / API 29 or newer;
- Pixel 9 Pro XL reference device;
- MVP verification is limited to that reference setup; installing on another Android device does not imply verified scan support;
- post-MVP goal: verify additional Android phone models for Pokémon GO;
- portrait orientation;
- English Pokémon GO UI;
- Slovak and English PokeAndScan UI;
- default Android display size and font size;
- live capture as the preferred user workflow, delivered within MVP;
- MP4 Android screen-recording import as fallback;
- appraisal pass only;
- species, CP, Attack IV, Defense IV, and Stamina IV;
- canonical species identity with supported named forms where visible;
- canonical species selection during review;
- manual correction of species, CP, and all three IVs;
- manual addition of a missed Pokémon record;
- internal parser confidence, record status, and retained source crops;
- local offline processing with no account or server;
- CSV and JSON export;
- independent local snapshots and snapshot history;
- partial exports with explicit warnings;
- launch Pokémon GO through a separate user action;
- live-capture stop action from the system notification;
- resumable or partial session handling after interruption.

## Explicitly outside MVP

- automatic taps, swipes, gameplay, or overlays;
- Pokémon GO credentials, private APIs, packet interception, process access, root, or hooking;
- moves, items, physical metadata, catch metadata, status flags, or nickname extraction;
- iOS recordings;
- general device support;
- XLSX export;
- AI-based recognition fallback, whether on-device or cloud; uncertain MVP values remain Unknown/reviewable instead of being inferred by an AI model;
- remote processing or uploading captured screens;
- automatic merge between scan sessions;
- copying and retaining the complete source video by default.

## Business rules

1. A row represents one individual Pokémon, not a species aggregate.
2. Two visually identical Pokémon remain separate when the user navigated to them separately.
3. Repeated frames of one stable screen must not create duplicate records.
4. A later scan creates a new snapshot and never silently overwrites or merges an earlier one.
5. Unknown is different from false and from an empty string.
6. The parser must not silently guess a value. Uncertain data becomes a review issue.
7. A partially recognized record remains exportable with an explicit warning.
8. Manual corrections are exported as user-confirmed values while the original observation remains available for diagnostics.
9. Nicknames are unsupported in MVP and must be called out in preparation guidance.
10. An intentional in-game filter or tag is part of the user's scan scope; accidental filters must be removed before scanning.
11. Results are persisted during processing. An interruption creates an incomplete scan rather than silently losing recognized records.
12. On return, the user chooses whether to resume, finish as a partial snapshot, or discard the incomplete scan.
13. If no stable appraisal state is captured after a navigation event, the application creates a visible Missed appraisal screen warning and never invents a Pokémon row.
14. The user may create a new record manually from a missed-screen warning. The record is explicitly marked as manually added and never presented as parser-detected.
15. A manually added record requires a canonical species. CP and each IV are optional and may remain Unknown; an entirely empty record is invalid.
16. A manually added record created from a missed-screen warning occupies that warning's position in the snapshot sequence and retains its time relationship to the source.
17. The user may immediately exclude an incorrect or duplicate record without physically deleting its evidence. Exclusion is reversible through Undo/Restore; excluded records are omitted from normal exports.
18. On first launch, PokeAndScan selects Slovak for a Slovak system and English for an English or unsupported system. Settings offers only Slovak and English; there is no System default option. This does not change the English-only Pokémon GO parser profile.
19. Normal CSV exports represent Unknown as the literal `UNKNOWN`; JSON exports represent Unknown as `null`. Both formats include record status and provenance.
20. IV percentage is calculated only when Attack, Defense, and Stamina IV are all known. Otherwise it is Unknown; when calculated, it is displayed with one decimal place.
21. MP4 import requires an MP4 container with a video track. Audio is ignored, and the app may use any video codec that Android can decode on the device. MOV/iOS recordings are outside MVP.
22. A significantly unsupported video layout is rejected during preflight. A minor profile deviation may continue with a persistent warning and must not be presented as fully verified. Profile-specific thresholds must be calibrated against labeled reference-profile fixtures before implementation classifies layouts; an agent must not invent numeric tolerances or label an unverified layout Supported.
23. A nickname that prevents species recognition does not block the scan. The record may retain trustworthy CP/IV values with species Unknown until manual review.
24. Canonical identity may distinguish a clearly named in-game form from the base species. Costume, shiny, and event visual variants are outside MVP fields.
25. The canonical species/form dictionary is versioned and bundled offline. Unknown species/form text remains evidence and review data, but free-form canonical species values are not allowed.
26. Parser confidence is an internal quality signal used for review prioritization and diagnostics. Normal CSV/JSON exports expose record status and provenance instead of raw confidence scores.
27. Export filenames are derived from the snapshot name with the selected extension. Invalid filesystem characters are sanitized, and overwriting an existing file requires confirmation.
28. A snapshot remains editable after completion and export. Corrections, manual additions, and exclusions affect future exports but never silently modify an existing file.
29. CSV exports contain Pokémon rows only. JSON exports additionally contain snapshot status, warning counts, and missed-appraisal events. CSV export shows a warning before proceeding when unresolved gaps exist.
30. MVP provides per-snapshot deletion but does not duplicate Android's system-wide Clear storage action. Complete app-data deletion is handled through Android Settings.
31. Notification permission is requested only when the user first starts a live scan, not during onboarding. Denying it does not block live capture, but limits notification-drawer controls and must be explained.
32. Live capture may observe the whole device screen. Non-game screens never produce Pokémon records; leaving Pokémon GO creates a warning but does not automatically stop the scan.
33. Non-game screen images are never retained. Only an anonymized time-based warning may remain; evidence crops come from appraisal or relevant transition states.
34. The snapshot name may be changed after creation. Rename updates the library label and future export filenames, but never changes the snapshot ID or existing exported files.
35. Record status is separate from snapshot lifecycle: `READY`, `NEEDS_REVIEW`, `PARTIAL`, and `EXCLUDED` describe records; `PROCESSING`, `INCOMPLETE`, and `COMPLETE` describe the snapshot lifecycle.
36. Snapshot completion is independent of data quality. When the app positively detects the end of the intended live traversal, it automatically finalizes the snapshot as `COMPLETE` with `INTENDED_RANGE` scope, even when warnings remain. If the user stops live capture before that detection, it finalizes the saved result with `PARTIAL` scope. An unexpected interruption leaves it `INCOMPLETE` until recovery.
37. Scope completeness is separate from lifecycle: `INTENDED_RANGE` means the intended traversal was positively detected as finished; `PARTIAL` means the user ended capture before that point or only a recovered portion was finalized. Reaching the end of an MP4 proves only that the selected file was processed, not that the intended in-game range was covered; do not infer `INTENDED_RANGE` from end-of-file alone.
38. Warning counts and types are derived quality information, not combined lifecycle states. Discarding an incomplete scan removes it instead of preserving a long-lived `DISCARDED` snapshot.
39. Review issues are presented in scan sequence order. Within one record, review follows species/form, CP, Attack IV, Defense IV, and Stamina IV. Skipping defers an issue; it does not resolve or delete it.
40. Stop manually ends an unfinished live scan as a partial result; it is not a Pause action. A successfully detected traversal end finishes automatically, without user confirmation. Mere inactivity or failure to recognize the next screen is not proof of completion.
41. Video processing must be efficient enough for practical use, show visible progress, and continue when the user leaves the app where Android permits. Exact throughput targets are deferred until the real pilot.
42. MP4 processing exposes determinate progress based on source position. Live capture exposes elapsed time and detected/review counts instead of a completion percentage because its intended length is unknown.
43. MP4 can enter through the in-app Android file picker or Android Share flow. The app processes the selected URI without copying the complete video by default.
44. PokeAndScan keeps access to the selected MP4 only while processing or while the scan remains incomplete and may need recovery. It releases access when the scan is finalized or discarded. Any later reprocessing requires the user to select the source video again; the app never deletes the source file.
45. Evidence retention is proportional to review value: clean records keep a minimal appraisal crop, uncertain/partial/corrected fields keep relevant field crops, and missed events keep only a transition crop. Full source video and non-game frames are never retained by the app.
46. Parser-derived corrections can be reset to the original observation. Manually added records have no parser value to restore; excluded records use a separate Restore action.
47. Any included record can be edited after scanning, not only records in the review queue. The same validation, provenance, and evidence-preservation rules apply.
48. MP4 processing should continue when PokeAndScan is backgrounded or the screen is locked, subject to Android execution rules. Checkpoints must make system interruption recoverable without losing processed results.
49. After an interruption, processing does not resume automatically. The user chooses Continue, Finish partial snapshot, or Discard from the recovered checkpoint.
50. Background MP4 processing uses a required Android foreground-service notification, but PokeAndScan does not request runtime notification permission solely for MP4 processing. In-app progress remains primary; if notification permission is denied, Android may show service status only through Task Manager, and processing continues where platform rules permit.
51. MVP CSV/JSON export is not a full application backup. Snapshot history, evidence crops, and review history are not restorable from normal exports; versioned backup/restore is deferred.
52. MVP permits only one active capture or processing job at a time. Existing snapshots remain available for review, editing, and export while no new job is active.
53. CSV and JSON exports are always English and stable regardless of the app UI language: field names, canonical species/form names, and technical status values are not localized.
54. Capture is video-only. PokeAndScan does not request, capture, process, or retain microphone or system audio.
55. MVP supports both dark and light visual themes. Dark remains the primary brand direction, but light is a supported first-class experience.
56. Theme settings offer `System default`, `Light`, and `Dark`; this visual preference is independent of app language, parser profile, and export content.
57. Android Auto Backup and device-to-device system transfer are disabled for PokeAndScan's app data in MVP. Snapshot history, review history, and evidence remain local; only user-initiated CSV/JSON exports leave the app's managed storage.
58. JSON exports include a top-level integer `schemaVersion`, starting at `1`; increment it when a future schema change is incompatible with the existing contract. CSV has no embedded schema-version field and follows its documented header contract.
59. JSON wall-clock timestamps use ISO 8601 UTC. Positions of records and warning events in a scan use a separate integer source offset in milliseconds from the beginning of the MP4 or live capture; they do not depend on the device's wall clock or time zone.
60. An auto-generated snapshot name localizes its prefix (`Sken`/`Scan`) using the app language at creation time. The generated name is stored as an ordinary snapshot name and is not automatically translated or renamed if the app language later changes.
61. In Review, the user may explicitly accept a parser-derived record as partial. This sets every remaining unresolved field in that record to Unknown, closes those review issues, preserves the original parser observations and evidence, and sets the record status to `PARTIAL`. The action does not alter fields the user has already confirmed. `Skip` leaves the current issue unresolved and the record at `NEEDS_REVIEW`. A manually added record with required species and optional Unknown numeric fields is saved directly as `PARTIAL`.

## Primary user journey

1. User opens PokeAndScan and starts a new appraisal scan.
2. User enters an optional snapshot name, chooses the scan scope, and selects live capture or MP4 import. Live is presented first.
3. The app shows the preparation checklist.
4. For live capture, the user grants Android screen-capture permission, taps Open Pokémon GO, and manually moves through the appraisal screens. For MP4, the user selects a recording, completes preflight, and starts processing.
5. Live capture ends automatically when the app positively detects the end of the intended traversal; an earlier user Stop saves a partial range. MP4 processing ends when the selected recording has been processed, but reaching its end does not prove that the intended in-game range was covered.
6. The app shows a scan summary.
7. User reviews uncertain records, corrects values, or explicitly accepts a record as partial when remaining fields should stay Unknown.
8. User exports CSV or JSON for the selected snapshot.

## Confirmed appraisal traversal protocol

The supported MVP traversal is continuous in-screen navigation:

1. The user opens the first Pokémon in storage.
2. The user opens **Appraise**.
3. The user moves to the next Pokémon using Pokémon GO's in-screen navigation.
4. The user waits for the appraisal screen to settle before moving again.
5. The user repeats this until the app detects the end of the intended traversal and completes the scan automatically. If the user chooses Stop first, the result is partial.

The user does not return to the storage list between every Pokémon. One settled appraisal state after a meaningful navigation transition represents one individual Pokémon. The same visible values on two successive Pokémon do not make them the same record.

The scan scope is chosen by the user in Pokémon GO before capture. It may be the whole collection or a filtered subset, such as Pokémon carrying a selected in-game tag. PokeAndScan processes the sequence exposed by that scope; it does not need to know the tag's name to produce the appraisal snapshot.

Before capture, the user may enter a snapshot name. If they leave it blank, PokeAndScan generates a date-based default localized to the current app language, such as `Sken 20.12.2023` or `Scan 20.12.2023`. The generated name is then stored and does not change with later language changes. The name is shown in snapshot history, snapshot detail, and export metadata.

If a scan is interrupted, PokeAndScan preserves the already processed results and marks the scan as incomplete. The user can resume it, finish the current results as a partial snapshot, or discard it explicitly. The application must not silently discard persisted records.

## MVP completion gate

The MVP is complete when it can process a real pilot of at least 100 Pokémon on the reference Pixel 9 Pro XL (aim for 200 to cover a longer traversal) and:

- never silently invents an uncertain value;
- every automatically exported non-Unknown value is verified against the pilot ground truth;
- keeps identical individuals separate after a visible navigation transition;
- suppresses repeated stable frames;
- exposes missed or incomplete records as warnings/review issues;
- surfaces navigation gaps as Missed appraisal screen warnings;
- automatically completes a live traversal only after a validated end signal, while manual Stop preserves a partial result;
- allows correction of every MVP field;
- allows incorrect or duplicate records to be excluded and restored;
- produces usable UTF-8 CSV and JSON exports;
- preserves evidence for reviewed fields;
- operates without network access.

Clean fixture accuracy and temporal deduplication must be measured with automated tests, not inferred from the pilot alone.

## Product roadmap

### Phase 1 — MVP foundation

MP4 import, appraisal parser, review, CSV/JSON, offline snapshots, Pixel 9 Pro XL profile, and the shared source-independent pipeline.

### Phase 1b — MVP live capture completion

Android MediaProjection live capture, foreground service, active notification where available, stop/recovery handling, live progress metrics, and the same appraisal parser used by MP4 import. MVP is not complete until both MP4 and live workflows pass the acceptance gate.

### Phase 2 — Reliability and coverage

Additional Android device profiles, better transition diagnostics, nickname support, and selected status flags.

### Phase 3 — Broader exporter

Moves, items, metadata, XLSX, additional profiles, and other recovery workflows.

## Product trust boundary

PokeAndScan is an independent companion/export tool. Product copy must not imply affiliation with Pokémon GO, Niantic, Scopely, or The Pokémon Company. The app must describe its input as user-visible screen content and its output as a locally generated snapshot.
