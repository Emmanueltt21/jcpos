package com.example.jetpackpos.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.jetpackpos.data.model.ShopInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(shopInfo: ShopInfo)

    // Use ShopInfo.DEFAULT_SHOP_INFO_ID for querying the single row
    @Query("SELECT * FROM shop_info WHERE id = :id")
    fun getShopInfo(id: Int = ShopInfo.DEFAULT_SHOP_INFO_ID): Flow<ShopInfo?>

    // Although we expect only one row, a clear method might be useful for complete resets
    // if not handled by a global AppDatabase clear method.
    @Query("DELETE FROM shop_info")
    suspend fun clear() // Potentially for a full reset.
}
