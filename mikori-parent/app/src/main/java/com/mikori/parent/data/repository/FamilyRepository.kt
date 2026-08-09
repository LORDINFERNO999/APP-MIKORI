package com.mikori.parent.data.repository

import com.mikori.parent.core.MikoriResult
import com.mikori.parent.core.network.apiCall
import com.mikori.parent.data.remote.MikoriApiService
import com.mikori.parent.data.remote.dto.ChildDto
import com.mikori.parent.data.remote.dto.CreateChildRequest
import com.mikori.parent.data.remote.dto.LinkCodeData
import com.mikori.parent.data.remote.dto.LinkStatusData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyRepository @Inject constructor(
    private val api: MikoriApiService,
) {
    suspend fun children(): MikoriResult<List<ChildDto>> = apiCall { api.children() }

    suspend fun child(id: Long): MikoriResult<ChildDto> = apiCall { api.child(id) }

    suspend fun createChild(name: String, birthdate: String?, avatar: String?): MikoriResult<ChildDto> =
        apiCall { api.createChild(CreateChildRequest(name, birthdate, avatar)) }

    suspend fun deleteChild(id: Long): MikoriResult<String> {
        return when (val r = apiCall { api.deleteChild(id) }) {
            is MikoriResult.Success -> MikoriResult.Success(r.data.message)
            is MikoriResult.Error -> r
        }
    }

    suspend fun generateLinkCode(childId: Long): MikoriResult<LinkCodeData> =
        apiCall { api.generateLinkCode(childId) }

    suspend fun linkStatus(childId: Long): MikoriResult<LinkStatusData> =
        apiCall { api.linkStatus(childId) }
}
