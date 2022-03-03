package com.marzec.trader.database

import com.marzec.trader.model.Paper
import com.marzec.trader.model.PaperType
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object PapersTable : IntIdTable("papers") {
    val code = varchar("code", 100)
    val name = varchar("name", 200)
    val type = varchar("type", 100)
}

class PaperEntity(id: EntityID<Int>) : IntEntity(id) {
    var code by PapersTable.code
    var name by PapersTable.name
    var type by PapersTable.type

    fun toDomain() = Paper(
        id = id.value.toLong(),
        code = code,
        name = name,
        type = PaperType.valueOf(type)
    )

    companion object : IntEntityClass<PaperEntity>(PapersTable)
}
