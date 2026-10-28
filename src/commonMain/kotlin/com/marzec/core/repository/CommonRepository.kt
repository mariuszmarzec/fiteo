package com.marzec.core.repository

interface CommonRepository<ID : Comparable<ID>, DOMAIN, CREATE, UPDATE> {

    fun getAll(): List<DOMAIN>

    fun getById(id: ID): DOMAIN

    fun addAll(items: List<CREATE>): List<DOMAIN>

    fun create(item: CREATE): DOMAIN

    fun update(id: ID, update: UPDATE): DOMAIN

    fun delete(id: ID): DOMAIN
}

interface CommonWithUserRepository<DOMAIN, CREATE, UPDATE> {

    fun getAll(userId: Int): List<DOMAIN>

    fun getById(userId: Int, id: Int): DOMAIN

    fun addAll(userId: Int, items: List<CREATE>): List<DOMAIN>

    fun create(userId: Int, item: CREATE): DOMAIN

    fun update(userId: Int, id: Int, update: UPDATE): DOMAIN

    fun delete(userId: Int, id: Int): DOMAIN
}
