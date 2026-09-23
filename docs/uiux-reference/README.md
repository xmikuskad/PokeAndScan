# PokeAndScan UI/UX reference pack

This directory contains the imported v0.4 UI/UX handoff supplied from the external design exploration.

The handoff and boards are historical visual references. They are not the product specification and do not override the current business, design, technical, brand, or domain decisions in this repository. The handoff contains superseded options and outdated inventory text; use this guide to interpret it.

## Current visual direction proposed by the handoff

- Friendly Explorer
- blue-led light theme with a first-class dark theme
- calm nature/exploration illustrations
- evidence-first review and explicit uncertainty
- primary logo route: Concept 1 — Scan Frame + Path
- Concept 1 is the only active logo/app-icon route for this project; other concepts in the supplied handoff are historical exploration only.
- Brand lockups use the Concept 1 symbol plus a two-color `PokeAndScan` wordmark; the launcher icon uses the symbol alone.
- MVP launcher assets include a full-color adaptive icon, a monochrome themed-icon variant, and a simplified small-size variant, all based on Concept 1.

The confirmed primary action colors are `#0067D6` for Light and `#60A5FA` for Dark. Secondary violet (`#7C3AED`/`#A78BFA`) is limited to supporting brand emphasis; coral (`#FF6B6B`/`#FF7A7A`) is decorative only. The [design specification](../design-specification.md) defines the current surface, text, semantic, and geometry tokens; detailed component anatomy still needs an implementation pass.

The semantic state palette is confirmed from the handoff with `Missed appraisal` grouped with amber warnings. See the current [design specification](../design-specification.md) for exact light/dark foreground and container values.

The project decision supersedes the handoff's earlier system-font recommendation: product UI typography uses **Plus Jakarta Sans**.

The project status vocabulary is also authoritative: records use `Ready`, `Needs review`, `Partial`, and `Excluded`; `Missed appraisal` is an event/warning and `Confirmed by you` is provenance.

The primary product tagline is `Your collection, captured clearly.` Other taglines visible in the supplied boards are optional campaign copy or historical exploration, not parallel product slogans.

Functional UI icons use Material Symbols Rounded consistently; the Concept 1 brand mark is the only custom icon.

## Confirmed deviations from the supplied handoff

- Remove the `Include only ready items` export checkbox shown in the dark-theme export concept. MVP normal exports include all records except explicitly Excluded records; uncertainty remains visible as `UNKNOWN`/`null` and through review status.
- Remove the `Advanced`/Developer diagnostics area and the custom `Data storage`/`Export & import` settings rows shown in the handoff. MVP Settings contains Language, Theme, and Privacy as an external GitHub Pages link, plus static app name/version information; full data deletion remains an Android system action.
- Use amber warning semantics for `Missed appraisal`; the handoff's red missed state is not adopted because this is a possible scan gap, not a failed record.

## Important interpretation rules

- Generated boards are not pixel-perfect production specifications.
- Phone mockups are presentation devices; production UI must follow the Android/Pixel 9 Pro XL target.
- Production uses Android/Material 3 conventions; iOS-looking headers, system chrome, navigation, and permission patterns in mockups are not adopted.
- Pokémon-like characters, Poké Ball-like marks, copied game UI, and official artwork shown in concepts must not become product assets.
- Landscape thumbnails in concepts must not imply that arbitrary screenshots or full source videos are retained.
- The current local product documents remain the source of truth.

## Files

- [PokeAndScan_UIUX_Design_Handoff_v0.4.md](./v0.4/PokeAndScan_UIUX_Design_Handoff_v0.4.md)
- [style comparison board](./v0.4/01_style_comparison_board.png)
- [Friendly Explorer foundation](./v0.4/02_friendly_explorer_foundation_light.png)
- [capture flow](./v0.4/03_capture_flow_light.png)
- [review, library, and export](./v0.4/04_review_library_export_light.png)
- [capture explanation and MP4 processing](./v0.4/05_capture_explanation_and_mp4_processing.png)
- [recovery, manual add, and settings](./v0.4/06_recovery_manual_add_settings.png)
- [snapshot detail and record edit](./v0.4/07_snapshot_detail_and_record_edit.png)
- [dark theme core flow](./v0.4/08_dark_theme_core_flow.png)
- [dark theme results flow](./v0.4/09_dark_theme_results_flow.png)
- [nature illustration moodboard](./v0.4/10_nature_illustration_moodboard.png)
- [logo and app icon exploration](./v0.4/11_logo_and_app_icon_exploration.png)
- [logo refinement](./v0.4/12_logo_refinement_primary_vs_secondary.png)
- [app icon iterations](./v0.4/13_app_icon_iterations.png)
- [mini brand pack and style guide](./v0.4/14_mini_brand_pack_style_guide.png)
