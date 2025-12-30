package com.marzec.fiteo.services

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.marzec.Api
import com.marzec.fiteo.FiteoConfig
import com.marzec.fiteo.model.domain.FcmToken
import com.marzec.fiteo.repositories.FcmTokenRepository
import com.marzec.todo.dto.TaskDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.FileInputStream

class FcmServiceImpl(
    private val fcmTokenRepository: FcmTokenRepository,
    private val authToken: String
) : FcmService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    init {
        try {
            val serviceAccountPath = if (authToken == Api.Auth.TEST) {
                FiteoConfig.FIREBASE_SERVICE_ACCOUNT_TEST
            } else {
                FiteoConfig.FIREBASE_SERVICE_ACCOUNT_PROD
            }
            
            if (!serviceAccountPath.isNullOrBlank()) {
                val serviceAccount = FileInputStream(serviceAccountPath)
                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options)
                    logger.info("Firebase initialized successfully with config: $serviceAccountPath")
                }
            } else {
                logger.warn("Firebase service account path is empty for auth token: $authToken")
            }
        } catch (e: Exception) {
            logger.error("Failed to initialize Firebase: ${e.message}")
        }
    }

    override fun addToken(userId: Int, fcmToken: String, platform: String?): FcmToken {
        return fcmTokenRepository.addToken(userId, fcmToken, platform)
    }

    override fun deleteToken(userId: Int, fcmToken: String) {
        fcmTokenRepository.deleteToken(userId, fcmToken)
    }

    override fun deleteTokensForUser(userId: Int) {
        fcmTokenRepository.deleteTokensForUser(userId)
    }

    override fun sendPushNotification(userId: Int, taskDto: TaskDto) {
        val tokens = fcmTokenRepository.getTokensForUser(userId)
        if (tokens.isEmpty()) {
            logger.info("No FCM tokens found for user $userId")
            return
        }

        val payload = mapOf(
            "type" to "TASK_SCHEDULED",
            "data" to Json.encodeToString(taskDto)
        )

        tokens.forEach { token ->
            try {
                val message = Message.builder()
                    .setToken(token.fcmToken)
                    .putAllData(payload)
                    .build()

                val response = FirebaseMessaging.getInstance().send(message)
                logger.info("Successfully sent message: $response to token: ${token.fcmToken}")
            } catch (e: Exception) {
                logger.error("Error sending message to token ${token.fcmToken}: ${e.message}")
                // Optionally remove invalid tokens here
            }
        }
    }
}
