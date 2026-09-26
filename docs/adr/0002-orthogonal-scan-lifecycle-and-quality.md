# Orthogonal scan lifecycle and quality model

The scan model uses a small lifecycle (`SETUP`, `PROCESSING`, `INCOMPLETE`, `COMPLETE`) plus separate scope-completeness and warning/record-quality data. `SETUP` reserves a resumable session identity before capture and is not an active job. This avoids an ever-growing combined state enum such as `COMPLETE_WITH_WARNINGS` while still distinguishing pre-capture setup, interruption, deliberate finalization, partial scope, review issues, and missed screens. Discard is an explicit deletion action rather than a retained snapshot state.
