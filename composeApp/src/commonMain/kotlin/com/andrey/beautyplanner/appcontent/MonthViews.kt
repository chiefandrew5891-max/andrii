// Existing imports
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color

// Other imports...

// Existing code...

@Composable
fun MonthCalendarGrid(...) {
    // Other code...

    // Change weekday header color
    val weekdayHeaderColor = MaterialTheme.colors.onBackground.copy(alpha = 0.65f)
    // Other code...

    // Change past day text color while keeping them clickable
    val pastDayTextColor = MaterialTheme.colors.onBackground.copy(alpha = 0.50f)
    // Other code...
}

// Remaining code...