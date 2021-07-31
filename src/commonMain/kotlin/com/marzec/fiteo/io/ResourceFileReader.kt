package com.marzec.fiteo.io

interface ResourceFileReader {

    fun read(fileName: String): String?
}