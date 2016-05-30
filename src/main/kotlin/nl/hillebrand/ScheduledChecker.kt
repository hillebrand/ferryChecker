package nl.hillebrand

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.*

@Component
class ScheduledChecker {

    val boatScheduleEndpoint = "https://boeken.rederij-doeksen.nl/products/departures?booking[departure_port]=H" +
            "&booking[arrival_port]=T&booking[departure_date]=23-07-2016&booking[party_type]=normal" +
            "&booking[pax][V]=0&booking[pax][V65]=2&booking[pax][K]=0&booking[pax][K4]=0&booking[pax][ip]=0" +
            "&booking[options][AL]=1&booking[specifications][AL]=FR&booking[retour_journey]=1" +
            "&booking[return_date]=13-08-2016&booking[ticket_type]=twoway&booking[only_available]=0"

    val slackEndpoint = "https://hooks.slack.com/services/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"

    val logger = LoggerFactory.getLogger(ScheduledChecker::class.java)

//        @Scheduled(fixedRate = 10000)
    @Scheduled(cron = "0 9 * * * *")
    fun check() {
        try {
            val restTemplate = RestTemplate()
            val timeTable = restTemplate.getForObject(boatScheduleEndpoint, TimeTable::class.java);

            val outwards: List<Passage> = timeTable.outwards.filter { passage -> passage.available }
            val outPassage = Attachment(
                    "Heenreis",
                    arrayListOf(Field(
                            "Tijd",
                            outwards.map { passage -> passage.departureTime }.joinToString("\n")
                    )),
                    if (outwards.isEmpty() || outwards.size < 2) {
                        "danger"
                    } else {
                        "good"
                    }
            )
            val retour: List<Passage> = timeTable.retour.filter { passage -> passage.available }
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
            val text: StringBuilder = StringBuilder()
            if (retour.isEmpty() || outwards.size >1) {
                text.append("@hillebrand ");
            }

            text.append("Beschikbaarheid op %td/%tm/%ty".format(Date(), Date(), Date()))
            val message = Message(
                    text.toString(),
                    arrayListOf(outPassage, retourPassage)
            )

            restTemplate.postForLocation(slackEndpoint, message)
        } catch(e: Exception) {
            logger.info("Exception while checking timetable", e)
        }
    }
}