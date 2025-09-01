package com.example.poker.ui.settings

import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.poker.R

@Composable
fun SettingsIconToggleItem(
    title: String,
    isPerformanceMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            IconToggleButton(
                checked = isPerformanceMode,
                onCheckedChange = onModeChange
            ) {
                if (isPerformanceMode) {
                    Icon(painter = painterResource(R.drawable.ic_speed), contentDescription = "Performance Mode")
                } else {
                    Icon(painter = painterResource(R.drawable.ic_high_quality), contentDescription = "Quality Mode")
                }
            }
        }
    )
}