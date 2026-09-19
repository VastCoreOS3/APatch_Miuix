package me.bmax.apatch.ui.screen

import androidx.compose.animation.core.*
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
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

@Destination<RootGraph>
@Composable
fun AboutScreen(navigator: DestinationsNavigator) {

    val scrollBehavior = MiuixScrollBehavior()
    val uriHandler = LocalUriHandler.current

    val topBarBackdrop = rememberBlurBackdrop(true)

    // ========== 动态流光背景动画参数 ==========
    val infiniteTransition = rememberInfiniteTransition(label = "flow_background")
    // 偏移 0..1 循环，控制渐变流动，6秒一圈线性流动
    val offsetProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_offset"
    )

    // 根据主题适配流光颜色，半透明不抢UI主体
    val isDark = MiuixTheme.colorScheme.isDark
    val flowColors = remember(isDark) {
        if (isDark) {
            listOf(
                Color(0xFF1A2340).copy(alpha = 0.22f),
                Color(0xFF282042).copy(alpha = 0.18f),
                Color(0xFF162A38).copy(alpha = 0.22f),
                Color(0xFF1A2340).copy(alpha = 0.22f),
            )
        } else {
            listOf(
                Color(0xFFD6E4FF).copy(alpha = 0.30f),
                Color(0xFFE8DFFF).copy(alpha = 0.24f),
                Color(0xFFD4EDF8).copy(alpha = 0.30f),
                Color(0xFFD6E4FF).copy(alpha = 0.30f),
            )
        }
    }

    Scaffold(
        modifier = Modifier.drawBehind {
            // 绘制底层流动斜向渐变流光背景
            val angleOffset = offsetProgress * 360f
            val rad = Math.toRadians(angleOffset.toDouble())
            val shiftX = (size.width * 0.75) * Math.cos(rad).toFloat()
            val shiftY = (size.height * 0.75) * Math.sin(rad).toFloat()

            val brush = Brush.linearGradient(
                colors = flowColors,
                start = Offset(x = shiftX, y = 0f),
                end = Offset(x = size.width - shiftX, y = size.height)
            )
            drawRect(brush = brush)
        },
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
