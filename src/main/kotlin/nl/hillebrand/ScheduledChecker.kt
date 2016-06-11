package nl.hillebrand

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.text.SimpleDateFormat
import java.util.*

@Component
open class ScheduledChecker {

    val boatScheduleEndpoint = "https://boeken.rederij-doeksen.nl/products/departures?booking[departure_port]=H" +
            "&booking[arrival_port]=T&booking[departure_date]=23-07-2016&booking[party_type]=normal" +
            "&booking[pax][V]=0&booking[pax][V65]=2&booking[pax][K]=0&booking[pax][K4]=0&booking[pax][ip]=0" +
            "&booking[options][AL]=1&booking[specifications][AL]=FR&booking[retour_journey]=1" +
            "&booking[return_date]=13-08-2016&booking[ticket_type]=twoway&booking[only_available]=0"

    val slackEndpoint = "https://hooks.slack.com/services/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"

    val logger = LoggerFactory.getLogger(ScheduledChecker::class.java)

    @Scheduled(fixedRate = 600000)
    fun checkFrequent() {
        check(false, "Overtocht beschikbaar", "#ferrychecker2")
    }

    @Scheduled(cron = "0 9 * * * *")
    fun checkRegular() {
        check(true, "Beschikbaarheid op %td/%tm/%ty %tH:%tM".format(Date(), Date(), Date(), Date(), Date()), "#ferrychecker")
    }

    fun check(allwaysSendMessage: Boolean, messageText: String, channel: String) {
        try {
            val restTemplate = RestTemplate()
            val timeTable = restTemplate.getForObject(boatScheduleEndpoint, TimeTable::class.java);

            val outwards: List<Passage> = timeTable.outwards.filter { passage -> passage.available }.filter { hasDesiredTime(it) }
            val retour: List<Passage> = timeTable.retour.filter { passage -> passage.available }.filter { hasDesiredTime(it) }

            if (!outwards.isEmpty() || !retour.isEmpty() || allwaysSendMessage) {
                sendSlackMessage(outwards, retour, messageText, channel)
            }
        } catch(e: Exception) {
            logger.info("Exception while checking timetable", e)
        }
    }

    private fun hasDesiredTime(passage: Passage): Boolean {
        val date = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(passage.departureTime)
        return date.hours > 8 && date.hours < 17
    }

    private fun sendSlackMessage(outwards: List<Passage>, retour: List<Passage>, text: String, channel: String) {
        val restTemplate = RestTemplate()
        val outPassage = Attachment(
                "Heenreis",
                arrayListOf(Field(
                        "Tijd",
                        outwards.map { passage -> passage.departureTime }.joinToString("\n")
                )),
                if (outwards.isEmpty()) {
                    "danger"
                } else {
                    "good"
                }
        )
        val retourPassage = Attachment(
                "Terugreis",
                arrayListOf(Field(
                        "Tijd",
                        retour.map { passage -> passage.departureTime }.joinToString("\n")
                )),
                if (retour.isEmpty()) {
                    "danger"
                } else {
                    "good"
                }
        )
        val message = Message(text, arrayListOf(outPassage, retourPassage), channel)

        restTemplate.postForLocation(slackEndpoint, message)
    }
}