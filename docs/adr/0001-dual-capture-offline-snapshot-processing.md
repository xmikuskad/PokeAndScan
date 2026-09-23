# Dual capture with offline snapshot processing

The application is Android-only and supports two user-controlled capture sources: live screen capture as the preferred workflow and imported Android screen recordings as the fallback. Both sources produce the same local scan model, but a scan session uses exactly one source. The parser and review pipeline are implemented against a source-independent frame contract, with MP4 import built first because it provides deterministic replay and regression testing before live `MediaProjection` is added. Both sources are required for MVP completion; live capture is the second implementation stage of MVP, not a post-MVP feature.

Processing is offline and local. Each session produces an independent snapshot; later scans do not silently merge into or overwrite earlier snapshots. The application stores review evidence crops rather than copying the complete source video by default. The product never automates Pokémon GO input or depends on credentials, private APIs, process access, root, or overlays.

## Consequences

- The first supported profile is Pixel 9 Pro XL, portrait, English UI, default display and font settings, with MP4 Android screen recordings.
- Live capture needs Android permission and foreground-service handling, but it does not require a second parsing architecture. MVP acceptance must cover both replayed MP4 and live capture.
- iOS-produced recordings, additional devices, nicknames, status flags, moves, items, and other metadata remain later compatibility/features rather than MVP requirements.
