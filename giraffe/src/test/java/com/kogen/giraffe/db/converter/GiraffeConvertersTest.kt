package com.kogen.giraffe.db.converter

import com.google.common.truth.Truth.assertThat
import com.kogen.giraffe.ui.common.domain.models.GiraffeChatStatus
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType
import org.junit.Test

class GiraffeConvertersTest {

    private val converters = GiraffeConverters()

    @Test
    fun `content type round-trips through its stored name`() {
        GiraffeContentType.entries.forEach { type ->
            val stored = converters.fromContentType(type)
            assertThat(converters.toContentType(stored)).isEqualTo(type)
        }
    }

    @Test
    fun `an unrecognized stored content type falls back to Unknown`() {
        assertThat(converters.toContentType("SomeFutureTypeThatDoesNotExistYet"))
            .isEqualTo(GiraffeContentType.Unknown)
    }

    @Test
    fun `chat status round-trips through its stored name`() {
        GiraffeChatStatus.entries.forEach { status ->
            val stored = converters.fromChatStatus(status)
            assertThat(converters.toChatStatus(stored)).isEqualTo(status)
        }
    }
}
