package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.trader.ApiPath
import com.marzec.trader.dto.PaperTagDto
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext
import kotlin.test.fail

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
            authorize = ApplicationTestBuilder::registerAndLogin,
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
            authorize = ApplicationTestBuilder::registerAndLogin,
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
    fun addPapersWithTags() {
        testPostEndpoint(
            uri = ApiPath.PAPERS,
            status = HttpStatusCode.OK,
            dto = paperDto2.copy(tags = listOf(tagDto, tagDto2)),
            responseDto = paperDto2.copy(tags = listOf(tagDto, tagDto2)),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                addPaperTag(tagDto)
                addPaperTag(tagDto2)
                addPaper(paperDto.copy(tags = listOf(tagDto)))
            },
            runRequestsAfter = {
                assertThat(papers()).isEqualTo(
                    listOf(
                        paperDto.copy(tags = listOf(tagDto)),
                        paperDto2.copy(tags = listOf(tagDto, tagDto2)),
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
            authorize = ApplicationTestBuilder::registerAndLogin,
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
            authorize = ApplicationTestBuilder::registerAndLogin,
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
    fun addTag() {
        testPostEndpoint(
            uri = ApiPath.ADD_PAPER_TAG,
            status = HttpStatusCode.OK,
            dto = tagDto,
            responseDto = tagDto,
            authorize = ApplicationTestBuilder::registerAndLogin
        )
    }

    @Test
    fun getTags() {
        testGetEndpoint(
            uri = ApiPath.PAPER_TAGS,
            status = HttpStatusCode.OK,
            responseDto = listOf(
                tagDto,
                tagDto2,
            ),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                addPaperTag(tagDto)
                addPaperTag(tagDto2)
            }
        )
    }

    @Test
    fun updateTag() {
        testPatchEndpoint(
            uri = ApiPath.UPDATE_PAPER_TAG.replace("{${Api.Args.ARG_ID}}", "1"),
            status = HttpStatusCode.OK,
            dto = tagDto2.copy(id = 1),
            responseDto = tagDto2.copy(id = 1),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                addPaperTag(tagDto)
            }
        )
    }

    @Test
    fun removeTag() {
        testDeleteEndpoint(
            uri = ApiPath.DELETE_PAPER_TAG.replace("{${Api.Args.ARG_ID}}", "2"),
            status = HttpStatusCode.OK,
            responseDto = tagDto2,
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                addPaperTag(tagDto)
                addPaperTag(tagDto2)
            },
            runRequestsAfter = {
                assertThat(tags()).isEqualTo(listOf(tagDto))
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
            authorize = ApplicationTestBuilder::registerAndLogin,
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
    fun getTransactions() {
        testGetEndpoint(
            uri = ApiPath.TRANSACTIONS,
            status = HttpStatusCode.OK,
            responseDto = listOf(
                transactionDto3,
                transactionDto2,
                transactionDto,
            ),
            authorize = ApplicationTestBuilder::registerAndLogin,
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
            authorize = ApplicationTestBuilder::registerAndLogin,
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
            authorize = ApplicationTestBuilder::registerAndLogin,
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
            authorize = ApplicationTestBuilder::registerAndLogin,
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