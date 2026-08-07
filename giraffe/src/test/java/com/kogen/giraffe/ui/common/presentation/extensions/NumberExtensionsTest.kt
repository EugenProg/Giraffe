package com.kogen.giraffe.ui.common.presentation.extensions

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.TimeZone

class NumberExtensionsTest {

    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun `msToDurationText formats sub-minute durations as 0 colon seconds`() {
        assertThat(0.msToDurationText()).isEqualTo("0:00")
        assertThat(5_000.msToDurationText()).isEqualTo("0:05")
        assertThat(59_000.msToDurationText()).isEqualTo("0:59")
    }

    @Test
    fun `msToDurationText carries whole minutes and zero-pads the seconds`() {
        assertThat(65_000.msToDurationText()).isEqualTo("1:05")
        assertThat(600_000.msToDurationText()).isEqualTo("10:00")
    }

    @Test
    fun `timestampToTime reports hour, minute and second at UTC midnight epoch`() {
        assertThat(0L.timestampToTime()).isEqualTo("0:0:0")
        // 1h 2m 3s after the epoch.
        assertThat(3_723_000L.timestampToTime()).isEqualTo("1:2:3")
    }

    @Test
    fun `timestampToDateTime matches java-util-Date's own toString`() {
        val ts = 1_700_000_000_000L

        assertThat(ts.timestampToDateTime()).isEqualTo(Date(ts).toString())
    }
}
