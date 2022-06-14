package com.marzec.trader

import com.marzec.trader.model.Paper
import com.marzec.trader.model.PaperTag
import com.marzec.trader.model.Transaction

class TraderService(
    private val repository: TraderRepository
) {
    fun getPapers(): List<Paper> = repository.getPapers()

    fun addPaper(paper: Paper): Paper = repository.addPaper(paper)

    fun updatePaper(paper: Paper): Paper = repository.updatePaper(paper)

    fun removePaper(paperId: Int): Paper = repository.removePaper(paperId)

    fun getTransactions(userId: Int): List<Transaction> = repository.getTransactions(userId)

    fun addTransaction(userId: Int, transaction: Transaction): Transaction = repository.addTransaction(userId, transaction)

    fun updateTransaction(userId: Int, transactionId: Int, transaction: Transaction): Transaction = repository.updateTransaction(userId, transactionId, transaction)

    fun removeTransaction(userId: Int, transactionId: Int): Transaction = repository.removeTransaction(userId, transactionId)

    fun getPaperTags(): List<PaperTag> = repository.getPaperTags()

    fun addPaperTag(tag: PaperTag): PaperTag = repository.addPaperTag(tag)

    fun updatePaperTag(tag: PaperTag): PaperTag = repository.updatePaperTag(tag)

    fun removePaperTag(tagId: Int): PaperTag = repository.removePaperTag(tagId)
}
