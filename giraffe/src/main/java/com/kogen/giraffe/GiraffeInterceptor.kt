package com.kogen.giraffe

import android.content.Context
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
import java.util.UUID

class GiraffeInterceptor(val context: Context) : ClientInterceptor {

    init {
        setApplicationContext(context)
    }

    val notificationService = inject<GiraffeNotificationService>()

    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel,
    ): ClientCall<ReqT, RespT> {

        val notificationId = UUID.randomUUID().hashCode()
        val methodShortName = method.fullMethodName.substringAfterLast("/")
        val host = next.authority()

        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            next.newCall(method, callOptions)
        ) {
            override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                super.start(
                    object : ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(
                        responseListener
                    ) {
                        override fun onMessage(message: RespT) {
                            sendNotification(
                                method = methodShortName,
                                host = host,
                                message = message.toString(),
                                notificationId = notificationId,
                            )
                            super.onMessage(message)
                        }

                        override fun onClose(status: Status, trailers: Metadata) {
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
                    notificationId = notificationId,
                )
                super.sendMessage(message)
            }
        }

    }

    fun sendNotification(method: String, host: String, message: String, notificationId: Int) {
        notificationService.sendTrafficNotification(
            methodName = method,
            host = host,
            message = message,
            notificationId = notificationId,
        )
    }
}
