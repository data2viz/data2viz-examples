import io.data2viz.color.Colors
import io.data2viz.scale.Scales
import io.data2viz.viz.JFxVizRenderer
import io.data2viz.viz.TextHAlign
import io.data2viz.viz.TextVAlign
import io.data2viz.viz.viz
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.stage.Stage

const val width = 500.0
const val height = 400.0
const val barHeight = 14.0
const val padding = 2.0

val data = listOf(4, 8, 15, 16, 23, 42)

val xScale = Scales.Continuous.linear {
    domain = listOf(.0, data.max()!!.toDouble())
    range = listOf(.0, width- 2* padding)
}

class BarChartJFX : Application() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Starting application")
            launch(BarChartJFX::class.java)
        }
    }

    override fun start(stage: Stage?) {
        val root = Group()
        stage?.let { it ->
            it.title = "JavaFX bar chart"
            it.scene = Scene(root, width, height)
            val canvas = Canvas(width, height)
            root.children.add(canvas)

            val viz = viz {
                data.forEachIndexed { index, datum ->
                    group {
                        transform {
                            translate(
                                x = padding,
                                y = padding + index * (padding + barHeight) )
                        }
                        rect {
                            width = xScale(datum)
                            height = barHeight
                            fill = Colors.Web.steelblue
                        }
                        text {
                            textContent = datum.toString()
                            hAlign = TextHAlign.RIGHT
                            vAlign = TextVAlign.HANGING
                            x = xScale(datum) - 2.0
                            y = 1.5
                            textColor = Colors.Web.white
                            fontSize = 10.0
                        }
                    }
                }
            }
            JFxVizRenderer(canvas, viz)
            viz.render()
            it.show()
        }
    }

}
