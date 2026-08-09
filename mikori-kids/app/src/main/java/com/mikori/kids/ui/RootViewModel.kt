package com.mikori.kids.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikori.kids.data.local.DeviceSessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    session: DeviceSessionStore,
) : ViewModel() {
    /** null = cargando, true = vinculado, false = sin vincular. */
    val isLinked: StateFlow<Boolean?> = session.isLinkedFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
