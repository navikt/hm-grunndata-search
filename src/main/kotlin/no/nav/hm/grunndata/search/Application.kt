package no.nav.hm.grunndata.search

import io.micronaut.runtime.Micronaut

object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .packages("no.nav.hm.grunndata.search")
            .mainClass(Application.javaClass)
            .start()
    }
}
