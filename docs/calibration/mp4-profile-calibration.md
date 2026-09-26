# MP4 reference-profile calibration

Status: **UNAPPROVED — reference evidence unavailable**  
Issue: [#5 — Calibrate MP4 profile preflight using labeled reference fixtures](https://github.com/xmikuskad/PokeAndScan/issues/5)  
Checked: 2026-09-26

## Decision

No profile classification threshold is approved. There is no evidence in this workspace from the agreed Pixel 9 Pro XL reference setup, so this report does not label any layout as supported, a minor deviation, or significantly unsupported. The preflight classification gate remains open as required by the [technical design](../technical-design.md) and [business rules](../business-specification.md).

## Evidence checked

- `adb devices -l` lists only `emulator-5554` (`sdk_gphone64_x86_64`); no Pixel 9 Pro XL is connected.
- A search of workspace files (including ignored files, excluding VCS and generated build state) found no `.mp4`/`.MP4` recordings or calibration fixtures.
- The app source contains MP4 setup and snapshot-source UI, but no MP4 profile classifier or threshold implementation to calibrate.
- Issue #5 has no comments containing recordings or additional labeling facts.

Reproduce the local inventory checks from the repository root:

```powershell
adb devices -l
rg --files --hidden --no-ignore -g '*.mp4' -g '*.MP4' -g '!**/.git/**' -g '!**/.gradle/**' -g '!**/build/**'
rg -n -i 'mp4|profile|preflight|video|orientation|resolution|aspect' app/src
```

On the checked workspace, the first command lists only the emulator, the second produces no paths (exit code 1 means no match), and the third finds MP4-related UI/source-type references but no classifier or calibrated thresholds.

## Missing evidence

No footage is available to establish any of the following:

- A valid reference-layout recording made on a Pixel 9 Pro XL running Android 10 or newer, in portrait, with English Pokémon GO and default Android display and font settings.
- Recorded device/build and capture details, plus measured video orientation, dimensions, and aspect ratio for each fixture.
- At least one labeled example of each class: valid reference layout, minor deviation, and significantly unsupported layout.
- Examples on both sides of each proposed decision boundary, with a rationale linking every label to the recorded setup and visible layout.
- Readable appraisal examples and a fixture with an audio track to verify audio does not affect profile classification.
- Review that candidate clips contain only permitted, anonymized Pokémon GO appraisal footage and no private notifications or unrelated content.

The available emulator is not evidence for the agreed physical-device profile. No emulator recording or synthetic metadata is substituted for the missing footage.

## Calibration record

| Decision | Value | Evidence / boundary examples |
| --- | --- | --- |
| Reference orientation | Unapproved | No reference fixture |
| Accepted dimensions | Unapproved | No reference fixture or boundary pair |
| Accepted aspect ratio | Unapproved | No reference fixture or boundary pair |
| Minor-deviation limits | Unapproved | No labeled deviation fixtures or boundary pair |
| Unsupported-layout limits | Unapproved | No labeled unsupported fixtures or boundary pair |
| Audio independence | Unverified | No fixture with an audio track |

There are currently zero fixtures. Consequently, no reproducible threshold-application report can classify them yet. Do not implement numeric cutoffs or present any layout as supported until the recordings, labels, and boundary evidence exist. Once supplied, record provenance and labels for every fixture, derive thresholds from examples on both sides of each boundary, and generate a report that flags missing metadata, disagreements, and ambiguous cases for human review.

## Inputs needed to resume

Provide or capture the reference and deviation recordings above, together with the device/build and capture facts needed to verify their labels. Keep the original source files available for review; before adding any media to the repository, confirm it contains only permitted, anonymized gameplay. Until those inputs are available, profile-specific preflight classifications remain unapproved.
