# Illustrated information and empty states — visual audit

**Status:** Implementation and final visual review complete
**Current authority:** [Design specification](../design-specification.md) and [illustration asset map](../illustration-asset-map.md)
**Device:** `emulator-5554`, Medium_Phone AVD, Android 17 / API 37, 1080 × 2400 px at 420 dpi
**Locale/theme:** Slovak, Light
**Frame:** Android system bars included; screenshots captured from the running app

This audit applies to the existing Welcome, Capture explanation, Preparation, New scan, and empty Scans/Library screens. Settings is intentionally illustration-free. The project is native Jetpack Compose, so the live UI loop uses Gradle installation and ADB screenshots in place of Flutter hot reload.

## Reference and intentional differences

- The current Design specification and illustration map are authoritative. Imported v0.4 UI boards are historical visual context only; they do not override current product decisions.
- Preserve the full image composition with proportional fit. Do not crop, mask, stretch, extract, or use landscape artwork as a scan thumbnail.
- The empty Scans/Library state may use its dedicated mapped empty-state illustration. The separate Library list motif remains a small 40–64 dp decoration and must never act as a record/media thumbnail.
- Center the illustration, heading, explanatory text, and primary action as one group on information/empty screens. Long content scrolls as a group; artwork is not cropped or squeezed to make the content fit.
- Forms, settings, and guidance sections remain left-aligned below any centered illustrated introduction. Settings remains without artwork.
- Existing boards may show different copy, spacing, device frames, or artwork placement. Those are accepted reference differences when the current specification or these rules require them.

## Initial evidence and findings

| ID | Screen/state | Initial observation | Expected relationship | Severity | Status |
|---|---|---|---|---|---|
| F-01 | Scans / Library — no scans, Slovak, Light, scroll top | Initial `pas_before.png` had no illustration and an unbalanced upper blank area. | Dedicated full empty-state artwork, title, copy, and CTA now form one centered group. | Medium | PASS — `pas_last_review_library.png`, `pas_last_review_library_dark.png` |
| F-02 | New scan, Slovak, Light/Dark | Initial `pas_before_new_scan.png` had one sentence and a CTA near the top with excessive unused space. | Centered illustration/introduction precedes a left-aligned name, scope, and selectable source form; Continue stays pinned while the form scrolls. | Medium | PASS — updated setup captures listed below |
| F-03 | Welcome, Slovak, Light/Dark | Initial state had no mapped artwork and plain left-aligned content. | Centered hero and introductory copy now precede readable, left-aligned details and language choices. | Medium | PASS — `pas_final_welcome_light.png`, `pas_final_welcome_dark_fixedbars.png` |
| F-04 | Capture explanation, Slovak, Light/Dark | Initial state had no art; Continue and Back had similar weight. | Centered education introduction, left-aligned method cards, and full-width Continue above secondary Back. | Medium | PASS — `pas_final_capture_light.png`, `pas_final_capture_dark.png` |
| F-05 | Preparation, Slovak, Light/Dark | Initial state had no art; Back and Continue shared an equal-weight row. | Centered illustrated introduction, left-aligned guidance, scrollable long copy, outlined app action, and dominant Continue. | Medium | PASS — top/bottom captures listed below |
| F-06 | Scans / Library, Slovak, Dark | Initial fresh screenshot showed dark system icons against the dark system-bar surface. | Unwrap Android Context to find the hosting Activity; dark theme now shows light system status/navigation icons. | Medium | PASS — `pas_last_review_library_dark.png` |

Initial screenshot paths (temporary and removed after final inspection):

- `C:\Users\Dominik\AppData\Local\Temp\pas_before.png`
- `C:\Users\Dominik\AppData\Local\Temp\pas_before_new_scan.png`

## Evidence manifest

| Route/state | Data | Locale | Theme | Viewport/device | Scroll | Unknown / not verified |
|---|---|---|---|---|---|---|
| Welcome | First-run onboarding | Slovak | Light / Dark | 1080 × 2400 px, Medium_Phone AVD, Android 17 / API 37, 420 dpi | Top | Narrow handset, keyboard/focus |
| Capture explanation | Education, both capture sources described | Slovak | Light / Dark | Same emulator viewport | Top | Narrow handset, keyboard/focus |
| Preparation | Guidance, Pokémon GO installed state not required | Slovak | Light / Dark | Same emulator viewport | Top and bottom | Narrow handset, keyboard/focus |
| New scan | Date-named draft and capture-source setup | Slovak | Light / Dark | Same emulator viewport | Intro and form; Continue pinned; 130% font scale checked | Narrow handset, keyboard/focus traversal |
| Scans / Library | No saved scans | Slovak | Light / Dark | Same emulator viewport | Centered | Narrow handset, keyboard/focus |

Android font-scale check: New scan and Preparation were inspected with Android `font_scale=1.3`; copy and source cards wrapped while remaining scrollable and the pinned Continue action stayed visible. The emulator was restored to `font_scale=1.0` afterward.

Scope boundary: This pass implements the source-selection and Preparation presentation. The current scaffold returns from “I’m ready” to New scan; the live-capture permission transition, Android file picker, and MP4 preflight are not implemented by this visual task and remain unverified.

The screenshots below were captured from the final installed APK. `pas_final_welcome_light.png` and `pas_final_welcome_light_visit2_settled.png` show different mapped art choices after leaving and revisiting Welcome. Theme selection is not available within these illustrated routes, so live theme switching mid-visit was not exercised; the chosen light/dark pair is held in the visit-selection code.

| Screen/state | Light evidence | Dark evidence |
|---|---|---|
| Welcome | `C:\Users\Dominik\AppData\Local\Temp\pas_final_welcome_light.png` | `C:\Users\Dominik\AppData\Local\Temp\pas_final_welcome_dark_fixedbars.png` |
| Welcome — later visit | `C:\Users\Dominik\AppData\Local\Temp\pas_final_welcome_light_visit2_settled.png` | — |
| Capture explanation | `C:\Users\Dominik\AppData\Local\Temp\pas_final_capture_light.png` | `C:\Users\Dominik\AppData\Local\Temp\pas_final_capture_dark.png` |
| Preparation — top | `C:\Users\Dominik\AppData\Local\Temp\pas_final_preparation_light_top.png` | `C:\Users\Dominik\AppData\Local\Temp\pas_final_preparation_dark_top.png` |
| Preparation — bottom | `C:\Users\Dominik\AppData\Local\Temp\pas_final_preparation_light_bottom.png` | `C:\Users\Dominik\AppData\Local\Temp\pas_final_preparation_dark_bottom.png` |
| New scan — setup, 100% | `C:\Users\Dominik\AppData\Local\Temp\pas_new_scan_setup_light.png` | `C:\Users\Dominik\AppData\Local\Temp\pas_new_scan_setup_dark.png` |
| Scans / Library — empty and AppBar title | `C:\Users\Dominik\AppData\Local\Temp\pas_last_review_library.png` | `C:\Users\Dominik\AppData\Local\Temp\pas_last_review_library_dark.png` |
| New scan — setup, font scale 130% | — | `C:\Users\Dominik\AppData\Local\Temp\pas_new_scan_font130.png` |

## Final review ledger

| Category | Status | Evidence / note |
|---|---|---|
| Macro composition | PASS | Centered states balance within the available content area; long Preparation content scrolls. |
| Typography | PASS | Plus Jakarta Sans roles match the implemented size/line-height/spacing/weight tokens; Slovak strings wrap. |
| Geometry and spacing | PASS | Shared 20 dp screen padding, centered max-width columns, 16/8/24 dp state rhythm, 52 dp primary actions, and 48 dp secondary targets are visible. |
| Styling and artwork fit | PASS | Full compositions use fit/contain; Light/Dark surfaces, text, CTA contrast, and system bars were inspected. |
| Cross-screen consistency | PASS | Empty Library uses the centered information-state pattern; New scan and education pages pair centered illustrated introductions with left-aligned content. |
| Final gestalt | PASS | Friendly Explorer illustrations reinforce the content without becoming record/media thumbnails. |
| Slovak localization and large font scaling | PASS | Slovak was inspected across all routes; New scan and Preparation reflowed at 130% without clipping, with Continue remaining visible. |
| Narrow viewport | NOT VERIFIED | Only the 1080 × 2400 px Medium_Phone AVD was used. |
| Keyboard/focus behavior | NOT VERIFIED | Theme-specific focus outlines are wired to interactive controls, but keyboard/switch traversal was not exercised. |
| Source action after Preparation | NOT IMPLEMENTED | The scaffold returns to New scan after “I’m ready”; permission, file-picker, and MP4-preflight behavior are outside this visual pass. |

Temporary screenshots and the one-off comparison script were removed after inspection; their exact capture paths above are retained as the audit trail.
