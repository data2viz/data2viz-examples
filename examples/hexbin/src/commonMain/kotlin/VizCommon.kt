

import io.data2viz.color.Colors
import io.data2viz.geom.Point
import io.data2viz.geom.Size
import io.data2viz.hexbin.Bin
import io.data2viz.hexbin.hexbinGenerator
import io.data2viz.random.RandomDistribution
import io.data2viz.scale.ScalesChromatic
import io.data2viz.scale.StrictlyContinuous
import io.data2viz.viz.PathNode
import io.data2viz.viz.Viz
import io.data2viz.viz.viz

const val POINT_COUNT = 2000

val vizSize = Size(600.0, 600.0)

val generator = RandomDistribution.normal(300.0, 80.0)

val allX = (1..POINT_COUNT).map { generator() }
val allY = (1..POINT_COUNT).map { generator() }
val points = allX.zip(allY).map { Point(it.first, it.second) }

val hexbin = hexbinGenerator {
    width = 600.0
    height = 600.0
    radius = 15.0
}

val bins by lazy { hexbin(points) }

val scale = ScalesChromatic.Sequential.SingleHue.purples {
    domain = StrictlyContinuous(0.0, bins.map { it.points.count().toDouble() }.max()!!)
}
fun buildViz(): Viz {

    val bins: MutableList<Bin> = hexbin(points)
    val paths = bins.map { bin ->
        val path = PathNode()
        hexbin.hexagon(path, Point(bin.x, bin.y))
        path.fill = scale(bin.points.size)
        path.stroke = Colors.Web.white
        path.strokeWidth = 1.0
        path
    }

    val viz = viz {
        size = vizSize
    }

    paths.forEach {
        viz.add(it)
    }
    return viz
}

