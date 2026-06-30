package com.kogen.giraffeapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kogen.giraffeapp.di.inject
import io.grpc.ManagedChannel

class MainActivity : AppCompatActivity() {

    private val channel: ManagedChannel by lazy { inject() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onDestroy() {
        super.onDestroy()
        channel.shutdown()
    }
}
