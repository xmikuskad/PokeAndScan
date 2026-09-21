---
name: android-live-coding
description: Iterate on Flutter Android UI live on an emulator with checkpoint batches, hot reload, targeted screenshots, and interaction-state checks. Use when implementing or tuning UI before final evidence verification.
---

# Android live coding

Use this skill for implementation-time visual feedback. It is a checkpoint
loop, not the final verdict. Invoke the `android-visual-verification` skill
after the requested changes are complete.

## Workflow

1. Read `AGENTS.md` and `docs/agents/build-and-test.md` before running Flutter
   commands. Locate the applicable `docs/design-reviews/*.md`, reference image,
   and any existing audit. Establish the evidence manifest: route, data state,
   locale, theme, viewport, device, scroll position, frame treatment, and
   unknown fields. Record intentional reference differences before judging
   pixels. Open and read the applicable design-review MD and reference image
   before editing. Completion criterion: the spec, reference, audit, manifest,
   and intentional-difference ledger are available.
2. Check `adb devices` and `flutter devices`. Run `adb`, `flutter`, and `dart`
   outside the sandbox. Select the first ready `emulator-*` serial unless the
   task specifies another. If no emulator is available, report the blocker;
   do not substitute web or desktop. Completion criterion: a ready emulator
   serial is selected or the blocker is reported.
3. Run `flutter run -d <DeviceId> --debug` from the repository root and keep
   the process attached. Wait for the Flutter process to report the app ready,
   restore the manifest route, then capture the initial state with
   `adb -s <DeviceId> exec-out screencap -p > <ScreenshotPath>`. Completion
   criterion: the attached process is ready and the initial screenshot reflects
   the manifest state.

## Checkpoint finding loop

1. Review one component group at a time: macro surface, header boundary,
   context, lists/tables/forms, repeated content, controls, navigation,
   keyboard, and interaction states. Completion criterion: every applicable
   component group has been inspected for the current state.
2. Record every mismatch before editing. A finding names the element, observed
   evidence, expected relationship or token, severity, likely root cause, and
   the smallest useful verification state. Record reusable findings in the
   audit MD. Completion criterion: every observed mismatch has a concrete
   finding or an explicit accepted-variance reason.
3. Group related findings into one **checkpoint batch**: one component group,
   one interaction state, or one visual hypothesis. Apply all related fixes in
   the batch before reloading. Keep the batch narrow enough that the screenshot
   can still be attributed to its shared cause. Complete all source edits in
   the batch before reloading. Completion criterion: the batch has no remaining
   in-scope source edit.
4. After the batch, send `r` for hot reload or `R` for hot restart and wait for
   the explicit completion message. Restore the exact route, data state,
   locale, theme, viewport, and scroll position from the manifest. Completion
   criterion: reload/restart reports completion and the manifest state is
   restored.
5. Capture one screenshot for the completed batch and compare every changed
   component plus its immediate neighbours. Mark findings `PASS` only when the
   expected relationship is visible, or `ACCEPTED VARIANCE` when the MD or
   intentional-difference ledger explicitly permits it. Completion criterion:
   every finding in the batch has a post-reload status.
6. Split the batch and verify immediately if the app stops compiling, crashes,
   loses the target route, or a change affects a separate state or component
   group. Changes to `pubspec.yaml`, Android/Gradle configuration, the manifest,
   plugins, or native Android code require final evidence verification.
   Completion criterion: an interrupted batch is split, revalidated, or
   escalated to final evidence verification.

## Interaction states

Check the applicable states for the screen:

- empty, loading, error, and populated;
- one item and multiple items, including first and last visible items;
- focus, keyboard-visible, validation, and submitted;
- positive, negative, zero, empty, selected, and disabled values;
- horizontal or vertical scroll at both ends with pinned content visible;
- long-press, overflow, confirmation, dismissal, and destructive actions;
- primary, secondary, disabled, and global-navigation actions.

For visual detail, expose emphasized and neutral items, short and long content,
localized strings, multi-digit values, and stable shared boundaries. Use
`adb shell input` for deterministic taps, swipes, and text entry. Capture a
screenshot after every meaningful interaction while validating that state; do
not infer visual success from logs or persisted data alone. Completion
criterion: every applicable interaction state has screenshot evidence.

## Completion

Live coding is complete when the audit has no unresolved HIGH or obvious MEDIUM
finding, every changed batch has a post-reload screenshot, every affected
interaction state has been rechecked, and the audit records the final status
for every applicable category. If a finding cannot be fixed within scope,
report it rather than carrying it silently into final verification.

Delete temporary screenshots and snapshots after inspection unless the user
explicitly asks to retain them.
