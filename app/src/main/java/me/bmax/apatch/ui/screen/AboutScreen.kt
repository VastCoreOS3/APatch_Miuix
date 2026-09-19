package me.bmax.apatch.ui.screen

import android.graphics.RuntimeShader
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import me.bmax.apatch.BuildConfig
import me.bmax.apatch.R
import me.bmax.apatch.ui.theme.getAppBarColor
import me.bmax.apatch.ui.theme.blurEffect
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
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

// AGSL 液态流动渐变 Shader，HyperOS风格
@Language("AGSL")
private val liquidGradientAgsl = """
uniform float2 resolution;
uniform float time;
uniform float4 colorA;
uniform float4 colorB;
uniform float4 colorC;

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / resolution;
    float t = time * 0.22;

    float wave1 = sin(uv.x * 2.2 + uv.y * 1.4 + t);
    float wave2 = sin(uv.x * 1.6 - uv.y * 2.1 + t * 0.73);
    float mixFactor = (wave1 + wave2) * 0.25 + 0.5;

    half4 c1 = colorA;
    half4 c2 = colorB;
    half4 c3 = colorC;

    half4 res = mix(mix(c1,c2, clamp(mixFactor,0.0,1.0)), c3, clamp(abs(mixFactor-0.5)*1.8,0.0,1.0));
    return res;
}
"""

@Destination<RootGraph>
@Composable
fun AboutScreen(navigator: DestinationsNavigator) {
    val scrollBehavior = MiuixScrollBehavior()
    val uriHandler = LocalUriHandler.current
    val lazyListState = rememberLazyListState()

    // 顶部栏捕获backdrop（原有逻辑不变）
    val topBarBackdrop = rememberBlurBackdrop(true)
    // 内容背景捕获backdrop：用于动态背景 → 卡片textureBlur玻璃效果
    val contentBackdrop = rememberBlurBackdrop(true)

    // 计算滚动进度：0=顶部，1=已经滚动
    val scrollProgress by remember {
        derivedStateOf {
            val info = lazyListState.layoutInfo
            if(info.totalItemsCount ==0) return@derivedStateOf 0f
            val first = info.visibleItemsInfo.firstOrNull()
            when {
                lazyListState.firstVisibleItemIndex >0 -> 1f
                first!=null -> (first.offset.toFloat() / first.size).coerceIn(0f,1f)
                else -> 0f
            }
        }
    }

    // Shader时间驱动
    var shaderTime by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while(true) {
            withInfiniteAnimationFrameMillis { ms ->
                shaderTime = ms /1000f
            }
        }
    }

    val isDark = MiuixTheme.isDark
    // 深浅两套流动背景色
    val shaderBrush = remember(isDark, shaderTime) {
        val rt = RuntimeShader(liquidGradientAgsl)
        rt.setFloatUniform("time", shaderTime)
        if(isDark) {
            rt.setColorUniform("colorA", Color(0xFF182030))
            rt.setColorUniform("colorB", Color(0xFF1F2C42))
            rt.setColorUniform("colorC", Color(0xFF141A28))
        } else {
            rt.setColorUniform("colorA", Color(0xFFE8F0F8))
            rt.setColorUniform("colorB", Color(0xFFD8E4F2))
            rt.setColorUniform("colorC", Color(0xFFF2F6FC))
        }
        ShaderBrush(rt)
    }

    // 卡片色彩混合参数
    val cardBlend: List<BlendColorEntry> = remember(isDark) {
        if(isDark) listOf(
            BlendColorEntry(Color(0x22ffffff), BlurBlendMode.ColorDodge)
        ) else listOf(
            BlendColorEntry(Color(0x22000000), BlurBlendMode.ColorBurn)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.blurEffect(topBarBackdrop),
                title = stringResource(R.string.about),
                color = topBarBackdrop.getAppBarColor(),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = {navigator.popBackStack()}) {
                        Icon(imageVector = MiuixIcons.Back, contentDescription = null)
                    }
                },
            )
        }
    ) { innerPadding ->
        // 层级关键：layerBackdrop包裹流动背景，让textureBlur可以抓到背景画面
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(contentBackdrop?.let { Modifier.layerBackdrop(it) } ?: Modifier)
        ) {
            // 动态流动背景，随滚动进度淡出
            Box(modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = (1f - scrollProgress).coerceIn(0f,1f)
                }
                .then(
                    if(contentBackdrop!=null) Modifier else Modifier.background(if(isDark) colorScheme.background else colorScheme.background)
                )
                .background(shaderBrush)
            )

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .then(topBarBackdrop?.let { Modifier.layerBackdrop(it) } ?: Modifier)
                    .fillMaxSize()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 16.dp,
                    start = 0.dp, end =0.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Surface(
                        modifier = Modifier.size(95.dp),
                        color = colorScheme.surfaceContainer,
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
                        text = "${BuildConfig.VERSION_CODE} (${BuildConfig.VERSION_NAME})",
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
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .then(
                                if(contentBackdrop!=null) Modifier.textureBlur(
                                    backdrop = contentBackdrop,
                                    shape = RoundedCornerShape(16.dp),
                                    blurRadius = 55f,
                                    noiseCoefficient = BlurDefaults.NoiseCoefficient,
                                    colors = BlurDefaults.blurColors(blendColors = cardBlend)
                                ) else Modifier
                            ),
                        colors = if(contentBackdrop!=null) CardDefaults.defaultColors(
                            containerColor = Color.Transparent,
                            clickableContainerColor = Color.Transparent
                        ) else CardDefaults.defaultColors()
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
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .then(
                                if(contentBackdrop!=null) Modifier.textureBlur(
                                    backdrop = contentBackdrop,
                                    shape = RoundedCornerShape(16.dp),
                                    blurRadius = 55f,
                                    noiseCoefficient = BlurDefaults.NoiseCoefficient,
                                    colors = BlurDefaults.blurColors(blendColors = cardBlend)
                                ) else Modifier
                            ),
                        colors = if(contentBackdrop!=null) CardDefaults.defaultColors(
                            containerColor = Color.Transparent,
                            clickableContainerColor = Color.Transparent
                        ) else CardDefaults.defaultColors()
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
