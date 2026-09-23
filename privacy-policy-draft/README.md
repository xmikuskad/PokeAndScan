# PokeAndScan privacy policy draft

This folder is a staging copy for the standalone `xmikuskad/pokeandscan-privacy-policy` repository requested in issue 22. It is not the Android app's final privacy URL and is not ready to publish.

## Publish and update

After the maintainer approves the Slovak and English wording and contact details, copy these files into the standalone public repository, enable GitHub Pages from its default branch, and verify the public HTTPS page on mobile and desktop. Keep the page static; it needs no build step or third-party scripts. Update the effective date and both language versions whenever actual data practices change. Keep this README as maintainer guidance and do not copy it into the public page.

Once Pages returns its canonical URL, record that URL in the Android project and wire the Settings privacy action to it. Do not use a guessed or temporary URL in the app.

## Publication blockers found during source review

- The current Android scaffold does not implement scanning, review, or exports. The page distinguishes those planned MVP behaviors from the current build.
- `AndroidManifest.xml` currently sets `android:allowBackup="true"`. The referenced legacy and Android 12+ backup rule files contain only template comments, while the approved technical design requires app data and device transfer to be excluded. Resolve and verify this mismatch before claiming that MVP data is not backed up.
- The privacy contact is assembled by first-party JavaScript from separate address components, with a `[at]`/`[dot]` fallback when JavaScript is unavailable. This only reduces simple harvesting; it does not prevent all bots or scraping.
- Public publication and final treatment of the policy remain subject to maintainer review and approval as required by issue 22.
