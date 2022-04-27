package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.trader.ApiPath
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext

class TraderTests {

    @Test
    fun getPapers() {
        testGetEndpoint(
            uri = ApiPath.PAPERS,
            status = HttpStatusCode.OK,
            responseDto = listOf(
                paperDto,
                paperDto2,
                paperDto3
            ),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addPaper(paperDto)
                addPaper(paperDto2)
                addPaper(paperDto3)
            }
        )
    }

    @Test
    fun addPapers() {
        testPostEndpoint(
            uri = ApiPath.PAPERS,
            status = HttpStatusCode.OK,
            dto = paperDto3,
            responseDto = paperDto3,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addPaper(paperDto)
                addPaper(paperDto2)
            },
            runRequestsAfter = {
                assertThat(papers()).isEqualTo(
                    listOf(
                        paperDto,
                        paperDto2,
                        paperDto3
                    )
                )
            }
        )
    }

    @Test
    fun addPapers_onlyOneSettlementCurrency() {
        testPostEndpoint(
            uri = ApiPath.PAPERS,
            status = HttpStatusCode.BadRequest,
            dto = paperDto.copy(name = "second"),
            responseDto = ErrorDto("There could be only one settlement currency"),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addPaper(paperDto)
            },
            runRequestsAfter = {
                assertThat(papers()).isEqualTo(
                    listOf(
                        paperDto
                    )
                )
            }
        )
    }

    @Test
    fun updatePapers() {
        testPatchEndpoint(
            uri = ApiPath.UPDATE_PAPERS,
            status = HttpStatusCode.OK,
            dto = paperDto3.copy(id = 1),
            responseDto = paperDto3.copy(id = 1),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addPaper(paperDto)
            }
        )
    }

    @Test
    fun removePaper() {
        testDeleteEndpoint(
            uri = ApiPath.DELETE_PAPERS.replace("{${Api.Args.ARG_ID}}", "2"),
            status = HttpStatusCode.OK,
            responseDto = paperDto2,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addPaper(paperDto)
                addPaper(paperDto2)
                addPaper(paperDto3)
            },
            runRequestsAfter = {
                assertThat(papers()).isEqualTo(
                    listOf(
                        paperDto,
                        paperDto3
                    )
                )
            }
        )
    }

    @Test
    fun getTransactions() {
        testGetEndpoint(
            uri = ApiPath.TRANSACTIONS,
            status = HttpStatusCode.OK,
            responseDto = listOf(
                transactionDto3,
                transactionDto2,
                transactionDto,
            ),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addPaper(paperDto)
                addPaper(paperDto2)
                addPaper(paperDto3)

                addTransaction(transactionDto)
                addTransaction(transactionDto2)
                addTransaction(transactionDto3)
            }
        )
    }

    @Test
    fun addTransactions() {
        testPostEndpoint(
            uri = ApiPath.TRANSACTIONS,
            status = HttpStatusCode.OK,
            dto = transactionDto3,
            responseDto = transactionDto3,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addPaper(paperDto)
                addPaper(paperDto2)
                addPaper(paperDto3)

                addTransaction(transactionDto)
                addTransaction(transactionDto2)
            },
            runRequestsAfter = {
                assertThat(transactions()).isEqualTo(
                    listOf(
                        transactionDto3,
                        transactionDto2,
                        transactionDto,
                    )
                )
            }
        )
    }

    @Test
    fun updateTransactions() {
        testPatchEndpoint(
            uri = ApiPath.UPDATE_TRANSACTIONS.replace("{${Api.Args.ARG_ID}}", "1"),
            status = HttpStatusCode.OK,
            dto = transactionDto3.copy(id = 1),
            responseDto = transactionDto3.copy(id = 1),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addPaper(paperDto)
                addPaper(paperDto2)
                addPaper(paperDto3)

                addTransaction(transactionDto)
            }
        )
    }

    @Test
    fun removeTransaction() {
        testDeleteEndpoint(
            uri = ApiPath.DELETE_TRANSACTIONS.replace("{${Api.Args.ARG_ID}}", "2"),
            status = HttpStatusCode.OK,
            responseDto = transactionDto2,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addPaper(paperDto)
                addPaper(paperDto2)
                addPaper(paperDto3)

                addTransaction(transactionDto)
                addTransaction(transactionDto2)
                addTransaction(transactionDto3)
            },
            runRequestsAfter = {
                assertThat(transactions()).isEqualTo(
                    listOf(
                        transactionDto3,
                        transactionDto
                    )
                )
            }
        )
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}