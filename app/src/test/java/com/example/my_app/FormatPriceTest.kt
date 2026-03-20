package com.example.my_app

import com.example.my_app.ui.screens.formatPrice
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatPriceTest {

    @Test
    fun testFormatPrice_withThousands() {
        val result = formatPrice(13500.83)
        assertEquals("13 500,83", result)
    }

    @Test
    fun testFormatPrice_withoutThousands() {
        val result = formatPrice(500.5)
        assertEquals("500,50", result)
    }
}