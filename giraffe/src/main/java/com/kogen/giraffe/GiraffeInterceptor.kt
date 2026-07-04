package com.kogen.giraffe

import android.content.Context
import com.kogen.giraffe.db.dao.GiraffeLogDao
import com.kogen.giraffe.db.entity.GiraffeChat
import com.kogen.giraffe.db.entity.GiraffeChatStatus
import com.kogen.giraffe.db.entity.GiraffeContentType
import com.kogen.giraffe.db.entity.GiraffeHeader
import com.kogen.giraffe.db.entity.GiraffeMessage
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

    init {
        setApplicationContext(context)
    }

    val notificationService = inject<GiraffeNotificationService>()
    val giraffeLogDao = inject<GiraffeLogDao>()
    private val scope = CoroutineScope(Dispatchers.Default)

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
                    val chat = GiraffeChat(
                        chatId = chatId.toString(),
                        url = url,
                        methodShortName = methodShortName,
                        timestamp = System.currentTimeMillis(),
                        status = GiraffeChatStatus.InProgress,
                    )

                    val reqHeaders = headers.keys().map { keyName ->
                        val key = Metadata.Key.of(keyName, Metadata.ASCII_STRING_MARSHALLER)
                        GiraffeHeader(
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
                            scope.launch {
                                // TODO: Передать объект message в будущий анализатор для вычисления типа и сохранения
                                val dbMessage = GiraffeMessage(
                                    chatId = chatId.toString(),
                                    isIncoming = true,
                                    contentType = GiraffeContentType.Json,
                                    textContent = message.toString(),
                                    filePath = null,
                                    timestamp = System.currentTimeMillis()
                                )
                                giraffeLogDao.insertMessage(dbMessage)
                            }
                            super.onMessage(message)
                        }

                        override fun onClose(status: Status, trailers: Metadata) {
                            scope.launch {
                                val respHeaders = trailers.keys().map { keyName ->
                                    val key =
                                        Metadata.Key.of(keyName, Metadata.ASCII_STRING_MARSHALLER)
                                    GiraffeHeader(
                                        chatId = chatId.toString(),
                                        isResponse = true,
                                        key = keyName,
                                        value = trailers.get(key).orEmpty(),
                                    )
                                }

                                val chatStatus =
                                    if (status.isOk) GiraffeChatStatus.Ok
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
                scope.launch {
                    // TODO: Передать объект message в будущий анализатор для вычисления типа и сохранения
                    val dbMessage = GiraffeMessage(
                        chatId = chatId.toString(),
                        isIncoming = false,
                        contentType = GiraffeContentType.Json,
                        textContent = message.toString(),
                        filePath = null,
                        timestamp = System.currentTimeMillis()
                    )
                    giraffeLogDao.insertMessage(dbMessage)
                }
                super.sendMessage(message)
            }
        }

    }

    fun sendNotification(method: String, host: String, message: String, notificationId: UUID) {
        notificationService.sendTrafficNotification(
            methodName = method,
            host = host,
            message = message,
            notificationId = notificationId.hashCode(),
        )
    }
}
