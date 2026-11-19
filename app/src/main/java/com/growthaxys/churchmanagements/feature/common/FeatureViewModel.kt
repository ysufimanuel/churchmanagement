package com.growthaxys.churchmanagements.feature.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.growthaxys.churchmanagements.core.LocalStrings
import com.growthaxys.churchmanagements.data.ActionResult
import com.growthaxys.churchmanagements.designsystem.ErrorBox
import com.growthaxys.churchmanagements.designsystem.LoadingBox
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface Load<out T> {
    data object Loading : Load<Nothing>
    data class Error(val message: String) : Load<Nothing>
    data class Ready<T>(val data: T) : Load<T>
}

/** A toast = success label key (translated) or a raw server error (translated via tm()). */
data class ToastMsg(val key: String? = null, val raw: String? = null, val isError: Boolean = false)

val LocalToaster = compositionLocalOf<(ToastMsg) -> Unit> { {} }

/**
 * Base for every screen: exposes immutable UI state (StateFlow), reloads after each successful mutation and emits toasts.
 * ViewModels never touch Supabase directly — only repositories do.
 */
abstract class FeatureViewModel<T> : ViewModel() {
    private val _state = MutableStateFlow<Load<T>>(Load.Loading)
    val state: StateFlow<Load<T>> = _state.asStateFlow()
    private val _toasts = MutableSharedFlow<ToastMsg>(extraBufferCapacity = 8)
    val toasts: SharedFlow<ToastMsg> = _toasts.asSharedFlow()
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()
    private var started = false

    protected abstract suspend fun fetch(): T

    fun start() { if (!started) { started = true; refresh() } }

    fun refresh() {
        viewModelScope.launch {
            runCatching { fetch() }
                .onSuccess { _state.value = Load.Ready(it) }
                .onFailure { if (_state.value !is Load.Ready) _state.value = Load.Error(it.message ?: "error") }
        }
    }

    protected fun toast(key: String) { _toasts.tryEmit(ToastMsg(key = key)) }
    protected fun toastError(raw: String?) { _toasts.tryEmit(ToastMsg(raw = raw, isError = true)) }

    /** Runs a repository mutation; success -> toast + refresh (+ onOk), failure -> error toast. */
    fun mutate(successKey: String = "saved-ok", onOk: () -> Unit = {}, block: suspend () -> ActionResult) {
        viewModelScope.launch {
            _busy.value = true
            val r = block()
            _busy.value = false
            if (r.ok) { toast(successKey); onOk(); refresh() } else toastError(r.error)
        }
    }

    /** Like [mutate] for calls that return a created row id (Web Manager sections). */
    fun mutateWithId(successKey: String = "saved-ok", onOk: (Long?) -> Unit = {}, block: suspend () -> com.growthaxys.churchmanagements.data.repo.IdResult) {
        viewModelScope.launch {
            _busy.value = true
            val r = block()
            _busy.value = false
            if (r.ok) { toast(successKey); onOk(r.id); refresh() } else toastError(r.error)
        }
    }
}

/** Wires a ViewModel's toasts to the app toaster and loads data once. */
@Composable
fun <T> FeatureViewModel<T>.Bind() {
    val toaster = LocalToaster.current
    LaunchedEffect(this) { start() }
    LaunchedEffect(this) { toasts.collect { toaster(it) } }
}

@Composable
fun <T> LoadGate(
    vm: FeatureViewModel<T>,
    padding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(),
    skeleton: com.growthaxys.churchmanagements.designsystem.SkeletonKind = com.growthaxys.churchmanagements.designsystem.SkeletonKind.Table,
    content: @Composable (T) -> Unit,
) {
    vm.Bind()
    val s by vm.state.collectAsStateWithLifecycle()
    val phase = when (s) { is Load.Loading -> 0; is Load.Error -> 1; is Load.Ready -> 2 }
    // skeleton -> content cross-fade (web: loading.tsx -> page)
    androidx.compose.animation.Crossfade(phase, animationSpec = androidx.compose.animation.core.tween(300), label = "loadgate") { p ->
        when (val st = s) {
            is Load.Loading -> com.growthaxys.churchmanagements.designsystem.PageSkeleton(skeleton, padding)
            is Load.Error -> androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.padding(padding)) { ErrorBox(LocalStrings.current.tm(st.message)) { vm.refresh() } }
            is Load.Ready -> if (p == 2) content(st.data)
        }
    }
}

@Suppress("unused") private typealias Unused = Flow<Unit>
