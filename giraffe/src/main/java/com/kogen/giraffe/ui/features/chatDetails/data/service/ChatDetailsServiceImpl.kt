package com.kogen.giraffe.ui.features.chatDetails.data.service

import com.kogen.giraffe.db.dao.GiraffeLogDao
import com.kogen.giraffe.ui.common.domain.models.GiraffeChat
import com.kogen.giraffe.ui.common.domain.models.toDomain
import com.kogen.giraffe.ui.features.chatDetails.domain.service.ChatDetailsService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kz.evko.kogen_di.annotations.KoGenComponent

@KoGenComponent(true)
internal class ChatDetailsServiceImpl(
    val dao: GiraffeLogDao,
) : ChatDetailsService {
    override suspend fun loadChatDetails(id: String): Flow<GiraffeChat?> {
        return dao.getChatDetailsById(id).map {
            it?.toDomain()
        }
    }
}