package com.example.jetpackpos.data.repository

import com.example.jetpackpos.data.db.ShopInfoDao
import com.example.jetpackpos.data.model.ShopInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopInfoRepository @Inject constructor(private val shopInfoDao: ShopInfoDao) {

    fun getShopInfo(): Flow<ShopInfo> = flow {
        val shopInfoFlow = shopInfoDao.getShopInfo(ShopInfo.DEFAULT_SHOP_INFO_ID)
            .map { it ?: ShopInfo() } // Provide default if null (e.g., first run)
        emitAll(shopInfoFlow)
    }


    suspend fun insertOrUpdateShopInfo(shopInfo: ShopInfo) {
        // Ensure the ID is always the default one for the singleton entry
        shopInfoDao.insertOrUpdate(shopInfo.copy(id = ShopInfo.DEFAULT_SHOP_INFO_ID))
    }

    // This might not be needed if full database reset is handled elsewhere
    suspend fun clearShopInfo() {
        shopInfoDao.clear()
    }
}
