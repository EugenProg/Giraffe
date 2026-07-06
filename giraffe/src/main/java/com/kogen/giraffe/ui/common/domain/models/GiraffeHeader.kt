package com.kogen.giraffe.ui.common.domain.models

import com.kogen.giraffe.db.entity.GiraffeHeaderEntity

internal data class GiraffeHeader(
    val id: Long,
    val isResponse: Boolean,
    val key: String,
    val value: String,
)

internal fun GiraffeHeaderEntity.toDomain(): GiraffeHeader {
    return GiraffeHeader(
        id = this.id,
        isResponse = this.isResponse,
        key = this.key,
        value = this.value,
    )
}