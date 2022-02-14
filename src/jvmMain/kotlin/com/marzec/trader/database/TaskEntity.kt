package com.marzec.trader.database

import com.marzec.database.IntEntityWithUser
import com.marzec.database.UserEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object TransactionTable : IntIdTable("trader_transactions") {
}

class TransactionEntity(id: EntityID<Int>) : IntEntityWithUser(id) {
    override var user: UserEntity
        get() = TODO("Not yet implemented")
        set(value) {}

}
