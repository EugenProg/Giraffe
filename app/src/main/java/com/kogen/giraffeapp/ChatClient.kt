package com.kogen.giraffeapp

import android.content.Context
import com.kogen.testgrpc.proto.ChatMessage
import com.kogen.testgrpc.proto.ChatServiceGrpcKt
import com.kogen.testgrpc.proto.ClientCommand
import com.kogen.testgrpc.proto.CommandType
import com.kogen.testgrpc.proto.clientCommand
import com.kogen.testgrpc.proto.roomRequest
import com.kogen.giraffe.GiraffeInterceptor
import io.grpc.ManagedChannel
import io.grpc.android.AndroidChannelBuilder
import kotlinx.coroutines.flow.Flow
import kz.evko.kogen_di.annotations.KoGenComponent

@KoGenComponent(singleton = true)
class ChatClient(private val context: Context) {

    private var channel: ManagedChannel? = null
    private var stub: ChatServiceGrpcKt.ChatServiceCoroutineStub? = null

    val isConnected: Boolean
        get() = channel?.let { !it.isShutdown && !it.isTerminated } ?: false

    fun connect(host: String, port: Int = 9090) {
        channel?.shutdown()
        channel = AndroidChannelBuilder
            .forAddress(host, port)
            .context(context)
            .usePlaintext()
            .intercept(GiraffeInterceptor(context))
            .build()
        channel?.let {
            stub = ChatServiceGrpcKt.ChatServiceCoroutineStub(it)
        }
    }

    fun disconnect() {
        channel?.shutdown()
        channel = null
        stub = null
    }

    fun subscribe(roomId: String, clientId: String): Flow<ChatMessage> =
        requireStub().subscribe(roomRequest {
            this.roomId = roomId
            this.clientId = clientId
        })

    fun chat(commands: Flow<ClientCommand>): Flow<ChatMessage> =
        requireStub().chat(commands)

    fun joinCommand(roomId: String, clientId: String): ClientCommand =
        clientCommand {
            this.roomId = roomId
            this.clientId = clientId
            type = CommandType.JOIN
        }

    fun leaveCommand(roomId: String, clientId: String): ClientCommand =
        clientCommand {
            this.roomId = roomId
            this.clientId = clientId
            type = CommandType.LEAVE
        }

    fun pingCommand(roomId: String, clientId: String): ClientCommand =
        clientCommand {
            this.roomId = roomId
            this.clientId = clientId
            type = CommandType.PING
        }

    fun messageCommand(roomId: String, clientId: String, text: String): ClientCommand =
        clientCommand {
            this.roomId = roomId
            this.clientId = clientId
            type = CommandType.MESSAGE
            payload = text
        }

    private fun requireStub() = checkNotNull(stub) { "Не подключён к серверу" }
}
