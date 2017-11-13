
import io.data2viz.color.colors.black
import io.data2viz.core.Point
import io.data2viz.core.random
import io.data2viz.interpolate.linkedTo
import io.data2viz.interpolate.scale
import io.data2viz.math.Angle
import io.data2viz.math.deg
import io.data2viz.svg.*
import io.data2viz.voronoi.Diagram
import io.data2viz.voronoi.Site
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.asList
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Date
import kotlin.math.round

data class SphereParams(
        var pointNumber: Int = 20,
        val chunkSize: Int = 1) {

    fun eventuallyUpdatePointNumber(curFPS: Double, curPoint: Int) {
        if (curFPS > 40.0 && curPoint <= pointNumber - chunkSize + 5) {
            pointNumber += chunkSize
        }
    }

}


@JsName("start")
fun start() {

    val size = 600

    val sphereParams = SphereParams()

    val fpsCalculator = FpsCalculator(document.querySelector("#fps span")!!)

    fun newPoints(count: Int = sphereParams.chunkSize) = Array(count) { GeoPoint((random() * 360).deg, (random() * 360).deg) }

    val randomPoints = newPoints(10).toMutableList()

    val circleRadius = scale.linear.numberToNumber(
            -1 linkedTo 3,
            1 linkedTo 1
    )

    val pointToScreen = scale.linear.numberToNumber(
            -1.0 linkedTo -200.0,
            1.0 linkedTo 200.0
    )

    fun List<GeoPoint>.sites() = mapIndexed { index, point -> Site(Point(pointToScreen(point.x).toDouble(), pointToScreen(point.y).toDouble()), index) }.toTypedArray()

    svg {
        width = size
        height = size
        var diagram: Diagram? = null

        val rotationAnimation = RotationAnimation(15.0)
        rotationAnimation { rotation ->
            fpsCalculator.updateFPS()
            document.querySelector("#num span")?.textContent = randomPoints.size.toString()
            sphereParams.eventuallyUpdatePointNumber(fpsCalculator.aveFps, randomPoints.size)
            if (randomPoints.size < sphereParams.pointNumber && fpsCalculator.aveFps > 25) {
                randomPoints.addAll(newPoints())
            } else if (randomPoints.size > sphereParams.pointNumber) {
                val pointSize = randomPoints.size
                (1..sphereParams.chunkSize).forEach { randomPoints.removeAt(pointSize - it) }
            }
            randomPoints.forEach { geoPoint -> geoPoint.rotation = rotation }
                diagram = Diagram(randomPoints.sites(), Point(-1000.0, -1000.0), Point(1000.0, 1000.0))
        }

        g {
            transform {
                translate(size / 2, size / 2)
                rotate((-20).deg)
            }

            fun Point.asCoordinate() = "$x,$y"

            circle {
                r = 200.0
                stroke = black
                fill = null
            }

            //polygons
            g {
                setAttribute("clip-path", "url(#circle-mask)")
                rotationAnimation { _ ->
                    val polygons = diagram!!.polygons()
                    selectAll<PathElement, List<Point>>("path", polygons) {
                        addAndUpdate = { path, points ->
                            path.element.setAttribute("d", "M${points.joinToString(separator = "L") { it.asCoordinate() }}Z" )
                        }
                    }
                }
            }

            //circles
            g {
                rotationAnimation { _ ->
                    val points:List<GeoPoint> = randomPoints
                    selectAll<CircleElement, GeoPoint>("circle", points) {
                        addAndUpdate = { circle, geoPoint ->
                            circle.r = circleRadius(geoPoint.z)
                            circle.cx = pointToScreen(geoPoint.x)
                            circle.cy = pointToScreen(geoPoint.y)
                        }
                    }
                }
            }
        }

    }
}

//------------   API test -----------------------


class Selection<E : ElementWrapper, T> {
    var add: ((E, T) -> Unit) = { _, _ -> }
    var addAndUpdate: ((E, T) -> Unit)
        get() = update
        set(value) {
            add = value
            update = value
        }
    var update: ((E, T) -> Unit) = { _, _ -> }
    var remove: ((Node) -> Unit) = {}
}

inline fun <reified E : ElementWrapper> wrap(element: Element): E {
    val c = E::class
    return when (c) {
        LineElement::class -> LineElement(element) as E
        PathElement::class -> PathElement(element) as E
        CircleElement::class -> CircleElement(element) as E
        else -> error("Unknown type $c")
    }
}

inline fun <reified E : ElementWrapper> ParentElement.create(): E {
    val c = E::class
    return when (c) {
        LineElement::class -> line {} as E
        PathElement::class -> path {} as E
        CircleElement::class -> circle {} as E
        else -> kotlin.error("Unknown type $c")
    }
}

inline fun <reified E : ElementWrapper, T> ParentElement.selectAll(selector: String, data: List<T>, init: Selection<E, T>.() -> Unit) {
    val selection = Selection<E, T>()
    selection.remove = { e -> removeChild(e) } //By default, on removal the element is removed from the parent.
    selection.init()

    val elements = element.querySelectorAll(selector).asList().map { it as Element }
    if (elements.size > data.size) elements.drop(data.size).forEach { selection.remove(it) }
    if (elements.size < data.size) data.drop(elements.size).forEachIndexed { _, t -> selection.add(create(), t) }
    data.take(elements.size).forEachIndexed { i, t -> selection.update(wrap(elements[i]), t) }
}

fun ParentElement.removeChild(child: Node) {
    element.removeChild(child)
}

class RotationAnimation(rotationTimeInSeconds: Double) {

    private val startTime = Date().getTime()
    private val rotationPerMs = (360.0 / (rotationTimeInSeconds * 1000)).deg
    private val blocksOfAnimation = mutableListOf<(Angle) -> Unit>()

    init {
        fun animate() {
            val currentTime = Date().getTime()
            window.requestAnimationFrame {
                val rotation = rotationPerMs * (currentTime - startTime)
                blocksOfAnimation.forEach { it(rotation) }
                animate()
            }
        }
        animate()
    }

    operator fun invoke(animation: (Angle) -> Unit) {
        blocksOfAnimation.add(animation)
    }
}


/**
 * Utility class used to calculate the FPS and show the current FPS.
 * updateFPS() must be called at each update.
 * The given fps element is set to the current FPS each 10 calls to updateFPS.
 */
class FpsCalculator(private var fps: Element?) {
    private var curFps = 100
    var aveFps = 100.0
    private val averageFps = mutableListOf<Int>()

    private var time0 = Date().getTime()
    private var time1 = Date().getTime()

    fun updateFPS() {
        if (fps == null) return
        time1 = Date().getTime()
        if (time1 != time0) {
            curFps = round(1000.0 / (time1 - time0)).toInt()
            averageFps.add(curFps)
            if (averageFps.size == 10) {
                aveFps = averageFps.average()
                fps?.textContent = aveFps.toInt().toString()
                averageFps.clear()
            }
        }
        time0 = time1
    }
}
