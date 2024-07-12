package cz.okycelt.compose.androidviewfocus

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import cz.okycelt.compose.androidviewfocus.ui.theme.AndroidViewFocusTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidViewFocusTheme {
                Screen(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun Screen(
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        val menuFocusRequester = remember { FocusRequester() }
        var hasMenuFocus by remember { mutableStateOf(false) }

        Menu(
            modifier = Modifier
                .fillMaxHeight()
                .width(64.dp)
                .focusRequester(menuFocusRequester)
                .onFocusChanged { hasMenuFocus = it.hasFocus }
        )

        Content(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        )

        BackHandler(
            enabled = !hasMenuFocus
        ) {
            menuFocusRequester.requestFocus()
        }
    }
}

@Composable
fun Menu(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        repeat(4) {
            FocusableBox()
        }
    }
}

@Composable
fun Content(
    modifier: Modifier = Modifier,
    contentType: ContentType = ContentType.AndroidView
) {
    when (contentType) {
        ContentType.AndroidView -> AndroidViewContent(modifier = modifier)
        ContentType.Compose -> ComposeContent(modifier = modifier)
    }
}

@Composable
fun AndroidViewContent(
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL

                //

                val focusedColor = Color.Blue
                val unfocusedColor = focusedColor.copy(alpha = 0.2f)

                val focusChangeListener = OnFocusChangeListener { v, hasFocus ->
                    val color = if (hasFocus) focusedColor else unfocusedColor
                    v.setBackgroundColor(color.toArgb())
                }

                repeat(10) {
                    val view = View(context)
                    view.isFocusable = true
                    view.isFocusableInTouchMode = true
                    view.setBackgroundColor(unfocusedColor.toArgb())
                    view.onFocusChangeListener = focusChangeListener

                    val layoutParams = LinearLayout.LayoutParams(
                        /* width = */ LinearLayout.LayoutParams.MATCH_PARENT,
                        /* height = */ dpToPx(32, context)
                    )
                    layoutParams.bottomMargin = dpToPx(8, context)
                    view.layoutParams = layoutParams

                    addView(view)
                }
            }
        }
    )
}

@Composable
fun ComposeContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(10) {
            FocusableBox(
                color = Color.Green,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun FocusableBox(
    modifier: Modifier = Modifier,
    color: Color = Color.Red
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 32.dp, minWidth = 32.dp)
            .drawBehind { drawRect(if (isFocused) color else color.copy(alpha = 0.2f)) }
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
    )
}

enum class ContentType {
    AndroidView, Compose
}

fun dpToPx(dp: Int, context: Context): Int {
    return (dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}