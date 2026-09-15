package me.bmax.apatch.ui.component

import androidx.annotation.StringRes
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.bmax.apatch.APApplication
import me.bmax.apatch.R
import me.bmax.apatch.ui.LocalHandlePageChange
import me.bmax.apatch.ui.LocalSelectedPage
import me.bmax.apatch.ui.theme.getAppBarColor
import me.bmax.apatch.ui.theme.blurEffect
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.blur.LayerBackdrop

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

    FloatingNavigationBar(
        modifier = Modifier.blurEffect(backdrop),
        color = backdrop.getAppBarColor(),
        // 悬浮栏左右边距，酷安样式
        horizontalPadding = 14.dp,
        // 距离屏幕底部距离，留出手势条空间
        bottomPadding = 14.dp,
        // 整体导航栏圆角
        cornerRadius = 24.dp
    ) {
        availablePages.forEachIndexed { index, destination ->
            val isSelected = selectedPage == index

            FloatingNavigationBarItem(
                selected = isSelected,
                onClick = {
                    handlePageChange(index)
                },
                icon = if (isSelected) destination.iconSelected else destination.iconNotSelected,
                label = stringResource(destination.label),
                // 选中胶囊圆角，和酷安保持一致
                indicatorCornerRadius = 16.dp,
                // 胶囊内边距，控制胶囊大小
                indicatorPadding = 6.dp
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
    Home(
        R.string.home,
        Icons.Filled.Home,
        Icons.Outlined.Home,
        false,
        false
    ),
    KModule(
        R.string.kpm,
        Icons.Filled.Build,
        Icons.Outlined.Build,
        true,
        false
    ),
    SuperUser(
        R.string.su_title,
        Icons.Filled.Security,
        Icons.Outlined.Security,
        true,
        false
    ),
    AModule(
        R.string.apm,
        Icons.Filled.Extension,
        Icons.Outlined.Extension,
        false,
        true
    ),
    Settings(
        R.string.settings,
        Icons.Filled.Settings,
        Icons.Outlined.Settings,
        false,
        false
    )
}
