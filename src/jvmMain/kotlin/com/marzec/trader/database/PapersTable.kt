package com.marzec.trader.database

import com.marzec.trader.model.Paper
import com.marzec.trader.model.PaperTag
import com.marzec.trader.model.PaperType
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object PapersTable : IntIdTable("papers") {
    val code = varchar("code", 100)
    val name = varchar("name", 200)
    val type = varchar("type", 100)
}

class PaperEntity(id: EntityID<Int>) : IntEntity(id) {
    var code by PapersTable.code
    var name by PapersTable.name
    var type by PapersTable.type
    var tags by PaperTagsEntity via PaperToTagTable

    fun toDomain(): Paper = Paper(
        id = id.value.toLong(),
        code = code,
        name = name,
        type = PaperType.valueOf(type),
        tags = tags.toList().map { it.toDomain() }.takeIf { it.isNotEmpty() }
    )

    companion object : IntEntityClass<PaperEntity>(PapersTable)
}

object PaperTagsTable : IntIdTable("paper_tags") {
    val name = varchar("name", 200)
}

class PaperTagsEntity(id: EntityID<Int>) : IntEntity(id) {
    var name by PaperTagsTable.name

    fun toDomain() = PaperTag(
        id = id.value.toLong(),
        name = name
    )
    companion object : IntEntityClass<PaperTagsEntity>(PaperTagsTable)
}

object PaperToTagTable : IntIdTable("paper_to_tags") {
    val paper = reference("paper_id", PapersTable, onDelete = ReferenceOption.CASCADE)
    val tag = reference("tag_id", PaperTagsTable, onDelete = ReferenceOption.RESTRICT)
}