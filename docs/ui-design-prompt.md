# Prompt for external UI design exploration

> Historical prompt used to generate external UI exploration. For current rules, follow the [agent document router](../AGENTS.md), especially the [design specification](./design-specification.md). Later decisions supersede exploratory details in this prompt.

Copy the prompt below into the design-oriented AI tool.

---

You are a senior Android product designer creating a visual UI concept for **PokeAndScan**.

PokeAndScan is an independent, unofficial Android companion that reads user-visible Pokémon GO appraisal screens and produces a trustworthy local export. It never automates Pokémon GO, never taps or swipes on behalf of the user, never uses game credentials/private APIs/root/overlays, and never uploads data to a server.

The product is being designed for a Pixel 9 Pro XL reference device in portrait orientation. MVP supports Android 10+, Slovak and English UI, light and dark themes, offline processing, MP4 Android screen-recording import, and live Android screen capture. The MVP must include both capture modes, although MP4 is implemented and validated first.

## Product promise

Prepare Pokémon GO, scan a selected collection range, review uncertain data, and export a trustworthy local snapshot.

The user may scan the whole collection or a deliberate in-game subset, such as Pokémon with a selected tag. The app does not need to know the tag name. The user opens the first Pokémon, opens Appraise, then uses Pokémon GO's in-screen navigation to move continuously through the selected range. They do not return to the storage list after every Pokémon.

## MVP data

Extract:

- canonical species;
- supported named form when clearly identifiable;
- CP;
- Attack IV;
- Defense IV;
- Stamina IV;
- derived IV percentage when all three IVs are known.

Costumes, shiny status, event variants, nicknames as a separate export field, moves, items, metadata, and status flags are outside MVP. If a nickname blocks species recognition, keep the record with species Unknown and send it to review.

## Important trust rules

- Never silently guess a value.
- Unknown data must be visible as Unknown or Needs review.
- Prefer an incomplete result over a confidently wrong result.
- Identical visible values can still belong to different individual Pokémon.
- Repeated stable frames must not create duplicates.
- A missed stable appraisal screen becomes a visible Missed appraisal screen warning, not a fabricated Pokémon record.
- The user can manually add a Pokémon from a missed-screen warning. Species is required; CP and each IV are optional. The record is marked Manually added and placed at the missed event's sequence position.
- The user can edit any included record after scanning.
- Parser-derived corrections can be reset to the original detection.
- Incorrect or duplicate records can be excluded and later restored.
- Evidence crops support review, but the full source video and non-game screen images are never retained.

## Capture flows

### Live capture

1. User starts a new appraisal scan.
2. User enters an optional snapshot name. Default example: `Scan 20.12.2023`.
3. User chooses Live capture.
4. On the first live start only, request Android notification permission when applicable. Do not request it during onboarding.
5. Request Android screen-capture consent.
6. Show a separate explicit button: **Open Pokémon GO**.
7. User manually prepares Pokémon GO and navigates through the appraisal screens.
8. PokeAndScan captures screen video only — no microphone and no system audio.
9. User stops using PokeAndScan or the foreground-service notification when available.

MediaProjection may observe the whole device screen while active. Explain this clearly. If the user leaves Pokémon GO, show a non-blocking warning, ignore the image for Pokémon records, and do not retain the non-game frame. Do not use overlays.

If notification permission is denied, live capture still works when MediaProjection consent succeeds. Explain that notification-drawer Stop is unavailable; stopping from PokeAndScan or Android Task Manager remains possible.

### MP4 import

Support:

- in-app Android file picker;
- Android Share flow from Files or Gallery;
- MP4 container with a video track;
- any video codec Android can decode on the device;
- audio ignored.

MOV/iOS recordings are outside MVP. Run a preflight before full processing. Reject significantly unsupported layouts; allow minor profile deviations only with a persistent warning.

## Required screens and states

Design a coherent Android flow for these screens:

1. Welcome / first-run onboarding
   - what the app does;
   - offline/local privacy promise;
   - manual Pokémon GO control boundary;
   - uncertainty and review promise;
   - unofficial companion disclaimer.

2. Capture explanation
   - Live capture, Recommended;
   - Import MP4 recording;
   - simple explanation of whole-screen capture and privacy.

3. Supported setup
   - Pixel 9 Pro XL reference profile;
   - portrait;
   - English Pokémon GO UI;
   - default Android display/font size;
   - no nicknames recommended for the pilot;
   - other devices may be unsupported.

4. New scan / snapshot setup
   - optional snapshot name;
   - appraisal scope;
   - Live capture or Import MP4;
   - only one active capture/processing job at a time.

5. Preparation checklist
   - set Pokémon GO to English;
   - portrait orientation;
   - default display/font settings;
   - choose whole collection or deliberate tag/filter scope;
   - clear accidental filters;
   - sort by Name A→Z where practical;
   - open first Pokémon and Appraise;
   - navigate continuously and wait for the appraisal screen to settle.

6. Live transition screen
   - screen-capture consent;
   - separate Open Pokémon GO button;
   - clear active-capture explanation;
   - no overlay controls.

7. MP4 preflight and processing
   - file metadata;
   - supported/unsupported profile result;
   - determinate progress bar based on video position;
   - current processing stage;
   - detected candidates;
   - review issues;
   - missed-screen warnings;
   - Stop/Cancel only, no Pause;
   - processing can continue in background and while screen is locked where Android permits;
   - optional progress notification without making notification permission mandatory.

8. Live active scan
   - elapsed time;
   - detected candidate count;
   - review count;
   - missed/non-game warnings;
   - current state;
   - no fake percentage because the intended range length is unknown;
   - Stop only.

9. Interrupted processing recovery
   - `Continue processing`;
   - `Finish partial snapshot`;
   - `Discard`;
   - never resume long-running processing automatically.

10. Summary
    Use simple user-facing labels derived from separate lifecycle and quality data:
    - Processing;
    - Incomplete;
    - Complete;
    - Complete with warnings;
    - Partial snapshot.

    Show candidate count, ready count, partial count, review count, missed-screen count, excluded count, and clear next actions.

11. Review queue
    - one issue at a time;
    - scan sequence order;
    - fields ordered: species/form, CP, Attack IV, Defense IV, Stamina IV;
    - evidence crop;
    - original detection;
    - edit controls;
    - Confirm;
    - Skip, which defers rather than resolves;
    - Reset to original detection when available;
    - Exclude record and Restore excluded record.

12. Manual add from missed event
    - species required from canonical dictionary;
    - CP and IVs optional;
    - empty numeric fields become Unknown;
    - provenance visibly marked Manually added;
    - record inserted at missed event position.

13. Snapshot library
    - independent snapshots, never a merged master collection;
    - snapshot name;
    - date/time;
    - source type;
    - lifecycle label;
    - scope completeness;
    - candidate/review/partial/warning counts;
    - rename;
    - edit;
    - export;
    - delete snapshot.

14. Snapshot detail / record edit
    - edit any included record;
    - original detection versus user-confirmed values;
    - evidence crop;
    - reset detection when possible;
    - exclude/restore.

15. Export
    - CSV and JSON;
    - Android Save/Share flow;
    - filename derived from snapshot name;
    - CSV rows only;
    - JSON includes snapshot metadata, lifecycle, scope completeness, warning counts, and missed-appraisal events;
    - CSV uses literal `UNKNOWN`;
    - JSON uses `null`;
    - excluded records omitted;
    - export content always English, regardless of UI language;
    - clearly warn before CSV export if unresolved issues or gaps remain.

16. Settings
    - Language: only `Slovenčina` and `English`; no System default option;
    - Theme: `System default`, `Light`, `Dark`;
    - About and privacy;
    - Developer diagnostics.

## State-model constraint

Avoid a huge state matrix. Keep the visual language simple and derive labels from separate concepts:

- snapshot lifecycle: `PROCESSING`, `INCOMPLETE`, `COMPLETE`;
- scope completeness: `INTENDED_RANGE` or `PARTIAL`;
- record status: `READY`, `NEEDS_REVIEW`, `PARTIAL`, `EXCLUDED`;
- warning counts/types are separate data, not combined enum values.

## Visual direction

Explore a trustworthy, calm, gaming-adjacent utility. Do not copy Pokémon GO UI, logo, characters, or official art. The working product name is **PokeAndScan** and the working descriptor is **Offline Pokémon collection scanner and exporter**.

The product needs both dark and light themes. Dark is the primary brand direction, but light must be a first-class design, not a simple inverted palette. The primary accent color is intentionally not finalized yet; propose a small number of options and explain how each works in both themes. Use amber carefully for warnings/review states and reserve red for destructive/error states.

## Deliverables

Create:

1. a complete information architecture;
2. high-fidelity screen designs for the main happy path and critical error/recovery states;
3. both Slovak and English copy for the most important user-facing screens;
4. light and dark variants;
5. design tokens for color, typography, spacing, shape, elevation, and touch targets;
6. accessibility notes for contrast, dynamic text, touch targets, and color-independent status communication;
7. a component inventory for reusable cards, status badges, progress states, evidence crops, review forms, dialogs, and export actions;
8. explicit assumptions and unresolved visual decisions, without changing the business or technical rules above.

Do not write production code. Focus on a concrete, reviewable visual/product design that can be used as the input for a later design grill session.

---
