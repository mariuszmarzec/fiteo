package com.marzec.mcp

import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.withCharset
import io.ktor.serialization.ContentConverter
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.charsets.Charset
import io.modelcontextprotocol.kotlin.sdk.types.JSONRPCMessage
import io.modelcontextprotocol.kotlin.sdk.types.McpJson
import kotlinx.serialization.encodeToString

/**
 * Keeps MCP JSON-RPC serialization independent from the application's REST JSON settings.
 *
 * The MCP SDK requires McpJson (notably encodeDefaults = true) for protocol responses.
 * This converter handles only JSON-RPC messages; all other responses fall through to the
 * application's existing JSON converter unchanged.
 */
internal class McpJsonContentConverter : ContentConverter {
    override suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?,
    ): TextContent? {
        val json = when (value) {
            is JSONRPCMessage -> McpJson.encodeToString(value)
            is List<*> -> {
                if (value.isEmpty() || value.any { it !is JSONRPCMessage }) return null
                McpJson.encodeToString(value.filterIsInstance<JSONRPCMessage>())
            }
            else -> return null
        }

        return TextContent(json, contentType.withCharset(charset))
    }

    override suspend fun deserialize(
        charset: Charset,
        typeInfo: TypeInfo,
        content: ByteReadChannel,
    ): Any? = null
}
