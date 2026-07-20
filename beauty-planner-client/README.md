# Beauty Planner Client

A Kotlin Multiplatform (KMP) companion app for the Beauty Planner ecosystem. This is the **client-facing** app where end users:

- Discover and open master profiles
- Browse a master's services
- Choose a date and time slot
- Submit a booking request
- Leave a rating and review after a completed appointment

---

## Current scope (skeleton / MVP scaffold)

This repository contains the **initial project scaffold**. The UI is functional enough to demo the full booking flow using in-memory fake data. No real backend integration is present yet.

### What is fake (in-memory)
- All master profiles, services, and available slots come from `FakeMastersRepository`
- All bookings are stored in memory via `FakeBookingRepository`
- All reviews are stored in memory via `FakeReviewsRepository`
- Session / login state is simulated via `FakeSessionRepository`

### What is future backend work
- Firebase / REST backend integration
- Real authentication (Google, Apple, email)
- Push notifications for booking confirmations and review reminders
- Master-side app integration (accepting/declining bookings)

---

## Main shared domain models

| Model | Purpose |
|---|---|
| `MasterProfile` | Master's public profile — display name, specialty, rating summary, review count |
| `MasterService` | A specific service offered — name, duration, price, currency |
| `AvailableSlot` | A bookable date/time slot for a specific master |
| `BookingRequest` | A booking submitted by the client — links client, master, service, and slot |
| `ClientProfile` | Client identity — ID, public display nickname for reviews |
| `MasterReview` | A review left after a completed appointment |
| `ReviewSubmission` | Form data used to submit a new review |
| `PendingReviewPrompt` | A reminder prompt to leave a review after an appointment |
| `MasterCategory` | Enum for filtering masters by specialty (e.g. Hair, Nails, Brows) |

### Review system rules
- Reviews are only allowed after **completed** appointments
- **One review per appointment** (enforced via `appointmentId`)
- Clients use a public **display nickname**, not their real name
- Masters may hide a review from public display but **cannot delete or alter its rating**
- Review reminder prompts can be **snoozed** and shown again later

---

## App navigation flow

```
WelcomeScreen (Discover Masters)
    └── MasterProfileScreen
            ├── ServicesScreen
            │       └── DateTimeScreen
            │               └── BookingFormScreen
            │                       └── BookingConfirmationScreen
            └── ReviewsScreen
                    └── LeaveReviewScreen

[Review reminder popup] → LeaveReviewScreen  (any time after completed booking)
```

---

## Architecture

- **Shared module** (`shared/`): domain models, repository interfaces, fake implementations, and Compose UI screens
- **Android app** (`androidApp/`): thin Android shell — sets up `Application`, provides the `MainActivity` entry point
- **iOS app** (`iosApp/`): thin Swift/SwiftUI shell — hosts the KMP Compose view
- **DI / wiring**: `AppModule.kt` wires all repository dependencies in one place
- Pattern: repositories are injected into screen-level composables via a simple manual DI pattern (no DI framework dependency required at this stage)

---

## Project setup

```bash
# Android
./gradlew :androidApp:assembleDebug

# Shared tests
./gradlew :shared:allTests
```

iOS: open `iosApp/iosApp.xcodeproj` in Xcode and run.
