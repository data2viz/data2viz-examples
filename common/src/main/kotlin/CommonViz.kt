package io.dat2viz.samples

import io.data2viz.color.Colors
import io.data2viz.geom.point
import io.data2viz.geom.size
import io.data2viz.viz.Viz
import io.data2viz.viz.viz

val commonViz:Viz =  viz {
    size = size(600,600)

    val gradient =
            Colors.Gradient.linear(point(0, 0), point(600, 0))
                    .withColor(Colors.Web.hotpink)
                    .andColor(Colors.Web.blueviolet)
    text {
        x = 10.0
        y = 70.0
        textContent = "Sketch your Viz!"
        textColor = gradient
        fontSize = 80.0
    }
}
