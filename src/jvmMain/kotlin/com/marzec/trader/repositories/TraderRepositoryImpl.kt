package com.marzec.trader.repositories

import com.marzec.database.UserEntity
import com.marzec.database.dbCall
import com.marzec.database.findByIdOrThrow
import com.marzec.trader.TraderRepository
import com.marzec.trader.database.PaperEntity
import com.marzec.trader.database.PapersTable
import com.marzec.trader.database.TransactionEntity
import com.marzec.trader.database.TransactionTable
import com.marzec.trader.model.Paper
import com.marzec.trader.model.Transaction
import kotlinx.datetime.toJavaLocalDateTime
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

    override fun getTransactions(userId: Int): List<Transaction> = database.dbCall {
        TransactionTable.selectAll()
            .andWhere { TransactionTable.userId.eq(userId) }
            .map { TransactionEntity.wrapRow(it).toDomain() }
    }

    override fun addTransaction(userId: Int, transaction: Transaction): Transaction = database.dbCall {
        val transactionEntity = TransactionEntity.new {
            title = transaction.title
            date = transaction.date.toJavaLocalDateTime()
            targetPaper = PaperEntity.findByIdOrThrow(transaction.targetPaper.id.toInt())
            sourcePaper = PaperEntity.findByIdOrThrow(transaction.sourcePaper.id.toInt())
            targetValue = transaction.targetValue.toString()
            totalPriceInSource = transaction.totalPriceInSource.toString()
            pricePerUnit = transaction.pricePerUnit.toString()
            settlementRate = transaction.settlementRate.toString()
            fee = transaction.fee.toString()
            feePaper = PaperEntity.findByIdOrThrow(transaction.feePaper.id.toInt())
            type = transaction.type.toString()
            user = UserEntity.findByIdOrThrow(userId)
        }
        transactionEntity.toDomain()
    }

    override fun updateTransaction(userId: Int, transactionId: Int, transaction: Transaction): Transaction =
        database.dbCall {
            TransactionEntity.findByIdOrThrow(transaction.id.toInt()).apply {
                title = transaction.title
                date = transaction.date.toJavaLocalDateTime()
                targetPaper = PaperEntity.findByIdOrThrow(transaction.targetPaper.id.toInt())
                sourcePaper = PaperEntity.findByIdOrThrow(transaction.sourcePaper.id.toInt())
                targetValue = transaction.targetValue.toString()
                totalPriceInSource = transaction.totalPriceInSource.toString()
                pricePerUnit = transaction.pricePerUnit.toString()
                settlementRate = transaction.settlementRate.toString()
                fee = transaction.fee.toString()
                feePaper = PaperEntity.findByIdOrThrow(transaction.feePaper.id.toInt())
                type = transaction.type.toString()
                user = UserEntity.findByIdOrThrow(userId)
            }.toDomain()
        }


    override fun removeTransaction(userId: Int, transactionId: Int): Transaction = database.dbCall {
        val entity = TransactionEntity.findByIdOrThrow(transactionId)
        val domain = entity.toDomain()
        entity.deleteIfBelongsToUserOrThrow(userId)
        domain
    }
}