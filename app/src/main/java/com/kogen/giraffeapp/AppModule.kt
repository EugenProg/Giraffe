package com.kogen.giraffeapp

import android.content.Context
import io.grpc.ManagedChannel
import io.grpc.android.AndroidChannelBuilder
import kz.evko.kogen_di.annotations.KoGenBean

@KoGenBean(singleton = true)
fun provideGrpcChannel(context: Context): ManagedChannel =
    AndroidChannelBuilder
        .forAddress("10.0.2.2", 50051)
        .context(context)
        .usePlaintext()
        .build()
