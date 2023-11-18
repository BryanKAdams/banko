package com.bryankeltonadams.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.bryankeltonadams.data.model.Player
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Insert
    fun insert(player: Player)


    @Delete
    fun delete(player: Player)

//    @Query("SELECT * FROM player WHERE id IS :itemId LIMIT 1")
//    fun get(itemId: Int): Flow<APIItem?>

    @Query("SELECT * FROM player")
    fun getAll(): Flow<List<Player>>


    @Query("DELETE FROM player")
    suspend fun deleteAll()

}