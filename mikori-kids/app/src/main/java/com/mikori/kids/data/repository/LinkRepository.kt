package com.mikori.kids.data.repository

import android.content.Context
import android.os.Build
import com.mikori.kids.core.MikoriResult
import com.mikori.kids.core.network.apiCall
import com.mikori.kids.data.local.DeviceSessionStore
import com.mikori.kids.data.remote.KidsApiService
import com.mikori.kids.data.remote.dto.RedeemData
import com.mikori.kids.data.remote.dto.RedeemRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: KidsApiService,
    private val session: DeviceSessionStore,
) {
    val isLinked = session.isLinkedFlow

    suspend fun redeem(code: String): MikoriResult<RedeemData> {
        val uid = session.deviceUid()
        val result = apiCall {
            api.redeem(
                RedeemRequest(
                    code = code.trim().uppercase(),
                    deviceUid = uid,
                    model = "${Build.MANUFACTURER} ${Build.MODEL}",
                    androidVersion = Build.VERSION.RELEASE,
                )
            )
        }
        if (result is MikoriResult.Success) {
            session.saveLink(result.data.deviceToken, result.data.childId)
        }
        return result
    }
}
