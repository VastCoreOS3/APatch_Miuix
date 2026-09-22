package me.bmax.apatch.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import me.bmax.apatch.BuildConfig
import me.bmax.apatch.R
import me.bmax.apatch.ui.theme.blurEffect
import me.bmax.apatch.ui.theme.getAppBarColor
import me.bmax.apatch.ui.theme.rememberBlurBackdrop
import me.bmax.apatch.util.Version
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import androidx.compose.foundation.isSystemInDarkTheme

// ========= 调参：提高速度，原来0.12f太慢，可以调到0.22~0.35f，数值越大流动越快
private const val BACKGROUND_SPEED = 0.26f
private const val COLOR_INTERPOLATION_SECONDS = 12f

private val LightGradientPalettes = listOf(
    listOf(Color(1f, 0.90f, 0.94f), Color(1f, 0.84f, 0.89f), Color(0.97f, 0.73f, 0.82f), Color(0.64f, 0.65f, 0.98f)),
    listOf(Color(0.58f, 0.74f, 1f), Color(1f, 0.90f, 0.93f), Color(0.74f, 0.76f, 1f), Color(0.97f, 0.77f, 0.84f)),
    listOf(Color(0.98f, 0.86f, 0.90f), Color(0.60f, 0.73f, 0.98f), Color(0.92f, 0.93f, 1f), Color(0.56f, 0.69f, 1f)),
)
private val DarkGradientPalettes = listOf(
    listOf(Color(0.20f, 0.06f, 0.88f, 0.40f), Color(0.30f, 0.14f, 0.55f, 0.50f), Color(0f, 0.64f, 0.96f, 0.50f), Color(0.11f, 0.16f, 0.83f, 0.40f)),
    listOf(Color(0.07f, 0.15f, 0.79f, 0.50f), Color(0.62f, 0.21f, 0.67f, 0.50f), Color(0.06f, 0.25f, 0.84f, 0.50f), Color(0f, 0.20f, 0.78f, 0.50f)),
    listOf(Color(0.58f, 0.30f, 0.74f, 0.40f), Color(0.27f, 0.18f, 0.60f, 0.50f), Color(0.66f, 0.26f, 0.62f, 0.50f), Color(0.12f, 0.16f, 0.70f, 0.60f)),
)

@Composable
private fun rememberAboutAnimationTime(isResumed: Boolean): Float {
    var animationTime by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isResumed) {
        // 页面不可见直接 return，协程结束，不再跑帧，解决后台空跑
        if (!isResumed) return@LaunchedEffect
        var previousFrame = 0L
        while (true) {
            withFrameNanos { frameTime ->
                if (previousFrame != 0L) {
                    val deltaSeconds = (frameTime - previousFrame) / 1_000_000_000f
                    animationTime += deltaSeconds
                }
                previousFrame = frameTime
            }
        }
    }
    return animationTime
}

@Composable
private fun AnimatedAboutBackground(
    animationTime: Float,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        drawAboutGradientField(
            animationTime = animationTime,
            colors = colors,
            fieldSize = size,
            sampleOrigin = Offset.Zero,
        )
    }
}

private fun DrawScope.drawAboutGradientField(
    animationTime: Float,
    colors: List<Color>,
    fieldSize: Size,
    sampleOrigin: Offset,
    blendMode: BlendMode = BlendMode.SrcOver,
) {
    val strengthenedColors = colors.map(::strengthenGradientColor)
    val translucentPalette = strengthenedColors.any { it.alpha < 0.8f }
    // 适度缩小半径，减轻大半径径向渐变绘制开销
    val radius = fieldSize.maxDimension * 0.54f
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
        blendMode = blendMode,
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
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(
                        alpha = if (translucentPalette) {
                            color.alpha * 0.96f
                        } else {
                            0.88f
                        },
                    ),
                    color.copy(alpha = 0f),
                ),
                center = localCenter,
                radius = radius,
            ),
            center = localCenter,
            radius = radius,
            blendMode = blendMode,
        )
    }
}

private fun strengthenGradientColor(color: Color): Color {
    val average = (color.red + color.green + color.blue) / 3f
    val saturation = 1.18f
    val brightnessOffset = 0.015f
    return Color(
        red = (average + (color.red - average) * saturation - brightnessOffset).coerceIn(0f, 1f),
        green = (average + (color.green - average) * saturation - brightnessOffset).coerceIn(0f, 1f),
        blue = (average + (color.blue - average) * saturation - brightnessOffset).coerceIn(0f, 1f),
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

@Destination<RootGraph>
@Composable
fun AboutScreen(navigator: DestinationsNavigator) {

    val scrollBehavior = MiuixScrollBehavior()
    val uriHandler = LocalUriHandler.current
    val topBarBackdrop = rememberBlurBackdrop(true)
    val isDarkTheme = isSystemInDarkTheme()

    val lifecycleOwner = LocalLifecycleOwner.current
    var isPageResumed by remember { mutableStateOf(false) }

    LaunchedEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isPageResumed = event == Lifecycle.Event.ON_RESUME
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        // 销毁时移除 observer，防止内存泄漏
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val animTime = rememberAboutAnimationTime(isResumed = isPageResumed)
    val colors = animatedGradientColors(animTime, dark = isDarkTheme)

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.blurEffect(topBarBackdrop),
                title = stringResource(R.string.about),
                color = topBarBackdrop.getAppBarColor(),
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
                animationTime = animTime,
                colors = colors,
                modifier = Modifier.fillMaxSize()
            )

            LazyColumn(
                modifier = Modifier
                    .then(topBarBackdrop?.let { Modifier.layerBackdrop(it) } ?: Modifier)
                    .fillMaxSize()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = innerPadding,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
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
                }

                item {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MiuixTheme.textStyles.title2,
                        fontWeight = FontWeight(550)
                    )
                    Text(
                        text = stringResource(
                            id = R.string.about_app_version,
                            if (BuildConfig.VERSION_NAME.contains(BuildConfig.VERSION_CODE.toString())) "${BuildConfig.VERSION_CODE}" else "${BuildConfig.VERSION_CODE} (${BuildConfig.VERSION_NAME})"
                        ),
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                    Text(
                        text = stringResource(
                            id = R.string.about_powered_by,
                            "KernelPatch (${Version.buildKPVString()})"
                        ),
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
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
                        }
                    }
                }
            }
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
