package com.marzec.trader

import com.marzec.trader.model.Paper
import com.marzec.trader.model.PaperTag
import com.marzec.trader.model.Transaction

interface TraderRepository {
    fun getPapers(): List<Paper>

    fun addPaper(paper: Paper): Paper

    fun updatePaper(paper: Paper): Paper

    fun removePaper(paperId: Int): Paper
    
    fun getPaperTags(): List<PaperTag>

    fun addPaperTag(tag: PaperTag): PaperTag

    fun updatePaperTag(tag: PaperTag): PaperTag

    fun removePaperTag(tagId: Int): PaperTag

    fun getTransactions(userId: Int): List<Transaction>

    fun addTransaction(userId: Int, transaction: Transaction): Transaction

    fun updateTransaction(userId: Int, transactionId: Int, transaction: Transaction): Transaction

    fun removeTransaction(userId: Int, transactionId: Int): Transaction
}
