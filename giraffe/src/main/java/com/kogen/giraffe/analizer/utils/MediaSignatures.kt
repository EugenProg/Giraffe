package com.kogen.giraffe.analizer.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

internal object MediaSignatures {
    private val JPEG = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
    private val PNG = byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte())
    private val GIF = byteArrayOf(0x47.toByte(), 0x49.toByte(), 0x46.toByte(), 0x38.toByte())
    private val WEBP =
        byteArrayOf(0x52.toByte(), 0x49.toByte(), 0x46.toByte(), 0x46.toByte())

    private val MP3 = byteArrayOf(0x49.toByte(), 0x44.toByte(), 0x33.toByte())
    private val WAV =
        byteArrayOf(0x52.toByte(), 0x49.toByte(), 0x46.toByte(), 0x46.toByte())

    private val MP4_FTYP = byteArrayOf(0x66.toByte(), 0x74.toByte(), 0x79.toByte(), 0x70.toByte())
    private val THREE_GP = byteArrayOf(0x33.toByte(), 0x67.toByte(), 0x70.toByte())

    fun isImage(bytes: ByteArray): Boolean {
        if (bytes.size < 4) return false
        return bytes.startsWith(JPEG) || bytes.startsWith(PNG) ||
                bytes.startsWith(GIF) || (bytes.startsWith(WEBP) && bytes.extractString(
            8,
            4
        ) == "WEBP")
    }

    fun isAudio(bytes: ByteArray): Boolean {
        if (bytes.size < 4) return false
        return bytes.startsWith(MP3) || (bytes.startsWith(WAV) && bytes.extractString(
            8,
            4
        ) == "WAVE")
    }

    fun isVideo(bytes: ByteArray): Boolean {
        if (bytes.size < 12) return false
        val hasFtyp = bytes[4] == MP4_FTYP[0] && bytes[5] == MP4_FTYP[1] &&
                bytes[6] == MP4_FTYP[2] && bytes[7] == MP4_FTYP[3]
        return hasFtyp
    }

    fun tryDecodeBase64(str: String): ByteArray? {
        val cleaned = str.substringAfter("base64,").trim()
        if (cleaned.length < 8 || cleaned.length % 4 != 0) return null
        return try {
            Base64.decode(cleaned, Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
        if (this.size < prefix.size) return false
        for (i in prefix.indices) {
            if (this[i] != prefix[i]) return false
        }
        return true
    }

    private fun ByteArray.extractString(offset: Int, length: Int): String {
        if (this.size < offset + length) return ""
        return String(this, offset, length, Charsets.US_ASCII)
    }

    fun tryDecodeProtobufOctal(str: String): ByteArray? {
        if (!str.contains("\\")) return null

        return try {
            val bytes = mutableListOf<Byte>()
            var i = 0
            while (i < str.length) {
                if (str[i] == '\\') {
                    if (i + 1 < str.length && str[i + 1].isDigit()) {
                        var length = 0
                        while (length < 3 && i + 1 + length < str.length && str[i + 1 + length].isDigit()) {
                            length++
                        }
                        val octalStr = str.substring(i + 1, i + 1 + length)
                        bytes.add(octalStr.toInt(8).toByte())
                        i += 1 + length
                    } else if (i + 1 < str.length) {
                        when (str[i + 1]) {
                            'n' -> bytes.add('\n'.code.toByte())
                            'r' -> bytes.add('\r'.code.toByte())
                            't' -> bytes.add('\t'.code.toByte())
                            'v' -> bytes.add(0x0B.toByte())
                            '\\' -> bytes.add('\\'.code.toByte())
                            '\"' -> bytes.add('\"'.code.toByte())
                            '\'' -> bytes.add('\''.code.toByte())
                            else -> bytes.add(str[i + 1].code.toByte())
                        }
                        i += 2
                    }
                } else {
                    bytes.add(str[i].code.toByte())
                    i++
                }
            }
            bytes.toByteArray()
        } catch (_: Exception) {
            null
        }
    }

    val PNG_END = byteArrayOf(0x49.toByte(),
        0x45.toByte(),
        0x4E.toByte(),
        0x44.toByte(),
        0xAE.toByte(),
        0x42.toByte(),
        0x60.toByte(),
        0x82.toByte())

    fun findEndOfMedia(bytes: ByteArray, start: Int, signatureEnd: ByteArray): Int {
        for (i in start until bytes.size - signatureEnd.size) {
            var match = true
            for (j in signatureEnd.indices) {
                if (bytes[i + j] != signatureEnd[j]) {
                    match = false
                    break
                }
            }
            if (match) return i + signatureEnd.size
        }
        return -1
    }
}

fun saveMediaToCache(
    context: Context,
    bytes: ByteArray,
    prefix: String,
    extension: String,
): String? {
    return try {
        val folder = File(context.cacheDir, "giraffe_media").apply { mkdirs() }
        val file = File(folder, "${prefix}_${UUID.randomUUID()}.$extension")
        FileOutputStream(file).use { it.write(bytes) }

        try {
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            Log.d(">>> tryDecode", "bitmap: ${bitmap.width}x${bitmap.height}, bytes: ${bytes.size}")
        } catch (e: Exception) {
            Log.e(">>>", e.toString())
        }


        file.absolutePath
    } catch (_: Exception) {
        null
    }
}