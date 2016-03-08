package nl.hillebrand

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class TimeTable(
        @JsonProperty("outwards")
        val outwards: List<Passage> = ArrayList(),
        @JsonProperty("retour")
        val retour: List<Passage> = ArrayList())

data class Passage(
        @JsonProperty("is_available")
        val available: Boolean = false,
        @JsonProperty("departure_time")
        val departureTime: String = "")