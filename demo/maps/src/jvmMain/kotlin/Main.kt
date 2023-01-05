import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import center.sciprog.attributes.AlphaAttribute
import center.sciprog.attributes.Attributes
import center.sciprog.attributes.ColorAttribute
import center.sciprog.attributes.ZAttribute
import center.sciprog.maps.compose.*
import center.sciprog.maps.coordinates.*
import center.sciprog.maps.features.*
import center.sciprog.maps.geojson.geoJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.URL
import java.nio.file.Path
import kotlin.math.PI
import kotlin.random.Random

private fun GeodeticMapCoordinates.toShortString(): String =
    "${(latitude.degrees.value).toString().take(6)}:${(longitude.degrees.value).toString().take(6)}"


@Composable
@Preview
fun App() {
    MaterialTheme {

        val scope = rememberCoroutineScope()

        val mapTileProvider = remember {
            OpenStreetMapTileProvider(
                client = HttpClient(CIO),
                cacheDirectory = Path.of("mapCache")
            )
        }

        val centerCoordinates = MutableStateFlow<Gmc?>(null)


        val pointOne = 55.568548 to 37.568604
        val pointTwo = 55.929444 to 37.518434
        val pointThree = 60.929444 to 37.518434

        MapView(
            mapTileProvider = mapTileProvider,
            config = ViewConfig(
                onViewChange = { centerCoordinates.value = focus },
                onClick = { _, viewPoint ->
                    println(viewPoint)
                }
            )
        ) {

            geoJson(URL("https://raw.githubusercontent.com/ggolikov/cities-comparison/master/src/moscow.geo.json"))
                .attribute(ColorAttribute, Color.Blue)
                .attribute(AlphaAttribute, 0.4f)

            image(pointOne, Icons.Filled.Home)

            val marker1 = rectangle(55.744 to 38.614, size = DpSize(10.dp, 10.dp)).color(Color.Magenta)
            val marker2 = rectangle(55.8 to 38.5, size = DpSize(10.dp, 10.dp)).color(Color.Magenta)
            val marker3 = rectangle(56.0 to 38.5, size = DpSize(10.dp, 10.dp)).color(Color.Magenta)

            draggableLine(marker1, marker2).color(Color.Blue)
            draggableLine(marker2, marker3).color(Color.Blue)
            draggableLine(marker3, marker1).color(Color.Blue)

            points(
                points = listOf(
                    55.742465 to 37.615812,
                    55.742713 to 37.616370,
                    55.742815 to 37.616659,
                    55.742320 to 37.617132,
                    55.742086 to 37.616566,
                    55.741715 to 37.616716
                ),
                pointMode = PointMode.Polygon
            )

            //remember feature ID
            val circleId = circle(
                centerCoordinates = pointTwo,
            )
            scope.launch {
                while (isActive) {
                    delay(200)
                    circleId.color(Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat()))
                }
            }


//            draw(position = pointThree) {
//                drawLine(start = Offset(-10f, -10f), end = Offset(10f, 10f), color = Color.Red)
//                drawLine(start = Offset(-10f, 10f), end = Offset(10f, -10f), color = Color.Red)
//            }

            arc(pointOne, 10.0.kilometers, (PI / 4).radians, -Angle.pi / 2)

            line(pointOne, pointTwo, id = "line")
            text(pointOne, "Home", font = { size = 32f })

            centerCoordinates.filterNotNull().onEach {
                group(id = "center") {
                    circle(center = it, id = "circle", size = 1.dp).color(Color.Blue)
                    text(position = it, it.toShortString(), id = "text").color(Color.Blue)
                }
            }.launchIn(scope)


            forEachWithType<Gmc, PolygonFeature<Gmc>> { id, feature ->
                id.onClick {
                    println("Click on $id")
                    //draw in top-level scope
                    with(this@MapView) {
                        points(
                            feature.points,
                            stroke = 4f,
                            pointMode = PointMode.Polygon,
                            attributes = Attributes(ZAttribute, 10f),
                            id = "selected",
                        ).color(Color.Magenta)
                    }
                }
            }
        }
    }
}


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
