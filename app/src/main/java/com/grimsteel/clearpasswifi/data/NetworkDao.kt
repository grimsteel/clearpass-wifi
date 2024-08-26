package com.grimsteel.clearpasswifi.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(network: Network)

    @Delete
    suspend fun delete(network: Network)

    @Query("SELECT * FROM networks WHERE id = :id")
    fun getNetwork(id: Int): Flow<Network>

    @Query("SELECT * FROM networks WHERE ssid = :ssid ORDER BY createdAt DESC")
    fun getNetworksBySsid(ssid: String): Flow<List<Network>>

    @Query("SELECT * FROM networks ORDER BY createdAt DESC")
    fun getAllNetworks(): Flow<List<Network>>
}