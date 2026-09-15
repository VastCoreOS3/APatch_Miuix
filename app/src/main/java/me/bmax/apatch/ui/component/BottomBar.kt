package me.bmax.apatch.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.livedata.observeAsState
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
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.DropdownMenu
import top.yukonga.miuix.kmp.basic.DropdownMenuItem
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.LayerBackdrop

@Composable
fun BottomBar(backdrop: LayerBackdrop) {
    val apState by APApplication.apStateLiveData.observeAsState(APApplication.State.UNKNOWN_STATE)
    val kPatchReady = apState != APApplication.State.UNKNOWN_STATE
    val aPatchReady = apState == APApplication.State.ANDROIDPATCH_INSTALLED

    val selectedPage = LocalSelectedPage.current
    val handlePageChange = LocalHandlePageChange.current

    // 记住用户隐藏配置：KModule / SuperUser / AModule
    var showKModule by rememberSaveable { mutableStateOf(true) }
    var showSuperUser by rememberSaveable { mutableStateOf(true) }
    var showAModule by rememberSaveable { mutableStateOf(true) }

    // 长按弹出菜单状态
    var showCustomMenu by remember { mutableStateOf(false) }

    val availablePages = remember(kPatchReady, aPatchReady, showKModule, showSuperUser, showAModule) {
        BottomBarDestination.entries.filter { d ->
            // 系统条件过滤
            val systemAvailable = !(d.kPatchRequired && !kPatchReady) && !(d.aPatchRequired && !aPatchReady)
            if (!systemAvailable) return@filter false
            // 用户手动隐藏过滤：Home、Settings永久显示
            when (d) {
                BottomBarDestination.KModule -> showKModule
                BottomBarDestination.SuperUser -> showSuperUser
                BottomBarDestination.AModule -> showAModule
                else -> true
            }
        }
    }

    NavigationBar(
        modifier = Modifier.blurEffect(backdrop),
        color = backdrop.getAppBarColor()
    ) {
        availablePages.forEachIndexed { index, destination ->
            val isSelected = selectedPage == index

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    handlePageChange(index)
                },
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = {
                        // 长按任意Tab，调出导航项自定义菜单
                        showCustomMenu = true
                    }
                ),
                icon = if (isSelected) destination.iconSelected else destination.iconNotSelected,
                label = stringResource(destination.label)
            )
        }
    }

    // 长按弹出菜单，勾选控制显示隐藏
    DropdownMenu(
        expanded = showCustomMenu,
        onDismissRequest = { showCustomMenu = false }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("自定义底部导航", modifier = Modifier.padding(vertical = 8.dp))

            DropdownMenuItem(
                onClick = { showKModule = !showKModule }
            ) {
                Checkbox(checked = showKModule, onCheckedChange = { showKModule = it })
                Text(text = stringResource(R.string.kpm), modifier = Modifier.padding(start = 8.dp))
            }

            DropdownMenuItem(
                onClick = { showSuperUser = !showSuperUser }
            ) {
                Checkbox(checked = showSuperUser, onCheckedChange = { showSuperUser = it })
                Text(text = stringResource(R.string.su_title), modifier = Modifier.padding(start = 8.dp))
            }

            DropdownMenuItem(
                onClick = { showAModule = !showAModule }
            ) {
                Checkbox(checked = showAModule, onCheckedChange = { showAModule = it })
                Text(text = stringResource(R.string.apm), modifier = Modifier.padding(start = 8.dp))
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
