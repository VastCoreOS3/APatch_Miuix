package me.bmax.apatch.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import me.bmax.apatch.APApplication
import me.bmax.apatch.BuildConfig
import me.bmax.apatch.R
import me.bmax.apatch.util.Version
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

private const val BACKGROUND_SPEED = 0.65f
private const val COLOR_INTERPOLATION_SECONDS = 3f
private const val MAX_ANIMATION_TIME = 999f

private const val HERO_HEIGHT_FRACTION = 0.55f
private const val HERO_CONTENT_OFFSET_DP = 24
private const val CONTENT_TOP_GAP_DP = 12
private const val LOGO_FADE_START_RATIO = 0.22f
private const val LOGO_FADE_DISTANCE_RATIO = 0.32f
private const val BACKGROUND_FADE_DP_VALUE = 360

private val LightGradientPalettes = listOf(
    listOf(Color(1f, 0.90f, 0.94f), Color(1f, 0.84f, 0.89f), Color(0.97f, 0.73f, 0.82f), Color(0.64f, 0.65f, 0.98f)),
    listOf(Color(0.58f, 0.74f, 1f), Color(1f, 0.90f, 0.93f), Color(0.74f, 0.76f, 1f), Color(0.97f, 0.77f, 0.84f)),
    listOf(Color(0.98f, 0.86f, 0.90f), Color(0.60f, 0.73f, 0.98f), Color(0.92f, 0.93f, 1f), Color(0.56f, 0.69f, 1f)),
)
private val DarkGradientPalettes = listOf(
    listOf(Color(0.31f, 0.18f, 0.24f, 0.45f), Color(0.34f, 0.22f, 0.28f, 0.45f), Color(0.38f, 0.20f, 0.30f, 0.48f), Color(0.22f, 0.24f, 0.48f, 0.50f)),
    listOf(Color(0.20f, 0.32f, 0.54f, 0.48f), Color(0.36f, 0.24f, 0.29f, 0.44f), Color(0.28f, 0.29f, 0.52f, 0.48f), Color(0.38f, 0.24f, 0.32f, 0.46f)),
    listOf(Color(0.36f, 0.26f, 0.30f, 0.44f), Color(0.21f, 0.31f, 0.52f, 0.48f), Color(0.34f, 0.35f, 0.50f, 0.42f), Color(0.19f, 0.28f, 0.50f, 0.48f)),
)

@Composable
private fun isInDarkTheme(mode: Int): Boolean {
    return when (mode) {
        1, 4 -> false
        2, 5 -> true
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }
}

@Composable
private fun AnimatedAboutBackground(
    isResumed: Boolean,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    var animationTime by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isResumed) {
        if (!isResumed) return@LaunchedEffect
        var previousFrameNanos = 0L
        while (true) {
            val frameTimeNanos = withFrameNanos { it }
            if (previousFrameNanos != 0L) {
                val deltaSeconds = (frameTimeNanos - previousFrameNanos) / 1_000_000_000f
                animationTime += deltaSeconds
                if (animationTime > MAX_ANIMATION_TIME) animationTime = 0f
            }
            previousFrameNanos = frameTimeNanos
        }
    }
    Canvas(modifier = modifier) {
        val currentColors = animatedGradientColors(animationTime, isDarkTheme)
        drawAboutGradientField(
            animationTime = animationTime,
            colors = currentColors,
            fieldSize = size,
            sampleOrigin = Offset.Zero,
            isDark = isDarkTheme
        )
    }
}

private fun DrawScope.drawAboutGradientField(
    animationTime: Float,
    colors: List<Color>,
    fieldSize: Size,
    sampleOrigin: Offset,
    isDark: Boolean,
) {
    val strengthenedColors = colors.map(::strengthenGradientColor)
    val translucentPalette = strengthenedColors.any { it.alpha < 0.8f }
    val baseRadius = fieldSize.maxDimension * 0.54f
    val motionTime = animationTime * BACKGROUND_SPEED
    drawRect(
        brush = Brush.linearGradient(
            colors = strengthenedColors.map { color ->
                color.copy(
                    alpha = if (translucentPalette) {
                        color.alpha * 0.72f
                    } else {
                        0.58f
                    },
                )
            },
            start = Offset(-sampleOrigin.x, -sampleOrigin.y),
            end = Offset(
                fieldSize.width - sampleOrigin.x,
                fieldSize.height - sampleOrigin.y,
            ),
        ),
        blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
    )
    val centers = listOf(
        Offset(
            x = fieldSize.width * (0.18f + 0.10f * sin(motionTime)),
            y = fieldSize.height * (0.20f + 0.08f * cos(motionTime * 0.8f)),
        ),
        Offset(
            x = fieldSize.width * (0.82f + 0.10f * cos(motionTime * 0.9f)),
            y = fieldSize.height * (0.78f + 0.10f * sin(motionTime * 0.7f)),
        ),
        Offset(
            x = fieldSize.width * (0.22f + 0.12f * cos(motionTime * 0.65f)),
            y = fieldSize.height * (0.80f + 0.08f * sin(motionTime * 0.85f)),
        ),
        Offset(
            x = fieldSize.width * (0.80f + 0.12f * sin(motionTime * 0.72f)),
            y = fieldSize.height * (0.20f + 0.08f * cos(motionTime * 0.62f)),
        ),
    )
    centers.forEachIndexed { index, globalCenter ->
        val safeIdx = index % strengthenedColors.size
        val color = strengthenedColors[safeIdx]
        val localCenter = globalCenter - sampleOrigin
        val radiusScale = 1f + 0.07f * sin(motionTime * (1.1f + index * 0.25f))
        val radius = baseRadius * radiusScale
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to color.copy(
                        alpha = if (translucentPalette) color.alpha * 0.96f else 0.88f
                    ),
                    0.55f to color.copy(alpha = 0.22f),
                    1f to color.copy(alpha = 0f),
                ),
                center = localCenter,
                radius = radius,
            ),
            center = localCenter,
            radius = radius,
            blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver
        )
    }
}

private fun strengthenGradientColor(color: Color): Color {
    val average = (color.red + color.green + color.blue) / 3f
    val saturation = 1.12f
    val brightnessOffset = 0.012f
    fun enhance(v: Float): Float {
        val res = average + (v - average) * saturation - brightnessOffset
        return res.coerceIn(0f, 1f)
    }
    return Color(
        red = enhance(color.red),
        green = enhance(color.green),
        blue = enhance(color.blue),
        alpha = color.alpha,
    )
}

private fun animatedGradientColors(
    animationTime: Float,
    dark: Boolean,
): List<Color> {
    val palettes = if (dark) DarkGradientPalettes else LightGradientPalettes
    val segmentValue = animationTime / COLOR_INTERPOLATION_SECONDS
    val segment = floor(segmentValue).toInt() % 4
    val rawProgress = segmentValue - floor(segmentValue)
    val progress = rawProgress * rawProgress * (3f - 2f * rawProgress)
    val start = when (segment) {
        0 -> palettes[1]
        1 -> palettes[0]
        2 -> palettes[1]
        else -> palettes[2]
    }
    val end = when (segment) {
        0 -> palettes[0]
        1 -> palettes[1]
        2 -> palettes[2]
        else -> palettes[1]
    }
    return start.indices.map { index -> lerp(start[index], end[index], progress) }
}

@Composable
private fun AboutHero(
    logoAlpha: Float,
    logoScale: Float,
    heroContentOffsetDp: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .offset(y = heroContentOffsetDp)
                .graphicsLayer {
                    alpha = logoAlpha
                    scaleX = logoScale
                    scaleY = logoScale
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(95.dp),
                color = colorResource(id = R.color.ic_launcher_background),
                shape = RoundedCornerShape(30.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "icon",
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "APatch",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight(550)
            )
            Text(
                text = "${BuildConfig.VERSION_CODE} (${BuildConfig.VERSION_NAME})",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                modifier = Modifier.padding(top = 5.dp)
            )
            Text(
                text = "Powered by KernelPatch (${Version.buildKPVString()})",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
    }
}

@Composable
private fun LinkItem(
    title: String,
    summary: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = title,
            style = MiuixTheme.textStyles.main
        )
        Text(
            text = summary,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Destination<RootGraph>
@Composable
fun AboutScreen(navigator: DestinationsNavigator) {
    val scrollBehavior = MiuixScrollBehavior()
    val uriHandler = LocalUriHandler.current
    val prefs = APApplication.sharedPreferences
    val colorMode = remember { prefs.getInt("color_mode", 0) }
    val isDarkTheme = isInDarkTheme(colorMode)
    val lifecycleOwner = LocalLifecycleOwner.current
    var isPageResumed by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val heroHeightDp = configuration.screenHeightDp.dp * HERO_HEIGHT_FRACTION
    val heroHeightPx = with(density) { heroHeightDp.toPx() }
    val bgFadeDistancePx = with(density) { BACKGROUND_FADE_DP_VALUE.dp.toPx() }

    val heroContentOffsetDp = HERO_CONTENT_OFFSET_DP.dp
    val contentTopGapDp = CONTENT_TOP_GAP_DP.dp

    val logoFadeStart = heroHeightPx * LOGO_FADE_START_RATIO
    val logoFadeDistance = heroHeightPx * LOGO_FADE_DISTANCE_RATIO

    val scrollOffset by remember(listState, heroHeightPx) {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) heroHeightPx
            else listState.firstVisibleItemScrollOffset.toFloat()
        }
    }

    val logoProgress by remember(listState, scrollOffset) {
        derivedStateOf {
            ((scrollOffset - logoFadeStart) / logoFadeDistance).coerceIn(0f,1f)
        }
    }
    val logoAlpha = 1f - logoProgress
    val logoScale = 1f - logoProgress * 0.10f
    val backgroundAlpha by remember(scrollOffset) {
        derivedStateOf {
            1f - (scrollOffset / bgFadeDistancePx).coerceIn(0f, 1f)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isPageResumed = event == Lifecycle.Event.ON_RESUME
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier,
                color = androidx.compose.ui.graphics.Color.Transparent,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = { navigator.popBackStack() }) {
                        Icon(imageVector = MiuixIcons.Back, contentDescription = null)
                    }
                },
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedAboutBackground(
                isResumed = isPageResumed,
                isDarkTheme = isDarkTheme,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(backgroundAlpha)
                    .graphicsLayer {
                        translationY = -listState.firstVisibleItemScrollOffset * 0.12f
                    }
            )

            AboutHero(
                logoAlpha = logoAlpha,
                logoScale = logoScale,
                heroContentOffsetDp = heroContentOffsetDp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .height(heroHeightDp)
                    .padding(horizontal = 12.dp)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = innerPadding,
            ) {
                item {
                    Spacer(modifier = Modifier.height(heroHeightDp + contentTopGapDp))
                }
                item {
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        LinkItem(
                            title = "GitHub",
                            summary = "Project Repository",
                            onClick = { uriHandler.openUri("https://github.com/bmax121/APatch") }
                        )
                        LinkItem(
                            title = "Telegram Channel",
                            summary = "Official update channel",
                            onClick = { uriHandler.openUri("https://t.me/APatchChannel") }
                        )
                        LinkItem(
                            title = "Weblate",
                            summary = "Help translate APatch",
                            onClick = { uriHandler.openUri("https://hosted.weblate.org/engage/APatch") }
                        )
                        LinkItem(
                            title = "Telegram Group",
                            summary = "Discussion group",
                            onClick = { uriHandler.openUri("https://t.me/apatch_discuss") }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                item {
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = "APatch is a powerful Android root solution.",
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                            )
                        }
                    }
                }
            }
        }
    }
}
