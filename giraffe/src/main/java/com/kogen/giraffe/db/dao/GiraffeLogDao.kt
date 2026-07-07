package com.kogen.giraffe.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kogen.giraffe.db.GiraffeDb
import com.kogen.giraffe.db.entity.ChatWithDetails
import com.kogen.giraffe.db.entity.GiraffeChatEntity
import com.kogen.giraffe.ui.common.domain.models.GiraffeChatStatus
import com.kogen.giraffe.db.entity.GiraffeHeaderEntity
import com.kogen.giraffe.db.entity.GiraffeMessageEntity
import kotlinx.coroutines.flow.Flow
import kz.evko.kogen_di.annotations.KoGenBean

@Dao
interface GiraffeLogDao {
    @Transaction
    @Query("SELECT * FROM giraffe_chat ORDER BY timestamp DESC")
    fun getAllChatsWithDetails(): Flow<List<ChatWithDetails>>

    @Transaction
    @Query("SELECT * FROM giraffe_chat WHERE chatId = :chatId")
    fun getChatDetailsById(chatId: String): Flow<ChatWithDetails?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertChat(chat: GiraffeChatEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHeaders(headers: List<GiraffeHeaderEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(message: GiraffeMessageEntity)

    @Update
    suspend fun updateChat(chat: GiraffeChatEntity)

    @Transaction
    suspend fun startChat(chat: GiraffeChatEntity, requestHeaders: List<GiraffeHeaderEntity>) {
        insertChat(chat)
        insertHeaders(requestHeaders)
    }

    @Transaction
    suspend fun completeChat(
        chatId: String,
        finalStatus: GiraffeChatStatus,
        responseHeaders: List<GiraffeHeaderEntity>,
    ) {
        updateChatStatus(chatId, finalStatus)
        insertHeaders(responseHeaders)
    }

    @Query("UPDATE giraffe_chat SET status = :finalStatus WHERE chatId = :chatId")
    suspend fun updateChatStatus(chatId: String, finalStatus: GiraffeChatStatus)

    @Query("UPDATE giraffe_chat SET status = :targetStatus WHERE status = :activeStatus")
    suspend fun sanitizeStuckChats(
        activeStatus: GiraffeChatStatus = GiraffeChatStatus.InProgress,
        targetStatus: GiraffeChatStatus = GiraffeChatStatus.Error,
    )

    @Query("DELETE FROM giraffe_chat WHERE chatId = :chatId")
    suspend fun deleteChatById(chatId: String)

    @Query("DELETE FROM giraffe_chat")
    suspend fun clearAllChats()
}

@KoGenBean(true)
internal fun provideGiraffeLogDao(db: GiraffeDb) = db.giraffeLogDao()