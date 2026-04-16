package com.marzec.cheatday.db

import com.marzec.cheatday.domain.CreateWeight
import com.marzec.cheatday.domain.UpdateWeight
import com.marzec.cheatday.domain.Weight
import com.marzec.core.entity.WithUserEntity
import com.marzec.core.entity.WithUserEntityClass
import com.marzec.database.IntEntityWithUser
import com.marzec.database.IntIdWithUserTable
import com.marzec.database.UserEntity
import com.marzec.database.UserTable
import com.marzec.extensions.update
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.javatime.datetime

object WeightsTable : IntIdWithUserTable("weights") {
    val value = float("weight_value")
    val date = datetime("weight_date")
    override val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
}

class WeightEntity(id: EntityID<Int>) : WithUserEntity<Weight>(id) {
    var value by WeightsTable.value
    var date by WeightsTable.date
    override var user: UserEntity by UserEntity referencedOn WeightsTable.userId

    override fun toDomain() = Weight(
        id = id.value,
        date = date.toKotlinLocalDateTime(),
        value = value
    )

    companion object : WithUserEntityClass<Weight, CreateWeight, UpdateWeight, WeightEntity>(WeightsTable) {

        override fun create(userId: Int, item: CreateWeight): WeightEntity =
            new {
                this.value = item.weight
                this.date = item.date.toJavaLocalDateTime()
                this.user = UserEntity.findById(userId)!!
            }

        override fun update(userId: Int, entity: WeightEntity, update: UpdateWeight): WeightEntity =
            entity.apply {
                update(this::value, update.value)
                update(this::date, update.date?.toLocalDateTime()?.toJavaLocalDateTime())
            }

    }
}
