import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.overScrollVertical
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.github.miuix.compose.MiuixIcons
import io.github.miuix.compose.MiuixTheme
import io.github.miuix.compose.TopAppBar
import io.github.miuix.compose.rememberBlurBackdrop
import io.github.miuix.compose.scroll.MiuixScrollBehavior
import io.github.miuix.compose.ui.IconButton
import io.github.miuix.compose.ui.Icon
import io.github.miuix.compose.ui.LinkItem
import io.github.miuix.compose.ui.sink
import android.content.Context
import androidx.compose.ui.platform.LocalUriHandler

@Composable
fun AboutScreen(navigator: DestinationsNavigator) {
    val scrollBehavior = MiuixScrollBehavior()
    val uriHandler = LocalUriHandler.current
    val topBarBackdrop = rememberBlurBackdrop(true)
    val density = LocalDensity.current

    val prefs = APApplication.sharedPreferences
    val colorMode = remember { prefs.getInt("color_mode", 0) }
    val isDarkTheme = isInDarkTheme(colorMode)

    val lifecycleOwner = LocalLifecycleOwner.current
    var isPageResumed by remember { mutableStateOf(false) }
    // 入场动画总开关，页面Resume后触发
    var enterAnimationReady by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isPageResumed = event == Lifecycle.Event.ON_RESUME
            if (event == Lifecycle.Event.ON_RESUME) {
                enterAnimationReady = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            enterAnimationReady = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(R.string.about),
                color = Color.Transparent,
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
            // HyperOS 视差背景：滚动时背景慢速跟随
            AnimatedAboutBackground(
                isResumed = isPageResumed,
                isDarkTheme = isDarkTheme,
                modifier = Modifier
                    .fillMaxSize()
                    .parallax(scrollBehavior.state, factor = 0.3f)
            )

            LazyColumn(
                modifier = (topBarBackdrop?.let { Modifier.layerBackdrop(it) } ?: Modifier)
                    .fillMaxSize()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = innerPadding,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 头部Logo区域，弹性入场
                item {
                    AnimatedVisibility(
                        visible = enterAnimationReady,
                        enter = fadeIn(animationSpec = spring(dampingRatio = 0.75f)) +
                                slideInVertically(
                                    animationSpec = spring(dampingRatio = 0.75f),
                                    initialOffsetY = { with(density) { 36.dp.toPx().toInt() } }
                                )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                                text = stringResource(id = R.string.app_name),
                                style = MiuixTheme.textStyles.title2,
                                fontWeight = FontWeight(550)
                            )

                            val versionText = remember {
                                if (BuildConfig.VERSION_NAME.contains(BuildConfig.VERSION_CODE.toString())) {
                                    "${BuildConfig.VERSION_CODE}"
                                } else {
                                    "${BuildConfig.VERSION_CODE} (${BuildConfig.VERSION_NAME})"
                                }
                            }
                            Text(
                                text = stringResource(id = R.string.about_app_version, versionText),
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
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // HyperOS 设置分组卡片：一整个Card容器，内部多个LinkItem加分隔线
                item {
                    AnimatedVisibility(
                        visible = enterAnimationReady,
                        enter = fadeIn(spring(0.75f)) + slideInVertically(
                            spring(0.75f),
                            initialOffsetY = { with(density) { 44.dp.toPx().toInt() } }
                        )
                    ) {
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp), // HyperOS大圆角
                            colors = CardDefaults.cardColors(
                                containerColor = MiuixTheme.colorScheme.surface.copy(alpha = 0.72f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // 去掉硬阴影，柔光玻璃
                        ) {
                            Column {
                                LinkItem(
                                    modifier = Modifier.sink(), // HyperOS按压下沉
                                    title = stringResource(R.string.about_github),
                                    summary = stringResource(R.string.about_github_summary),
                                    icon = painterResource(R.drawable.github)
                                ) {
                                    try {
                                        uriHandler.openUri("https://github.com/bmax121/APatch")
                                    } catch (_: Exception) {}
                                }
                                Divider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    color = MiuixTheme.colorScheme.outlineVariant.copy(0.35f),
                                    thickness = 0.5.dp
                                )

                                LinkItem(
                                    modifier = Modifier.sink(),
                                    title = stringResource(R.string.about_telegram_channel),
                                    summary = stringResource(R.string.about_telegram_channel_summary),
                                    icon = painterResource(R.drawable.channel)
                                ) {
                                    try {
                                        uriHandler.openUri("https://t.me/APatchChannel")
                                    } catch (_: Exception) {}
                                }
                                Divider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    color = MiuixTheme.colorScheme.outlineVariant.copy(0.35f),
                                    thickness = 0.5.dp
                                )

                                LinkItem(
                                    modifier = Modifier.sink(),
                                    title = stringResource(R.string.about_weblate),
                                    summary = stringResource(R.string.about_weblate_summary),
                                    icon = painterResource(R.drawable.weblate)
                                ) {
                                    try {
                                        uriHandler.openUri("https://hosted.weblate.org/engage/APatch")
                                    } catch (_: Exception) {}
                                }
                                Divider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    color = MiuixTheme.colorScheme.outlineVariant.copy(0.35f),
                                    thickness = 0.5.dp
                                )

                                LinkItem(
                                    modifier = Modifier.sink(),
                                    title = stringResource(R.string.about_telegram_group),
                                    summary = stringResource(R.string.about_telegram_group_summary),
                                    icon = painterResource(R.drawable.telegram)
                                ) {
                                    try {
                                        uriHandler.openUri("https://t.me/apatch_discuss")
                                    } catch (_: Exception) {}
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 描述卡片，延迟入场
                item {
                    AnimatedVisibility(
                        visible = enterAnimationReady,
                        enter = fadeIn(spring(0.75f, stiffness = 350f, visibilityThreshold = 0.01f)) +
                                slideInVertically(
                                    spring(0.75f),
                                    initialOffsetY = { with(density) { 52.dp.toPx().toInt() } }
                                )
                    ) {
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MiuixTheme.colorScheme.surface.copy(alpha = 0.72f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

/**
 * HyperOS 风格视差修饰符，背景随滚动慢速移动
 * factor: 视差系数，0~1，越小越慢
 */
fun Modifier.parallax(scrollState: MiuixScrollBehavior.State, factor: Float = 0.3f): Modifier {
    return this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val scrollOffset = scrollState.offset * factor
        layout(placeable.width, placeable.height) {
            placeable.placeRelative(x = 0, y = scrollOffset.toInt())
        }
    }
}
