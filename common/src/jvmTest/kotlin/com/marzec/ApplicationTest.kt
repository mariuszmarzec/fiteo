package com.marzec

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ApplicationTest {

    @Before
    fun setUp() {
        setupDb()
    }

    @Test
    fun testContainer() {
        assertThat(true).isTrue()
    }

    @After
    fun cleanUp() {
    }
}

