package com.kogen.giraffe.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kogen.giraffe.db.converter.GiraffeConverters
import com.kogen.giraffe.db.dao.GiraffeLogDao
import com.kogen.giraffe.db.entity.GiraffeChat
import com.kogen.giraffe.db.entity.GiraffeHeader
import com.kogen.giraffe.db.entity.GiraffeMessage
import kz.evko.kogen_di.annotations.KoGenBean

@Database(
    entities = [
        GiraffeChat::class,
        GiraffeHeader::class,
        GiraffeMessage::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(GiraffeConverters::class)
abstract class GiraffeDb : RoomDatabase() {
    abstract fun giraffeLogDao(): GiraffeLogDao
}

@KoGenBean(true)
internal fun provideDB(context: Context): GiraffeDb = Room.databaseBuilder(
    context.applicationContext,
    GiraffeDb::class.java,
    "giraffe_traffic_logs.db"
).fallbackToDestructiveMigration(true)//TODO: delete to prod release
    .build()