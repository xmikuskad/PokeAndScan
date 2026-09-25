# UX and Design Specification

**Product:** PokeAndScan
**Status:** Agreed MVP direction
**Date:** 2026-09-21

The overall visual direction is confirmed as **Friendly Explorer**. This specification records the confirmed brand tokens, logo route, screen structure, states, and interaction rules for the MVP. The supplied visual reference pack is stored in [uiux-reference](./uiux-reference/README.md); explicit deviations from that reference are listed there.

## Design principles

- One clear primary action per screen.
- Explain preparation in user language, not OCR/CV terminology.
- Make uncertainty visible and actionable.
- Keep the app useful while a scan is partial.
- Keep diagnostics out of normal screens.
- Never make the user wonder whether screen capture is still active.
- Use the confirmed Friendly Explorer direction: approachable but capable, with a blue-led action system, calm exploration motifs, and clear Android utility behavior.
- Keep illustrations concentrated in onboarding, education, empty states, and recovery; data-heavy screens stay restrained and evidence-first.
- Treat light and dark themes as first-class experiences rather than simple color inversion.

Every MVP screen defines a clear empty/no-content state for cases where its expected data or results are absent. Each state briefly explains the situation and gives the user a relevant next action. An illustration is optional and must suit the screen's purpose.

The supplied visual boards are presentation references. Production UI follows Android/Material 3 conventions on the Pixel 9 Pro XL target, including Android system chrome, back behavior, permission surfaces, and Save/Share flows. iOS-specific mockup conventions are not copied into product behavior.

Illustrations use original, generic nature/exploration motifs. The [illustration asset map](./illustration-asset-map.md) is the source of truth for screen assignments, light/dark candidates, and assets kept out of the app.

Use illustrations on Welcome, Capture explanation, Supported setup, New scan, Preparation, the live-capture permission transition, MP4 preflight states, interrupted-scan recovery, Summary, the empty Scans home, and the Library/search empty state as listed in the asset map. Keep active capture, processing, review, manual add, scan details and record editing, export, and settings/privacy utilitarian and evidence-first; their dedicated illustration files remain source-only in Downloads.

Each screen or state has its own candidate pool for each theme. On entry, choose one candidate from that exact pool and keep it for the whole visit. On a later visit, choose again; when a pool has multiple candidates, avoid choosing the same candidate twice in a row. The map pairs light and dark variants by candidate slot, so a theme change during a visit keeps the selected slot. Never reuse one asset in different screen/state pools.

Show the complete image composition. Scale proportionally with a fit/contain behavior; do not crop, mask, stretch, or cut out parts. Keep the whole illustration visible at the largest intended display size for the Pixel 9 Pro XL reference and let surrounding content reflow. In the Library, use only the mapped small decorative motif; never use artwork as a scan or media thumbnail. Product illustrations must not use Pokémon characters, Poké Ball-like symbols, official artwork, copied Pokémon GO UI, or landscape/video thumbnails that imply retained source media.

### Illustrated information and empty-state layout

Use one shared layout pattern for illustrated information screens and empty states:

- Center one complete illustration above the title, explanatory copy, and the primary action. Center this group vertically in the available content area when it fits; if content grows, let the entire screen scroll without clipping text or artwork.
- Use a centered illustration slot up to 360 dp wide and 208 dp tall, with fit/contain scaling. Keep at least 16 dp between the image and title, 8 dp between title and copy, and 24 dp between copy and primary action.
- Limit the full content column to 440 dp and explanatory copy to 360 dp. Use 20 dp horizontal screen padding and a 52 dp minimum primary-action height. Keep the primary action full-width within the content column; place secondary actions below it with quieter styling and at least a 48 dp touch target.
- Use the Screen title typography token for the state title and Body for its explanation. Both are centered. Keep localization and Android font scaling enabled; allow copy and controls to wrap and the screen to scroll rather than shrinking text or artwork.
- Treat the illustration as decorative when the adjacent text conveys the same meaning, so it does not add redundant TalkBack output.

Use the centered illustration-and-introduction pattern on Welcome, Capture explanation, New scan, and Preparation. Keep the following content left-aligned: Welcome details and language choices; Capture explanation source cards and reminder; New scan name, scope, and selectable source cards; Preparation guidance. New scan keeps one Continue action fixed at the bottom while its form scrolls. Apply the full vertically centered information/empty-state layout to Scans/Library when no scans exist. Do not center forms or data-heavy content as a whole. Settings remains illustration-free. The full empty-state image on a no-scans screen is distinct from the Library list's 40–64 dp decorative motif; neither may be used as a scan/media thumbnail.

Keep all original packs in their existing Downloads folders. Copy only the mapped runtime candidates into the app as reduced, optimized derivatives; use lossless WebP when it is smaller, otherwise keep an optimized PNG. Do not convert or copy source-only assets into the project.

## Navigation

~~~
Scans (home)
├── New scan
│   ├── Setup (name, scope, capture source)
│   ├── Preparation
│   ├── Capture / processing
│   └── Summary
├── Scan details
│   ├── Review
│   └── Export
└── Settings (app-bar action)
    ├── Language
    ├── Theme
    └── Privacy (external link)
~~~

Scans is the home screen. New scan is its dominant action, while Settings is an app-bar action. Capture, processing, review, and export are focused flows without a permanent bottom navigation bar; they use normal back behavior where it is safe.

## Language

The interface supports Slovak and English in MVP. On first launch, the app preselects Slovak only when the Android system language is Slovak; English is preselected for English and all unsupported system languages. Welcome shows `Slovenčina` and `English` so the user can override that initial choice before continuing. Settings later offers the same two explicit choices; there is no `System default` option. Changing the app language does not change the required English Pokémon GO setup.

User-facing terminology uses `sken`/`skeny` in Slovak and `scan`/`scans` in English for an independent collection result. `Snapshot` remains an internal/domain term. Evidence crops and source recordings are named separately so they are not confused with a scan.

App localization applies to labels, instructions, statuses, warnings, and actions. Canonical Pokémon species/form values remain official English names in both Slovak and English UI, matching the English Pokémon GO profile and the always-English export contract.

Theme settings offer `System default`, `Light`, and `Dark`. Dark is the primary brand direction, but both Light and Dark are complete supported experiences.

On first launch, the selected theme is `System default`, so PokeAndScan follows the current Android appearance. Users may select Light or Dark at any time in Settings.

The confirmed primary action colors are `#0067D6` for Light and `#60A5FA` for Dark. Secondary violet is limited to supporting brand emphasis, while coral is decorative only. Status colors remain semantic and separate from the brand accent: amber for review/warnings, green for ready/confirmed, and red/crimson for errors or destructive confirmation.

Secondary violet is `#7C3AED` in Light and `#A78BFA` in Dark, used only for occasional supporting brand emphasis. Decorative coral is `#FF6B6B` in Light and `#FF7A7A` in Dark, used only in illustrations. Neither accent is used for primary actions or semantic record statuses.

Confirmed surface/text tokens:

| Role | Light | Dark |
|---|---|---|
| Background | `#EEF4FF` | `#0B1220` |
| Surface | `#F8FAFF` | `#111827` |
| Raised/card surface | `#FFFFFF` | `#172033` |
| Subtle surface | `#E7F0FF` | `#162238` |
| Primary text | `#0F172A` | `#F8FAFC` |
| Secondary text | `#5B6B82` | `#CBD5E1` |
| Outline | `#C9D7EA` | `#334155` |
| Focus outline | `#0067D6` | `#60A5FA` |
| OnPrimary | `#FFFFFF` | `#0B1220` |

Confirmed semantic status tokens (foreground / container):

| State | Light | Dark |
|---|---|---|
| Ready | `#15803D` / `#DCFCE7` | `#4ADE80` / `#123522` |
| Needs review / Missed appraisal | `#B45309` / `#FEF3C7` | `#FBBF24` / `#3B2A08` |
| Partial | `#1D4ED8` / `#DBEAFE` | `#93C5FD` / `#153255` |
| Excluded | `#475569` / `#E2E8F0` | `#CBD5E1` / `#243041` |
| Error / destructive | `#BE123C` / `#FFE4E6` | `#FB7185` / `#4A1724` |

Status color is always paired with a text label and icon.

The product UI uses **Plus Jakarta Sans** for its typography system. The implementation must preserve Slovak diacritics, support Android font scaling, keep CP/IV values legible, and reflow instead of clipping critical labels or actions.

Typography tokens (size / line height / letter spacing / weight; dimensions in sp):

| Role | Size | Line height | Letter spacing | Weight |
|---|---:|---:|---:|---|
| Large screen title | 28 sp | 36 sp | −0.25 sp | Bold (700) |
| Screen title | 24 sp | 30 sp | −0.2 sp | SemiBold (600) |
| Section title | 20 sp | 26 sp | 0 sp | SemiBold (600) |
| Card title | 18 sp | 24 sp | 0 sp | SemiBold (600) |
| Body | 16 sp | 24 sp | 0 sp | Regular (400) |
| Supporting body | 14 sp | 20 sp | 0 sp | Regular (400) |
| Label | 14 sp | 20 sp | +0.1 sp | Medium (500) |
| Compact status badge | 12 sp | 16 sp | +0.1 sp | SemiBold (600) |
| Large metric | 30 sp | 36 sp | −0.2 sp | Bold (700) |

Android font scaling must remain supported; layouts reflow rather than clipping critical labels, values, or actions.

Shared component geometry uses a 4 dp spacing grid, 20 dp default horizontal screen padding on the Pixel 9 Pro XL reference, 16 dp primary card corners, 12 dp control/button corners, and 52 dp primary button height. Use the Focus outline token for visible keyboard/switch focus indicators; the subtler Outline token is not a substitute. Prefer tonal surface hierarchy and restrained outlines/elevation over large shadows. Each screen should have one dominant CTA; secondary and destructive actions remain visually quieter unless a real destructive confirmation is active.

The app provides shared UI tokens in [`Dimensions.kt`](../app/src/main/java/com/falconsocka/pokeandscan/ui/theme/Dimensions.kt) (`AppSpacing`, `AppDimensions`, and `AppShapes`) and shared action buttons in [`ActionButtons.kt`](../app/src/main/java/com/falconsocka/pokeandscan/ui/components/ActionButtons.kt) (`PrimaryActionButton`, `SecondaryActionButton`, and `QuietActionButton`). Prefer these tokens and components across screens so spacing, shape, size, and focus treatment stay consistent. Use custom styling only when a screen or interaction has a specific requirement the shared options do not support, and keep that exception local.

Privacy opens the English or Slovak external GitHub Pages policy according to the app's saved language. The canonical URLs are maintained in [privacy-policy.md](./privacy-policy.md); keep the app's URL mapping aligned with that source. The app must not imply that deleting a scan removes the original MP4 selected from outside the app.

Settings MVP contains only Language, Theme, and Privacy. There is no About page, Advanced/Developer diagnostics section, custom data-storage manager, Delete all data action, or CSV/JSON restore entry point in MVP. The Settings screen shows the app name and version as static footer information.

## First-run onboarding

The first-run flow is a short guided sequence rather than a marketing carousel:

`Welcome → Capture explanation → Supported setup → Scans`

Each screen has one purpose and a clear Continue action. The sequence is guided but not permanently blocking; it can be exited and is not repeated on every scan. No screen-capture or notification permission is requested during onboarding.

### Welcome

Explain what the app does and does not do:

- local/offline processing;
- user-visible screen capture only;
- manual Pokémon GO navigation remains the user's responsibility;
- uncertain data goes to review;
- independent, unofficial companion disclaimer.

Use one scrollable Welcome screen rather than multiple marketing slides. Primary action: Continue.

Show the `Slovenčina`/`English` language choice here, with the system-derived language preselected and immediately applied when changed.

### Capture explanation

Explain the two sources:

- Live capture — preferred, analyzes the screen while the user plays manually;
- Import MP4 — fallback, analyzes an Android screen recording.

This screen is educational only. It does not choose the capture source, request capture/notification permissions, or repeat on every scan. Primary action: Continue to Supported setup. The source is selected later in New scan; no separate revisit screen is required in MVP.

### Supported setup

Show the MVP requirements:

- Pixel 9 Pro XL reference profile;
- portrait orientation;
- English Pokémon GO UI;
- default Android display size and font size;
- no nicknames for the first pilot.

Other devices may work but are not verified.

The MVP supports Android 10 and newer. The verified device profile is Pixel 9 Pro XL; other Android devices are not guaranteed until their screen layout is profiled.

Separate reference requirements from facts detected on the current device. Do not show green completion checks for conditions the app cannot actually verify. Pokémon GO language, active scope/filter, and whether the user opened Appraise remain explicit user confirmations. A different device receives a non-blocking compatibility warning rather than an automatic rejection. Nickname guidance remains an amber recommendation, not an error.

## New scan flow

The initial New scan screen is one scrollable setup view with a single sticky Continue action. It contains the optional scan name, scan scope, and capture source. The user makes these choices before entering the preparation checklist; MVP does not split them into separate wizard steps. Capture-source cards are selectable options and do not start capture or open the file picker. Continue opens Preparation. After the user confirms readiness, the selected source determines the next action: live capture opens its permission transition; MP4 import opens the file picker followed by preflight.

### Scope

MVP shows one enabled scope:

~~~
Pokémon appraisal
Species · CP · IVs
~~~

Future scopes such as moves, items, and metadata stay hidden or visibly marked as planned rather than appearing as half-working controls.

Primary action: Continue.

### Scan name

Before preparation, show an optional name field for the new scan:

~~~
Scan name (optional)
[ Sken 20.12.2023 ] / [ Scan 20.12.2023 ]
~~~

The date-based default is editable and localized to the current app language when generated. Once saved, it is an ordinary stored name and does not change after a language switch. The name should be visible later in scan history, scan details, and export metadata. The user can describe a tag or purpose here, for example `PVP Pokémoni`.

### Preparation checklist

This is a short per-scan reminder, not a repeat of the onboarding lesson. Supported setup explains the device/game requirements once during onboarding. Before each scan, briefly recheck the current scan scope and traversal readiness; remind the user of the language/orientation/display requirements without repeating their full explanations. The checklist is instructional guidance, not a form requiring a manual tick for every recommendation. Use one final `I'm ready`/`Som pripravený` confirmation. Only show an automatic completion state when the app can genuinely verify it; otherwise label the item as a user instruction. The user may continue with a warning when a recommendation is not met.

1. Set Pokémon GO to English.
2. Keep the phone in portrait.
3. Use default display and font settings in Pokémon GO. This is a requirement of the verified capture profile, not permission to disable font scaling inside PokeAndScan. PokeAndScan must respect Android font scaling and reflow its own UI rather than shrinking text or clipping content.
4. Open Pokémon storage.
5. Confirm that the intended scan scope is active: the whole collection, or a deliberate in-game filter/tag such as a chosen tag.
6. Clear accidental filters and sort by Name A→Z where practical.
7. Open the first Pokémon.
8. Open Appraise.
9. Move slowly enough for each screen to settle.
10. Avoid nicknames for the MVP pilot.

The user may continue with a warning. The checklist is guidance, not a permanent hard gate.

### Appraisal traversal guidance

The active-scan instructions must make the supported navigation pattern explicit:

- open the first Pokémon and choose **Appraise**;
- use Pokémon GO's in-screen navigation to move to the next Pokémon;
- wait until the appraisal screen settles before moving again;
- do not return to the storage list after every Pokémon;
- let the app finish automatically when it detects the end of the intended range; use Stop only to save an earlier, partial result.

The app should show this guidance before capture and make it available during an active scan without covering Pokémon GO with an overlay.

### Capture source

Primary card:

~~~
Live capture
Recommended
Analyze the screen while you manually move through Pokémon.
~~~

Secondary card:

~~~
Import MP4 recording
Use an Android screen recording when live capture is unavailable.
~~~

These cards only select the source; neither has a start/import button. The single sticky Continue action opens Preparation. After the final `I'm ready` confirmation, Live capture starts its permission transition. For Import MP4, open the Android file picker and then show MP4 preflight. The app also accepts an MP4 shared from Android Files or Gallery. Both import entry points lead to the same preflight; the original video remains owned by the user-selected external location.

### MP4 preflight

Show the selected filename, a concise duration summary, and a plain-language profile result first. Keep resolution, orientation, video-track, codec, and decoder details collapsed under `Details`/`Podrobnosti` by default. Expand them when the user asks or when a deviation/failure needs a concrete explanation.

Profile result states:

- `Supported` → offer `Process recording`;
- `Supported with deviations` → show an amber warning and still allow processing;
- `Unsupported` → show a clear reason and offer only `Choose another file`.

### Live capture transition

After the user confirms readiness with Live capture selected:

1. Request notification permission if the Android version requires it and the user has not decided yet.
2. Show Android screen-capture consent.
3. Explain that the screen is being captured.
4. Show OPEN POKÉMON GO as a separate explicit action.
5. Keep the user informed through the persistent Android notification when available.

PokeAndScan must not display overlay controls over Pokémon GO.

If notification permission is denied, live capture still starts when MediaProjection consent succeeds. Show a visible warning in the scan setup/active scan state that the notification-drawer Stop action is unavailable; provide Stop in PokeAndScan and explain that Android Task Manager can also stop the service.

Explain before consent that live capture observes the whole screen while active. If the user leaves Pokémon GO, show a non-blocking warning such as “Pokémon GO is not in the foreground.” Do not create records from other apps, the launcher, system dialogs, or notifications, and do not stop automatically.

Do not expose or preview retained images for non-game states. The user may see the warning timestamp, but only appraisal/transition evidence can be opened as a crop.

Evidence detail should match the issue: clean records show a compact appraisal crop, while review issues show the relevant field crop and missed events show their transition crop. Do not present the crop system as a gallery of the entire source recording.

### Live active scan

While the user is inside Pokémon GO, PokeAndScan is not visible. The active screen is therefore a return/status surface rather than a live dashboard or guidance overlay. Show `Capture active`, elapsed time, detected candidates, review count, missed/non-game warning count, and a current parser state such as `Waiting for appraisal screen` or `Candidate captured`.

Do not show a percentage because the intended range is unknown. Non-game and foreground warnings are non-blocking. When the app detects the traversal's end, it finishes the scan automatically and shows the summary. `Stop` ends capture earlier and saves a partial-range result. Make Stop clear and accessible but do not style it as permanent destructive red. Never place controls or overlays over Pokémon GO.

### Processing

Show:

- current pass;
- timestamp-based progress;
- Pokémon candidates detected;
- confident records;
- review issues;
- incomplete/warning count;
- Stop for live capture or Cancel for import processing.

Import processing must show a visible progress bar and a current-stage label. The user may leave PokeAndScan while processing continues where Android permits; returning to the app shows the current progress and recovered checkpoint state. Do not imply that processing is finished merely because the app was backgrounded.

For MP4, the progress bar is determinate and based on the processed video position. For live capture, show elapsed time, detected candidate count, review count, and current state instead of a percentage.

The processing status must remain recoverable when the user locks the phone. Reopening PokeAndScan shows the last checkpoint and offers:

~~~
Processing was interrupted.

[ CONTINUE PROCESSING ]
[ FINISH AS PARTIAL SCAN ]
[ DISCARD ]
~~~

Do not resume long-running work automatically.

The recovery surface shows the scan name, source type/file, last saved progress, retained candidates, review count, and missed-screen count. `Continue processing` is the primary action, `Finish as partial scan` is secondary, and `Discard` is destructive and requires confirmation. If the source URI is no longer available, disable Continue with a plain-language explanation while keeping the other two choices available.

If a capture or processing job is already active, disable **New scan** and explain that only one active job is supported in MVP. Existing finalized scans remain accessible.

When MP4 processing continues in the background, provide the foreground-service notification required by Android. Do not request runtime notification permission solely for MP4 processing. Keep in-app progress as the primary status surface; when permission is denied, explain that Android may expose service status/stop through Task Manager rather than the notification drawer. Processing and checkpoint-based recovery remain functional where platform rules permit.

The current record may be shown as a compact preview. Technical parser diagnostics stay out of MVP user screens.

For MVP, active scan controls expose **Stop** and no separate Pause or Finish button. The app completes a successfully detected traversal automatically; Stop saves a partial-range result. If capture or processing is interrupted unexpectedly, the incomplete-scan recovery flow handles continuation.

If processing or capture is interrupted, show the scan as incomplete and preserve the processed results. When the user returns, offer:

~~~
This scan was interrupted.

[ CONTINUE SCAN ]
[ FINISH AS PARTIAL SCAN ]
[ DISCARD SCAN ]
~~~

Discard requires confirmation. Finishing creates a clearly marked partial scan and keeps its warnings visible.

### Summary

Example:

~~~
Scan finished

Pokémon candidates       198
Ready to export          184
Needs review              11
Partial                    3
Warnings                   2

[ REVIEW ISSUES ]
[ VIEW SCAN ]
[ EXPORT ]
~~~

The summary separates record counts (candidates, Ready, Needs review, Partial, Excluded) from warning counts (including Missed appraisal). It does not use percentages that could be mistaken for recognition confidence. If unresolved issues remain, `Review issues (N)` is the dominant action; otherwise `Export` is dominant. Viewing the scan/detail remains available as a quieter secondary action.

If no Pokémon records were captured, show a `No Pokémon captured`/localized state with a plain explanation. If missed-appraisal events exist, offer manual add from each event. If no record can be added from an event, offer navigation to scan details or the library, where the user can start another scan.

The summary never implies that a finalized scan is free of unresolved issues or missed screens; it shows those warnings alongside lifecycle and scope labels.

Do not optimize the summary to maximize the number of filled fields. A visible Unknown or Needs review state is preferable to a plausible but unverified value.

Use separate labels for record and snapshot state. A manually saved species-only record is `Partial`, an unresolved parser result is `Needs review`, and a user-excluded record is hidden from the normal result but recoverable.

Summary copy must derive simple labels from separate fields:

- `PROCESSING` → **Processing**;
- `INCOMPLETE` → **Incomplete**;
- `COMPLETE` + `INTENDED_RANGE` + no warnings → **Complete**;
- `COMPLETE` + `PARTIAL` scope → **Partial scan**, with supporting copy that the intended range was not verified, not a claim that a Pokémon was missed; show an additional warning label when needed;
- `COMPLETE` + `INTENDED_RANGE` + warnings → **Complete with warnings**.

Do not expose a matrix of technical enum combinations to the user.

Record status presentation uses only `Ready`, `Needs review`, `Partial`, and `Excluded`. `Missed appraisal` remains a warning/event and `Confirmed by you` remains provenance. Every status is communicated with an icon and text label in addition to semantic color; color alone is never sufficient.

Use amber warning semantics for `Missed appraisal`, since it indicates a possible gap rather than a failed Pokémon record. Reserve red/crimson for actual errors and destructive actions. This intentionally changes the red `Missed` treatment shown in the supplied handoff.

For a missed appraisal screen, use plain language such as:

> We may have missed one Pokémon between 12:41 and 12:43 because the screen changed too quickly.

Show the event in warnings with its timestamp. Do not present it as an editable Pokémon record because no trustworthy record was captured.

The warning must also offer:

~~~
[ ADD POKÉMON MANUALLY ]
~~~

The manual form uses the same validation as review, marks the resulting record as `Manually added`, and makes clear that the Pokémon was added manually and its values were not detected from an appraisal screen. A Missed appraisal warning may retain its own transition crop; that crop belongs to the warning and is not parser-detected evidence for the manually added record. The user may cancel without creating a record.

Manual-add form:

~~~
Species *
[ Choose species          ]

CP (optional)             [          ]
Attack IV (optional)      [          ]
Defense IV (optional)     [          ]
Stamina IV (optional)     [          ]

[ CANCEL ]       [ ADD POKÉMON ]
~~~

Species is required. Empty numeric fields become Unknown; invalid ranges are rejected inline.

After saving, the manually added Pokémon appears in the position of the missed event and the warning changes to resolved while retaining its provenance history.

## Review

Review is a focused queue, one issue at a time.

If the user opens Review when no unresolved issues remain, show a concise `Nothing to review`/localized message and the primary action `Back to scan details`. The screen must not look broken or leave the user in an empty queue.

~~~
Review · 11 remaining

[ source crop ]

Species
[ Choose species          ]

Form (when applicable)
[ Choose form             ]

CP
[ 3821                   ]

Attack IV   [ 15 ]
Defense IV  [ 14 ]
Stamina IV  [ 15 ]

Original detection: uncertain

[ SKIP ]                         [ CONFIRM ]
[ SAVE AS PARTIAL ]  (when this record has unresolved fields)
~~~

Rules:

- species is selected from the canonical English dictionary;
- CP must be a valid non-negative integer;
- IV values must be integers from 0 through 15;
- confirmation marks values as user-confirmed;
- original evidence remains available;
- skipping leaves the issue unresolved and exportable as UNKNOWN.
- **Save as partial** is an explicit record-level action for a parser-derived record with unresolved fields. It sets every remaining unresolved field in that record to Unknown, closes those review issues, preserves the original parser observations and evidence, and marks the record `Partial`. Values already corrected or confirmed by the user stay unchanged. This action means the user accepts the known values and does not want the remaining fields to stay in the review queue.
- **Skip** means decide later: the current issue remains open and the record stays `Needs review`.

The queue follows scan order. Missed appraisal events appear at their sequence position, and fields within a record follow Species/Form, CP, Attack IV, Defense IV, and Stamina IV. **Skip** leaves the issue unresolved and moves it later in the queue; leaving Review preserves the queue for the next visit.

When species recognition is blocked by a nickname, show the captured text as original evidence and leave Species as Unknown until the user selects a canonical species. The scan does not stop because of this issue.

For manually added records, species is required and optional numeric fields may be Unknown. Saving a valid record with one or more Unknown numeric fields marks it `Partial` directly; it has no parser review issue or parser observation to accept.

Species selection may include a supported named form. Costume, shiny, and event appearance are not editable MVP fields.

Species and form pickers use the bundled dictionary only. If captured text is not in the current dictionary, show it as original text/evidence and explain that an app update may add support; do not offer free-form canonical values.

If a species search has no match, show `No species found`/localized copy, provide a clear-search action, and explain that the offline species list is extended through app updates. Do not allow free-form canonical species entry.

Review also offers **Exclude record** when the whole record is wrong or duplicated. Exclusion is immediate and reversible: the record leaves the normal scan result and export, while a snackbar offers `Undo` and Scan details provides `Restore`/`Show excluded`. Do not show a confirmation dialog for this action.

For parser-derived records, expose **Reset to original detection** when a field has a retained parser observation. Do not show this action for manually added records.

Scan details provides **Edit** for every included record, even when it is not in the review queue. The edit screen uses the same species/form and numeric validation as Review and shows whether values came from detection or user confirmation.

Scan details uses lightweight filters (`All`, `Needs review`, `Partial`, `Excluded`). Record rows show sequence, species/form or Unknown, CP/IV summary, and record status. The shared editor shows current values, relevant evidence, original detection, and provenance; it does not expose raw technical confidence percentages, location metadata, or internal extracted-region details. `Reset to original detection` is available only for parser-derived records, while excluded records expose `Restore`.

If the selected filter has no matching records, show `No records match this filter`/localized copy and a `Clear filters` action. This is a filtered empty result only; it does not imply that records were deleted.

## Scans and library

The library lists independent scans, not a merged master collection.

When there are no scans yet, show a small original Friendly Explorer illustration, a concise `No scans yet` message (localized in the UI), one sentence explaining that the first scan creates a local result, and the dominant `New scan` action. Do not show Pokémon/media thumbnails or repeat onboarding content.

If the library has an active or interrupted job but no finalized scans, pin that job first and make `Continue`/`View` the primary action. A quieter `No completed scans yet` message may explain the empty history. Do not present `New scan` as available while the single-job limit prevents starting another job.

Each scan card shows:

- scan name and creation time;
- source type (`Live` or `MP4`);
- lifecycle and scope-completeness labels;
- compact review, partial, and warning counts;
- export action;
- delete action.

Do not show source-video, screenshot, or landscape thumbnails. Use an abstract source/status/brand symbol so the library cannot imply retained media. The mapped Library illustration may appear only as a small decorative motif of about 40–64 dp; it is not a scan-media thumbnail. The library remains a list of independent scans, not a merged master collection. Pin an active or recoverable job above finalized scans with its primary Continue/View action.

Deleting a scan requires confirmation and deletes its evidence crops and review data together. It does not delete the original MP4 outside the app.

## Export

MVP format choices:

- CSV;
- JSON.

The export screen should make the selected scan obvious and show whether unresolved issues remain. It should provide Export anyway for partial results, with a clear warning.

Normal CSV and JSON exports include every record except `Excluded`; `Ready`, `Needs review`, and `Partial` records remain included. Do not add an `Include only ready items` checkbox in MVP. This is an intentional deviation from the supplied UI handoff: an export must not silently become an incomplete subset. If the user does not want a record exported, they must explicitly exclude it first.

If there are no exportable Pokémon records, show a `No records to export`/localized empty state and offer `Back to scan details` to restore excluded records. Disable CSV because it would contain no data rows. Allow JSON when it still contains useful snapshot metadata or missed-appraisal events. If JSON would contain no useful scan data either, disable export actions and explain why.

A scan remains editable through Scan details after export. Show the last export time, but do not imply that an existing file was updated. A later export uses the current snapshot state and may overwrite a destination only after confirmation.

Scan details also offers **Rename**. The new scan name appears in the library and is used for future export filenames; existing files are not renamed.

Normal exports contain final values, local IDs, record status, and provenance. Technical confidence and evidence belong to a later diagnostics export.

Label CSV/JSON as data exports, not backups. Do not promise that importing them will recreate snapshot history, evidence crops, or review history in MVP.

Export preview and format documentation must state that CSV/JSON content is always English even when the PokeAndScan interface is Slovak.

Live-capture explanation should explicitly state that PokeAndScan captures screen video only and does not use the microphone or device audio.

Make the Unknown convention visible before export:

- CSV writes `UNKNOWN` in an unknown field;
- JSON writes `null`;
- both formats include record status and provenance;
- excluded records are not included in the normal export.

The Android save/share flow proposes a filename derived from the scan name, for example `PVP Pokemoni.csv` or `PVP Pokemoni.json`. Invalid filename characters are sanitized. If the destination already contains the filename, ask before replacing it.

For CSV, show a confirmation warning when the scan contains unresolved review issues or missed appraisal screens. JSON includes the snapshot status, warning counts, and missed-appraisal event details so it can preserve the complete scan context.

Show IV percentage as `UNKNOWN`/empty-state in the UI until all three IVs are known. Once available, display one decimal place, for example `93.3%`.

## Error states and user language

Use the confirmed brand voice: friendly, calm, clear, and capable. Copy should be concise and factual, make uncertainty actionable without alarmism, and never pressure the user to replace Unknown values with guesses.

Use actionable language:

- “We could not find a stable appraisal screen.”
- “Some CP values need review.”
- “The recording uses an unsupported layout or format.”
- “This MP4 does not contain a video format supported on this device.”
- “This recording uses a layout we do not support yet.”
- “This recording is slightly different from the verified profile. Results may need more review.”
- “Capture stopped. Your partial results are safe.”
- “This scan is incomplete; export is still available.”

Do not expose internal categories such as IV_GEOMETRY_UNCERTAIN as the primary message.

## Visual and accessibility direction

- dark and light themes with shared semantic design tokens;
- dark theme is the primary brand direction, while light theme is fully supported rather than being a simple color inversion;
- one accent color for the primary action;
- high-contrast text and controls;
- large touch targets;
- no information conveyed by color alone;
- dynamic text should not break review controls;
- technical confidence is not shown in normal user screens; use plain labels such as Needs review.
- motion is optional enhancement only; every state, progress value, warning, and action must remain understandable and usable without animation;
- respect Android accessibility and reduced-motion settings.

Use Material Symbols Rounded as the consistent icon family for functional UI. The Concept 1 brand mark is the only custom icon exception. Do not mix icon families or stroke/fill styles in the same interface.

MVP accessibility also requires 48×48 dp minimum touch targets, Android font scaling without clipping species/status/warning/CTA content, TalkBack/content descriptions for icons and evidence crops, WCAG AA contrast, and logical focus order for keyboard or switch access. Evidence crops always have a text explanation of the issue they support.
