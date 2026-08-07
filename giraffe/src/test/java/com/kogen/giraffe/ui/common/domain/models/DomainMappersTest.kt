package com.kogen.giraffe.ui.common.domain.models

import com.google.common.truth.Truth.assertThat
import com.kogen.giraffe.db.entity.ChatWithDetails
import com.kogen.giraffe.db.entity.GiraffeChatEntity
import com.kogen.giraffe.db.entity.GiraffeHeaderEntity
import com.kogen.giraffe.db.entity.GiraffeMessageEntity
import org.junit.Test

class DomainMappersTest {

    @Test
    fun `GiraffeHeaderEntity maps field-for-field to its domain model`() {
        val entity = GiraffeHeaderEntity(id = 1, chatId = "chat-1", isResponse = true, key = "x-trace", value = "abc")

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo(1L)
        assertThat(domain.isResponse).isTrue()
        assertThat(domain.key).isEqualTo("x-trace")
        assertThat(domain.value).isEqualTo("abc")
    }

    @Test
    fun `GiraffeMessageEntity pretty-prints JSON object text content`() {
        val entity = message(textContent = """{"a":1,"b":"two"}""")

        val domain = entity.toDomain()

        assertThat(domain.textContent).isEqualTo(org.json.JSONObject("""{"a":1,"b":"two"}""").toString(2))
    }

    @Test
    fun `GiraffeMessageEntity pretty-prints JSON array text content`() {
        val entity = message(textContent = """[1,2,3]""")

        val domain = entity.toDomain()

        assertThat(domain.textContent).isEqualTo(org.json.JSONArray("""[1,2,3]""").toString(2))
    }

    @Test
    fun `GiraffeMessageEntity leaves non-JSON text content untouched`() {
        val entity = message(textContent = "plain text, not JSON")

        assertThat(entity.toDomain().textContent).isEqualTo("plain text, not JSON")
    }

    @Test
    fun `GiraffeMessageEntity leaves malformed JSON-looking text untouched instead of throwing`() {
        val entity = message(textContent = "{not actually valid json")

        assertThat(entity.toDomain().textContent).isEqualTo("{not actually valid json")
    }

    @Test
    fun `GiraffeMessageEntity passes null text content through`() {
        val entity = message(textContent = null)

        assertThat(entity.toDomain().textContent).isNull()
    }

    @Test
    fun `ChatWithDetails maps the chat plus all nested headers and messages`() {
        val chat = GiraffeChatEntity(
            chatId = "chat-1",
            url = "example.com/svc/Method",
            methodShortName = "Method",
            timestamp = 42L,
            status = GiraffeChatStatus.Ok,
        )
        val headers = listOf(
            GiraffeHeaderEntity(id = 1, chatId = "chat-1", isResponse = false, key = "k1", value = "v1"),
        )
        val messages = listOf(message(id = 5, textContent = "hi"))
        val details = ChatWithDetails(chat = chat, headers = headers, messages = messages)

        val domain = details.toDomain()

        assertThat(domain.id).isEqualTo("chat-1")
        assertThat(domain.url).isEqualTo("example.com/svc/Method")
        assertThat(domain.methodShortName).isEqualTo("Method")
        assertThat(domain.timestamp).isEqualTo(42L)
        assertThat(domain.status).isEqualTo(GiraffeChatStatus.Ok)
        assertThat(domain.headers).hasSize(1)
        assertThat(domain.headers.single().key).isEqualTo("k1")
        assertThat(domain.messages).hasSize(1)
        assertThat(domain.messages.single().id).isEqualTo(5L)
    }

    private fun message(id: Long = 1, textContent: String?) = GiraffeMessageEntity(
        id = id,
        chatId = "chat-1",
        isIncoming = true,
        contentType = GiraffeContentType.PlainText,
        textContent = textContent,
        filePath = null,
        timestamp = 100L,
    )
}
