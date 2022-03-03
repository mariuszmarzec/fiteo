package com.marzec.trader

import com.marzec.trader.model.Paper
import com.marzec.trader.model.Transaction

interface TraderRepository {
    fun getPapers(): List<Paper>

    fun addPaper(paper: Paper): Paper

    fun updatePaper(paper: Paper): Paper

    fun removePaper(paperId: Int): Paper

    fun getTransactions(userId: Int): List<Transaction>

    fun addTransaction(userId: Int, transaction: Transaction): Transaction

    fun updateTransaction(userId: Int, transactionId: Int, transaction: Transaction): Transaction

    fun removeTransaction(userId: Int, transactionId: Int): Transaction
}
