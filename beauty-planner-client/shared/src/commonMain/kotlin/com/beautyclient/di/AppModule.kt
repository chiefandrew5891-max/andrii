package com.beautyclient.di

import com.beautyclient.data.fake.FakeBookingRepository
import com.beautyclient.data.fake.FakeMastersRepository
import com.beautyclient.data.fake.FakeReviewsRepository
import com.beautyclient.data.fake.FakeSessionRepository
import com.beautyclient.data.repository.BookingRepository
import com.beautyclient.data.repository.MastersRepository
import com.beautyclient.data.repository.ReviewsRepository
import com.beautyclient.data.repository.SessionRepository

/**
 * Simple manual DI container.
 *
 * Swap [mastersRepository], [bookingRepository], [reviewsRepository], or [sessionRepository]
 * with real implementations once backend integration is ready.
 * All screens receive their dependencies from this single object.
 */
object AppModule {

    val mastersRepository: MastersRepository = FakeMastersRepository()

    val bookingRepository: BookingRepository = FakeBookingRepository()

    val reviewsRepository: ReviewsRepository = FakeReviewsRepository()

    val sessionRepository: SessionRepository = FakeSessionRepository()
}
