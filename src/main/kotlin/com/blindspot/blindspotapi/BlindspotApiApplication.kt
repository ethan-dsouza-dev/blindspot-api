package com.blindspot.blindspotapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BlindspotApiApplication

fun main(args: Array<String>) {
    runApplication<BlindspotApiApplication>(*args)
}
