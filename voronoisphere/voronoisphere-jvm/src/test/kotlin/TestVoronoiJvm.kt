import io.data2viz.math.deg
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class TestVoronoiJvm {

    @Test
    fun geoPoint(){
        val pt = GeoPoint(0.deg, 0.deg)
        assertEquals(1.0, pt.x)
        assertEquals(0.0, pt.y)
        assertEquals(0.0, pt.z)
    }

    @Test
    fun geoPointAtPole(){
        val pt = GeoPoint(90.deg, 0.deg)
        assertTrue (pt.x < .0000001)
        assertEquals(1.0, pt.y)
        assertEquals(0.0, pt.z)
    }
}
