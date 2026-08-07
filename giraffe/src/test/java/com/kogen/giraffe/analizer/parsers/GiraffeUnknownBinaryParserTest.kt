package com.kogen.giraffe.analizer.parsers

import com.google.common.truth.Truth.assertThat
import com.kogen.giraffe.testutil.fakeContext
import com.kogen.giraffe.testutil.lengthDelimitedField
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class GiraffeUnknownBinaryParserTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val parser = GiraffeUnknownBinaryParser()

    @Test
    fun `saves any opaque binary leaf as an unknown-content blob`() {
        val binary = ByteArray(24) { (it * 37 xor 0x5A).toByte() }
        val originalBytes = lengthDelimitedField(fieldNumber = 11, payload = binary)
        val context = fakeContext(tempFolder.root)

        val result = parser.parse(originalBytes, context)

        assertThat(result).isNotNull()
        assertThat(result!!.contentType).isEqualTo(GiraffeContentType.Unknown)
        assertThat(result.bytes).isEqualTo(binary)
        val savedFile = File(result.filePath!!)
        assertThat(savedFile.readBytes()).isEqualTo(binary)
        assertThat(savedFile.extension).isEqualTo("bin")
    }

    @Test
    fun `returns null when the message has no binary leaves at all`() {
        val originalBytes = lengthDelimitedField(
            fieldNumber = 11,
            payload = "just a short plain-text field".toByteArray(),
        )
        val context = fakeContext(tempFolder.root)

        assertThat(parser.parse(originalBytes, context)).isNull()
    }
}
