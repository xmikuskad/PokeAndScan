# Orthogonal scan lifecycle and quality model

The scan model uses a small lifecycle (`PROCESSING`, `INCOMPLETE`, `COMPLETE`) plus separate scope-completeness and warning/record-quality data. This avoids an ever-growing combined state enum such as `COMPLETE_WITH_WARNINGS` while still distinguishing interruption, deliberate finalization, partial scope, review issues, and missed screens. Discard is an explicit deletion action rather than a retained snapshot state.
