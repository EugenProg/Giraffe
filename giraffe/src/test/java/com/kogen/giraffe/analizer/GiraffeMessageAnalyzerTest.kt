package com.kogen.giraffe.analizer

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType
import io.mockk.mockk
import org.junit.Test

class GiraffeMessageAnalyzerTest {

    private val analyzer = GiraffeMessageAnalyzer(mockk<Context>(relaxed = true))

    // --- unescapeProtobufString / tryDecodeAsText -----------------------------------------

    @Test
    fun `unescapeProtobufString decodes standard C-style escapes`() {
        val bytes = analyzer.unescapeProtobufString("\\n\\t\\\"\\\\")

        assertThat(bytes).isEqualTo(byteArrayOf(0x0A, 0x09, 0x22, 0x5C))
    }

    @Test
    fun `unescapeProtobufString decodes up to three octal digits`() {
        val bytes = analyzer.unescapeProtobufString("\\101\\102")

        assertThat(bytes).isEqualTo(byteArrayOf('A'.code.toByte(), 'B'.code.toByte()))
    }

    @Test
    fun `tryDecodeAsText returns the decoded string for a clean round trip`() {
        assertThat(analyzer.tryDecodeAsText("hello")).isEqualTo("hello")
    }

    @Test
    fun `tryDecodeAsText returns null when the escaped bytes are not valid UTF-8`() {
        // A lone 0xFF byte is never a valid standalone UTF-8 sequence.
        assertThat(analyzer.tryDecodeAsText("\\377")).isNull()
    }

    // --- escapeLikeProtobuf ------------------------------------------------------------------

    @Test
    fun `escapeLikeProtobuf keeps printable ASCII and escapes control and high bytes`() {
        val escaped = analyzer.escapeLikeProtobuf(byteArrayOf('A'.code.toByte(), 0x0A, 0x89.toByte()))

        assertThat(escaped).isEqualTo("A\\n\\211")
    }

    // --- truncateForDb -------------------------------------------------------------------------

    @Test
    fun `truncateForDb leaves short text untouched and passes null through`() {
        assertThat(analyzer.truncateForDb("short")).isEqualTo("short")
        assertThat(analyzer.truncateForDb(null)).isNull()
    }

    @Test
    fun `truncateForDb cuts text longer than maxLength`() {
        assertThat(analyzer.truncateForDb("0123456789", maxLength = 4)).isEqualTo("0123")
    }

    // --- cutMediaFromString --------------------------------------------------------------------

    @Test
    fun `cutMediaFromString replaces the escaped media run with a placeholder`() {
        val mediaBytes = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val escapedMedia = analyzer.escapeLikeProtobuf(mediaBytes)
        val fullString = "data: \"$escapedMedia\"\nother: \"1\""

        val result = analyzer.cutMediaFromString(fullString, mediaBytes, placeholder = "IMAGE")

        assertThat(result).isEqualTo("data: \"IMAGE\"\nother: \"1\"")
    }

    @Test
    fun `cutMediaFromString returns the input unchanged when the edges are not found`() {
        val fullString = "no media bytes referenced here"

        val result = analyzer.cutMediaFromString(fullString, byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8), "IMAGE")

        assertThat(result).isEqualTo(fullString)
    }

    // --- analyze ---------------------------------------------------------------------------------

    private class FakeProtoMessage(private val repr: String) {
        override fun toString(): String = repr
    }

    @Test
    fun `analyze converts a protobuf-style toString into pretty JSON`() {
        val message = FakeProtoMessage("name: \"John\"\nage: \"30\"\n")

        val result = analyzer.analyze(message)

        assertThat(result.contentType).isEqualTo(GiraffeContentType.Json)
        assertThat(result.filePath).isNull()
        assertThat(result.textContent).contains("\"name\": \"John\"")
        assertThat(result.textContent).contains("\"age\": \"30\"")
    }

    @Test
    fun `analyze passes through plain non-JSON text as Unknown content`() {
        val message = FakeProtoMessage("just a plain log line with no colon separated fields")

        val result = analyzer.analyze(message)

        assertThat(result.contentType).isEqualTo(GiraffeContentType.Unknown)
        assertThat(result.textContent).isEqualTo(message.toString())
        assertThat(result.filePath).isNull()
    }
}
