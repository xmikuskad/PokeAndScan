package com.falconsocka.pokeandscan

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {
    @Test
    fun slovakSystemLanguageSelectsSlovak() {
        assertEquals(AppLanguage.Slovak, initialLanguage("sk-SK"))
    }

    @Test
    fun everyNonSlovakSystemLanguageSelectsEnglish() {
        assertEquals(AppLanguage.English, initialLanguage("en-US"))
        assertEquals(AppLanguage.English, initialLanguage("de-DE"))
    }
}
