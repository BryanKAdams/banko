package com.bryankeltonadams.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bryankeltonadams.data.model.Player

@Database(
    version = 1,
    exportSchema = true,
    entities = [
        Player::class,
    ],
)
//@TypeConverters(Converters::class)
abstract class FishbowlDB: RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "banko.db"
    }

    abstract fun playerDao(): PlayerDao

}