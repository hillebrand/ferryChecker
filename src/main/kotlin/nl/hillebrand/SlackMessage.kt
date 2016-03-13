package nl.hillebrand

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.ArrayList

data class Message(
        @JsonProperty("text")
        val text: String,
        @JsonProperty("attachments")
        val attachments: List<Attachment>)

data class Attachment(
        @JsonProperty("text")
        val text: String,
        @JsonProperty("fields")
        val fields: List<Field>,
        @JsonProperty("color")
        val color: String)

data class Field(
        @JsonProperty("title")
        val title: String,
        @JsonProperty("value")
        val value: String,
        @JsonProperty("short")
        val isShort: Boolean = true )