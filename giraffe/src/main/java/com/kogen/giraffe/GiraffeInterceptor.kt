package com.kogen.giraffe

import android.content.Context
import android.util.Log
import com.kogen.giraffe.analizer.GiraffeMessageAnalyzer
import com.kogen.giraffe.db.dao.GiraffeLogDao
import com.kogen.giraffe.db.entity.GiraffeChatEntity
import com.kogen.giraffe.ui.common.domain.models.GiraffeChatStatus
import com.kogen.giraffe.db.entity.GiraffeHeaderEntity
import com.kogen.giraffe.db.entity.GiraffeMessageEntity
import com.kogen.giraffe.di.inject
import com.kogen.giraffe.di.setApplicationContext
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.ForwardingClientCallListener
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class GiraffeInterceptor(val context: Context) : ClientInterceptor {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var giraffeLogDao: GiraffeLogDao
    private var notificationService: GiraffeNotificationService
    private var analyzer: GiraffeMessageAnalyzer

    init {
        setApplicationContext(context)
        giraffeLogDao = inject()
        notificationService = inject()
        analyzer = inject()
        scope.launch {
            giraffeLogDao.sanitizeStuckChats()
        }
    }

    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel,
    ): ClientCall<ReqT, RespT> {
        val chatId = UUID.randomUUID()
        val methodShortName = method.fullMethodName.substringAfterLast("/")
        val host = next.authority()
        val url = "$host/${method.fullMethodName}"

        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            next.newCall(method, callOptions)
        ) {
            override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                scope.launch {
                    val chat = GiraffeChatEntity(
                        chatId = chatId.toString(),
                        url = url,
                        methodShortName = methodShortName,
                        timestamp = System.currentTimeMillis(),
                        status = GiraffeChatStatus.InProgress,
                    )

                    val reqHeaders = headers.keys().map { keyName ->
                        val key = Metadata.Key.of(keyName, Metadata.ASCII_STRING_MARSHALLER)
                        GiraffeHeaderEntity(
                            chatId = chatId.toString(),
                            isResponse = false,
                            key = keyName,
                            value = headers.get(key) ?: ""
                        )
                    }

                    giraffeLogDao.startChat(chat, reqHeaders)
                }

                super.start(
                    object : ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(
                        responseListener
                    ) {
                        override fun onMessage(message: RespT) {
                            sendNotification(
                                method = methodShortName,
                                host = host,
                                message = message.toString(),
                                notificationId = chatId,
                            )
                            saveMessage(
                                chatId = chatId.toString(),
                                isIncoming = true,
                                message = message as Any,
                            )
                            super.onMessage(message)
                        }

                        override fun onClose(status: Status, trailers: Metadata) {
                            scope.launch {
                                val respHeaders = trailers.keys().map { keyName ->
                                    val key =
                                        Metadata.Key.of(keyName, Metadata.ASCII_STRING_MARSHALLER)
                                    GiraffeHeaderEntity(
                                        chatId = chatId.toString(),
                                        isResponse = true,
                                        key = keyName,
                                        value = trailers.get(key).orEmpty(),
                                    )
                                }

                                val chatStatus =
                                    if (status.isOk || status.code == Status.Code.CANCELLED) GiraffeChatStatus.Ok
                                    else GiraffeChatStatus.Error
                                giraffeLogDao.completeChat(
                                    chatId = chatId.toString(),
                                    finalStatus = chatStatus,
                                    responseHeaders = respHeaders,
                                )
                            }

                            super.onClose(status, trailers)
                        }
                    },
                    headers
                )
            }

            override fun sendMessage(message: ReqT) {
                sendNotification(
                    method = methodShortName,
                    host = host,
                    message = message.toString(),
                    notificationId = chatId,
                )
                saveMessage(
                    chatId = chatId.toString(),
                    isIncoming = false,
                    message = message as Any,
                )
                super.sendMessage(message)
            }
        }

    }

    private fun saveMessage(chatId: String, isIncoming: Boolean, message: Any) {
        scope.launch {
            try {
                val analysis = analyzer.analyze(message)
                val dbMessage = GiraffeMessageEntity(
                    chatId = chatId,
                    isIncoming = isIncoming,
                    contentType = analysis.contentType,
                    textContent = analysis.textContent,
                    filePath = analysis.filePath,
                    timestamp = System.currentTimeMillis(),
                )
                giraffeLogDao.insertMessage(dbMessage)
            } catch (_: Exception) {

            }
        }
    }

    private fun sendNotification(
        method: String,
        host: String,
        message: String,
        notificationId: UUID
    ) {
        notificationService.sendTrafficNotification(
            methodName = method,
            host = host,
            message = message,
            notificationId = notificationId,
        )
    }
}
