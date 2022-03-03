package com.marzec.trader.repositories

import com.marzec.database.dbCall
import com.marzec.database.findByIdOrThrow
import com.marzec.todo.database.TasksTable
import com.marzec.trader.TraderRepository
import com.marzec.trader.database.PaperEntity
import com.marzec.trader.database.PapersTable
import com.marzec.trader.model.Paper
import com.marzec.trader.model.Transaction
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

class TraderRepositoryImpl(private val database: Database) : TraderRepository {

    override fun getPapers(): List<Paper> = database.dbCall {
        PapersTable.selectAll()
            .map { PaperEntity.wrapRow(it).toDomain() }
    }

    override fun addPaper(paper: Paper): Paper = database.dbCall {
        val paperEntity = PaperEntity.new {
            code = paper.code
            name = paper.name
            type = paper.type.toString()
        }
        paperEntity.toDomain()
    }

    override fun updatePaper(paper: Paper): Paper = database.dbCall {
        val paperEntity = PaperEntity.findByIdOrThrow(paper.id.toInt())
        paperEntity.code = paper.code
        paperEntity.name = paper.name
        paperEntity.type = paper.type.toString()
        paperEntity.toDomain()
    }

    override fun removePaper(paperId: Int): Paper = database.dbCall {
        val paperEntity = PaperEntity.findByIdOrThrow(paperId)
        val paper = paperEntity.toDomain()
        paperEntity.delete()
        paper
    }

    override fun getTransactions(userId: Int): List<Transaction> {
        TODO("Not yet implemented")
    }

    override fun addTransaction(userId: Int, transaction: Transaction): Transaction {
        TODO("Not yet implemented")
    }

    override fun updateTransaction(userId: Int, transactionId: Int, transaction: Transaction): Transaction {
        TODO("Not yet implemented")
    }

    override fun removeTransaction(userId: Int, transactionId: Int): Transaction {
        TODO("Not yet implemented")
    }

}
