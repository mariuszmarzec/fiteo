package com.marzec.io

interface ResourceFileReader {

    fun read(fileName: String): String
}

expect class ResourceFileReaderImpl() : ResourceFileReader