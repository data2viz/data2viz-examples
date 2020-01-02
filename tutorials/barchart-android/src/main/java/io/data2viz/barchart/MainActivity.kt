package io.data2viz.barchart

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.data2viz.color.Colors
import io.data2viz.geom.size
import io.data2viz.scale.Scales

import io.data2viz.viz.*



class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val vizView = viz
			.toView(this)
		setContentView(vizView)
	}
}


const val vizSize = 500.0

val data = listOf(4, 8, 15, 16, 23, 42)
const val barHeight = 14.0
const val padding = 2.0


val xScale = Scales.Continuous.linear {
	domain = listOf(.0, data.max()!!.toDouble())
	range = listOf(.0, vizSize - 2 * padding)
}


val viz = viz {

	size = size(vizSize, vizSize)


	data.forEachIndexed { index, datum ->
		group {
			transform {
				translate(
					x = padding,
					y = padding + index * (padding + barHeight))
			}
			rect {
				width = xScale(datum)
				height = barHeight
				fill = Colors.Web.indianred
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


