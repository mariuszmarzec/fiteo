package com.marzec

import org.flywaydb.core.Flyway
import org.junit.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val PAT_MIGRATION_TEST_DB = "fiteo_test_pat_migration"
private const val PAT_MIGRATION_ENDPOINT =
    "jdbc:mysql://127.0.0.1:3306/$PAT_MIGRATION_TEST_DB?createDatabaseIfNotExist=TRUE"
private const val PAT_UPGRADE_TEST_DB = "fiteo_test_pat_upgrade"
private const val PAT_UPGRADE_ENDPOINT =
    "jdbc:mysql://127.0.0.1:3306/$PAT_UPGRADE_TEST_DB?createDatabaseIfNotExist=TRUE"

private fun migrationLocation(): String =
    "filesystem:${System.getProperty("user.dir")}/src/commonMain/resources/db/migration"

/**
 * Migration-level coverage for V26: verifies the Flyway migration actually
 * creates the personal_access_tokens table with the expected schema, column
 * metadata, FK (CASCADE), unique constraint, and index — using the
 * repository's Flyway test setup (same endpoint pattern as the CI build
 * workflow). Also verifies a V25 -> V26 upgrade applies V26 on top of an
 * existing schema, and that the FK actually cascades deletions.
 *
 * Uses dedicated databases to avoid race conditions with other tests
 * that share the main test database.
 */
class PersonalAccessTokenMigrationTest {

    companion object {
        @JvmStatic
        @org.junit.BeforeClass
        fun setUpClass() {
            Flyway.configure()
                .dataSource(PAT_MIGRATION_ENDPOINT, "root", "1234")
                .load()
                .clean()
            Flyway.configure()
                .dataSource(PAT_MIGRATION_ENDPOINT, "root", "1234")
                .load()
                .migrate()
        }
    }

    @Test
    fun `V26 creates personal_access_tokens table with correct columns`() {
        val conn = DriverManager.getConnection(PAT_MIGRATION_ENDPOINT, "root", "1234")
        try {
            val meta = conn.metaData
            val catalog = conn.catalog

            val tables = meta.getTables(catalog, null, "personal_access_tokens", arrayOf("TABLE"))
            assertTrue(tables.next(), "Table personal_access_tokens should exist")
            assertEquals("TABLE", tables.getString("TABLE_TYPE"))

            val columns = meta.getColumns(catalog, null, "personal_access_tokens", null)
            val columnNames = mutableSetOf<String>()
            while (columns.next()) {
                columnNames.add(columns.getString("COLUMN_NAME").lowercase())
            }
            assertEquals(
                setOf("id", "user_id", "name", "token_hash", "token_prefix", "created_at", "expires_at", "last_used_at"),
                columnNames,
                "Table should have exactly the expected columns"
            )
        } finally {
            conn.close()
        }
    }

    @Test
    fun `V26 creates columns with expected types, sizes, and nullability`() {
        val conn = DriverManager.getConnection(PAT_MIGRATION_ENDPOINT, "root", "1234")
        try {
            val catalog = conn.catalog
            val columns = conn.metaData.getColumns(catalog, null, "personal_access_tokens", null)
            // Connector/J can return duplicate rows; keep the first entry per column
            val actual = mutableMapOf<String, Triple<String, Int, String>>()
            while (columns.next()) {
                val name = columns.getString("COLUMN_NAME").lowercase()
                if (name !in actual) {
                    actual[name] = Triple(
                        columns.getString("TYPE_NAME").uppercase(),
                        columns.getInt("COLUMN_SIZE"),
                        columns.getString("IS_NULLABLE")
                    )
                }
            }
            val expected = mapOf(
                "id" to Triple("INT", 10, "NO"),
                "user_id" to Triple("INT", 10, "NO"),
                "name" to Triple("VARCHAR", 255, "NO"),
                "token_hash" to Triple("VARCHAR", 64, "NO"),
                "token_prefix" to Triple("VARCHAR", 16, "NO"),
                "created_at" to Triple("DATETIME", 19, "NO"),
                "expires_at" to Triple("DATETIME", 19, "YES"),
                "last_used_at" to Triple("DATETIME", 19, "YES")
            )
            assertEquals(expected, actual, "Column metadata should match the V26 definitions")
        } finally {
            conn.close()
        }
    }

    @Test
    fun `V26 creates FK cascade on user_id`() {
        val conn = DriverManager.getConnection(PAT_MIGRATION_ENDPOINT, "root", "1234")
        val stmt = conn.createStatement()
        try {
            val rs = stmt.executeQuery(
                "SELECT DELETE_RULE FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS " +
                    "WHERE CONSTRAINT_SCHEMA = '$PAT_MIGRATION_TEST_DB' " +
                    "AND TABLE_NAME = 'personal_access_tokens' " +
                    "AND CONSTRAINT_NAME = 'fk_personal_access_tokens_user'"
            )
            assertTrue(rs.next(), "FK fk_personal_access_tokens_user should exist")
            assertEquals("CASCADE", rs.getString("DELETE_RULE"), "FK should cascade on user delete")
        } finally {
            stmt.close()
            conn.close()
        }
    }

    @Test
    fun `V26 creates unique constraint on token_hash`() {
        val conn = DriverManager.getConnection(PAT_MIGRATION_ENDPOINT, "root", "1234")
        val stmt = conn.createStatement()
        try {
            val rs = stmt.executeQuery(
                "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS " +
                    "WHERE CONSTRAINT_SCHEMA = '$PAT_MIGRATION_TEST_DB' " +
                    "AND TABLE_NAME = 'personal_access_tokens' " +
                    "AND CONSTRAINT_TYPE = 'UNIQUE' " +
                    "AND CONSTRAINT_NAME = 'uq_personal_access_tokens_token_hash'"
            )
            assertTrue(rs.next(), "Unique constraint uq_personal_access_tokens_token_hash should exist")
        } finally {
            stmt.close()
            conn.close()
        }
    }

    @Test
    fun `V26 creates index on user_id`() {
        val conn = DriverManager.getConnection(PAT_MIGRATION_ENDPOINT, "root", "1234")
        val stmt = conn.createStatement()
        try {
            val rs = stmt.executeQuery(
                "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
                    "WHERE TABLE_SCHEMA = '$PAT_MIGRATION_TEST_DB' " +
                    "AND TABLE_NAME = 'personal_access_tokens' " +
                    "AND INDEX_NAME = 'idx_personal_access_tokens_user_id'"
            )
            assertTrue(rs.next(), "Index idx_personal_access_tokens_user_id should exist")
        } finally {
            stmt.close()
            conn.close()
        }
    }

    @Test
    fun `Flyway history includes V26 as applied`() {
        val flyway = Flyway.configure()
            .dataSource(PAT_MIGRATION_ENDPOINT, "root", "1234")
            .load()
        val allInfos = flyway.info().all()
        val v26 = allInfos.firstOrNull { it.version.toString() == "26" }
        assertNotNull(v26, "V26 should be present in Flyway schema history")
        assertTrue(v26!!.state.isApplied, "V26 should be applied successfully (state=${v26.state})")
    }

    @Test
    fun `deleting a user cascades to its personal access tokens`() {
        val conn = DriverManager.getConnection(PAT_MIGRATION_ENDPOINT, "root", "1234")
        conn.autoCommit = false
        try {
            val userId = conn.prepareStatement(
                "INSERT INTO users (email, password) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS
            ).use { insert ->
                insert.setString(1, "cascade-test@example.com")
                insert.setString(2, "password")
                insert.executeUpdate()
                insert.generatedKeys.use { keys ->
                    assertTrue(keys.next())
                    keys.getInt(1)
                }
            }
            conn.prepareStatement(
                "INSERT INTO personal_access_tokens (user_id, name, token_hash, token_prefix) " +
                    "VALUES (?, ?, ?, ?)"
            ).use { insert ->
                insert.setInt(1, userId)
                insert.setString(2, "cascade token")
                insert.setString(3, "cascade-test-token-hash")
                insert.setString(4, "casctest")
                insert.executeUpdate()
            }
            assertEquals(
                1,
                countRows(conn, "SELECT COUNT(*) FROM personal_access_tokens WHERE user_id = ?", userId),
                "PAT row should exist before the user is deleted"
            )

            conn.createStatement().use { it.executeUpdate("DELETE FROM users WHERE id = $userId") }
            assertEquals(
                0,
                countRows(conn, "SELECT COUNT(*) FROM personal_access_tokens WHERE user_id = ?", userId),
                "Deleting the user should cascade to its PAT rows"
            )
            conn.commit()
        } catch (e: SQLException) {
            conn.rollback()
            throw e
        } finally {
            conn.close()
        }
    }

    @Test
    fun `V25 to V26 upgrade applies V26 on an existing schema`() {
        // Simulate a legacy database already at migration V25
        runFlyway(PAT_UPGRADE_ENDPOINT, "25").clean()
        runFlyway(PAT_UPGRADE_ENDPOINT, "25").migrate()

        val conn = DriverManager.getConnection(PAT_UPGRADE_ENDPOINT, "root", "1234")
        val legacyUserId = conn.prepareStatement(
            "INSERT INTO users (email, password) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS
        ).use { insert ->
            insert.setString(1, "upgrade-user@example.com")
            insert.setString(2, "password")
            insert.executeUpdate()
            insert.generatedKeys.use { keys ->
                assertTrue(keys.next())
                keys.getInt(1)
            }
        }
        assertFalse(
            tableExists(conn, "personal_access_tokens"),
            "PAT table must not exist before the V26 upgrade"
        )
        conn.close()

        // Upgrade: run migrations again without a target; V26 must apply on top
        runFlyway(PAT_UPGRADE_ENDPOINT).migrate()

        val v26 = runFlyway(PAT_UPGRADE_ENDPOINT).info().all()
            .firstOrNull { it.version.toString() == "26" }
        assertNotNull(v26, "V26 should be present in the Flyway schema history")
        assertTrue(v26!!.state.isApplied, "V26 should be applied (state=${v26.state})")

        val upgraded = DriverManager.getConnection(PAT_UPGRADE_ENDPOINT, "root", "1234")
        try {
            assertTrue(
                tableExists(upgraded, "personal_access_tokens"),
                "Upgrade should create personal_access_tokens"
            )
            assertEquals(
                1,
                countRows(upgraded, "SELECT COUNT(*) FROM users WHERE id = ?", legacyUserId),
                "Legacy user should survive the upgrade"
            )
        } finally {
            upgraded.close()
        }

        // Clean the dedicated upgrade database for the next run
        runFlyway(PAT_UPGRADE_ENDPOINT).clean()
    }

    private fun runFlyway(endpoint: String, target: String? = null): Flyway {
        val builder = Flyway.configure()
            .dataSource(endpoint, "root", "1234")
            .locations(migrationLocation())
        if (target != null) {
            builder.target(target)
        }
        return builder.load()
    }

    private fun tableExists(conn: Connection, tableName: String): Boolean {
        val catalog = conn.catalog
        return conn.metaData.getTables(catalog, null, tableName, arrayOf("TABLE")).next()
    }

    private fun countRows(conn: Connection, sql: String, vararg args: Any): Int {
        val statement = conn.prepareStatement(sql)
        args.forEachIndexed { index, value -> statement.setObject(index + 1, value) }
        return statement.use {
            it.executeQuery().use { rs ->
                assertTrue(rs.next())
                rs.getInt(1)
            }
        }
    }
}
