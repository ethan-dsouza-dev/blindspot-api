package com.blindspot.blindspotapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class BlindspotApiApplication

fun main(args: Array<String>) {
    runApplication<BlindspotApiApplication>(*args)
}
