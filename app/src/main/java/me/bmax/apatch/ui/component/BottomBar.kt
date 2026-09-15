package me.bmax.apatch.ui.component

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.bmax.apatch.APApplication
import me.bmax.apatch.R
import me.bmax.apatch.ui.LocalHandlePageChange
import me.bmax.apatch.ui.LocalSelectedPage
import me.bmax.apatch.ui.theme.getAppBarColor
import me.bmax.apatch.ui.theme.blurEffect
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.Text as MiuixText

/**
 * 自定义悬浮导航Item，兼容Miuix‑kmp 0.9.3
 * 不依赖material3，使用Miuix内置组件
 */
@Composable
fun CustomFloatingNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    iconSelected: ImageVector,
    iconNotSelected: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    indicatorCornerRadius: Int = 16,
    indicatorPadding: Int = 6
) {
    // 胶囊动画：选中时放大，未选中收缩
    val capsuleDp by animateDpAsState(
        targetValue = if (selected) indicatorPadding.dp else 0.dp,
        label = "capsule_padding"
    )
    // 文字透明度动画
    val textAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.7f,
        label = "text_alpha"
    )

    Box(
        modifier = modifier
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 胶囊背景
            AnimatedVisibility(visible = selected) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MiuixTheme.colorScheme.primary.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(indicatorCornerRadius.dp)
                        )
                        .padding(horizontal = capsuleDp, vertical = 4.dp)
                ) {
                    MiuixIcon(
                        imageVector = iconSelected,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            AnimatedVisibility(visible = !selected) {
                MiuixIcon(
                    imageVector = iconNotSelected,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.size(4.dp))
            MiuixText(
                text = label,
                fontSize = 11.sp,
                alpha = textAlpha
            )
        }
    }
}

@Composable
fun BottomBar(backdrop: LayerBackdrop) {
    val apState by APApplication.apStateLiveData.observeAsState(APApplication.State.UNKNOWN_STATE)
    val kPatchReady = apState != APApplication.State.UNKNOWN_STATE
    val aPatchReady = apState == APApplication.State.ANDROIDPATCH_INSTALLED

    val selectedPage = LocalSelectedPage.current
    val handlePageChange = LocalHandlePageChange.current

    val availablePages = remember(kPatchReady, aPatchReady) {
        BottomBarDestination.entries.filter { d ->
            !(d.kPatchRequired && !kPatchReady) && !(d.aPatchRequired && !aPatchReady)
        }
    }

    // 毛玻璃底色，酷安同款半透
    val navBgColor = backdrop.getAppBarColor().copy(alpha = 0.72f)

    FloatingNavigationBar(
        modifier = Modifier.blurEffect(backdrop),
        color = navBgColor,
        cornerRadius = 24.dp,
        horizontalOutSidePadding = 14.dp,
        shadowElevation = 8.dp,
        showDivider = true,
        defaultWindowInsetsPadding = true
    ) {
        availablePages.forEachIndexed { index, destination ->
            val isSelected = selectedPage == index
            CustomFloatingNavItem(
                selected = isSelected,
                onClick = { handlePageChange(index) },
                iconSelected = destination.iconSelected,
                iconNotSelected = destination.iconNotSelected,
                label = stringResource(destination.label),
                indicatorCornerRadius = 16,
                indicatorPadding = 6
            )
        }
    }
}

enum class BottomBarDestination(
    @param:StringRes val label: Int,
    val iconSelected: ImageVector,
    val iconNotSelected: ImageVector,
    val kPatchRequired: Boolean,
    val aPatchRequired: Boolean,
) {
    Home(R.string.home, Icons.Filled.Home, Icons.Outlined.Home, false, false),
    KModule(R.string.kpm, Icons.Filled.Build, Icons.Outlined.Build, true, false),
    SuperUser(R.string.su_title, Icons.Filled.Security, Icons.Outlined.Security, true, false),
    AModule(R.string.apm, Icons.Filled.Extension, Icons.Outlined.Extension, false, true),
    Settings(R.string.settings, Icons.Filled.Settings, Icons.Outlined.Settings, false, false)
}
