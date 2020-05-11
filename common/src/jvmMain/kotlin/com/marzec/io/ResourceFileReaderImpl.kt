package com.marzec.io

actual class ResourceFileReaderImpl : ResourceFileReader {

    override fun read(fileName: String): String {
        return Any::class.java.getResource(fileName).readText()
    }
}