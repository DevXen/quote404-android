package com.quotebrowser.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// v1.79: custom top bar replacing M3 TopAppBar for the main navigation
// chrome. M3 measures its title slot inside the fixed bar height AFTER
// consuming the status-bar inset, so the v1.77 forced 56.dp left only
// ~28.dp for the title — squishing the database/Categories pills below
// their 40.dp minimum while their labels spilled out of the pill. This
// bar puts the status-bar inset OUTSIDE the 56.dp content row (same
// insets M3's TopAppBar consumed), so the pills always measure at their
// natural height and the content row stays a uniform 56.dp like the
// global search row beneath it.
@Composable
fun QuoteTopBar(
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(
                    WindowInsets.systemBars.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Top
                    )
                )
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // v1.79: the title takes the remaining width after the action
            // icons; its 12.dp start inset matches M3's title padding.
            Box(
                Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                title()
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                actions()
            }
        }
    }
}
