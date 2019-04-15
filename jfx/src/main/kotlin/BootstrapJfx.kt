package io.dat2viz.samples

import io.data2viz.color.*
import io.data2viz.geom.*
import io.data2viz.math.*
import io.data2viz.viz.*
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.stage.Stage


val vizWidth = 600.0
val vizHeight = 600.0


class BootstrapJfx : Application() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Application.launch(BootstrapJfx::class.java)
        }
    }

    override fun start(stage: Stage?) {
        println("Building viz")
        val root = Group()
        val canvas = Canvas(vizWidth, vizHeight)
        val viz = commonViz

        JFxVizRenderer(canvas,viz)

        root.children.add(canvas)

        stage?.let {
            it.scene = (Scene(root, vizWidth, vizHeight))
            it.show()
            stage.title = "JavaFx - data2viz - Line Of SightJfx.kt"
        }

        viz.render()
    }

}