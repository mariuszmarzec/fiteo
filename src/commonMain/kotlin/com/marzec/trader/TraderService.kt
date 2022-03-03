package com.marzec.trader

import com.marzec.trader.model.Paper
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
}
