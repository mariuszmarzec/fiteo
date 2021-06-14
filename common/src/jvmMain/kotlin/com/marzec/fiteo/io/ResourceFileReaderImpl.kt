package com.marzec.fiteo.io

class ResourceFileReaderImpl : ResourceFileReader {

    override fun read(fileName: String): String {
        return Any::class.java.getResource(fileName).readText()
    }
}