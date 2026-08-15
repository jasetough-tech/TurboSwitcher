package com.turboswitcher.domain.learning

import com.turboswitcher.data.model.RawEvent
import com.turboswitcher.data.repository.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class EventCollector(
    private val eventRepository: EventRepository,
    private val scope: CoroutineScope
) {
    fun capture(event: RawEvent) {
        scope.launch {
            try {
                eventRepository.record(event)
                Timber.d("Event captured: type=${event.actionType}, package=${event.appPackage}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to capture event")
            }
        }
    }
}
