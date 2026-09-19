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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
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
import kotlinx.coroutines.delay
import me.bmax.apatch.BuildConfig
import me.bmax.apatch.R
import me.bmax.apatch.ui.theme.blurEffect
import me.bmax.apatch.ui.theme.getAppBarColor
import me.bmax.apatch.ui.theme.isInDarkTheme
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
import kotlin.math.sin

@Destination<RootGraph>
@Composable
fun AboutScreen(navigator: DestinationsNavigator) {
    val scrollBehavior = MiuixScrollBehavior()
    val uriHandler = LocalUriHandler.current
    val topBarBackdrop = rememberBlurBackdrop(true)
    val isDark = isInDarkTheme() // 如果这里报themeMode参数，改成 isInDarkTheme(themeMode)

    // 动画时间，兼容低版本Compose，不用mutableFloatStateOf委托
    var time by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            time += 0.012f
        }
    }

    // 深浅两套配色
    val colorSet = remember(isDark) {
        if (isDark) {
            Triple(Color(0xFF0F1419), Color(0xFF1A2433), Color(0xFF122838))
        } else {
            Triple(Color(0xFFF4F7FA), Color(0xFFE8F0F8), Color(0xFFEFF4FB))
        }
    }
    val collapseFraction = scrollBehavior.state.collapsedFraction
    val bgAlpha = (1f - collapseFraction).coerceIn(0f, 1f)

    Box(modifier = Modifier.fillMaxSize()) {
        // 纯Canvas流体背景，无RuntimeShader，全版本兼容
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val (cA, cB, cC) = colorSet
                    val paint = Paint().apply {
                        style = PaintingStyle.Fill
                        isAntiAlias = true
                    }
                    // 基础底色
                    drawRect(cA.copy(alpha = bgAlpha))

                    val w = size.width
                    val h = size.height
                    // 多层流动径向渐变波纹
                    repeat(3) { index ->
                        val t = time * (0.7f + index * 0.3f)
                        val centerX = w * (0.35f + sin(t + index * 2) * 0.22f)
                        val centerY = h * (0.45f + sin(t * 0.8f + index) * 0.18f)
                        val radius = (w * 0.55f) + sin(t * 1.2f + index) * w * 0.15f
                        val gradientColor = when(index){
                            0 -> cB
                            1 -> cC
                            else -> cB
                        }
                        drawCircle(
                            color = gradientColor.copy(alpha = 0.22f * bgAlpha),
                            radius = radius,
                            center = Offset(centerX, centerY),
                            paint = paint
                        )
                    }
                }
        )

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
