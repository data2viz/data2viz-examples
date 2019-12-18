

import io.data2viz.viz.JFxVizRenderer
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.stage.Stage


class MainJfx : Application() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(MainJfx::class.java)
        }
    }

    override fun start(stage: Stage?) {
        println("Building viz")
        val root = Group()
        stage?.let {
            it.scene = (Scene(root, vizSize.width, vizSize.height))
            it.show()
            val canvas = Canvas(vizSize.width, vizSize.height)
            root.children.add(canvas)

            val viz = buildViz()
            JFxVizRenderer(canvas, viz)
            viz.render()
        }
    }

}
