package com.falconsocka.pokeandscan.ui.illustration

import androidx.annotation.DrawableRes
import com.falconsocka.pokeandscan.R
import kotlin.random.Random

internal data class ScreenIllustrationPair(
    @param:DrawableRes @get:DrawableRes val light: Int,
    @param:DrawableRes @get:DrawableRes val dark: Int
) {
    @DrawableRes
    fun resourceFor(darkTheme: Boolean): Int = if (darkTheme) dark else light
}

internal enum class ScreenIllustrationPool(val options: List<ScreenIllustrationPair>) {
    Welcome(
        listOf(
            ScreenIllustrationPair(R.drawable.pas_lt_alpine_sunrise_lake_trail, R.drawable.pas_dt_pg001_welcome_hero),
            ScreenIllustrationPair(R.drawable.pas_lt_tranquil_mountain_lake_hiking_vista, R.drawable.pas_dt_sh001_hero_lake_panorama),
            ScreenIllustrationPair(R.drawable.pas_lt_peaceful_mountain_lake_panorama, R.drawable.pas_dt_sh002_forest_lake_mid_scene)
        )
    ),
    CaptureExplanation(
        listOf(
            ScreenIllustrationPair(R.drawable.pas_lt_sunlit_mountain_lake_trail, R.drawable.pas_dt_pg002_capture_explanation_hero),
            ScreenIllustrationPair(R.drawable.pas_lt_mountain_valley_hiking_trail, R.drawable.pas_dt_sh003_mountain_valley_path),
            ScreenIllustrationPair(R.drawable.pas_lt_scenic_mountain_trail_viewpoint, R.drawable.pas_dt_sh004_signpost_trail_scene)
        )
    ),
    Preparation(
        listOf(
            ScreenIllustrationPair(R.drawable.pas_lt_winding_trail_to_the_mountain_lake, R.drawable.pas_dt_pg005_preparation_hero)
        )
    ),
    NewScan(
        listOf(
            ScreenIllustrationPair(R.drawable.pas_lt_sunlit_valley_trail_panorama, R.drawable.pas_dt_pg004_new_scan_header)
        )
    ),
    ScansEmpty(
        listOf(
            ScreenIllustrationPair(R.drawable.pas_lt_mountain_valley_trail_at_sunrise, R.drawable.pas_dt_es001_no_scans_yet)
        )
    )
}

/** Selects a paired theme option once per visit and avoids an immediate repeat for multi-option pools. */
internal class ScreenIllustrationSelector(
    private val random: Random = Random.Default
) {
    private val previousOptionByPool = mutableMapOf<ScreenIllustrationPool, Int>()

    fun select(pool: ScreenIllustrationPool): ScreenIllustrationPair {
        val optionCount = pool.options.size
        val previousOption = previousOptionByPool[pool]
        val selectedIndex = when {
            optionCount <= 1 -> 0
            previousOption == null -> random.nextInt(optionCount)
            else -> {
                val indexExceptPrevious = random.nextInt(optionCount - 1)
                if (indexExceptPrevious >= previousOption) indexExceptPrevious + 1 else indexExceptPrevious
            }
        }
        previousOptionByPool[pool] = selectedIndex
        return pool.options[selectedIndex]
    }
}
