# PokeAndScan privacy policy draft

This folder is the standalone-site staging copy for `xmikuskad/pokeandscan-privacy-policy`, requested in issue 22. The site has two language URLs: `/en/` and `/sk/`.

## Publish and update

The public site uses `en/index.html` and `sk/index.html`; GitHub Pages serves them at:

- https://xmikuskad.github.io/pokeandscan-privacy-policy/en/
- https://xmikuskad.github.io/pokeandscan-privacy-policy/sk/

GitHub Pages is enabled from the default branch root. Shared first-party CSS and the contact link script live in `assets/`. Verify both HTTPS pages on mobile and desktop. Keep the pages static; they need no build step or third-party scripts. Update the date and both language versions whenever actual data practices change. Keep this README as maintainer guidance and do not copy it into the public page.

The Android Settings action opens the Slovak or English URL according to the saved app language. Change the base only if GitHub Pages reports a different canonical URL.

## Implementation notes for maintainers

- The current Android scaffold does not implement scanning, review, or exports. The page distinguishes those planned MVP behaviors from the current build.
- `AndroidManifest.xml` sets `android:allowBackup="false"`; the legacy and Android 12+ rules explicitly exclude app data. Some Android manufacturers may not honor device-to-device transfer exclusions, so avoid an absolute guarantee for every device.
- The privacy contact is assembled by first-party JavaScript from separate address components, with a `[at]`/`[dot]` fallback when JavaScript is unavailable. This only reduces simple harvesting; it does not prevent all bots or scraping.
- The owner requested publication of these pages in the public repository. Review the wording and both URLs whenever actual data practices change.
