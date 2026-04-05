package com.myguide.views


import android.R.attr.type
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.TextButton
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myguide.R
import com.myguide.UI.BUTTON
import com.myguide.colorScheme
import com.myguide.current
import com.myguide.data.Query
import com.myguide.data.Query.ITEM
import com.myguide.data.Query.SHOPS
import com.myguide.data.VM
import com.myguide.data.VM.Type
import com.myguide.screen
import com.myguide.sortable
import com.myguide.toDp
import com.myguide.toolbar
import com.myguide.typography

private var experimental = MutableLiveData(false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Toolbar(modifier: Modifier) {
    val ident by current.observeAsState()
    if (ident == null) return
    val vm = screen[ident!!]!!.vm
    val display by vm.type.observeAsState()
    val exp by vm.exp.observeAsState()
    val filter by vm.filter.observeAsState()
    val fps by vm.fps.observeAsState(VM.FPS.FPS30)
    val margin by vm.margin.collectAsStateWithLifecycle()
    val mode = remember { mutableStateOf<Boolean?>(null) }
    val ratio by vm.ratio.observeAsState()
    val ratioH by vm.ratioH.observeAsState()
    val ratioV by vm.ratioV.observeAsState()
    val scale by vm.scale.observeAsState()
    val sort by vm.sort.observeAsState()
    var visible by remember { mutableStateOf(true) }
    var showExpDialog by remember { mutableStateOf(false) }
    if (showExpDialog) {
        AlertDialog(
            onDismissRequest = { showExpDialog = false },
            confirmButton = {
                TextButton(onClick = { showExpDialog = false }) {
                    Text("Close")
                }
            },
            text = { Exp() }
        )
    }
    @Composable
    fun Slider1(modifier: Modifier) {
        SliderRow(
            action = {
                vm.adjust.value = true
                vm.scale.value = 1f
            },
            icon = R.drawable._text,
            minus = {
                vm.adjust.value = true
                vm.scale.value = vm.scale.value!! - .01f
            },
            plus = {
                vm.scale.value = vm.scale.value!! + .01f
                vm.adjust.postValue(true)
            },
            sliderValue = scale!!,
            sliderChange = {
                vm.adjust.value = false
                vm.scale.value = it
            },
            sliderFinished = { vm.adjust.value = true },
            txt = "%.2f".format(vm.scale.value!!),
            valueRange = 0.85f..2f,
            modifier = modifier
        )
    }

    @Composable
    fun Slider2(modifier: Modifier) {
        SliderRow(
            action = {
                if (mode.value == null) {
                    vm.ratioH.value = vm.ratio.value
                    vm.ratioV.value = vm.ratio.value
                }
                mode.value =
                    when (mode.value) {
                        false -> true
                        true -> null
                        null -> false
                    }
                if (mode.value == null) {
                    vm.ratio.value = 1f
                    vm.ratioH.value = null
                    vm.ratioV.value = null
                    vm.adjust.value = true
                }
            },
            icon = when (mode.value) {
                false -> R.drawable._horizontal
                true -> R.drawable._vertical
                null -> R.drawable._hv
            },
            minus = {
                when (mode.value) {
                    false -> vm.ratioH.value = vm.ratioH.value!! - .01f
                    true -> vm.ratioV.value = vm.ratioH.value!! - .01f
                    null -> vm.ratio.value = vm.ratio.value!! - .01f
                }
                vm.adjust.value = true
            },
            plus = {
                when (mode.value) {
                    false -> vm.ratioH.value = vm.ratioH.value!! + .01f
                    true -> vm.ratioV.value = vm.ratioH.value!! + .01f
                    null -> vm.ratio.value = vm.ratio.value!! + .01f
                }
                vm.adjust.value = true
            },
            sliderValue =
                when (mode.value) {
                    false -> ratioH ?: ratio!!
                    true -> ratioV ?: ratio!!
                    else -> ratio!!
                },
            sliderChange = {
                vm.adjust.value = false
                when (mode.value) {
                    false -> vm.ratioH.value = it
                    true -> vm.ratioV.value = it
                    null -> vm.ratio.value = it
                }
            },
            sliderFinished = { vm.adjust.value = true },
            txt =
                "%.2f".format(
                    when (mode.value) {
                        false -> vm.ratioH.value ?: 1f
                        true -> vm.ratioV.value ?: 1f
                        null -> vm.ratio.value!!
                    }
                ),
            valueRange = 0.75f..1.5f,
            modifier = modifier
        )
    }

    @Composable
    fun Slider3(modifier: Modifier) {
        SliderRow(
            action = {
                vm.adjust.value = true
                vm.margin(1f)
            },
            icon = R.drawable._margin,
            minus = {
                vm.adjust.value = true
                vm.margin(false)
            },
            plus = {
                vm.margin(true)
                vm.adjust.postValue(true)
            },
            sliderValue = margin,
            sliderChange = {
                vm.adjust.value = false
                vm.margin(it)
            },
            sliderFinished = { vm.adjust.value = true },
            txt = "%.2f".format(margin),
            valueRange = 0.5f..2.5f,
            modifier = modifier
        )
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colorScheme.background)
            .padding(horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable._select),
                contentDescription = "screen switch",
                colorFilter = ColorFilter.tint(colorScheme.secondary),
                modifier = Modifier
                    .size(BUTTON.toDp())
                    .padding(10.dp)
            )
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (current.value!!) "B" else "A",
                    color = colorScheme.background,
                    style =
                        typography.labelLarge.copy(
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            ),
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        ),
                    fontWeight = FontWeight.W900,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(
                    when (filter) {
                        false -> R.drawable._east
                        true -> R.drawable._west
                        null -> R.drawable._filter
                    }
                ),
                contentDescription = "filter",
                colorFilter =
                    ColorFilter.tint(
                        if (sortable.value!!) colorScheme.primary
                        else colorScheme.secondary
                    ),
                modifier = Modifier
                    .clickable(
                        enabled = sortable.value!!,
                        onClick = {
                            screen[current.value!!]!!.vm.filter.value =
                                when (screen[current.value!!]!!.vm.filter.value) {
                                    false -> true
                                    true -> null
                                    null -> false
                                }
                        }
                    )
                    .size(BUTTON.toDp())
                    .padding(8.dp)
            )
            Image(
                painter = painterResource(R.drawable._sort),
                contentDescription = "sort",
                colorFilter =
                    ColorFilter.tint(
                        if (sortable.value!!) colorScheme.primary
                        else colorScheme.secondary
                    ),
                modifier = Modifier
                    .scale(scaleX = 1f, scaleY = if (sort == true) -1f else 1f)
                    .clickable(
                        enabled = sortable.value!!,
                        onClick = { vm.sort.value = !vm.sort.value!! }
                    )
                    .size(BUTTON.toDp())
                    .padding(8.dp)
            )
            Image(
                painter = painterResource(display!!.drawable),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorScheme.primary),
                modifier = Modifier
                    .clickable(
                        enabled = !exp!!,
                        onClick = {
                            screen[current.value!!]!!.display(
                                if (toolbar.items.last().query == SHOPS || toolbar.items.last().query == Query.ITEM)
                                    display!!.nextShop
                                else
                                    display!!.nextItem
                            )
                        }
                    )
                    .size(BUTTON.toDp())
                    .padding(8.dp)
            )
            Image(
                painter = painterResource(fps.drawable),
                contentDescription = "fps selector",
                colorFilter = ColorFilter.tint(colorScheme.primary),
                modifier = Modifier
                    .clickable(onClick = { vm.fps.value = fps.next })
                    .size(BUTTON.toDp())
                    .padding(8.dp)
            )
            Image(
                painter = painterResource(R.drawable._exp),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    if (exp!!) colorScheme.background
                    else colorScheme.primary
                ),
                modifier = Modifier
                    .background(
                        if (exp!!) colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable(
                        onClick = {
                            vm.exp.value = !vm.exp.value!!
                            screen[current.value!!]!!.display(Type.V)
                            if (vm.exp.value!!) showExpDialog = true
                        }
                    )
                    .size(BUTTON.toDp())
                    .padding(8.dp)
            )
            Image(
                painter = painterResource(R.drawable._back),
                contentDescription = "",
                colorFilter = ColorFilter.tint(colorScheme.secondary),
                modifier = Modifier
                    .clickable(
                        onClick = { visible = !visible }
                    )
                    .rotate(if (visible) -90f else 90f)
                    .size(
                        BUTTON.toDp(),
                        BUTTON.toDp()
                    )
                    .padding(6.dp)
            )
        }
        AnimatedVisibility(
            visible = visible,
            enter = EnterTransition.None,
            exit = ExitTransition.None
        ) {
            when (LocalConfiguration.current.orientation) {
                Configuration.ORIENTATION_LANDSCAPE ->
                    Row {
                        Slider1(Modifier.weight(1f))
                        Slider2(Modifier.weight(1f))
                        Slider3(Modifier.weight(1f))
                    }

                else ->
                    Column {
                        Slider1(Modifier)
                        Slider2(Modifier)
                        Slider3(Modifier)
                    }
            }

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderRow(
    action: (() -> Unit)? = null,
    icon: Int,
    minus: () -> Unit,
    plus: () -> Unit,
    sliderValue: Float,
    sliderChange: (Float) -> Unit,
    sliderFinished: () -> Unit,
    txt: String,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(icon),
            null,
            colorFilter = ColorFilter.tint(colorScheme.primary),
            modifier = Modifier
                .size(BUTTON.toDp())
                .padding(8.dp)
                .clickable(onClick = action ?: {})
        )
        Text(
            txt,
            style = typography.labelSmall,
            fontSize = 12.sp,
            modifier = Modifier.width(32.dp)
        )
        Image(
            painter = painterResource(R.drawable._remove),
            null,
            colorFilter = ColorFilter.tint(colorScheme.primary),
            modifier = Modifier
                .size(BUTTON.toDp())
                .padding(6.dp)
                .clickable(
                    onClick = minus
                )
        )
        Slider(
            value = sliderValue,
            onValueChange = sliderChange,
            onValueChangeFinished = sliderFinished,
            valueRange = valueRange,
            modifier = Modifier
                .weight(1f)
                .height(20.dp),
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    thumbTrackGapSize = 0.dp,
                    colors = SliderDefaults.colors(
                        thumbColor = colorScheme.primary,
                        activeTrackColor = colorScheme.secondaryContainer,
                        inactiveTrackColor = colorScheme.secondaryContainer,
                    )
                )
            }
        )
        Image(
            painter = painterResource(R.drawable._add),
            "increase font scale",
            colorFilter = ColorFilter.tint(colorScheme.primary),
            modifier = Modifier
                .size(BUTTON.toDp())
                .padding(6.dp)
                .clickable(onClick = plus)
        )
    }
}

@Composable
fun Exp() {
    val uriHandler = LocalUriHandler.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Experimental Mode",
            fontSize = typography.bodyLarge.fontSize,
            lineHeight = typography.bodyLarge.fontSize,
            color = colorScheme.secondary,
            style = typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            "This utility aims to deliver view rendering functionality without reliance on a complex UI engine. " +
            "\n\nInstead it draws the document tree by applying arithmetic to a view template transpiled from " +
            "normal UI and will find use in low power devices without a traditional OS.\n" +
            "\n" +
            "It is under development.\n" +
            "\n" +
            "Browser prototype:",
            fontSize = typography.bodyMedium.fontSize,
            lineHeight = typography.bodyMedium.fontSize,
            color = colorScheme.secondary,
            style = typography.bodyLarge,
            modifier = Modifier.padding(8.dp).fillMaxWidth()
        )
        val browserUrl = "https://sheinin.github.io/sheinin/"
        val linkText = buildAnnotatedString {
            pushStringAnnotation(tag = "URL", annotation = browserUrl)
            withStyle(
                style = SpanStyle(
                    color = Color(0xFF1E88E5),
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(browserUrl)
            }
            pop()
        }
        ClickableText(
            text = linkText,
            style = typography.bodyMedium.copy(color = Color(0xFF1E88E5)),
            modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth(),
            onClick = { offset ->
                linkText.getStringAnnotations("URL", offset, offset)
                    .firstOrNull()
                    ?.let { uriHandler.openUri(it.item) }
            }
        )
        Text(
            "Source code:",
            fontSize = typography.bodyMedium.fontSize,
            lineHeight = typography.bodyMedium.fontSize,
            color = colorScheme.secondary,
            style = typography.bodyLarge,
            modifier = Modifier.padding(8.dp).fillMaxWidth()
        )
        val browserUrl1 = "https://github.com/sheinin/myguide"
        val linkText1 = buildAnnotatedString {
            pushStringAnnotation(tag = "URL", annotation = browserUrl1)
            withStyle(
                style = SpanStyle(
                    color = Color(0xFF1E88E5),
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(browserUrl1)
            }
            pop()
        }
        ClickableText(
            text = linkText1,
            style = typography.bodyMedium.copy(color = Color(0xFF1E88E5)),
            modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth(),
            onClick = { offset ->
                linkText1.getStringAnnotations("URL", offset, offset)
                    .firstOrNull()
                    ?.let { uriHandler.openUri(it.item) }
            }
        )
    }
}