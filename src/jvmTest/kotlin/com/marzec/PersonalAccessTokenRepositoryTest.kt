package com.marzec

import com.marzec.database.PersonalAccessTokenTable
import com.marzec.database.UserTable
import com.marzec.fiteo.repositories.PersonalAccessTokenRepositoryImpl
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
        val token = repository.createToken(
            userId = 1,
            name = "Test Token",
            tokenHash = "hash123",
            tokenPrefix = "abc1",
            expiresAt = null
        )

        assertEquals(1, token.userId)
        assertEquals("Test Token", token.name)
        assertEquals("hash123", token.tokenHash)
        assertEquals("abc1", token.tokenPrefix)
        assertNotNull(token.createdAt)
        assertNull(token.expiresAt)
        assertNull(token.lastUsedAt)
    }

    @Test
    fun getTokensForUser() {
        repository.createToken(1, "Token A", "hashA", "preA", null)
        repository.createToken(1, "Token B", "hashB", "preB", null)
        repository.createToken(2, "Token C", "hashC", "preC", null)

        val user1Tokens = repository.getTokensForUser(1)
        val user2Tokens = repository.getTokensForUser(2)

        assertEquals(2, user1Tokens.size)
        assertEquals(1, user2Tokens.size)
        assertTrue(user1Tokens.all { it.userId == 1 })
        assertTrue(user2Tokens.all { it.userId == 2 })
    }

    @Test
    fun findByTokenHash() {
        repository.createToken(1, "Token A", "hashA", "preA", null)

        val found = repository.findByTokenHash("hashA")
        val notFound = repository.findByTokenHash("nonexistent")

        assertNotNull(found)
        assertEquals("hashA", found!!.tokenHash)
        assertNull(notFound)
    }

    @Test
    fun updateLastUsedAt() {
        val token = repository.createToken(1, "Token A", "hashA", "preA", null)
        assertNull(token.lastUsedAt)

        repository.updateLastUsedAt(token.id)

        val updated = repository.findByTokenHash("hashA")
        assertNotNull(updated?.lastUsedAt)
    }

    @Test
    fun deleteToken() {
        val token1 = repository.createToken(1, "Token A", "hashA", "preA", null)
        val token2 = repository.createToken(1, "Token B", "hashB", "preB", null)

        repository.deleteToken(userId = 1, tokenId = token1.id)

        val remainingTokens = repository.getTokensForUser(1)
        assertEquals(1, remainingTokens.size)
        assertEquals(token2.id, remainingTokens.first().id)
    }

    @Test
    fun deleteTokenDoesNotAffectOtherUser() {
        val tokenUser1 = repository.createToken(1, "Token A", "hashA", "preA", null)
        val tokenUser2 = repository.createToken(2, "Token B", "hashB", "preB", null)

        repository.deleteToken(userId = 1, tokenId = tokenUser2.id)

        val user2Tokens = repository.getTokensForUser(2)
        assertEquals(1, user2Tokens.size)
        assertEquals(tokenUser2.id, user2Tokens.first().id)

        val user1Tokens = repository.getTokensForUser(1)
        assertEquals(1, user1Tokens.size)
        assertEquals(tokenUser1.id, user1Tokens.first().id)
    }

    @Test
    fun deleteUserCascadesToTokens() {
        repository.createToken(1, "Token A", "hashA", "preA", null)
        repository.createToken(1, "Token B", "hashB", "preB", null)
        repository.createToken(2, "Token C", "hashC", "preC", null)

        transaction(database) {
            UserTable.deleteWhere { UserTable.id eq 1 }
        }

        val user1Tokens = repository.getTokensForUser(1)
        val user2Tokens = repository.getTokensForUser(2)

        assertTrue(user1Tokens.isEmpty())
        assertEquals(1, user2Tokens.size)
    }
}
