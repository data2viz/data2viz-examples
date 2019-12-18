
import io.data2viz.viz.KPointerDoubleClick
import io.data2viz.viz.bindRendererOnNewCanvas

fun main() {

    println("starting chord viz")
    val chordViz = chordViz()
    chordViz.bindRendererOnNewCanvas()
    chordViz.on(KPointerDoubleClick) { evt ->
        println("AFTER INIT Pointer double click:: ${evt.pos}")
    }
}
