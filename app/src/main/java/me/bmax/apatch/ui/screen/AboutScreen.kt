package me.bmax.apatch.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import me.bmax.apatch.ui.theme.getAppBarColor
import me.bmax.apatch.ui.theme.rememberBlurBackdrop
import me.bmax.apatch.util.Version
import top.yukonga.miuix.kmp.basic.ArrowPreference
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

private const val BACKGROUND_SPEED = 0.65f
private const val COLOR_INTERPOLATION_SECONDS = 3f
private const val MAX_ANIMATION_TIME = 999f

// ==========【新增：从 HyperIsland 复制Hero相关常量】==========
private const val HERO_HEIGHT_FRACTION = 0.60f
private val DEVELOPER_TOP_GAP = 16.dp
private val HERO_CONTENT_OFFSET = 30.dp

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

/**
 * color_mode: 0=自动跟随系统，1=浅色，2=深色
 */
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
    animationTime: Float,
    modifier: Modifier = Modifier,
) {
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
        // 光斑大小轻微扰动，模拟呼吸效果
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

/**
 * 优化版颜色增强，避免硬截断，饱和度柔和提升
 */
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
    // ease‑in‑out cubic
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

// =====================【新增：Hero相关全部组件】=====================
@Composable
private fun rememberAboutAnimationTime(running: Boolean): Float {
    var animationTime by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        var previousFrame = 0L
        while (true) {
            withFrameNanos { frameTime ->
                if (previousFrame != 0L) {
                    val deltaSeconds = (frameTime - previousFrame) / 1_000_000_000f
                    animationTime += deltaSeconds
                    if (animationTime > MAX_ANIMATION_TIME) animationTime = 0f
                }
                previousFrame = frameTime
            }
        }
    }
    return animationTime
}

private fun aboutArtworkBlendColors(darkMode: Boolean): List<BlendColorEntry> =
    if (darkMode) {
        listOf(
            BlendColorEntry(Color(0xE6A1A1A1), BlurBlendMode.ColorDodge),
            BlendColorEntry(Color(0x4DE6E6E6), BlurBlendMode.LinearLight),
            BlendColorEntry(Color(0xFF1AF500), BlurBlendMode.Lab),
        )
    } else {
        listOf(
            BlendColorEntry(Color(0xCC4A4A4A), BlurBlendMode.ColorBurn),
            BlendColorEntry(Color(0xFF4F4F4F), BlurBlendMode.LinearLight),
            BlendColorEntry(Color(0xFF1AF200), BlurBlendMode.Lab),
        )
    }

private fun Modifier.colorfulMask(brush: Brush): Modifier = graphicsLayer {
    compositingStrategy = CompositingStrategy.Offscreen
}.drawWithCache {
    onDrawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.SrcIn)
    }
}

private fun animatedGradientBrush(animationTime: Float, colors: List<Color>): Brush {
    val center = Offset(310f, 90f)
    val vector = Offset(
        x = cos(animationTime * BACKGROUND_SPEED) * 620f,
        y = sin(animationTime * BACKGROUND_SPEED) * 180f,
    )
    val opaqueColors = colors.map { it.copy(alpha = 1f) }
    return Brush.linearGradient(
        colors = opaqueColors + opaqueColors.first(),
        start = center - vector,
        end = center + vector,
    )
}

@Composable
private fun BackgroundBlendedArtwork(
    resourceId: Int,
    animationTime: Float,
    colors: List<Color>,
    backdrop: LayerBackdrop?,
    darkMode: Boolean,
    blurRadius: Float,
    shape: Shape,
    modifier: Modifier = Modifier,
    contentScale: androidx.compose.ui.layout.ContentScale = androidx.compose.ui.layout.ContentScale.Fit,
) {
    val blendColors = remember(darkMode) { aboutArtworkBlendColors(darkMode) }
    val fallbackBrush = animatedGradientBrush(animationTime, colors)
    val effectModifier = if (backdrop != null) {
        Modifier.textureBlur(
            backdrop = backdrop,
            shape = shape,
            blurRadius = blurRadius,
            noiseCoefficient = 0f,
            colors = BlurColors(blendColors = blendColors),
            contentBlendMode = BlendMode.DstIn,
        )
    } else {
        Modifier.colorfulMask(fallbackBrush)
    }
    Image(
        painter = painterResource(resourceId),
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier.then(effectModifier),
    )
}

@Composable
private fun AboutAppIcon(
    animationTime: Float,
    colors: List<Color>,
    backdrop: LayerBackdrop?,
    darkMode: Boolean,
    modifier: Modifier = Modifier,
) {
    BackgroundBlendedArtwork(
        resourceId = R.drawable.about_logo_mark, // 替换为你的图标资源
        animationTime = animationTime,
        colors = colors,
        backdrop = backdrop,
        darkMode = darkMode,
        blurRadius = 200f,
        shape = androidx.compose.ui.graphics.RectangleShape,
        modifier = modifier.size(88.dp),
    )
}

@Composable
private fun AboutHero(
    animationTime: Float,
    gradientColors: List<Color>,
    backdrop: LayerBackdrop?,
    darkMode: Boolean,
    logoAlpha: Float,
    logoScale: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier
                .offset(y = HERO_CONTENT_OFFSET)
                .graphicsLayer {
                    alpha = logoAlpha
                    scaleX = logoScale
                    scaleY = logoScale
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AboutAppIcon(
                animationTime = animationTime,
                colors = gradientColors,
                backdrop = backdrop,
                darkMode = darkMode,
            )
            Spacer(Modifier.height(20.dp))
            BackgroundBlendedArtwork(
                resourceId = R.drawable.about_wordmark, // 项目文字logo
                animationTime = animationTime,
                colors = gradientColors,
                backdrop = backdrop,
                darkMode = darkMode,
                blurRadius = 150f,
                shape = RoundedCornerShape(12.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
                modifier = Modifier
                    .width(280.dp)
                    .height(40.dp),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "${BuildConfig.VERSION_CODE} (${BuildConfig.VERSION_NAME})",
                fontSize = 15.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

// =====================【修改主入口 AboutScreen】=====================
@Destination<RootGraph>
@Composable
fun AboutScreen(navigator: DestinationsNavigator) {
    val scrollBehavior = MiuixScrollBehavior()
    val uriHandler = LocalUriHandler.current
    val topBarBackdrop = rememberBlurBackdrop(true)
    val prefs = APApplication.sharedPreferences
    val colorMode = remember { prefs.getInt("color_mode", 0) }
    val isDarkTheme = isInDarkTheme(colorMode)
    val lifecycleOwner = LocalLifecycleOwner.current
    var isPageResumed by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isPageResumed = event == Lifecycle.Event.ON_RESUME
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // --------新增 Hero滚动状态、动画时间、屏幕尺寸--------
    val listState = rememberLazyListState()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current
    val heroHeight = screenHeight * HERO_HEIGHT_FRACTION
    val heroHeightPx = with(density) { heroHeight.toPx() }
    val backgroundFadeDistance = with(density) { 389.dp.toPx() }
    val logoFadeStart = heroHeightPx * 0.25f
    val logoFadeDistance = heroHeightPx * 0.35f

    val scrollOffset by remember(listState, heroHeightPx) {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                heroHeightPx
            } else {
                listState.firstVisibleItemScrollOffset.toFloat()
            }
        }
    }
    val reachedListEnd by remember(listState) {
        derivedStateOf {
            !listState.canScrollForward &&
                    (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0)
        }
    }
    val backgroundAlpha = if (reachedListEnd) {
        0f
    } else {
        1f - (scrollOffset / backgroundFadeDistance).coerceIn(0f, 1f)
    }
    val logoProgress = if (reachedListEnd) {
        1f
    } else {
        ((scrollOffset - logoFadeStart) / logoFadeDistance).coerceIn(0f, 1f)
    }
    val logoAlpha = 1f - logoProgress
    val logoScale = 1f - logoProgress * 0.1f

    val animationTime = rememberAboutAnimationTime(isPageResumed)
    val gradientColors = animatedGradientColors(animationTime, isDarkTheme)

    val logoBackdrop = if (isRuntimeShaderSupported()) {
        rememberLayerBackdrop {
            drawRect(MiuixTheme.colorScheme.background)
            drawContent()
        }
    } else {
        null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier,
                title = stringResource(R.string.about),
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
            // 修改背景：增加视差translationY、alpha、layerBackdrop
            AnimatedAboutBackground(
                isResumed = isPageResumed,
                animationTime = animationTime,
                isDarkTheme = isDarkTheme,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight + 180.dp)
                    .alpha(backgroundAlpha)
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                        translationY = -listState.firstVisibleItemScrollOffset * 0.12f
                    }
                    .then(
                        if (logoBackdrop != null) Modifier.layerBackdrop(logoBackdrop) else Modifier
                    )
            )

            LazyColumn(
                state = listState, // 绑定listState！
                modifier = Modifier
                    .then(topBarBackdrop?.let { Modifier.layerBackdrop(it) } ?: Modifier)
                    .fillMaxSize()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    start = 0.dp,
                    top = innerPadding.calculateTopPadding(),
                    end = 0.dp,
                    bottom = innerPadding.calculateBottomPadding()
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 【关键】空出Hero悬浮层高度，原来的顶部图标文字全部移除！
                item {
                    Spacer(modifier = Modifier.height(heroHeight + DEVELOPER_TOP_GAP))
                }
                item {
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        LinkItem(
                            title = stringResource(R.string.about_github),
                            summary = stringResource(R.string.about_github_summary),
                            icon = painterResource(R.drawable.github)
                        ) {
                            uriHandler.openUri("https://github.com/bmax121/APatch")
                        }
                        LinkItem(
                            title = stringResource(R.string.about_telegram_channel),
                            summary = stringResource(R.string.about_telegram_channel_summary),
                            icon = painterResource(R.drawable.channel)
                        ) {
                            uriHandler.openUri("https://t.me/APatchChannel")
                        }
                        LinkItem(
                            title = stringResource(R.string.about_weblate),
                            summary = stringResource(R.string.about_weblate_summary),
                            icon = painterResource(R.drawable.weblate)
                        ) {
                            uriHandler.openUri("https://hosted.weblate.org/engage/APatch")
                        }
                        LinkItem(
                            title = stringResource(R.string.about_telegram_group),
                            summary = stringResource(R.string.about_telegram_group_summary),
                            icon = painterResource(R.drawable.telegram)
                        ) {
                            uriHandler.openUri("https://t.me/apatch_discuss")
                        }
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
                                text = stringResource(id = R.string.about_app_desc),
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                            )
                            Spacer(modifier.height(8.dp))
                            Text(
                                text = stringResource(
                                    id = R.string.about_powered_by,
                                    "KernelPatch (${Version.buildKPVString()})"
                                ),
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                            )
                        }
                    }
                }
            }

            // =========悬浮在Box顶层：Hero头部=========
            AboutHero(
                animationTime = animationTime,
                gradientColors = gradientColors,
                backdrop = logoBackdrop,
                darkMode = isDarkTheme,
                logoAlpha = logoAlpha,
                logoScale = logoScale,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = innerPadding.calculateTopPadding())
                    .height(heroHeight)
                    .padding(horizontal =16.dp)
            )
        }
    }
}

@Composable
fun LinkItem(
    title: String,
    summary: String,
    icon: Painter,
    onClick: () -> Unit
) {
    ArrowPreference(
        title = title,
        summary = summary,
        onClick = onClick,
        startAction = {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    )
}
