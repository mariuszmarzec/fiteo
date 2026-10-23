package com.marzec.scripts

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RunAScriptArgumentsTest {

    @Test
    fun passesDateRangeAndOnlyTodoFlag() {
        assertThat(
            buildRunAScriptArguments(
                dateFrom = "2026-08-01",
                dateTo = "2026-08-31",
                onlyTodo = "true"
            )
        ).isEqualTo(listOf("01-08-2026", "31-08-2026", "--only-todo"))
    }

    @Test
    fun dateFromAloneUsesExistingPythonDefaultRange() {
        assertThat(
            buildRunAScriptArguments(
                dateFrom = "2026-08-01",
                dateTo = null,
                onlyTodo = null
            )
        ).isEqualTo(listOf("01-08-2026"))
    }

    @Test
    fun dateToAloneUsesSevenDayWindowEndingOnDateTo() {
        assertThat(
            buildRunAScriptArguments(
                dateFrom = null,
                dateTo = "2026-08-31",
                onlyTodo = null
            )
        ).isEqualTo(listOf("24-08-2026", "31-08-2026"))
    }

    @Test
    fun onlyTodoWithoutDatesPassesFlagAndKeepsPythonAutomaticRange() {
        assertThat(
            buildRunAScriptArguments(
                dateFrom = null,
                dateTo = null,
                onlyTodo = "TRUE"
            )
        ).isEqualTo(listOf("--only-todo"))
    }

    @Test
    fun falseOnlyTodoDoesNotPassFlag() {
        assertThat(
            buildRunAScriptArguments(
                dateFrom = "2026-08-01",
                dateTo = null,
                onlyTodo = "false"
            )
        ).isEqualTo(listOf("01-08-2026"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidDateFormat() {
        buildRunAScriptArguments(
            dateFrom = "01-08-2026",
            dateTo = null,
            onlyTodo = null
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsDateToBeforeDateFrom() {
        buildRunAScriptArguments(
            dateFrom = "2026-08-31",
            dateTo = "2026-08-01",
            onlyTodo = null
        )
    }
}
