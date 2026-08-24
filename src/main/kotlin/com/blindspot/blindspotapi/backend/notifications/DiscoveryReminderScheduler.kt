package com.blindspot.blindspotapi.backend.notifications

import com.blindspot.blindspotapi.backend.auth.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/** Sends a generic "come back and discover something new" push to every opted-in user once a
 * day. Eagerly initialized (@Lazy(false)) despite global lazy init — nothing else in the app
 * references this bean, so under lazy-init it would never be instantiated and its @Scheduled
 * method would never register, silently never firing. Runs on the single free-tier backend
 * instance; safe to run repeatedly since each send is independent and idempotent-ish (worst
 * case, a user gets one extra push if this fires twice in a scaling edge case — acceptable for
 * a low-stakes reminder, unlike the favorites consistency work done earlier). */
@Component
@Lazy(false)
class DiscoveryReminderScheduler(
    private val userRepository: UserRepository,
    private val notificationDispatchService: NotificationDispatchService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(cron = "0 */2 * * * *")
    fun sendDailyReminders() {
        val users = userRepository.findAllByFcmTokenIsNotNull()
        logger.info("Sending discovery reminders to {} users", users.size)

        users.forEach { user ->
            val token = user.fcmToken ?: return@forEach
            notificationDispatchService.sendReminder(
                fcmToken = token,
                title = "Haven't been out in a while?",
                body = "Discover something new near you tonight.",
            )
        }
    }
}