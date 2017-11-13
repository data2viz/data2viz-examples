import io.data2viz.core.Point
import io.data2viz.core.random
import io.data2viz.interpolate.linkedTo
import io.data2viz.math.Angle
import io.data2viz.math.deg
import io.data2viz.voronoi.Diagram
import io.data2viz.voronoi.Site
import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.scene.shape.SVGPath
import javafx.stage.Stage

data class SphereParams(
        var pointNumber: Int = 20,
        val chunkSize: Int = 1) {

    fun eventuallyUpdatePointNumber(curFPS: Double, curPoint: Int) {
        if (curFPS > 40.0 && curPoint <= pointNumber - chunkSize + 5) {
            pointNumber += chunkSize
        }
    }

}


fun main(args: Array<String>) {
    Application.launch(Main::class.java)
}

class Main : Application() {

    val size = 600

    val sphereParams = SphereParams()

    val randomPoints = newPoints(500).toMutableList()

    fun newPoints(count: Int = sphereParams.chunkSize) = Array(count) { GeoPoint((random() * 360).deg, (random() * 360).deg) }

    val circleRadius = io.data2viz.interpolate.scale.linear.numberToNumber(
            -1 linkedTo 3,
            1 linkedTo 1
    )

    val pointToScreen = io.data2viz.interpolate.scale.linear.numberToNumber(
            -1.0 linkedTo -200.0,
            1.0 linkedTo 200.0
    )


    fun List<GeoPoint>.sites() = mapIndexed { index, point -> Site(Point(pointToScreen(point.x).toDouble(), pointToScreen(point.y).toDouble()), index) }.toTypedArray()

    override fun start(stage: Stage) {

        stage.title = "JavaFX Scene Graph Demo"

        stage.scene = svg("voronoi.css") {

            var diagram:Diagram? = null

            val rotationAnimation = RotationAnimation(15.0)

            rotationAnimation { rotation ->
                randomPoints.forEach { geoPoint -> geoPoint.rotation = rotation }
                diagram = Diagram(randomPoints.sites(), Point(-1000.0, -1000.0), Point(1000.0, 1000.0))
            }


            g {
                transform {
                    translate(300.0, 300.0)
                    rotate((-20).deg)
                }

                fun Point.asCoordinate() = "$x,$y"

                //polygons
                g {
                    element.clip = Circle(.0, .0,200.0)
                    rotationAnimation { _ ->
                        val polygons = diagram!!.polygons()
                        selectAll<PathElement, List<Point>>("path", polygons) {
                            addAndUpdate = { path, points ->
                                path.element.content = "M${points.joinToString(separator = "L") { it.asCoordinate() }}Z"
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
                                circle.r = circleRadius(geoPoint.z).toDouble()
                                circle.cx = pointToScreen(geoPoint.x).toDouble()
                                circle.cy = pointToScreen(geoPoint.y).toDouble()
                            }
                        }
                    }
                }

            }

        }
        stage.show()
    }

    private fun svg(classFile: String = "", init: SVGElement.() -> Unit): Scene {
        val root = Group()
        val svg = SVGElement(root)
        init(svg)
        return Scene(root, 600.0, 600.0, Color.WHITE).apply {
            if (classFile.isNotBlank()) stylesheets.add(classFile)
        }
    }
}



class SVGElement(val root: Group) {

    fun g(init: GroupElement.() -> Unit){
        val g = GroupElement(Group())
        init(g)
        root.children.add(g.element)
    }

    fun rect(init: RectElement.() -> Unit) {
        val rect = RectElement()
        init(rect)
        root.children.add(rect.rectangle)
    }

    fun circle(init: CircleElement.() -> Unit = {}): CircleElement {
        val circleElement = CircleElement()
        init(circleElement)
        root.children.add(circleElement.element)
        return circleElement
    }

    fun selectChildren() {
        root.children
    }
}


interface ParentElement: ElementWrapper {
    fun removeChild(){
        (element as Parent)
    }
}

interface ElementWrapper {
    val element:Node
}

class GroupElement(override val element: Group): ElementWrapper {
    fun transform(init: TransformElement.() -> Unit) {
        val t = TransformElement(element)
        init(t)
    }

    fun g(init: GroupElement.() -> Unit){
        val g = GroupElement(Group())
        init(g)
        element.children.add(g.element)
    }


    fun rect(init: RectElement.() -> Unit):RectElement {
        val rect = RectElement()
        init(rect)
        element.children.add(rect.rectangle)
        return rect
    }

    fun circle(init: CircleElement.() -> Unit):CircleElement {
        val circle = CircleElement()
        init(circle)
        element.children.add(circle.element)
        return circle
    }

    fun path(init: PathElement.() -> Unit):PathElement {
        val path = PathElement()
        init(path)
        element.children.add(path.element)
        return path
    }



}

class TransformElement(val element: Group) {

    fun translate(x:Double, y:Double) {
        element.translateX = x
        element.translateY = y
    }

    fun rotate(angle: Angle){
        element.rotate = angle.deg
    }
}


class CircleElement(override val element: Circle = Circle()):ElementWrapper {
    init {
        element.radius = 5.0
    }

    var r:Double
        get() = element.radius
        set(value) { element.radius = value}

    var cx:Double
        get() = element.centerX
        set(value) { element.centerX = value}

    var cy:Double
        get() = element.centerY
        set(value) { element.centerY = value}

}

class PathElement(override val element: SVGPath = SVGPath()):ElementWrapper{

    init {
        element.styleClass.add("polygon")
    }
}

class RectElement(val rectangle: Rectangle = Rectangle()) {
    var width: Double
        get() = rectangle.width
        set(value) {
            rectangle.width = value
        }

    var height: Double
        get() = rectangle.height
        set(value) {
            rectangle.height = value
        }
}

class RotationAnimation(rotationTimeInSeconds: Double) {

    private val rotationPerMs = (360.0 / (rotationTimeInSeconds * 1000)).deg
    private val blocksOfAnimation = mutableListOf<(Angle) -> Any>()

    init {

        val timer = object : AnimationTimer() {
            var startTime = 0L

            override fun handle(now: Long) {
                if (startTime == 0L){
                    startTime = now
                    return
                }
                val elapsedMs = (now - startTime) / 1000000
                val rotation = rotationPerMs * elapsedMs
                blocksOfAnimation.forEach { it(rotation) }
            }
        }

        timer.start()
    }


    operator fun invoke(animation: (Angle) -> Unit) {
        blocksOfAnimation.add(animation)
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

inline fun <reified E : ElementWrapper, T> GroupElement.selectAll(selector: String, data: List<T>, init: Selection<E, T>.() -> Unit) {
    val selection = Selection<E, T>()
    selection.remove = { e -> element.children.remove(e) } //By default, on removal the element is removed from the parent.
    selection.init()

    val elements = element.children
    if (elements.size > data.size) elements.drop(data.size).forEach { selection.remove(it) }
    if (elements.size < data.size) data.drop(elements.size).forEachIndexed { _, t -> selection.add(create(), t) }
    data.take(elements.size).forEachIndexed { i, t -> selection.update(wrap(elements[i]), t) }
}

inline fun <reified E : ElementWrapper> wrap(element: Node): E {
    val c = E::class
    return when (c) {
//        LineElement::class -> LineElement(element) as E
        PathElement::class -> PathElement(element as SVGPath) as E
        CircleElement::class -> CircleElement(element as Circle) as E
        else -> error("Unknown type $c")
    }
}

inline fun <reified E : ElementWrapper> GroupElement.create(): E {
    val c = E::class
    return when (c) {
//        LineElement::class -> line {} as E
        PathElement::class -> path {} as E
        CircleElement::class -> circle {} as E
        else -> kotlin.error("Unknown type $c")
    }
}
