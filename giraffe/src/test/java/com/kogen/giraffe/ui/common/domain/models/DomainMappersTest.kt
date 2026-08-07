package com.kogen.giraffe.ui.common.domain.models

import com.google.common.truth.Truth.assertThat
import com.kogen.giraffe.db.entity.ChatWithDetails
import com.kogen.giraffe.db.entity.GiraffeChatEntity
import com.kogen.giraffe.db.entity.GiraffeHeaderEntity
import com.kogen.giraffe.db.entity.GiraffeMessageEntity
import com.kogen.giraffe.ui.common.presentation.extensions.timestampToDateTime
import org.junit.Test

class DomainMappersTest {

    @Test
    fun `GiraffeHeaderEntity maps field-for-field to its domain model`() {
        val entity = GiraffeHeaderEntity(
            id = 1,
            chatId = "chat-1",
            isResponse = true,
            key = "x-trace",
            value = "abc"
        )

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

        assertThat(domain.textContent).isEqualTo(
            org.json.JSONObject("""{"a":1,"b":"two"}""").toString(2)
        )
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
            GiraffeHeaderEntity(
                id = 1,
                chatId = "chat-1",
                isResponse = false,
                key = "k1",
                value = "v1"
            ),
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

    // --- GiraffeChat.toClipboardText -----------------------------------------------------------

    private fun domainChat(
        status: GiraffeChatStatus = GiraffeChatStatus.Ok,
        headers: List<GiraffeHeader> = emptyList(),
        messages: List<GiraffeMessage> = emptyList(),
    ) = GiraffeChat(
        id = "chat-1",
        url = "host/Service/Method",
        methodShortName = "Method",
        timestamp = 1_000L,
        status = status,
        headers = headers,
        messages = messages,
    )

    private fun domainMessage(
        id: Long,
        isIncoming: Boolean,
        textContent: String?,
        timestamp: Long = 2_000L
    ) =
        GiraffeMessage(
            id = id,
            isIncoming = isIncoming,
            contentType = GiraffeContentType.Json,
            textContent = textContent,
            filePath = null,
            timestamp = timestamp,
        )

    @Test
    fun `toClipboardText lists the request line, headers and bodies in wire order`() {
        val chat = domainChat(
            headers = listOf(
                GiraffeHeader(id = 1, isResponse = false, key = "x-request-id", value = "abc"),
                GiraffeHeader(id = 2, isResponse = true, key = "x-trace", value = "def"),
            ),
            messages = listOf(
                domainMessage(id = 1, isIncoming = false, textContent = "{\"q\":1}"),
                domainMessage(
                    id = 2,
                    isIncoming = true,
                    textContent = "{\"a\":1}",
                    timestamp = 3_000L
                ),
            ),
        )

        val text = chat.toClipboardText()
        val lines = text.lines()

        assertThat(lines[0]).isEqualTo("URL: host/Service/Method")
        assertThat(lines[1]).isEqualTo("Method: Method")
        assertThat(lines[2]).isEqualTo("Status: Ok")
        assertThat(lines[3]).isEqualTo("Start: ${1_000L.timestampToDateTime()}")
        assertThat(lines[4]).isEqualTo("End: ${3_000L.timestampToDateTime()}")
        assertThat(text).contains("▶ REQUEST x-request-id: abc")
        assertThat(text).contains("◀ RESPONSE x-trace: def")
        // The request body immediately follows its own "▶ REQUEST" marker, not the header one.
        assertThat(text.indexOf("▶ REQUEST\n{\"q\":1}")).isGreaterThan(-1)
        assertThat(text.indexOf("◀ RESPONSE\n{\"a\":1}")).isGreaterThan(text.indexOf("▶ REQUEST\n{\"q\":1}"))
    }

    @Test
    fun `toClipboardText omits the End line while the call is still in progress`() {
        val chat = domainChat(status = GiraffeChatStatus.InProgress)

        assertThat(chat.toClipboardText()).doesNotContain("End:")
    }

    @Test
    fun `toClipboardText skips messages with no text content`() {
        val chat = domainChat(
            messages = listOf(
                domainMessage(id = 1, isIncoming = false, textContent = "  "),
                domainMessage(id = 2, isIncoming = false, textContent = null),
                domainMessage(id = 3, isIncoming = true, textContent = "{\"ok\":true}"),
            ),
        )

        val text = chat.toClipboardText()

        assertThat(text).contains("{\"ok\":true}")
        assertThat(text.lines().count { it == "▶ REQUEST" || it == "◀ RESPONSE" }).isEqualTo(1)
    }

    @Test
    fun `toClipboardText never includes a file path`() {
        val chat = domainChat(
            messages = listOf(
                GiraffeMessage(
                    id = 1,
                    isIncoming = true,
                    contentType = GiraffeContentType.Image,
                    textContent = "{\"data\":\"Image\"}",
                    filePath = "/cache/giraffe_media/img_secret.png",
                    timestamp = 2_000L,
                ),
            ),
        )

        assertThat(chat.toClipboardText()).doesNotContain("img_secret.png")
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
