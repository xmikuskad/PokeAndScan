# Brand Direction

**Status:** MVP brand direction and decisions confirmed; production asset files and public-launch clearance remain pending
**Date:** 2026-09-21

The overall visual direction is confirmed as **Friendly Explorer**. The supplied external exploration is imported in [uiux-reference](./uiux-reference/README.md). The MVP name, brand route, product typography, color tokens, voice, tagline, and icon variant requirements are confirmed here and in the design specification. Final production-ready artwork files have not yet been created; public distribution remains subject to a name-availability/trademark review and store-asset review.

## MVP product name

Use **PokeAndScan** as the confirmed product and app name throughout MVP development and testing.

The name is descriptive, matches the repository, and communicates the two core actions: inspecting a Pokémon collection and scanning user-visible screens. It is not a claim of affiliation with Pokémon GO or its rights holders.

Before public distribution, the name must receive a separate availability and trademark review. If that review identifies a material conflict, revisit the public-facing name before release. This document is a product/brand direction, not a legal opinion.

## Positioning

**Category:** offline Pokémon collection scanner and exporter  
**Audience:** players who want structured local data from a large collection  
**Promise:** turn a manual screen scan into a reviewable export without requiring game credentials or cloud upload

## Descriptor

Use this descriptor in the app store draft and onboarding:

> Offline Pokémon collection scanner and exporter

## Primary brand route

Use **Concept 1 — Scan Frame + Path** as the only active MVP logo and app-icon direction.

The mark combines a scan frame with a path through a simple landscape. It should be refined into production-ready full-color, dark-background, monochrome, adaptive-icon, and small-size variants. The visual reference handoff contains other exploratory concepts, but they are not active alternatives for this product.

Brand lockups pair the Concept 1 symbol with the `PokeAndScan` wordmark. The wordmark uses dark navy for `PokeAnd` and primary blue for `Scan`. The launcher icon uses the symbol alone. Add the primary tagline only to lockups with enough space; it is not required beneath every logo instance.

MVP launcher asset set:

- full-color adaptive launcher icon as the default;
- monochrome version for Android themed-icon support;
- simplified small-size variant preserving the scan frame and path silhouette.

All variants use Concept 1 and must remain recognizable without relying on color alone.

## Typography

Use **Plus Jakarta Sans** throughout the product UI and brand materials. It must support Slovak diacritics, Android font scaling, readable CP/IV values, and layouts that reflow rather than clip critical labels or actions.

## UI iconography

Use Material Symbols Rounded as the consistent functional icon family. Concept 1 — Scan Frame + Path is the only custom brand icon; do not mix additional icon families into the UI.

## Tagline

Working tagline:

> Your collection, captured clearly.

It emphasizes clarity and reviewability rather than automation or an official data connection.

This is the only primary product tagline. `Explore. Scan. Keep control.` may appear as occasional campaign/hero copy, but it is not a parallel product slogan. `Scan. Explore. Organize.` is not used as the main tagline.

## Voice

- friendly, calm, clear, and capable;
- concise and practical;
- transparent about uncertainty;
- respectful of the user's collection;
- technical only when it helps the user act.

Avoid:

- claims of perfect or guaranteed recognition;
- “official”, “powered by Pokémon GO”, or similar affiliation language;
- aggressive automation language;
- alarmist or overly cute error copy;
- gamification that pressures users to hide or fill Unknown values;
- hiding partial results behind a success state.

## Visual direction

- Friendly Explorer: approachable but not childish, trustworthy before playful, and capable enough for long scanning/review sessions;
- blue-led primary actions: `#0067D6` in light theme and `#60A5FA` in dark theme;
- limited secondary violet (`#7C3AED` Light / `#A78BFA` Dark) for supporting brand emphasis, not primary actions;
- coral (`#FF6B6B` Light / `#FF7A7A` Dark) reserved for decorative illustration accents, not error semantics;
- amber reserved for review and warning states, green for ready/confirmed states, and red/crimson for errors and destructive confirmation;
- calm nature/exploration motifs used mainly for onboarding, education, empty states, and recovery;
- restrained, evidence-first data screens with explicit uncertainty;
- light and dark themes are both first-class experiences;
- no Pokémon GO logo, official character art, Poké Ball-like product marks, or copied UI assets in the product identity.

## Naming rules for features

Prefer plain product terms:

- Live capture
- Import MP4
- Appraisal scan
- Review issues
- Scan / Sken
- Export CSV
- Export JSON

Keep `Snapshot` as an internal/domain term rather than a normal user-facing noun. Avoid other internal terms in user-facing copy:

- OCR;
- ROI;
- temporal deduplication;
- classifier;
- parser confidence score.

## Brand decision boundary

The MVP uses the confirmed name PokeAndScan. A public Google Play launch requires a name-availability/trademark review, store-copy review, and final production-asset review; only a material issue from that review reopens the name decision.
