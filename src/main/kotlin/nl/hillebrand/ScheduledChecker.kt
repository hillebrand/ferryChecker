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
        val sdf = SimpleDateFormat("dd MMM HH:mm")
        sdf.timeZone = TimeZone.getTimeZone("Europe/Amsterdam")
        val messageText = "Beschikbaarheid op " + sdf.format(Date())
        check(true, messageText, "#ferrychecker")
    }

    fun check(sendMessage: Boolean, messageText: String, channel: String) {
        try {
            var doSend = sendMessage
            val restTemplate = RestTemplate()
            val timeTable = restTemplate.getForObject(boatScheduleEndpoint, TimeTable::class.java);

            val outwards: List<Passage>
            val retour: List<Passage>
            if (sendMessage) {
                outwards = timeTable.outwards.filter { passage -> passage.available }
                retour = timeTable.retour.filter { passage -> passage.available }
            } else {
                outwards = timeTable.outwards.filter { passage -> passage.available }.filter { hasDesiredTime(it) }
                retour = timeTable.retour.filter { passage -> passage.available }.filter { hasDesiredTime(it) }
                doSend = (!outwards.isEmpty() || !retour.isEmpty())
            }

            if (doSend) {
                sendSlackMessage(outwards, retour, messageText, channel)
            }
        } catch(e: Exception) {
            logger.info("Exception while checking timetable", e)
        }
    }

    private fun hasDesiredTime(passage: Passage): Boolean {
        val date = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(passage.departureTime)
        return date.hours > 8 && date.hours < 16
    }

    private fun sendSlackMessage(outwards: List<Passage>, retour: List<Passage>, text: String, channel: String) {
        val restTemplate = RestTemplate()
        val outPassage = Attachment(
                "Heenreis",
                arrayListOf(Field(
                        "Tijd",
                        outwards.map { passage -> passage.departureTime }.joinToString("\n")
                )),
                if (outwards.filter { hasDesiredTime(it) }.isEmpty()) {
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
                if (retour.filter { hasDesiredTime(it) }.isEmpty()) {
                    "danger"
                } else {
                    "good"
                }
        )
        val message = Message(text, arrayListOf(outPassage, retourPassage), channel)

        restTemplate.postForLocation(slackEndpoint, message)
    }
}