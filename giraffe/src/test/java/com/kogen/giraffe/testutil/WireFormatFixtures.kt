package com.kogen.giraffe.testutil

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import java.io.File

/**
 * Hand-rolled protobuf wire-format encoders used to build realistic byte payloads for the
 * analyzer/parser tests, without depending on an actual .proto schema or generated message.
 */

/** Encodes [value] as a protobuf base-128 varint. */
fun varint(value: Long): ByteArray {
    val out = mutableListOf<Byte>()
    var v = value
    do {
        var b = (v and 0x7F).toInt()
        v = v ushr 7
        if (v != 0L) b = b or 0x80
        out.add(b.toByte())
    } while (v != 0L)
    return out.toByteArray()
}

/** Encodes a field tag (field number + wire type). */
fun tag(fieldNumber: Int, wireType: Int): ByteArray =
    varint(((fieldNumber.toLong() shl 3) or wireType.toLong()))

/** Wraps [payload] as a length-delimited (wire type 2) field. */
fun lengthDelimitedField(fieldNumber: Int, payload: ByteArray): ByteArray =
    tag(fieldNumber, 2) + varint(payload.size.toLong()) + payload

/** Wraps [value] as a varint (wire type 0) field. */
fun varintField(fieldNumber: Int, value: Long): ByteArray =
    tag(fieldNumber, 0) + varint(value)

/** A [Context] stub whose [Context.getCacheDir] points at a real temp directory on disk. */
fun fakeContext(cacheDir: File): Context {
    val context = mockk<Context>()
    every { context.cacheDir } returns cacheDir
    return context
}
