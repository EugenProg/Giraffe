package com.kogen.giraffe

import android.util.Log
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.ForwardingClientCallListener
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.Status

class GiraffeInterceptor : ClientInterceptor {

    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel,
    ): ClientCall<ReqT, RespT> {
        val methodName = method.fullMethodName

        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            next.newCall(method, callOptions)
        ) {
            override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                Log.d(TAG, "→ CALL  $methodName")
                Log.d(TAG, "→ HEADERS ${headers}")
                super.start(
                    object : ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                        override fun onMessage(message: RespT) {
                            Log.d(TAG, "← MSG   $methodName | $message")
                            super.onMessage(message)
                        }

                        override fun onClose(status: Status, trailers: Metadata) {
                            if (status.isOk) {
                                Log.d(TAG, "← CLOSE $methodName | OK")
                            } else {
                                Log.w(TAG, "← CLOSE $methodName | ${status.code} ${status.description}")
                            }
                            super.onClose(status, trailers)
                        }
                    },
                    headers
                )
            }

            override fun sendMessage(message: ReqT) {
                Log.d(TAG, "→ MSG   $methodName | $message")
                super.sendMessage(message)
            }
        }
    }

    companion object {
        const val TAG = "Giraffe"
    }
}
