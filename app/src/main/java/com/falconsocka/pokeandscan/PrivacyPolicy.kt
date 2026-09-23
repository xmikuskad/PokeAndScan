package com.falconsocka.pokeandscan

private const val PRIVACY_POLICY_BASE_URL = "https://xmikuskad.github.io/pokeandscan-privacy-policy"

internal fun privacyPolicyUrl(language: AppLanguage): String =
    "$PRIVACY_POLICY_BASE_URL/${language.languageTag}/"
