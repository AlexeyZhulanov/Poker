package com.example.poker.ui.settings

import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role

@Composable
fun SettingsSwitchItem(
    title: String,
    description: String,
    icon: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        // Заголовок настройки
        headlineContent = { Text(title) },
        // Описание, что делает эта настройка
        supportingContent = { Text(description) },
        // Иконка слева
        leadingContent = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
            )
        },
        // Переключатель справа
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null // null, т.к. обработчик на всей строке
            )
        },
        modifier = modifier
            // Делаем всю строку кликабельной
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch
            )
    )
}