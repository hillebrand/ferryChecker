package nl.hillebrand

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class ScheduledChecker {

    val baseUri = "https://boeken.rederij-doeksen.nl/products/departures?booking[departure_port]=H" +
            "&booking[arrival_port]=T&booking[departure_date]=23-07-2016&booking[party_type]=normal" +
            "&booking[pax][V]=0&booking[pax][V65]=2&booking[pax][K]=0&booking[pax][K4]=0&booking[pax][ip]=0" +
            "&booking[options][AL]=1&booking[specifications][AL]=FR&booking[retour_journey]=1" +
            "&booking[return_date]=13-08-2016&booking[ticket_type]=twoway&booking[only_available]=0"

    val restTemplate = RestTemplate()

    @Scheduled(fixedRate = 10000)
    fun check() {
        val timeTable = restTemplate.getForObject(baseUri, TimeTable::class.java);
        println("---------------------------------------------------------------------")

        for (passage in timeTable.outwards) {
            val maybe = if (passage.available) " " else " not "
            println("Outward passage on ${passage.departureTime} is${maybe}available ")
        }
        println("---------------------------------------------------------------------")
        for (passage in timeTable.retour) {
            val maybe = if (passage.available) " " else " not "
            println("Retour passage on ${passage.departureTime} is${maybe}available")
        }
        println("---------------------------------------------------------------------")
    }
}