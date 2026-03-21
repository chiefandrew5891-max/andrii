import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TransferDialog(onDismiss: () -> Unit) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(8, 0)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Target Date and Time") },
        text = {
            Column {
                MonthCalendarGrid(selectedDate) { date -> selectedDate = date }
                TimeSlotPicker(selectedTime) { time -> selectedTime = time }
            }
        },
        buttons = {
            Button(onClick = onDismiss) { Text("Confirm") }
        }
    )
}

@Composable
fun RescheduleDialog(onDismiss: () -> Unit, availableSlots: List<LocalTime>) {
    var selectedTime by remember { mutableStateOf(availableSlots.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reschedule Appointment") },
        text = {
            Column {
                TimeSlotPicker(selectedTime, availableSlots) { time -> selectedTime = time }
            }
        },
        buttons = {
            Button(onClick = onDismiss) { Text("Confirm") }
        }
    )
}

@Composable
fun TimeSlotPicker(selectedTime: LocalTime, availableSlots: List<LocalTime> = (8..20).map { LocalTime.of(it, 0) }, onTimeSelected: (LocalTime) -> Unit) {
    Column {
        availableSlots.forEach { time ->
            TextButton(onClick = { onTimeSelected(time) }) {
                Text(time.format(DateTimeFormatter.ofPattern("HH:mm")))
            }
        }
    }
}

@Preview
@Composable
fun PreviewTransferDialog() {
    TransferDialog(onDismiss = {})
}