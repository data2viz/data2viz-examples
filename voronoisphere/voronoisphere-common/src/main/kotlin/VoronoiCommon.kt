
import io.data2viz.math.Angle
import io.data2viz.math.deg


data class GeoPoint(private val lat: Angle, private val long: Angle) {
    var rotation: Angle = 0.deg
    val y = lat.sin
    private val r = lat.cos
    val x: Double get() = r * (rotation + long).cos
    val z: Double get() = r * (rotation + long).sin
}
