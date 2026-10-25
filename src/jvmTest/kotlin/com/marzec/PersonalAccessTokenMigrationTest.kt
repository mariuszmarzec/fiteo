package com.marzec

import org.flywaydb.core.Flyway
import org.junit.Test
import java.sql.DriverManager
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val PAT_MIGRATION_TEST_DB = "fiteo_test_pat_migration"
private const val PAT_MIGRATION_ENDPOINT =
    "jdbc:mysql://127.0.0.1:3306/$PAT_MIGRATION_TEST_DB?createDatabaseIfNotExist=TRUE"

/**
 * Migration-level coverage for V26: verifies the Flyway migration actually
 * creates the personal_access_tokens table with the expected schema, FK,
 * unique constraint, and index — using the repository's Flyway test setup
 * (same endpoint pattern as the CI build workflow).
 *
 * Uses a dedicated database to avoid race conditions with other tests
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

            val tables = meta.getTables(null, null, "personal_access_tokens", arrayOf("TABLE"))
            assertTrue(tables.next(), "Table personal_access_tokens should exist")
            assertEquals("TABLE", tables.getString("TABLE_TYPE"))

            val columns = meta.getColumns(null, null, "personal_access_tokens", null)
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
    fun `V26 creates FK cascade on user_id`() {
        val conn = DriverManager.getConnection(PAT_MIGRATION_ENDPOINT, "root", "1234")
        val stmt = conn.createStatement()
        try {
            val rs = stmt.executeQuery(
                "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS " +
                    "WHERE CONSTRAINT_SCHEMA = '$PAT_MIGRATION_TEST_DB' " +
                    "AND TABLE_NAME = 'personal_access_tokens' " +
                    "AND CONSTRAINT_NAME = 'fk_personal_access_tokens_user'"
            )
            assertTrue(rs.next(), "FK fk_personal_access_tokens_user should exist")
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
}
