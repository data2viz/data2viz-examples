package io.dat2viz.samples.boostrapjs

import io.data2viz.color.colors
import io.data2viz.viz.bindRendererOn
import io.data2viz.viz.viz

fun main(args: Array<String>) {
    println("starting a first viz")
    viz {
        width = 400.0
        height = 400.0

        rect {
            width = 50.0
            height = 50.0
            x = 100.0
            y = 100.0
            style.fill = colors.red
        }

    }.bindRendererOn("viz")

}