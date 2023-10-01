import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import center.sciprog.attributes.Attributes
import center.sciprog.maps.compose.*
import center.sciprog.maps.coordinates.GeodeticMapCoordinates
import center.sciprog.maps.coordinates.Gmc
import center.sciprog.maps.coordinates.kilometers
import center.sciprog.maps.features.*
import io.ktor.client.HttpClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.renderComposable
import space.kscience.kmath.geometry.Angle
import space.kscience.kmath.geometry.degrees
import space.kscience.kmath.geometry.radians
import kotlin.math.PI
import kotlin.random.Random

public fun GeodeticMapCoordinates.toShortString(): String =
    "${(latitude.degrees).toString().take(6)}:${(longitude.degrees).toString().take(6)}"


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun App() {

    val scope = rememberCoroutineScope()

    val mapTileProvider = remember {
        OpenStreetMapTileProvider(
            client = HttpClient(),
        )
    }

    val centerCoordinates = MutableStateFlow<Gmc?>(null)

    val pointOne = 55.568548 to 37.568604
    val pointTwo = 55.929444 to 37.518434
//        val pointThree = 60.929444 to 37.518434

    MapView(
        mapTileProvider = mapTileProvider,
        config = ViewConfig(
            onViewChange = { centerCoordinates.value = focus },
            onClick = { _, viewPoint ->
                println(viewPoint)
            }
        )
    ) {

//        icon(pointOne, Icons.Filled.Home)

        val marker1 = rectangle(55.744 to 38.614, size = DpSize(10.dp, 10.dp))
            .color(Color.Magenta)
        val marker2 = rectangle(55.8 to 38.5, size = DpSize(10.dp, 10.dp))
            .color(Color.Magenta)
        val marker3 = rectangle(56.0 to 38.5, size = DpSize(10.dp, 10.dp))
            .color(Color.Magenta)

        draggableLine(marker1, marker2, id = "line 1").color(Color.Red).onClick {
            println("line 1 clicked")
        }
        draggableLine(marker2, marker3, id = "line 2").color(Color.DarkGray).onClick {
            println("line 2 clicked")
        }
        draggableLine(marker3, marker1, id = "line 3").color(Color.Blue).onClick {
            println("line 3 clicked")
        }


        multiLine(
            points = listOf(
                55.742465 to 37.615812,
                55.742713 to 37.616370,
                55.742815 to 37.616659,
                55.742320 to 37.617132,
                55.742086 to 37.616566,
                55.741715 to 37.616716
            ),
        )

        //remember feature ref
        val circleId = circle(
            centerCoordinates = pointTwo,
        )
        scope.launch {
            while (isActive) {
                delay(200)
                circleId.color(Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat()))
            }
        }

        arc(pointOne, 10.0.kilometers, (PI / 4).radians, -Angle.pi / 2)


        line(pointOne, pointTwo, id = "line")
        text(pointOne, "Home", font = { size = 32f })


        pixelMap(
            space.Rectangle(
                Gmc(latitude = 55.58461879539754.degrees, longitude = 37.8746197303493.degrees),
                Gmc(latitude = 55.442792937592415.degrees, longitude = 38.132240805463844.degrees)
            ),
            0.005.degrees,
            0.005.degrees
        ) { gmc ->
            Color(
                red = ((gmc.latitude + Angle.piDiv2).degrees * 10 % 1f).toFloat(),
                green = ((gmc.longitude + Angle.pi).degrees * 10 % 1f).toFloat(),
                blue = 0f,
                alpha = 0.3f
            )
        }

        centerCoordinates.filterNotNull().onEach {
            group(id = "center") {
                circle(center = it, id = "circle", size = 1.dp).color(Color.Blue)
                text(position = it, it.toShortString(), id = "text").color(Color.Blue)
            }
        }.launchIn(scope)

        //Add click listeners for all polygons
        forEachWithType<Gmc, PolygonFeature<Gmc>> { ref ->
            ref.onClick(PointerMatcher.Primary) {
                println("Click on ${ref.id}")
                //draw in top-level scope
                with(this@MapView) {
                    multiLine(
                        ref.resolve().points,
                        attributes = Attributes(ZAttribute, 10f),
                        id = "selected",
                    ).modifyAttribute(StrokeAttribute, 4f).color(Color.Magenta)
                }
            }
        }
    }

}


fun main() {
    renderComposable(rootElementId = "root") {
        CompositionLocalProvider(
            LocalDensity provides Density(1.0f),
            LocalLayoutDirection provides LayoutDirection.Ltr,
//            LocalViewConfiguration provides DefaultViewConfiguration(Density(1.0f)),
//            LocalInputModeManager provides InputModeManagerObject,
            LocalFontFamilyResolver provides createFontFamilyResolver()
        ) {
            App()
        }
    }
}
