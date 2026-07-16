package com.kogen.giraffe.analizer.utils

class ProtoWireScanner {
    fun scan(data: ByteArray): List<ProtoField> {
        val fields = mutableListOf<ProtoField>()
        var pos = 0

        while (pos < data.size) {
            val tagStart = pos
            val (tag, tagLen) = readVariant(data, pos) ?: break
            pos += tagLen

            val fieldNumber = (tag shr 3).toInt()
            when (val wireType = (tag and 0x7).toInt()) {
                0 -> {
                    val (_, len) = readVariant(data, pos) ?: break
                    pos += len
                    fields.add(ProtoField(fieldNumber, wireType, null, tagStart, pos))
                }
                1 -> {
                    if (pos + 8 > data.size) break
                    pos += 8
                    fields.add(ProtoField(fieldNumber, wireType, null, tagStart, pos))
                }
                2 -> {
                    val (len, lenLen) = readVariant(data, pos) ?: break
                    pos += lenLen
                    if (len < 0 || pos + len > data.size) break
                    val payload = data.copyOfRange(pos, pos + len.toInt())
                    pos += len.toInt()
                    fields.add(ProtoField(fieldNumber, wireType, payload, tagStart, pos))
                }
                5 -> {
                    if (pos + 4 > data.size) break
                    pos += 4
                    fields.add(ProtoField(fieldNumber, wireType, null, tagStart, pos))
                }
                else -> break
            }
        }

        return fields
    }

    private fun readVariant(data: ByteArray, start: Int): Pair<Long, Int>? {
        var result = 0L
        var shift = 0
        var pos = start
        while (pos < data.size) {
            val b = data[pos].toInt() and 0xFF
            result = result or ((b and 0x7F).toLong() shl shift)
            pos++
            if (b and 0x80 == 0) return Pair(result, pos - start)
            shift += 7
            if (shift > 63) return null
        }
        return null
    }
}

data class ProtoField(
    val fieldNumber: Int,
    val wireType: Int,
    val bytes: ByteArray?,
    val startOffset: Int,
    val endOffset: Int
)