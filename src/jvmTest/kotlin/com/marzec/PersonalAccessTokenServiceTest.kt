package com.marzec

import com.marzec.core.UtcPatTimeProvider
import com.marzec.core.sha256Hex
import com.marzec.database.PersonalAccessTokenTable
import com.marzec.database.UserTable
import com.marzec.exceptions.HttpException
import com.marzec.exceptions.HttpStatus
import com.marzec.fiteo.model.domain.PersonalAccessToken
import com.marzec.fiteo.model.domain.TokenExpiration
import com.marzec.fiteo.model.domain.User
import com.marzec.fiteo.repositories.PersonalAccessTokenRepositoryImpl
import com.marzec.fiteo.repositories.UserRepositoryImpl
import com.marzec.fiteo.services.PersonalAccessTokenService
import com.marzec.fiteo.services.PersonalAccessTokenServiceImpl
import kotlinx.datetime.LocalDateTime
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PersonalAccessTokenServiceTest {

    private val database = Database.connect(
        url = "jdbc:h2:mem:pat_service_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
        driver = "org.h2.Driver"
    )
    private lateinit var repository: PersonalAccessTokenRepositoryImpl
    private lateinit var service: PersonalAccessTokenService
    private lateinit var timeProvider: MutablePatTimeProvider

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
        }
        timeProvider = MutablePatTimeProvider()
        repository = PersonalAccessTokenRepositoryImpl(database, timeProvider)
        service = PersonalAccessTokenServiceImpl(repository, UserRepositoryImpl(database), timeProvider)
    }

    @After
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(PersonalAccessTokenTable, UserTable)
        }
    }

    // ---- create: randomness / format ----

    @Test
    fun `create generates random tokens with expected format`() {
        timeProvider.setOtherTime(16, 5, 2021, 10, 0)

        val a = service.create(userId = 1, name = "A", expiration = TokenExpiration.NEVER)
        val b = service.create(userId = 1, name = "B", expiration = TokenExpiration.NEVER)

        for (creation in listOf(a, b)) {
            assertTrue(
                creation.token.matches(Regex("^[0-9a-f]{64}$")),
                "Token must be a 64-char lowercase hex string, was: ${creation.token.take(16)}..."
            )
        }
        assertNotEquals(a.token, b.token, "Two generated tokens must differ")
    }

    @Test
    fun `create persists only the sha256 hash, never the raw secret`() {
        timeProvider.setOtherTime(16, 5, 2021, 10, 0)
        val fixedCreatedAt = timeProvider.currentTime()

        val creation = service.create(userId = 1, name = "My Token", expiration = TokenExpiration.NEVER)

        // The raw secret is returned once, in the creation result only
        assertTrue(creation.token.length > 0)
        // The stored hash must be the sha256 of the raw secret
        assertEquals(sha256Hex(creation.token.toByteArray()), creation.pat.tokenHash)
        // The raw secret must not appear in the persisted object
        assertFalse(creation.pat.tokenHash == creation.token)

        val found = repository.findByTokenHash(creation.pat.tokenHash)
        val expected = PersonalAccessToken(
            id = creation.pat.id,
            userId = 1,
            name = "My Token",
            tokenHash = sha256Hex(creation.token.toByteArray()),
            tokenPrefix = sha256Hex(creation.token.toByteArray()).take(8),
            createdAt = fixedCreatedAt,
            expiresAt = null,
            lastUsedAt = null
        )
        assertEquals(expected, found)
    }

    @Test
    fun `create stores non-secret prefix of length 8`() {
        timeProvider.setOtherTime(16, 5, 2021, 10, 0)

        val creation = service.create(userId = 1, name = "Prefix", expiration = TokenExpiration.NEVER)

        assertEquals(8, creation.pat.tokenPrefix.length)
        assertTrue(creation.pat.tokenPrefix.matches(Regex("^[0-9a-f]{8}$")))
        assertEquals(creation.pat.tokenHash.take(8), creation.pat.tokenPrefix)
    }

    // ---- create: expiration options (UTC calendar months) ----

    @Test
    fun `create with THREE_MONTHS computes calendar month expiration`() {
        timeProvider.setOtherTime(16, 5, 2021, 10, 0)
        val fixedCreatedAt = timeProvider.currentTime()

        val creation = service.create(userId = 1, name = "Three", expiration = TokenExpiration.THREE_MONTHS)

        // 2021-05-16 + 3 calendar months = 2021-08-16, time preserved
        val expected = PersonalAccessToken(
            id = creation.pat.id,
            userId = 1,
            name = "Three",
            tokenHash = sha256Hex(creation.token.toByteArray()),
            tokenPrefix = sha256Hex(creation.token.toByteArray()).take(8),
            createdAt = fixedCreatedAt,
            expiresAt = LocalDateTime(2021, 8, 16, 10, 0),
            lastUsedAt = null
        )
        assertEquals(expected, creation.pat)
    }

    @Test
    fun `create with THREE_MONTHS does not use fixed day count on short months`() {
        timeProvider.setOtherTime(16, 1, 2021, 10, 0)
        val fixedCreatedAt = timeProvider.currentTime()

        val creation = service.create(userId = 1, name = "Three", expiration = TokenExpiration.THREE_MONTHS)

        // 2021-01-16 + 3 calendar months = 2021-04-16 (Jan has 31 days, so a fixed
        // 90/91 day count would land somewhere else)
        assertEquals(LocalDateTime(2021, 4, 16, 10, 0), creation.pat.expiresAt)
    }

    @Test
    fun `create with THREE_MONTHS clamps day-of-month to end of shorter month`() {
        timeProvider.setOtherTime(31, 12, 2021, 23, 59)
        val creation = service.create(userId = 1, name = "Three", expiration = TokenExpiration.THREE_MONTHS)

        // 2021-12-31 + 3 calendar months = 2022-03-31 (March has 31 days) - keep
        assertEquals(LocalDateTime(2022, 3, 31, 23, 59), creation.pat.expiresAt)

        timeProvider.setOtherTime(31, 1, 2021, 23, 59)
        val creationShort = service.create(userId = 1, name = "ThreeShort", expiration = TokenExpiration.THREE_MONTHS)
        // 2021-01-31 + 3 calendar months = 2021-04-30 (April has 30 days, day clamps)
        assertEquals(LocalDateTime(2021, 4, 30, 23, 59), creationShort.pat.expiresAt)
    }

    @Test
    fun `create with SIX_MONTHS computes calendar month expiration`() {
        timeProvider.setOtherTime(16, 5, 2021, 10, 0)
        val fixedCreatedAt = timeProvider.currentTime()

        val creation = service.create(userId = 1, name = "Six", expiration = TokenExpiration.SIX_MONTHS)

        // 2021-05-16 + 6 calendar months = 2021-11-16
        val expected = PersonalAccessToken(
            id = creation.pat.id,
            userId = 1,
            name = "Six",
            tokenHash = sha256Hex(creation.token.toByteArray()),
            tokenPrefix = sha256Hex(creation.token.toByteArray()).take(8),
            createdAt = fixedCreatedAt,
            expiresAt = LocalDateTime(2021, 11, 16, 10, 0),
            lastUsedAt = null
        )
        assertEquals(expected, creation.pat)
    }

    @Test
    fun `create with NEVER keeps expiresAt null`() {
        timeProvider.setOtherTime(16, 5, 2021, 10, 0)
        val fixedCreatedAt = timeProvider.currentTime()

        val creation = service.create(userId = 1, name = "Forever", expiration = TokenExpiration.NEVER)

        assertNull(creation.pat.expiresAt)
        assertEquals(fixedCreatedAt, creation.pat.createdAt)
    }

    // ---- verify: happy path, owner, last_used_at ----

    @Test
    fun `verify accepts valid token and returns its owner`() {
        timeProvider.setOtherTime(16, 5, 2021, 10, 0)
        val creation = service.create(userId = 1, name = "Valid", expiration = TokenExpiration.THREE_MONTHS)

        assertEquals(User(id = 1, email = "user1@test.com"), service.verify(creation.token))
    }

    @Test
    fun `verify updates lastUsedAt only on success`() {
        timeProvider.setOtherTime(16, 5, 2021, 10, 0)
        val creation = service.create(userId = 1, name = "Used", expiration = TokenExpiration.NEVER)
        assertNull(repository.findByTokenHash(creation.pat.tokenHash)!!.lastUsedAt)

        // A failed verification (unknown token) must not touch lastUsedAt of other tokens
        assertFailsWith<HttpException> { service.verify("0".repeat(64)) }
        assertNull(repository.findByTokenHash(creation.pat.tokenHash)!!.lastUsedAt)

        timeProvider.setOtherTime(16, 5, 2021, 11, 30)
        val fixedLastUsedAt = timeProvider.currentTime()

        service.verify(creation.token)

        val stored = repository.findByTokenHash(creation.pat.tokenHash)!!
        assertEquals(fixedLastUsedAt, stored.lastUsedAt)
    }

    // ---- verify: invalid / missing / expired ----

    @Test
    fun `verify rejects unknown token with 401`() {
        timeProvider.setOtherTime(16, 5, 2021, 10, 0)
        service.create(userId = 1, name = "Some", expiration = TokenExpiration.NEVER)

        val ex = assertFailsWith<HttpException> { service.verify("doesnotexist") }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.httpStatus)
    }

    @Test
    fun `verify rejects token that no longer exists after deletion`() {
        timeProvider.setOtherTime(16, 5, 2021, 10, 0)
        val creation = service.create(userId = 1, name = "Deleted", expiration = TokenExpiration.NEVER)

        service.deleteToken(userId = 1, tokenId = creation.pat.id)

        val ex = assertFailsWith<HttpException> { service.verify(creation.token) }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.httpStatus)
    }

    @Test
    fun `verify rejects expired token`() {
        timeProvider.setOtherTime(16, 5, 2021, 10, 0)
        val creation = service.create(userId = 1, name = "Expiring", expiration = TokenExpiration.THREE_MONTHS)

        // Still valid before expiration
        service.verify(creation.token)

        // Just after expiration
        timeProvider.setOtherTime(17, 8, 2021, 10, 0)
        val ex = assertFailsWith<HttpException> { service.verify(creation.token) }
        assertEquals(HttpStatus.UNAUTHORIZED, ex.httpStatus)

        // Expired token must not refresh lastUsedAt
        val stored = repository.findByTokenHash(creation.pat.tokenHash)!!
        assertEquals(LocalDateTime(2021, 5, 16, 10, 0), stored.lastUsedAt)
    }

    @Test
    fun `verify rejects token exactly at expiration boundary`() {
        timeProvider.setOtherTime(16, 5, 2021, 10, 0)
        val creation = service.create(userId = 1, name = "Boundary", expiration = TokenExpiration.THREE_MONTHS)

        timeProvider.setOtherTime(16, 8, 2021, 10, 0)
        val ex = assertFailsWith<HttpException> { service.verify(creation.token) }

        assertEquals(HttpStatus.UNAUTHORIZED, ex.httpStatus)
        assertNull(repository.findByTokenHash(creation.pat.tokenHash)!!.lastUsedAt)
    }

    @Test
    fun `UTC PAT time provider ignores the configured clock zone`() {
        val instant = Instant.parse("2021-05-16T10:00:00Z")
        val expected = LocalDateTime(2021, 5, 16, 10, 0)

        assertEquals(expected, UtcPatTimeProvider(Clock.fixed(instant, ZoneId.of("Europe/Warsaw"))).currentTime())
        assertEquals(expected, UtcPatTimeProvider(Clock.fixed(instant, ZoneOffset.UTC)).currentTime())
    }

    // ---- user-scoped operations ----

    @Test
    fun `getTokensForUser returns only the user tokens`() {
        timeProvider.setOtherTime(16, 5, 2021, 10, 0)
        val creation = service.create(userId = 1, name = "Mine", expiration = TokenExpiration.NEVER)

        val tokens = service.getTokensForUser(1)

        assertEquals(1, tokens.size)
        assertEquals(creation.pat.id, tokens.first().id)
        assertTrue(service.getTokensForUser(999).isEmpty())
    }
}
