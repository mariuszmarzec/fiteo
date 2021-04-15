package com.marzec

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ApplicationTest {

    @Rule
    @JvmField
    var mysql = KMySQLContainer("mysql:8.0.22")
        .withDatabaseName("previous")
        .withUsername("test")
        .withPassword("test");

    @Before
    fun setUp() {
        mysql.start()
    }

    @Test
    fun testContainer() {
        mysql.jdbcUrl
        assertThat(mysql.jdbcUrl).isEmpty()
    }

    @After
    fun cleanUp() {
        mysql.stop()
    }
}

