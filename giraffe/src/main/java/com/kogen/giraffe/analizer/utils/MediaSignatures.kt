package com.kogen.giraffe.analizer.utils

import android.content.Context
import android.util.Base64
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

internal object MediaSignatures {
    val JPEG = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
    val PNG = byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte())
    val GIF = byteArrayOf(0x47.toByte(), 0x49.toByte(), 0x46.toByte(), 0x38.toByte())
    val WEBP =
        byteArrayOf(0x52.toByte(), 0x49.toByte(), 0x46.toByte(), 0x46.toByte())
    val WEBP_TAG = byteArrayOf(0x57.toByte(), 0x45.toByte(), 0x42.toByte(), 0x50.toByte())

    val MP3 = byteArrayOf(0x49.toByte(), 0x44.toByte(), 0x33.toByte())
    val WAV =
        byteArrayOf(0x52.toByte(), 0x49.toByte(), 0x46.toByte(), 0x46.toByte())
    val WAVE_TAG = byteArrayOf(0x57.toByte(), 0x41.toByte(), 0x56.toByte(), 0x45.toByte())

    val MP4_FTYP = byteArrayOf(0x66.toByte(), 0x74.toByte(), 0x79.toByte(), 0x70.toByte())
    private val THREE_GP = byteArrayOf(0x33.toByte(), 0x67.toByte(), 0x70.toByte())

    fun tryDecodeBase64(str: String): ByteArray? {
        val cleaned = str.substringAfter("base64,").trim()
        if (cleaned.length < 8 || cleaned.length % 4 != 0) return null
        return try {
            Base64.decode(cleaned, Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun isLikelyUtf8Text(bytes: ByteArray): Boolean {
        return try {
            val decoded = String(bytes, Charsets.UTF_8)
            val reEncoded = decoded.toByteArray(Charsets.UTF_8)
            reEncoded.contentEquals(bytes) &&
                    decoded.none { it.code < 0x09 }
        } catch (_: Exception) {
            false
        }
    }

    val PNG_END = byteArrayOf(
        0x49.toByte(),
        0x45.toByte(),
        0x4E.toByte(),
        0x44.toByte(),
        0xAE.toByte(),
        0x42.toByte(),
        0x60.toByte(),
        0x82.toByte()
    )
    val JPEG_END = byteArrayOf(0xFF.toByte(), 0xD9.toByte())
    val GIF_END = byteArrayOf(0x3B.toByte())

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

    fun indexOf(bytes: ByteArray, signature: ByteArray, from: Int = 0): Int {
        for (i in from..bytes.size - signature.size) {
            var match = true
            for (j in signature.indices) {
                if (bytes[i + j] != signature[j]) {
                    match = false
                    break
                }
            }
            if (match) return i
        }
        return -1
    }

    fun findRiffEnd(bytes: ByteArray, riffStart: Int): Int {
        if (riffStart + 8 > bytes.size) return -1
        val chunkSize = (bytes[riffStart + 4].toInt() and 0xFF) or
                ((bytes[riffStart + 5].toInt() and 0xFF) shl 8) or
                ((bytes[riffStart + 6].toInt() and 0xFF) shl 16) or
                ((bytes[riffStart + 7].toInt() and 0xFF) shl 24)
        val end = riffStart + 8 + chunkSize
        return if (end in (riffStart + 8)..bytes.size) end else -1
    }

    fun findMp4End(bytes: ByteArray, ftypIndex: Int): Int {
        var pos = ftypIndex - 4
        if (pos < 0) return -1

        while (pos + 8 <= bytes.size) {
            val boxSize = ((bytes[pos].toInt() and 0xFF) shl 24) or
                    ((bytes[pos + 1].toInt() and 0xFF) shl 16) or
                    ((bytes[pos + 2].toInt() and 0xFF) shl 8) or
                    (bytes[pos + 3].toInt() and 0xFF)
            if (boxSize < 8) break

            val next = pos + boxSize
            if (next <= pos || next > bytes.size) break
            pos = next
        }

        return if (pos > ftypIndex) pos else -1
    }

    fun findLastEndOfMedia(bytes: ByteArray, start: Int, signatureEnd: ByteArray): Int {
        var result = -1
        var from = start
        while (true) {
            val idx = indexOf(bytes, signatureEnd, from)
            if (idx == -1) break
            result = idx + signatureEnd.size
            from = idx + 1
        }
        return result
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


        file.absolutePath
    } catch (_: Exception) {
        null
    }
}