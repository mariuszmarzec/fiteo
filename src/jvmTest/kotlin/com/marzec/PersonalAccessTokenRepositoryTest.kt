package com.marzec

import com.marzec.core.CurrentTimeUtil
import com.marzec.core.currentTime
import com.marzec.database.PersonalAccessTokenTable
import com.marzec.database.UserTable
import com.marzec.fiteo.model.domain.PersonalAccessToken
import com.marzec.fiteo.repositories.PersonalAccessTokenRepositoryImpl
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PersonalAccessTokenRepositoryTest {

    private val database = Database.connect(
        url = "jdbc:h2:mem:pat_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
        driver = "org.h2.Driver"
    )
    private lateinit var repository: PersonalAccessTokenRepositoryImpl

    @Before
    fun setUp() {
        transaction(database) {
            SchemaUtils.drop(PersonalAccessTokenTable, UserTable)
            SchemaUtils.create(UserTable, PersonalAccessTokenTable)
            UserTable.insert {
                it[UserTable.id] = 1
                it[UserTable.email] = "user1@test.com"
                it[UserTable.password] = "password"
            }
            UserTable.insert {
                it[UserTable.id] = 2
                it[UserTable.email] = "user2@test.com"
                it[UserTable.password] = "password"
            }
        }
        repository = PersonalAccessTokenRepositoryImpl(database)
    }

    @Test
    fun createToken() {
        CurrentTimeUtil.setOtherTime(16, 5, 2021, 10, 0)
        val fixedCreatedAt = currentTime()

        val token = repository.createToken(
            userId = 1,
            name = "Test Token",
            tokenHash = "hash123",
            tokenPrefix = "abc1",
            expiresAt = null
        )

        val expected = PersonalAccessToken(
            id = token.id,
            userId = 1,
            name = "Test Token",
            tokenHash = "hash123",
            tokenPrefix = "abc1",
            createdAt = fixedCreatedAt,
            expiresAt = null,
            lastUsedAt = null
        )
        assertEquals(expected, token)
    }

    @Test
    fun getTokensForUser() {
        CurrentTimeUtil.setOtherTime(16, 5, 2021, 10, 0)
        val fixedCreatedAt = currentTime()

        val token1 = repository.createToken(1, "Token A", "hashA", "preA", null)
        val token2 = repository.createToken(1, "Token B", "hashB", "preB", null)
        val token3 = repository.createToken(2, "Token C", "hashC", "preC", null)

        val user1Tokens = repository.getTokensForUser(1).sortedBy { it.id }
        val user2Tokens = repository.getTokensForUser(2).sortedBy { it.id }

        val expectedUser1Tokens = listOf(
            PersonalAccessToken(
                id = token1.id,
                userId = 1,
                name = "Token A",
                tokenHash = "hashA",
                tokenPrefix = "preA",
                createdAt = fixedCreatedAt,
                expiresAt = null,
                lastUsedAt = null
            ),
            PersonalAccessToken(
                id = token2.id,
                userId = 1,
                name = "Token B",
                tokenHash = "hashB",
                tokenPrefix = "preB",
                createdAt = fixedCreatedAt,
                expiresAt = null,
                lastUsedAt = null
            )
        )
        val expectedUser2Tokens = listOf(
            PersonalAccessToken(
                id = token3.id,
                userId = 2,
                name = "Token C",
                tokenHash = "hashC",
                tokenPrefix = "preC",
                createdAt = fixedCreatedAt,
                expiresAt = null,
                lastUsedAt = null
            )
        )
        assertEquals(expectedUser1Tokens, user1Tokens)
        assertEquals(expectedUser2Tokens, user2Tokens)
    }

    @Test
    fun findByTokenHash() {
        CurrentTimeUtil.setOtherTime(16, 5, 2021, 10, 0)
        val fixedCreatedAt = currentTime()

        val created = repository.createToken(1, "Token A", "hashA", "preA", null)

        val found = repository.findByTokenHash("hashA")
        val notFound = repository.findByTokenHash("nonexistent")

        val expectedFound = PersonalAccessToken(
            id = created.id,
            userId = 1,
            name = "Token A",
            tokenHash = "hashA",
            tokenPrefix = "preA",
            createdAt = fixedCreatedAt,
            expiresAt = null,
            lastUsedAt = null
        )
        assertEquals(expectedFound, found)
        assertNull(notFound)
    }

    @Test
    fun updateLastUsedAt() {
        CurrentTimeUtil.setOtherTime(16, 5, 2021, 10, 0)
        val fixedCreatedAt = currentTime()

        val token = repository.createToken(1, "Token A", "hashA", "preA", null)
        assertNull(token.lastUsedAt)

        CurrentTimeUtil.setOtherTime(16, 5, 2021, 10, 5)
        val fixedLastUsedAt = currentTime()

        repository.updateLastUsedAt(token.id)

        val updated = repository.findByTokenHash("hashA")!!

        val expected = token.copy(
            id = token.id,
            lastUsedAt = fixedLastUsedAt
        )
        assertEquals(expected, updated)
        assertEquals(fixedLastUsedAt, updated.lastUsedAt)
    }

    @Test
    fun deleteToken() {
        CurrentTimeUtil.setOtherTime(16, 5, 2021, 10, 0)
        val fixedCreatedAt = currentTime()

        val token1 = repository.createToken(1, "Token A", "hashA", "preA", null)
        val token2 = repository.createToken(1, "Token B", "hashB", "preB", null)

        repository.deleteToken(userId = 1, tokenId = token1.id)

        val remainingTokens = repository.getTokensForUser(1)

        val expectedRemaining = PersonalAccessToken(
            id = token2.id,
            userId = 1,
            name = "Token B",
            tokenHash = "hashB",
            tokenPrefix = "preB",
            createdAt = fixedCreatedAt,
            expiresAt = null,
            lastUsedAt = null
        )
        assertEquals(expectedRemaining, remainingTokens.first())
    }

    @Test
    fun deleteTokenDoesNotAffectOtherUser() {
        CurrentTimeUtil.setOtherTime(16, 5, 2021, 10, 0)
        val fixedCreatedAt = currentTime()

        val tokenUser1 = repository.createToken(1, "Token A", "hashA", "preA", null)
        val tokenUser2 = repository.createToken(2, "Token B", "hashB", "preB", null)

        repository.deleteToken(userId = 1, tokenId = tokenUser2.id)

        val user2Tokens = repository.getTokensForUser(2)
        val user1Tokens = repository.getTokensForUser(1)

        val expectedUser2Token = PersonalAccessToken(
            id = tokenUser2.id,
            userId = 2,
            name = "Token B",
            tokenHash = "hashB",
            tokenPrefix = "preB",
            createdAt = fixedCreatedAt,
            expiresAt = null,
            lastUsedAt = null
        )
        val expectedUser1Token = PersonalAccessToken(
            id = tokenUser1.id,
            userId = 1,
            name = "Token A",
            tokenHash = "hashA",
            tokenPrefix = "preA",
            createdAt = fixedCreatedAt,
            expiresAt = null,
            lastUsedAt = null
        )
        assertEquals(expectedUser2Token, user2Tokens.first())
        assertEquals(expectedUser1Token, user1Tokens.first())
    }

    @Test
    fun deleteUserCascadesToTokens() {
        CurrentTimeUtil.setOtherTime(16, 5, 2021, 10, 0)
        val fixedCreatedAt = currentTime()

        val token1 = repository.createToken(1, "Token A", "hashA", "preA", null)
        val token2 = repository.createToken(1, "Token B", "hashB", "preB", null)
        val token3 = repository.createToken(2, "Token C", "hashC", "preC", null)

        transaction(database) {
            UserTable.deleteWhere { UserTable.id eq 1 }
        }

        val user1Tokens = repository.getTokensForUser(1)
        val user2Tokens = repository.getTokensForUser(2)

        assertTrue(user1Tokens.isEmpty())

        val expectedUser2Token = PersonalAccessToken(
            id = token3.id,
            userId = 2,
            name = "Token C",
            tokenHash = "hashC",
            tokenPrefix = "preC",
            createdAt = fixedCreatedAt,
            expiresAt = null,
            lastUsedAt = null
        )
        assertEquals(expectedUser2Token, user2Tokens.first())
    }
}