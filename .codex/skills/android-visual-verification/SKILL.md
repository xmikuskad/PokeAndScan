---
name: android-visual-verification
description: Verify completed Flutter Android UI on an emulator with current-spec-first evidence, screenshot comparison, adversarial review passes, and a fresh final verdict. Use for final visual QA, evidence verification, layout inspection, typography review, responsive behavior, or interaction verification after implementation.
---

# Android visual verification

Use a reference-first emulator check for the final verdict or changes that
require a rebuild. Live hot-reload iteration is defined separately in
`android-live-coding`.

The separate `android-live-coding` skill handles hot-reload iteration. This skill
owns the final evidence verdict, with the fresh emulator screenshot checked
against the applicable design-review MD and reference image. The MD is
authoritative; the reference image is supporting context and may contain
documented AI variations, device framing, or intentionally non-identical
details. Screenshots are temporary verification material: delete every
screenshot created by this skill after inspection unless the user explicitly
asks to retain it.

Act as a senior product designer and Flutter UI engineer reviewing somebody
else's implementation. The goal is to find defects, not to confirm that the
screen renders. Separate implementation mode from review mode: finish the
requested change, then review it as potentially wrong before fixing findings.

Before judging pixels, create an intentional-difference ledger. Record every
reference mismatch that the MD/spec says is expected. Classify other
differences as `intentional product change`, `implementation defect`, or
`reference drift`; a stale reference is not a defect by itself.

## Workflow

1. Read `AGENTS.md` and `docs/agents/build-and-test.md` before running Flutter commands. Locate the applicable `docs/design-reviews/*.md` file and its referenced screenshot(s). Read the relevant theme/components. Record route, data state, locale, theme, viewport, device, scroll position, frame treatment, and unknown fields in an evidence manifest. Create the intentional-difference ledger before review. Preserve existing user changes. Completion criterion: the applicable spec, reference, theme, manifest, and ledger are identified.
2. Check `adb devices` and `flutter devices`. `adb`, `flutter`, and `dart` commands must be run outside the sandbox because the Android SDK, emulator connection, Flutter SDK, and caches live outside the project workspace. Select the first ready `emulator-*` serial automatically unless a specific emulator is required. If no emulator is available, report the blocker instead of substituting web or desktop. Completion criterion: a ready emulator serial is selected or the blocker is reported.
3. Run evidence verification with
   `.codex/skills/android-visual-verification/scripts/run-android-visual-check.ps1`
   from the repository root. Override `-DeviceId`, `-ScreenshotPath`,
   `-SkipBuild`, or `-LaunchWaitSeconds` only when needed. This mode performs
   the build/install/launch check and waits for the app's cold start before
   capturing. Completion criterion: the clean evidence script finishes and
   produces a fresh emulator screenshot.
4. Use evidence verification after changes to `pubspec.yaml`, Android/Gradle configuration, the manifest, plugins, native Android code, or whenever a clean install is part of the acceptance criteria. Hot reload does not validate those changes. Completion criterion: every applicable clean-install requirement is covered by the evidence run.
5. Validate the evidence manifest before judging the UI. If any field is unknown, set it to `unknown`; do not infer equivalence. Completion criterion: no manifest field is silently inferred.
6. Open the clean reference and fresh emulator PNG separately, then inspect them side by side and with the optional overlay/diff artifacts. Align route/state/viewport before treating movement as meaningful. Treat explicit MD corrections as authoritative over pixels. Completion criterion: macro and detail evidence has been inspected.
7. Perform six adversarial passes: macro composition; typography; geometry/rhythm; styling/detail; consistency; final gestalt. Record every applicable category as `PASS`, `FAIL`, `ACCEPTED VARIANCE`, or `NOT VERIFIED`. Run the detail gate below before assigning `PASS` to typography, geometry, or styling. Completion criterion: every applicable category has a status and supporting evidence.

   Cover proportions, density, balance, grouping, whitespace, hierarchy;
   resolved font family/size/weight/line height, wrapping, truncation and
   baseline; actual bounds, alignment, padding, gaps, safe areas, tap targets
   and repeated dimensions; radius, borders, dividers, elevation, clipping and
   Material defaults; icon optical alignment, weight, crop and fit; narrow
   widths, localization, keyboard, overflow and flex behavior where relevant.
   Ask what a senior designer would send back. Do not invent a defect to meet
   a quota. Completion criterion: every category has a status and evidence or
   an explicit not-applicable reason.
   The detail gate is mandatory: compare rendered colors against the resolved
   theme/design tokens for text, actions, surfaces, component states, focus,
   and dividers; compare font family, size, weight, line height, and baseline
   for every visible text role; inspect every shared-component boundary for
   the required divider; verify alignment of repeated headers, values, icons,
   and controls; and test content-driven sizing with short/long strings,
   localized strings, repeated items, and narrow viewports. Any emphasized
   state must prove both its icon/decoration placement and its required
   typography or contrast treatment. Skip categories that the screen does not
   contain, but record them as not applicable rather than assuming they pass.
8. Use suspicion-driven inspection. When the screenshot raises a concern,
   inspect the resolved runtime snapshot if the project provides one, then the
   relevant Flutter source. Prefer actual rendered bounds and resolved
   `TextStyle` values over source declarations alone; `theme.textTheme.titleMedium`
   does not prove the final rendered values. Do not dump the whole widget tree.
   See [runtime-ui-inspection.md](references/runtime-ui-inspection.md).
   Completion criterion: each non-trivial finding has visual evidence plus
   runtime or source evidence where feasible.
9. Report findings before changing UI. Each finding states severity (`HIGH`,
   `MEDIUM`, or `LOW`), element, visual evidence, expected relationship or
   spec/token, likely implementation cause, and a concrete fix/inspection.
   “Spacing feels off” is incomplete. Severity reflects product impact, not
   pixel count. Completion criterion: high-impact findings precede polish.
10. Fix root causes with existing tokens/components and responsive structure.
    Group related final fixes into a checkpoint batch when safe, then run one
    fresh evidence verification after the batch. A build pass, absence of overflow
    logs, or broad resemblance is not a stop condition. Completion criterion:
    no unresolved HIGH or obvious MEDIUM defect remains, every MD row is `PASS`
    or `ACCEPTED VARIANCE`, every detail gate item has evidence, and final
    evidence matches the manifest. If the fresh verification exposes a new
    defect, record it, fix it in the next batch, and repeat evidence verification
    until this criterion is satisfied.
11. Keep exact reference, actual, snapshot, overlay, diff, and checklist paths
   while reporting. Delete temporary screenshots, snapshots, overlays, and
   diff artifacts after the user has received or explicitly declined the
   evidence, unless retention was requested. Completion criterion: the report
   contains the exact evidence paths and temporary artifacts are removed or
   explicitly retained.

## Failure handling

- If `adb` is not on PATH, use the SDK path from the repository instructions or standard Windows Android SDK location.
- If Gradle is stuck, stop only the Gradle daemons started for this check, then retry one clean build.
- If dependencies cannot be downloaded, report the failure; do not edit dependency versions.
- If the app crashes or the focused window is not the target application, capture evidence and mark the visual verdict `not verified`.

## Verdict gate

Use exactly one final verdict:

- `verified`: the target application is focused; fresh screenshot and applicable reference
  were inspected; the manifest matches; every MD row is `PASS` or `ACCEPTED
  VARIANCE`; every detail-gate item has concrete evidence; and no ambiguity or
  unresolved HIGH/obvious MEDIUM finding remains.
- `not verified`: required evidence, state, route, runtime check, or MD row is
  missing/unknown, or the app is not focused/crashed.
- `failed`: evidence is complete and shows an unresolved requirement or visual
  defect.

Pixel diff is supporting diagnostics. Runtime metadata is supporting evidence.
The clean rendered screenshot and current written requirements remain required.

The deterministic command mechanics live in [scripts/run-android-visual-check.ps1](scripts/run-android-visual-check.ps1). The comparison helper can additionally produce `-OverlayPath <path>` and `-SideBySidePath <path>` artifacts for human/LLM inspection.
