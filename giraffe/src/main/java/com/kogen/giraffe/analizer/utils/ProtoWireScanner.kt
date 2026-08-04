package com.kogen.giraffe.analizer.utils

class ProtoWireScanner {
    companion object {
        private const val MIN_MESSAGE_BYTE_COVERAGE = 0.9
    }

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

    /**
     * Returns length-delimited fields that are NOT themselves fully-formed nested messages,
     * descending into any depth of oneof/message wrapping to find the true opaque leaf bytes
     * (e.g. the raw content of a `bytes data` field buried inside several wrapper messages).
     * A payload is treated as a nested message (and recursed into) only if scanning it consumes
     * it in full as valid wire-format fields; otherwise it's reported as a leaf candidate.
     */
    fun findBinaryLeaves(data: ByteArray, minSize: Int = 17): List<ByteArray> {
        val leaves = mutableListOf<ByteArray>()
        collectLeaves(data, minSize, leaves)
        return leaves
    }

    private fun collectLeaves(data: ByteArray, minSize: Int, out: MutableList<ByteArray>) {
        for (field in scan(data)) {
            val payload = field.bytes ?: continue
            if (field.wireType != 2) continue
            if (MediaSignatures.isLikelyUtf8Text(payload)) continue

            val nested = scan(payload)
            val consumedAll = nested.isNotEmpty() && nested.last().endOffset == payload.size

            // Protobuf's wire format is loose enough that arbitrary binary (e.g. raw audio) can
            // "fully parse" as a sequence of tiny wireType=0/1/5 fields purely by chance — those
            // carry no bytes and get silently dropped, which would otherwise shred most of a real
            // media payload down to whatever small wireType=2 fragment happened to survive. Only
            // trust the nested-message interpretation if real (bytes-carrying) fields account for
            // nearly all of the payload — a genuine nested message barely wastes any bytes on
            // framing, while a false-positive reinterpretation of noise loses most of them.
            val byteFieldCoverage = nested.filter { it.wireType == 2 }
                .sumOf { it.bytes?.size ?: 0 }
            val looksLikeRealMessage = consumedAll &&
                payload.isNotEmpty() &&
                byteFieldCoverage.toDouble() / payload.size >= MIN_MESSAGE_BYTE_COVERAGE

            if (looksLikeRealMessage) {
                val nestedLeaves = mutableListOf<ByteArray>()
                collectLeaves(payload, minSize, nestedLeaves)
                if (nestedLeaves.isNotEmpty()) {
                    out.addAll(nestedLeaves)
                    continue
                }
                // Looked like a fully-formed nested message (e.g. a run of zero bytes parses as
                // valid-but-empty wireType=0 fields), but recursing produced no usable bytes —
                // fall back to treating the whole payload as an opaque leaf instead of losing it.
            }

            if (payload.size >= minSize) {
                out.add(payload)
            }
        }
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