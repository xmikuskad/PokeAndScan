# Illustration asset map

**Status:** Approved assignment; optimized resources prepared and current-screen integration complete
**Date:** 2026-09-25
**Related spec:** [Design specification](./design-specification.md#visual-and-accessibility-direction)

This map covers the 118 PNG illustrations in the Light and Dark v0.9 packs: 28 Light masters, 32 Light production images, and 58 Dark images. It assigns each illustration to one screen/state and records whether it is a runtime candidate or source-only. The four Light JPG contact sheets and package documents are reference material, not app illustrations; they stay in Downloads.

## Runtime behavior and file handling

- The Light source pack is `PokeAndScan_Light_Assets_FULL_v0.9/PokeAndScan_Light_Assets_FULL_v0.9`; the Dark source pack is `PokeAndScan_Dark_Assets_FULL_v0.9/PokeAndScan_Dark_Assets_FULL_v0.9`. Both original packs stay in their existing Downloads folders.
- The rows marked **Runtime** are copied to `app/src/main/res/drawable-nodpi/` as reduced Android resources. All originals remain in Downloads. Use lossless WebP when it is smaller than an optimized PNG; otherwise use the optimized PNG. Do not copy any **Downloads only** row into the project or convert it to WebP.
- Resize each runtime copy proportionally for the largest intended display slot on the Pixel 9 Pro XL reference. Preserve the whole composition with fit/contain behavior. Do not crop, mask, stretch, or extract parts from any image.
- Current output bounds are 1200×640 px for screen heroes, 960×600 px for empty states, and 256×256 px for status minis and the Library motif. Each file keeps its source aspect ratio and fits inside its assigned bound.
- Each screen/state has a separate Light/Dark candidate pool. The `Option` letters pair Light and Dark versions of the same screen choice; the scene itself may differ by theme. Choose one option on entry, keep that option for the full visit, and select again on the next visit. Avoid an immediate repeat when that pool has multiple options. A file is assigned to only one screen/state.
- The app's visit-level selection should retain its option when the user changes theme. Use the matching Light or Dark file from that same row.
- Current Compose routes wire the Welcome, Capture explanation, Preparation, New scan, and Scans-empty pools. Assets assigned to planned routes remain staged until those screens/states exist.
- `Downloads only` means the file remains available as source/reference material in Downloads but is not converted, copied into the repository, or shipped in the app.
- All 44 runtime resources are currently lossless WebP. Their combined size is 20,149,930 bytes (about 19.2 MiB), down from 75,765,536 source bytes (about 72.3 MiB), a 73.4% reduction. Every output was decoded and pixel-compared against its resized source image.

## Runtime candidate map

Paths in the Light column are relative to `masters/` or `production/`, as shown. Dark paths are relative to the Dark pack root. Runtime resource names are relative to `app/src/main/res/drawable-nodpi/`.

| Screen or state | Option | Light source | Dark source | Project resources (Light / Dark) | Display/use |
|---|:---:|---|---|---|---|
| Welcome | A | `masters/alpine_sunrise_lake_trail.png` | `pas_dt_pg001_welcome_hero.png` | `pas_lt_alpine_sunrise_lake_trail.webp` / `pas_dt_pg001_welcome_hero.webp` | Onboarding hero |
| Welcome | B | `masters/tranquil_mountain_lake_hiking_vista.png` | `pas_dt_sh001_hero_lake_panorama.png` | `pas_lt_tranquil_mountain_lake_hiking_vista.webp` / `pas_dt_sh001_hero_lake_panorama.webp` | Onboarding hero |
| Welcome | C | `masters/peaceful_mountain_lake_panorama.png` | `pas_dt_sh002_forest_lake_mid_scene.png` | `pas_lt_peaceful_mountain_lake_panorama.webp` / `pas_dt_sh002_forest_lake_mid_scene.webp` | Onboarding hero |
| Capture explanation | A | `masters/sunlit_mountain_lake_trail.png` | `pas_dt_pg002_capture_explanation_hero.png` | `pas_lt_sunlit_mountain_lake_trail.webp` / `pas_dt_pg002_capture_explanation_hero.webp` | Education hero |
| Capture explanation | B | `masters/mountain_valley_hiking_trail.png` | `pas_dt_sh003_mountain_valley_path.png` | `pas_lt_mountain_valley_hiking_trail.webp` / `pas_dt_sh003_mountain_valley_path.webp` | Education hero |
| Capture explanation | C | `masters/scenic_mountain_trail_viewpoint.png` | `pas_dt_sh004_signpost_trail_scene.png` | `pas_lt_scenic_mountain_trail_viewpoint.webp` / `pas_dt_sh004_signpost_trail_scene.webp` | Education hero |
| Supported setup | A | `masters/pastel_mountain_lake_panorama.png` | `pas_dt_pg003_supported_setup_header.png` | `pas_lt_pastel_mountain_lake_panorama.webp` / `pas_dt_pg003_supported_setup_header.webp` | Setup header |
| New scan | A | `masters/sunlit_valley_trail_panorama.png` | `pas_dt_pg004_new_scan_header.png` | `pas_lt_sunlit_valley_trail_panorama.webp` / `pas_dt_pg004_new_scan_header.webp` | Setup header |
| Preparation | A | `masters/winding_trail_to_the_mountain_lake.png` | `pas_dt_pg005_preparation_hero.png` | `pas_lt_winding_trail_to_the_mountain_lake.webp` / `pas_dt_pg005_preparation_hero.webp` | Preparation hero |
| Live-capture permission transition | A | `masters/tranquil_mountain_lake_trail.png` | `pas_dt_pg006_live_transition_hero.png` | `pas_lt_tranquil_mountain_lake_trail.webp` / `pas_dt_pg006_live_transition_hero.webp` | Permission transition |
| Live-capture permission transition | B | `masters/forked_path_to_a_mountain_sunrise.png` | `pas_dt_sh005_moonlit_success_lake_scene.png` | `pas_lt_forked_path_to_a_mountain_sunrise.webp` / `pas_dt_sh005_moonlit_success_lake_scene.webp` | Permission transition |
| MP4 preflight — selected file | A | `masters/sunrise_mountain_lake_panorama.png` | `pas_dt_pg007_mp4_preflight_header.png` | `pas_lt_sunrise_mountain_lake_panorama.webp` / `pas_dt_pg007_mp4_preflight_header.webp` | Preflight header |
| MP4 preflight — no file selected | A | `production/empty_states/pas_lt_es010_no_file_selected.png` | `pas_dt_es010_no_file_selected.png` | `pas_lt_es010_no_file_selected.webp` / `pas_dt_es010_no_file_selected.webp` | Empty file-selection state |
| MP4 preflight — unsupported recording | A | `masters/blocked_trail_to_mountain_lake.png` | `pas_dt_es009_unsupported_mp4.png` | `pas_lt_blocked_trail_to_mountain_lake.webp` / `pas_dt_es009_unsupported_mp4.webp` | Unsupported-file state |
| Interrupted scan recovery | A | `masters/mountain_lake_trail_fork.png` | `pas_dt_pg010_interrupted_recovery_hero.png` | `pas_lt_mountain_lake_trail_fork.webp` / `pas_dt_pg010_interrupted_recovery_hero.webp` | Recovery hero |
| Summary — general hero | A | `masters/serene_sunrise_alpine_lake_trail.png` | `pas_dt_pg011_summary_hero.png` | `pas_lt_serene_sunrise_alpine_lake_trail.webp` / `pas_dt_pg011_summary_hero.webp` | Summary hero |
| Summary — complete | A | `production/state_minis/pas_lt_st001_processing_complete_mini.png` | `pas_dt_st001_processing_complete_mini.png` | `pas_lt_st001_processing_complete_mini.webp` / `pas_dt_st001_processing_complete_mini.webp` | Complete state illustration |
| Summary — complete with warnings | A | `production/state_minis/pas_lt_st002_complete_with_warnings_mini.png` | `pas_dt_st002_complete_with_warnings_mini.png` | `pas_lt_st002_complete_with_warnings_mini.webp` / `pas_dt_st002_complete_with_warnings_mini.webp` | Warning state illustration |
| Summary — partial scan | A | `production/state_minis/pas_lt_st003_partial_snapshot_mini.png` | `pas_dt_st003_partial_snapshot_mini.png` | `pas_lt_st003_partial_snapshot_mini.webp` / `pas_dt_st003_partial_snapshot_mini.webp` | Partial state illustration |
| Scans home — no scans yet | A | `masters/mountain_valley_trail_at_sunrise.png` | `pas_dt_es001_no_scans_yet.png` | `pas_lt_mountain_valley_trail_at_sunrise.webp` / `pas_dt_es001_no_scans_yet.webp` | Empty home state |
| Library — scan list | A | `masters/whimsical_mountain_lake_valley.png` | `pas_dt_pg014_snapshot_library_header.png` | `pas_lt_whimsical_mountain_lake_valley.webp` / `pas_dt_pg014_snapshot_library_header.webp` | Small 40–64 dp decorative motif; never a row thumbnail |
| Library — no search results | A | `production/empty_states/pas_lt_es002_no_search_results.png` | `pas_dt_es002_no_search_results.png` | `pas_lt_es002_no_search_results.webp` / `pas_dt_es002_no_search_results.webp` | Search empty state |

## Source-only inventory

These files are assigned to a relevant screen or state for traceability. They are **Downloads only**: no conversion, repository copy, or runtime use. Assets for screens intentionally kept illustration-free are explicitly archived here.

### Light master PNGs — Downloads only (11)

| Intended screen/state | Files |
|---|---|
| Preparation | `forked_trail_to_a_mountain_sunrise.png`, `sunrise_alpine_lake_trail.png` |
| Summary | `cheerful_mountain_lake_river_valley.png` |
| Capture transition | `sunlit_forked_trail_to_mountain_lake.png`, `sunrise_mountain_trail_sticker.png` |
| New scan | `sunlit_mountain_trail_valley.png` |
| Supported setup | `sunny_valley_hiking_trail.png` |
| Welcome | `sunrise_alpine_lake_valley.png` |
| MP4 preflight | `sunrise_mountain_lake_trail.png` |
| Library | `serene_mountain_lake_panorama.png`, `tranquil_mountain_lake_panorama.png` |

These are additional master alternatives that are not in the paired runtime pools. Their full-resolution originals remain in `masters/` in Downloads.

### Light production PNGs — Downloads only (27)

| Intended screen/state | Files |
|---|---|
| Review — no review issues | `empty_states/pas_lt_es004_no_partial_records.png` |
| Scan details — excluded records | `empty_states/pas_lt_es005_no_excluded_records.png` |
| Review — missed appraisal events | `empty_states/pas_lt_es006_no_missed_events.png` |
| Scan details — no records in scan | `empty_states/pas_lt_es007_no_records_in_snapshot.png` |
| Export — no exportable records | `empty_states/pas_lt_es008_no_exportable_records.png` |
| Component accents — no screen pool | `micro/pas_lt_mi001_pine_cluster.png`, `micro/pas_lt_mi002_mountain_silhouette.png`, `micro/pas_lt_mi003_shoreline_cluster.png`, `micro/pas_lt_mi004_rock_cluster.png`, `micro/pas_lt_mi005_bush_grass_cluster.png`, `micro/pas_lt_mi006_cloud_group.png`, `micro/pas_lt_mi007_warm_sun.png`, `micro/pas_lt_mi008_blank_signpost.png`, `micro/pas_lt_mi009_trail_segment.png`, `micro/pas_lt_mi010_small_island.png`, `micro/pas_lt_mi011_leaf_accent.png`, `micro/pas_lt_mi012_discovery_spark.png` |
| Library decorative strip — not selected | `shared/pas_lt_sh006_nature_detail_strip.png` |
| Active capture — non-game frame state | `state_minis/pas_lt_st004_non_game_frame_ignored.png` |
| Settings — privacy state | `state_minis/pas_lt_st005_privacy_local_only_scene.png` |
| Manual add — success state | `state_minis/pas_lt_st006_manual_add_success_mini.png` |
| Library cards — snapshot-tile concepts, prohibited as thumbnails | `tiles/pas_lt_tl001_snapshot_lake.png`, `tiles/pas_lt_tl002_snapshot_mountain.png`, `tiles/pas_lt_tl003_snapshot_forest.png`, `tiles/pas_lt_tl004_snapshot_sunrise.png`, `tiles/pas_lt_tl005_snapshot_trail.png`, `tiles/pas_lt_tl006_snapshot_valley.png` |

### Dark PNGs — Downloads only (36)

| Intended screen/state | Files |
|---|---|
| Processing — no illustration | `pas_dt_pg008_processing_illustration.png` |
| Active capture — no illustration | `pas_dt_pg009_live_active_scan_illustration.png`, `pas_dt_st004_non_game_frame_ignored.png` |
| Review — no illustration | `pas_dt_pg012_review_queue_header_strip.png`, `pas_dt_es003_no_review_issues.png`, `pas_dt_es004_no_partial_records.png`, `pas_dt_es006_no_missed_events.png` |
| Manual add — no illustration | `pas_dt_pg013_manual_add_hero.png`, `pas_dt_st006_manual_add_success_mini.png` |
| Scan details/edit — no illustration | `pas_dt_pg015_snapshot_detail_header.png`, `pas_dt_pg016_record_edit_accent_strip.png`, `pas_dt_es005_no_excluded_records.png`, `pas_dt_es007_no_records_in_snapshot.png` |
| Export — no illustration | `pas_dt_pg017_export_hero.png`, `pas_dt_es008_no_exportable_records.png` |
| Settings/privacy — no illustration | `pas_dt_pg018_settings_privacy_header.png`, `pas_dt_st005_privacy_local_only_scene.png` |
| Shared decorative strip — no screen pool | `pas_dt_sh006_nature_detail_strip.png` |
| Component accents — no screen pool | `pas_dt_mi001_pine_cluster.png`, `pas_dt_mi002_mountain_silhouette.png`, `pas_dt_mi003_shoreline_cluster.png`, `pas_dt_mi004_rock_cluster.png`, `pas_dt_mi005_bush_grass_cluster.png`, `pas_dt_mi006_cloud_group.png`, `pas_dt_mi007_warm_sun.png`, `pas_dt_mi008_blank_signpost.png`, `pas_dt_mi009_trail_segment.png`, `pas_dt_mi010_small_island.png`, `pas_dt_mi011_leaf_accent.png`, `pas_dt_mi012_discovery_spark.png` |
| Library cards — snapshot-tile concepts, prohibited as thumbnails | `pas_dt_tl001_snapshot_lake.png`, `pas_dt_tl002_snapshot_mountain.png`, `pas_dt_tl003_snapshot_forest.png`, `pas_dt_tl004_snapshot_moonrise.png`, `pas_dt_tl005_snapshot_trail.png`, `pas_dt_tl006_snapshot_valley.png` |
## Legacy in-app illustration

`app/src/main/res/drawable-nodpi/explorer_landscape_fade.png` is rejected and must not be used. Remove it and its UI reference. It is not part of either source pack and is not a candidate in the runtime map.
