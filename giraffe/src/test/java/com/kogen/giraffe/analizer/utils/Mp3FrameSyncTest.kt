package com.kogen.giraffe.analizer.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class Mp3FrameSyncTest {

    /**
     * Builds one valid MPEG-1 Layer III frame header (frame sync + version + layer + a
     * 128 kbps / 44100 Hz bitrate/sample-rate pair) followed by [bodySize] filler bytes,
     * matching the 418-byte frame length that combination produces.
     */
    private fun mp3Frame(bodySize: Int = FRAME_LENGTH - 4): ByteArray {
        val header = byteArrayOf(
            0xFF.toByte(), // frame sync (byte 1/2)
            0xFB.toByte(), // sync cont. + MPEG-1 (11) + Layer III (01) + no CRC (1)
            0x90.toByte(), // bitrate index 9 (128kbps) + sample rate index 0 (44100) + no padding
            0x00,
        )
        return header + ByteArray(bodySize)
    }

    @Test
    fun `findValidatedStart locates a chain of consecutive valid frames at offset zero`() {
        val bytes = mp3Frame() + mp3Frame() + mp3Frame()

        assertThat(Mp3FrameSync.findValidatedStart(bytes)).isEqualTo(0)
    }

    @Test
    fun `findValidatedStart skips leading garbage that is not a real frame sync`() {
        val garbage = byteArrayOf(1, 2, 3, 4, 5, 6)
        val bytes = garbage + mp3Frame() + mp3Frame() + mp3Frame()

        assertThat(Mp3FrameSync.findValidatedStart(bytes)).isEqualTo(garbage.size)
    }

    @Test
    fun `findValidatedStart requires the configured number of chained frames`() {
        // Only two valid frames in a row is not enough evidence of a real MP3 stream.
        val bytes = mp3Frame() + mp3Frame()

        assertThat(Mp3FrameSync.findValidatedStart(bytes, minChainedFrames = 3)).isNull()

        assertThat(Mp3FrameSync.findValidatedStart(bytes, minChainedFrames = 2)).isEqualTo(0)
    }

    @Test
    fun `findValidatedStart returns null for arbitrary non-audio bytes`() {
        val bytes = ByteArray(64) { (it * 13).toByte() }

        assertThat(Mp3FrameSync.findValidatedStart(bytes)).isNull()
    }

    @Test
    fun `findValidatedStart returns null when input is too short for a full header`() {
        assertThat(Mp3FrameSync.findValidatedStart(byteArrayOf(0xFF.toByte(), 0xFB.toByte()))).isNull()
    }

    companion object {
        // 144 * 128000 / 44100 (+0 padding), per the MPEG-1 Layer III frame-length formula.
        private const val FRAME_LENGTH = 144 * 128_000 / 44_100
    }
}
