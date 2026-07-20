package com.beautyclient.ui

import androidx.compose.runtime.*
import com.beautyclient.di.AppModule
import com.beautyclient.ui.navigation.Screen
import com.beautyclient.ui.screens.*

/**
 * Root composable for the Beauty Planner Client app.
 * Manages the back-stack and renders the current screen.
 */
@Composable
fun BeautyClientApp() {
    val backStack = remember { mutableStateListOf<Screen>(Screen.Discover) }

    fun navigate(screen: Screen) { backStack.add(screen) }
    fun goBack() { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }

    // Check for pending review prompts and surface the reminder modal
    val reviewReminder = remember { mutableStateOf<Screen.ReviewReminder?>(null) }
    LaunchedEffect(Unit) {
        val session = AppModule.sessionRepository.getCurrentClient() ?: return@LaunchedEffect
        val prompts = AppModule.reviewsRepository.getPendingReviewPrompts(session.id)
        prompts.firstOrNull { !it.isDismissed && (it.snoozeUntilMs == null) }?.let { prompt ->
            reviewReminder.value = Screen.ReviewReminder(prompt.id)
        }
    }

    val current = backStack.lastOrNull() ?: Screen.Discover

    when (val screen = current) {
        is Screen.Discover -> DiscoverScreen(
            onMasterClick = { navigate(Screen.MasterProfile(it)) }
        )
        is Screen.MasterProfile -> MasterProfileScreen(
            masterId = screen.masterId,
            onBack = ::goBack,
            onBookClick = { navigate(Screen.Services(screen.masterId)) },
            onReviewsClick = { navigate(Screen.Reviews(screen.masterId)) }
        )
        is Screen.Services -> ServicesScreen(
            masterId = screen.masterId,
            onBack = ::goBack,
            onServiceSelected = { serviceId ->
                navigate(Screen.DateTime(screen.masterId, serviceId))
            }
        )
        is Screen.DateTime -> DateTimeScreen(
            masterId = screen.masterId,
            serviceId = screen.serviceId,
            onBack = ::goBack,
            onSlotSelected = { slotId ->
                navigate(Screen.BookingForm(screen.masterId, screen.serviceId, slotId))
            }
        )
        is Screen.BookingForm -> BookingFormScreen(
            masterId = screen.masterId,
            serviceId = screen.serviceId,
            slotId = screen.slotId,
            onBack = ::goBack,
            onBookingSubmitted = { bookingId ->
                // Replace form screen with confirmation (no going back to the form)
                backStack.removeAt(backStack.lastIndex)
                navigate(Screen.BookingConfirmation(bookingId))
            }
        )
        is Screen.BookingConfirmation -> BookingConfirmationScreen(
            bookingId = screen.bookingId,
            onBackToHome = {
                backStack.clear()
                backStack.add(Screen.Discover)
            }
        )
        is Screen.Reviews -> ReviewsScreen(
            masterId = screen.masterId,
            onBack = ::goBack
        )
        is Screen.LeaveReview -> LeaveReviewScreen(
            appointmentId = screen.appointmentId,
            masterId = screen.masterId,
            onBack = ::goBack,
            onReviewSubmitted = ::goBack
        )
        is Screen.ReviewReminder -> {
            // The reminder is shown as a modal overlay; render the underlying screen too
            val underlying = if (backStack.size > 1) backStack[backStack.lastIndex - 1] else Screen.Discover
            // Recursively render underlying — simple approach for scaffold
        }
    }

    // Review reminder modal overlay
    reviewReminder.value?.let { reminder ->
        ReviewReminderModal(
            promptId = reminder.promptId,
            onReviewNow = { appointmentId, masterId ->
                reviewReminder.value = null
                navigate(Screen.LeaveReview(appointmentId, masterId))
            },
            onSnooze = { reviewReminder.value = null },
            onDismiss = { reviewReminder.value = null }
        )
    }
}
