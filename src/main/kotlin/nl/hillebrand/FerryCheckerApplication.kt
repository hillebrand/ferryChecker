package nl.hillebrand

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
open class FerryCheckerApplication {
    companion object {

        @JvmStatic fun main(args: Array<String>) {
            SpringApplication.run(FerryCheckerApplication::class.java)
        }
    }
}