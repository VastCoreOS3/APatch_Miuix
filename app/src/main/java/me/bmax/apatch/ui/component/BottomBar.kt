package me.bmax.apatch.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.bmax.apatch.APApplication
import me.bmax.apatch.R
import me.bmax.apatch.ui.LocalHandlePageChange
import me.bmax.apatch.ui.LocalSelectedPage
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BottomBar(
    backdrop: LayerBackdrop,
    blurActive: Boolean = true,
    blurRadius: Float = 25f
) {
    val apState by APApplication.apStateLiveData.observeAsState(APApplication.State.UNKNOWN_STATE)
    val kPatchReady = apState != APApplication.State.UNKNOWN_STATE
    val aPatchReady = apState == APApplication.State.ANDROIDPATCH_INSTALLED

    val selectedPage = LocalSelectedPage.current
    val handlePageChange = LocalHandlePageChange.current

    // 根据KPatch/APatch状态动态过滤可用tab
    val availablePages = remember(kPatchReady, aPatchReady) {
        BottomBarDestination.entries.filter { d ->
            !(d.kPatchRequired && !kPatchReady) && !(d.aPatchRequired && !aPatchReady)
        }
    }

    val labelList = remember(availablePages) {
        availablePages.map { stringResource(it.label) }
    }
    val iconsSelected = remember(availablePages) {
        availablePages.map { it.iconSelected }
    }
    val iconsUnselected = remember(availablePages) {
        availablePages.map { it.iconNotSelected }
    }

    FloatingBottomNavigationBarAdapt(
        items = labelList,
        iconsSelected = iconsSelected,
        iconsUnselected = iconsUnselected,
        selectedIndex = selectedPage,
        backdrop = backdrop,
        blurActive = blurActive,
        blurRadius = blurRadius,
        onItemSelected = { index ->
            handlePageChange(index)
        }
    )
}

/**
 * 适配版悬浮底部导航栏，内置本文件，无外部依赖
 * 支持选中/未选中两套图标、textureBlur毛玻璃
 */
@Composable
fun FloatingBottomNavigationBarAdapt(
    items: List<String>,
    iconsSelected: List<ImageVector>,
    iconsUnselected: List<ImageVector>,
    selectedIndex: Int,
    backdrop: LayerBackdrop?,
    blurActive: Boolean,
    blurRadius: Float,
    onItemSelected: (Int) -> Unit,
) {
    val floatingBarColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surfaceContainer
    val floatingBarShape = RoundedCornerShape(28.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        FloatingNavigationBar(
            modifier = if (blurActive && backdrop != null) {
                Modifier
                    .widthIn(max = 440.dp)
                    .textureBlur(
                        backdrop = backdrop,
                        shape = floatingBarShape,
                        blurRadius = blurRadius,
                        colors = BlurDefaults.blurColors(
                            blendColors = listOf(
                                BlendColorEntry(color = MiuixTheme.colorScheme.surfaceContainer.copy(0.4f))
                            )
                        )
                    )
            } else {
                Modifier.widthIn(max = 440.dp)
            },
            color = floatingBarColor
        ) {
            items.forEachIndexed { index, label ->
                val currentIcon = if (selectedIndex == index) iconsSelected[index] else iconsUnselected[index]
                FloatingNavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { onItemSelected(index) },
                    icon = currentIcon,
                    label = label,
                    enabled = true
                )
            }
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
