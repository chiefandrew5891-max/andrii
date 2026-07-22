package com.andrey.beautyplanner

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileRatingTest {

    @Test
    fun formatsFractionalRatingForDisplay() {
        assertEquals("4.7 / 5.0", formatProfileRating(4.7f))
    }

    @Test
    fun computesStarFractionsForPartialStar() {
        assertEquals(
            listOf(1f, 1f, 1f, 1f, 0.7f),
            profileRatingStarFillFractions(4.7f)
        )
    }

    @Test
    fun clampsRatingIntoExpectedRange() {
        assertEquals(
            listOf(1f, 1f, 1f, 1f, 1f),
            profileRatingStarFillFractions(6.2f)
        )
        assertEquals(
            listOf(0f, 0f, 0f, 0f, 0f),
            profileRatingStarFillFractions(-1f)
        )
    }
}
