package nl.hillebrand

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

    val restTemplate = RestTemplate()

//    @Scheduled(fixedRate = 10000)
    @Scheduled(cron="0 9 * * * *")
    fun check() {
    try {
        val timeTable = restTemplate.getForObject(boatScheduleEndpoint, TimeTable::class.java);

        val outPassage = Attachment(
                "Heenreis",
                arrayListOf(Field(
                        "Tijd",
                        timeTable.outwards.map{ passage -> passage.departureTime }.joinToString("\n")
                ), Field(
                        "Beschikbaar",
                        timeTable.outwards.map{ passage -> passage.available }.joinToString("\n")
                )),
                "danger"
        )
        val retourPassage = Attachment(
                "Terugreis",
                arrayListOf(Field(
                        "Tijd",
                        timeTable.retour.map{ passage -> passage.departureTime }.joinToString("\n")
                ), Field(
                        "Beschikbaar",
                        timeTable.retour.map{ passage -> passage.available }.joinToString("\n")
                )),
                "danger"
        )

        val message = Message(
                "Beschikbaarheid op %td/%tm/%ty".format(Date(), Date(), Date()),
                arrayListOf(outPassage, retourPassage)
        )

        restTemplate.postForLocation(slackEndpoint, message)
    } catch(e: Exception) {
        e.printStackTrace();
    }
}
}