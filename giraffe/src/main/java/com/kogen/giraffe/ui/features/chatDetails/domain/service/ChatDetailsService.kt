package com.kogen.giraffe.ui.features.chatDetails.domain.service

import com.kogen.giraffe.ui.common.domain.models.GiraffeChat
import kotlinx.coroutines.flow.Flow

internal interface ChatDetailsService {
    val chatDetails: Flow<GiraffeChat?>
    suspend fun loadChatDetails(id: String)
}