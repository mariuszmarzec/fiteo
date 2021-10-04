package com.marzec.fiteo.io

class ResourceFileReaderImpl : ResourceFileReader {

    override fun read(fileName: String): String? {
        return try {
            Any::class.java.getResource(fileName)!!.readText()
        } catch(ignore: Exception) {
            null
        }
    }
}
