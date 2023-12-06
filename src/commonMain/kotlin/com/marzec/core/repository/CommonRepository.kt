package com.marzec.core.repository

interface CommonRepository<ID: Comparable<ID>, DOMAIN, CREATE, UPDATE> {

    fun getAll(): List<DOMAIN>

    fun getById(id: ID): DOMAIN

    fun addAll(items: List<CREATE>): List<DOMAIN>

    fun create(item: CREATE): DOMAIN

    fun update(id: ID, update: UPDATE): DOMAIN

    fun delete(id: ID): DOMAIN
}
