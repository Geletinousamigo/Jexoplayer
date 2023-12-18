package com.geletinousamigo.media.jexoplayer.util

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FlowDebouncer<T>(timeoutMillis: Long): Flow<T> {

    private val sourceChannel: Channel<T> = Channel(capacity = 1)

    @OptIn(FlowPreview::class)
    private val flow: Flow<T> = sourceChannel.consumeAsFlow().debounce(timeoutMillis)

    suspend fun put(item: T) {
        sourceChannel.send(item)
    }

    override suspend fun collect(collector: FlowCollector<T>) {
        flow.collect(collector)
    }
}

