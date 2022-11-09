package no.nav.hm.grunndata.search


import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/internal")
class StatusController() {

    @Get("/isReady")
    fun isReady(): String = "OK"

    @Get("/isAlive")
    fun isAlive(): String = "ALIVE"

}
