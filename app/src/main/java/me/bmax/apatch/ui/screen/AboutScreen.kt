package me.bmax.apatch.ui.screen

import android.graphics.RuntimeShader
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
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
import me.bmax.apatch.ui.theme.isInDarkTheme
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
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

// AGSL 流体渐变着色器
private const val FLUID_SHADER_SRC = """
uniform float2 resolution;
uniform float time;
uniform float4 colorA;
uniform float4 colorB;
uniform float4 colorC;
uniform float alpha;

vec4 main(vec2 fragCoord) {
    vec2 uv = fragCoord / resolution;
    float t = time * 0.12;
    float wave1 = sin(uv.x * 2.2 + t) * 0.45;
    float wave2 = sin(uv.y * 2.8 + t * 0.7) * 0.45;
    float wave3 = sin((uv.x + uv.y) * 1.6 + t * 0.4) * 0.35;
    float mixVal = wave1 + wave2 + wave3;

    vec4 col = mix(colorA, colorB, smoothstep(-1.2, 1.2, mixVal));
    col = mix(col, colorC, smoothstep(-0.6, 0.8, mixVal * 0.6));
    col.a = alpha;
    return col;
}
"""

@Destination<RootGraph>
@Composable
fun AboutScreen(navigator: DestinationsNavigator) {
    val scrollBehavior = MiuixScrollBehavior()
    val uriHandler = LocalUriHandler.current
    val topBarBackdrop = rememberBlurBackdrop(true)
    val isDark = isInDarkTheme()

    // 时间驱动流体动画
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            withInfiniteAnimationFrameNanos { frameTimeNanos ->
                time = frameTimeNanos / 1_000_000_000f
            }
        }
    }

    // 深浅两套流体配色，APatch风格
    val (colorA, colorB, colorC) = remember(isDark) {
        if (isDark) {
            Triple(
                Color(0xFF0F1419),
                Color(0xFF1A2433),
                Color(0xFF122838)
            )
        } else {
            Triple(
                Color(0xFFF4F7FA),
                Color(0xFFE8F0F8),
                Color(0xFFEFF4FB)
            )
        }
    }

    // 流体Shader
    val shader = remember {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            RuntimeShader(FLUID_SHADER_SRC)
        } else null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ===== 底层动态流体背景 =====
        Modifier
            .fillMaxSize()
            .drawBehind {
                if (shader != null) {
                    shader.setFloatUniform("resolution", size.width, size.height)
                    shader.setFloatUniform("time", time)
                    shader.setColorUniform("colorA", colorA)
                    shader.setColorUniform("colorB", colorB)
                    shader.setColorUniform("colorC", colorC)
                    shader.setFloatUniform("alpha", 1f)
                    drawRect(shader)
                } else {
                    // 低版本降级纯色
                    drawRect(if(isDark) colorA else colorA)
                }
            }

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
