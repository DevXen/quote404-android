package com.quotebrowser.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.siteThemeFor
import com.quotebrowser.app.ui.components.SiteThemeQuoteCard
import com.quotebrowser.app.ui.rememberNickAnnotatedText
import com.quotebrowser.app.ui.theme.scaledTextStyle
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

/**
 * Nav route for the v1.59 live theme preview screen. Registered by the
 * nav owner (AppNav): `composable(ThemePreviewRoute) {
 * ThemePreviewScreen(manager = manager, onBack =
 * { navController.popBackStack() }) }`.
 */
const val ThemePreviewRoute = "theme_preview"

/** Short sample quote rendered on every preview card. */
private val PreviewSampleQuote = Quote(
    number = 1234,
    rating = 42,
    text = "<alice> so the app chrome keeps my theme now?\n" +
        "<bob> exactly — and each card still looks like its real site",
    url = "",
    status = "approved"
)

/**
 * v1.59: live theme preview. Shows a mock of the current app chrome (top
 * app bar + bottom nav bar rendered with the CURRENT MaterialTheme
 * colors, labeled "Your current theme"), then one sample site-theme quote
 * card per database that has a site theme — the exact
 * [SiteThemeQuoteCard] the Browse list uses, so this is a true preview of
 * "my chrome + that database's cards" before flipping the
 * "Database themes only on quote cards" toggle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePreviewScreen(manager: DatabaseManager, onBack: () -> Unit) {
    val databases by manager.availableDatabases()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val themedDbs = remember(databases) {
        databases.filter { siteThemeFor(it.id) != null }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theme preview") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            flingBehavior = rememberCappedFlingBehavior()) {
            item {
                MockAppChrome(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
            items(themedDbs, key = { it.id }) { db ->
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = db.label,
                        style = MaterialTheme.typography.titleMedium
                    )
                    SiteThemeQuoteCard(
                        quote = PreviewSampleQuote,
                        dbId = db.id,
                        showNumber = true,
                        showRating = true,
                        body = {
                            val bodyStyle =
                                scaledTextStyle(MaterialTheme.typography.bodyLarge)
                            Text(
                                text = rememberNickAnnotatedText(
                                    PreviewSampleQuote.text,
                                    bodyStyle
                                ),
                                style = bodyStyle,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Mock of the app chrome (top bar + bottom nav) rendered with the current
 * MaterialTheme colors — labeled so the user can see exactly what their
 * chrome looks like next to the site-themed cards below.
 */
@Composable
private fun MockAppChrome(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Your current theme",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // Mock top app bar.
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.FormatQuote,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp)
                )
                Text(
                    text = "Quote Browser",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    Icons.Filled.Casino,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp)
                )
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
        // Mock bottom nav bar.
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MockNavItem(Icons.Filled.FormatQuote, "Browse", selected = true)
                MockNavItem(Icons.Filled.Casino, "Random", selected = false)
                MockNavItem(Icons.Filled.Favorite, "Favorites", selected = false)
            }
        }
        Text(
            text = "This is your current theme's chrome. The cards below " +
                "show how each database's site theme looks on it — flip " +
                "\"Database themes only on quote cards\" to keep this " +
                "chrome everywhere and style only the cards.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MockNavItem(icon: ImageVector, label: String, selected: Boolean) {
    val tint = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}
