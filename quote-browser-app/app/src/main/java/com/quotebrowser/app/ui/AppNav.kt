package com.quotebrowser.app.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.VerticalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Wallpaper
import com.quotebrowser.app.ui.components.AccentDropdownMenu
import com.quotebrowser.app.ui.components.CategoryFilterDropdown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import com.quotebrowser.app.ui.components.DatabaseMultiSelectDialog
import com.quotebrowser.app.ui.components.DatabaseInfoDialog
import com.quotebrowser.app.ui.components.LocalGesturesEnabled
import com.quotebrowser.app.ui.components.LocalFlagEnabled
import com.quotebrowser.app.ui.components.LocalExplainEnabled
import com.quotebrowser.app.ui.theme.DevXenBgMode
import com.quotebrowser.app.ui.theme.DevXenDimmedBackground
import com.quotebrowser.app.ui.theme.Quote404DimmedBackground
import com.quotebrowser.app.ui.theme.LocalDevXenBgMode
import com.quotebrowser.app.ui.theme.LocalQuote404Theme
import com.quotebrowser.app.ui.theme.contentOn
import com.quotebrowser.app.ui.LocalSiteShareCardsEnabled
import com.quotebrowser.app.ui.components.LocalQdbCounts
import com.quotebrowser.app.ui.components.LocalDbTotals
import com.quotebrowser.app.ui.components.LocalVoiceIconActions
import com.quotebrowser.app.ui.components.QueryAutocomplete
import com.quotebrowser.app.ui.components.QuoteTopBar
import com.quotebrowser.app.ui.components.VoiceSearchButton
import com.quotebrowser.app.ui.components.VoiceIconActions
import com.quotebrowser.app.ui.components.LocalServedDates
import com.quotebrowser.app.ui.components.LocalBlackOutsideQuotes
import com.quotebrowser.app.ui.components.LocalPersonalRatings
import com.quotebrowser.app.ui.components.LocalPersonalRatingsEnabled
import com.quotebrowser.app.ui.components.LocalRowSwipeConfig
import com.quotebrowser.app.ui.components.LocalBrowseDatabase
import com.quotebrowser.app.ui.components.LocalRowSwipeHandler
import com.quotebrowser.app.ui.components.LocalTranslations
import com.quotebrowser.app.ui.components.RowSwipeConfig
import com.quotebrowser.app.ui.components.SwipeAction
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import com.quotebrowser.app.data.Category
import com.quotebrowser.app.data.DbInfo
import com.quotebrowser.app.ui.components.SearchFilterPanel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.foundation.layout.Column
import com.quotebrowser.app.data.BattleStore
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.QuoteListStore
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.components.DbSwitcher
import com.quotebrowser.app.ui.components.LocalDbThemeSnapshots
import com.quotebrowser.app.ui.components.dbAccentColor
import com.quotebrowser.app.ui.components.PlaylistBar
import com.quotebrowser.app.ui.screens.BrowseScreen
import com.quotebrowser.app.ui.screens.BulkCategorizeScreen
import com.quotebrowser.app.ui.screens.BasketScreen
import com.quotebrowser.app.ui.screens.BlockedWordsScreen
import com.quotebrowser.app.ui.screens.CategoriesScreen
import com.quotebrowser.app.ui.screens.DailyChallengeScreen
import com.quotebrowser.app.ui.screens.DatabaseOrderScreen
import com.quotebrowser.app.ui.screens.DeckScreen
import com.quotebrowser.app.ui.screens.DetailScreen
import com.quotebrowser.app.ui.screens.DuplicateScreen
import com.quotebrowser.app.ui.screens.AmbientScreen
import com.quotebrowser.app.ui.screens.FavoritesScreen
import com.quotebrowser.app.ui.screens.FlashcardScreen
import com.quotebrowser.app.ui.screens.GamesScreen
import com.quotebrowser.app.ui.screens.OnThisDayScreen
import com.quotebrowser.app.ui.screens.WhoSaidScreen
import com.quotebrowser.app.ui.screens.MutedNicksScreen
import com.quotebrowser.app.ui.screens.QuoteBattleScreen
import com.quotebrowser.app.ui.screens.QuoteListsScreen
import com.quotebrowser.app.ui.screens.RatingGuessScreen
import com.quotebrowser.app.ui.screens.ExportHubScreen
import com.quotebrowser.app.ui.screens.GameScreen
import com.quotebrowser.app.ui.screens.HelpScreen
import com.quotebrowser.app.ui.screens.HistoryScreen
import com.quotebrowser.app.ui.screens.LayoutEditorScreen
import com.quotebrowser.app.ui.screens.HiddenQuotesScreen
import com.quotebrowser.app.ui.screens.NotesScreen
import com.quotebrowser.app.ui.screens.RandomScreen
import com.quotebrowser.app.ui.screens.RadioScreen
import com.quotebrowser.app.ui.screens.PluginsScreen
import com.quotebrowser.app.ui.screens.RatingDeckScreen
import com.quotebrowser.app.ui.screens.SettingsScreen
import com.quotebrowser.app.ui.screens.SpeakerVoicesScreen
import com.quotebrowser.app.ui.screens.ThemesScreen
import com.quotebrowser.app.ui.screens.ThemePreviewRoute
import com.quotebrowser.app.ui.screens.ThemePreviewScreen
import com.quotebrowser.app.ui.screens.VERIFY_DATABASES_ROUTE
import com.quotebrowser.app.ui.screens.VerifyDatabasesScreen
import com.quotebrowser.app.ui.screens.StatsScreen
import com.quotebrowser.app.ui.screens.ExportScreen
import com.quotebrowser.app.ui.screens.ExportFormat
import com.quotebrowser.app.ui.screens.ExportScope
import com.quotebrowser.app.ui.screens.MyQuotesScreen
import com.quotebrowser.app.ui.screens.OcrImportScreen
import com.quotebrowser.app.ui.screens.QotdHistoryScreen
import com.quotebrowser.app.ui.screens.SearchScreen
import com.quotebrowser.app.ui.screens.NickLeaderboardScreen
import com.quotebrowser.app.ui.screens.NickScreen
import com.quotebrowser.app.ui.screens.TopBottomScreen
import com.quotebrowser.app.ui.screens.UpNextScreen
import com.quotebrowser.app.ui.screens.BackupScreen
import com.quotebrowser.app.ui.screens.games.FillBlankGameScreen
import com.quotebrowser.app.ui.screens.games.OddOneOutGameScreen
import com.quotebrowser.app.ui.screens.games.SortByRatingGameScreen
import com.quotebrowser.app.ui.screens.games.GuessDbGameScreen
import com.quotebrowser.app.ui.screens.games.SpeedRoundGameScreen
import com.quotebrowser.app.ui.screens.games.TournamentGameScreen
import com.quotebrowser.app.ui.screens.games.PassAndPlayGameScreen
import com.quotebrowser.app.ui.screens.games.CompleteQuoteGameScreen
import com.quotebrowser.app.ui.screens.games.TrueFalseGameScreen
import com.quotebrowser.app.ui.screens.games.Top100GameScreen
import com.quotebrowser.app.ui.screens.games.MemoryMatchGameScreen
import com.quotebrowser.app.ui.screens.games.SurvivorGameScreen
import com.quotebrowser.app.ui.screens.games.LineScrambleGameScreen
import com.quotebrowser.app.ui.screens.games.BlitzWhoSaidItGameScreen
import com.quotebrowser.app.ui.screens.games.RatingGuessGameScreen
import com.quotebrowser.app.ui.screens.NickVsNickScreen
import kotlinx.coroutines.launch

private object Routes {
    const val BROWSE = "browse"
    const val SEARCH = "search"
    /** v1.61.20: search with the optional "Has voice" filter pre-enabled. */
    const val SEARCH_PATTERN = "search?voiceOnly={voiceOnly}"
    fun voiceSearch() = "search?voiceOnly=true"
    const val RANDOM = "random"
    /** v1.62: quote radio — endless shuffled read-aloud. */
    const val RADIO = "radio"
    const val FAVORITES = "favorites"
    const val HISTORY = "history"
    const val TOP100 = "top100"
    const val BOTTOM100 = "bottom100"
    const val DETAIL_PATTERN = "detail/{number}"
    const val NICK_PATTERN = "nick/{query}"
    const val NICKS = "nicks"
    const val DECK = "deck"
    const val RATING_DECK = "rating_deck"
    const val DUPLICATES = "duplicates"
    const val HELP = "help"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val PLUGINS = "plugins"
    const val LAYOUT_EDITOR = "layout_editor"
    const val THEMES = "themes"
    const val EXPORT = "export"
    const val BASKET = "basket"
    const val CATEGORIES = "categories"
    const val BULK_CATEGORIZE = "bulk_categorize"
    const val MY_QUOTES = "myquotes"
    const val QOTD_HISTORY = "qotd_history"
    const val CONTENT_FILTER = "content_filter"
    const val MUTED_NICKS = "muted_nicks"
    const val HIDDEN_QUOTES = "hidden_quotes"
    const val DB_ORDER = "db_order"
    const val DAILY_CHALLENGE = "daily_challenge"
    const val QUOTE_LISTS = "quote_lists"
    const val GAME = "game"
    const val NOTES = "notes"
    const val FLASHCARDS = "flashcards"
    const val GAMES = "games"
    const val WHO_SAID = "who_said"
    const val RATING_GUESS = "rating_guess"
    const val BATTLE = "quote_battle"
    const val FILL_BLANK = "game_fill_blank"
    const val ODD_ONE_OUT = "game_odd_one_out"
    const val SORT_RATING = "game_sort_rating"
    const val GUESS_DB = "game_guess_db"
    const val SPEED_ROUND = "game_speed_round"
    const val TOURNAMENT = "game_tournament"
    const val PASS_AND_PLAY = "game_pass_and_play"
    const val COMPLETE_QUOTE = "game_complete_quote"
    const val TRUE_FALSE = "game_true_false"
    const val TOP_100_GAME = "game_top_100"
    const val MEMORY_MATCH = "game_memory_match"
    const val SURVIVOR = "game_survivor"
    const val LINE_SCRAMBLE = "game_line_scramble"
    const val BLITZ_WHO_SAID_IT = "game_blitz_who_said_it"
    const val RATING_GUESS_SLIDER = "game_rating_guess_slider"
    const val NICK_VS_NICK = "nick_vs_nick"
    const val UP_NEXT = "up_next"
    const val BACKUP_RESTORE = "backup_restore"
    /** v1.61.20: saved read-aloud recordings management. */
    const val VOICE_RECORDINGS = "voice_recordings"
    /** v1.63: per-nick speaker voice assignment. */
    const val SPEAKER_VOICES = "speaker_voices"
    const val EXPORT_HUB = "export_hub"
    const val OCR_IMPORT = "ocr_import"
    // v1.53: ambient mode + dedicated On-this-day screen.
    const val AMBIENT = "ambient"
    const val ON_THIS_DAY = "on_this_day"
    fun detail(number: Int) = "detail/$number"
    /** Nick deep-dive route, e.g. from tapping a nick. */
    fun nickDetail(nick: String) = "nick/${Uri.encode(nick)}"
}

/**
 * Startup screen catalog for the "startup_screen" pref (default "home").
 *
 * Each entry is a choice key plus its user-visible label. Most keys ARE the
 * NavHost route strings; "home" is the alias for today's launch behavior
 * (the Browse tab). [routeFor] maps a stored key to a NavHost route,
 * falling back to the Browse route for unknown keys so a stale or
 * hand-edited value can never break launch.
 */
data class StartupDestination(val key: String, val label: String)

object StartupDestinations {
    const val DEFAULT_KEY = "home"

    val all: List<StartupDestination> = listOf(
        StartupDestination(DEFAULT_KEY, "Home"),
        StartupDestination(Routes.BROWSE, "Browse"),
        StartupDestination(Routes.SEARCH, "Search"),
        StartupDestination(Routes.RANDOM, "Random"),
        StartupDestination(Routes.TOP100, "Top 100"),
        StartupDestination(Routes.FAVORITES, "Favorites"),
        StartupDestination(Routes.STATS, "Stats"),
        StartupDestination(Routes.SETTINGS, "Settings"),
        // Every Games-hub entry, in GamesScreen order with its labels.
        StartupDestination(Routes.DAILY_CHALLENGE, "Daily Challenge"),
        StartupDestination(Routes.GAME, "Higher or lower"),
        StartupDestination(Routes.FLASHCARDS, "Flashcards"),
        StartupDestination(Routes.WHO_SAID, "Who said it?"),
        StartupDestination(Routes.BATTLE, "Quote battle"),
        StartupDestination(Routes.RATING_GUESS, "Guess the rating"),
        StartupDestination(Routes.FILL_BLANK, "Fill in the blank"),
        StartupDestination(Routes.ODD_ONE_OUT, "Odd one out"),
        StartupDestination(Routes.SORT_RATING, "Sort by rating"),
        StartupDestination(Routes.GUESS_DB, "Guess the database"),
        StartupDestination(Routes.SPEED_ROUND, "Speed round"),
        StartupDestination(Routes.TOURNAMENT, "Tournament"),
        StartupDestination(Routes.PASS_AND_PLAY, "Pass & Play"),
        StartupDestination(Routes.COMPLETE_QUOTE, "Complete the Quote"),
        StartupDestination(Routes.TRUE_FALSE, "True or False"),
        StartupDestination(Routes.TOP_100_GAME, "Top 100 or Not"),
        StartupDestination(Routes.MEMORY_MATCH, "Memory Match"),
        StartupDestination(Routes.SURVIVOR, "Survivor"),
        StartupDestination(Routes.LINE_SCRAMBLE, "Line scramble"),
        StartupDestination(Routes.BLITZ_WHO_SAID_IT, "Blitz who-said-it"),
        StartupDestination(Routes.RATING_GUESS_SLIDER, "Rating guess")
    )

    /** Choice key -> NavHost route; unknown keys fall back to Browse. */
    fun routeFor(key: String): String = when {
        key == DEFAULT_KEY -> Routes.BROWSE
        all.any { it.key == key } -> key
        else -> Routes.BROWSE
    }

    /** User-visible label for a stored key; unknown keys show "Home". */
    fun labelFor(key: String): String =
        all.firstOrNull { it.key == key }?.label ?: "Home"
}

private data class BottomItem(val route: String, val label: String, val icon: ImageVector)

private val bottomItems = listOf(
    BottomItem(Routes.BROWSE, "Browse", Icons.Filled.Home),
    BottomItem(Routes.SEARCH, "Search", Icons.Filled.Search),
    BottomItem(Routes.RANDOM, "Random", Icons.Filled.Casino),
    BottomItem(Routes.FAVORITES, "Favorites", Icons.Filled.Favorite),
    BottomItem(Routes.HISTORY, "History", Icons.Filled.History)
)

/**
 * v1.71: categories live in the top bar, next to the database pill. A
 * labeled pull-down ("Categories") whose dropdown is the shared
 * CategoryFilterDropdown; it tints when a filter is active.
 */
@Composable
private fun TopBarCategoriesButton(
    categories: List<com.quotebrowser.app.data.Category>,
    selectedIds: Set<Long>,
    onSelect: (Set<Long>) -> Unit,
    // v1.77: lets callers give this pill the leftover row width (the
    // database pill keeps its natural width and never clips).
    modifier: Modifier = Modifier
) {
    CategoryFilterDropdown(
        categories = categories,
        selectedIds = selectedIds,
        onSelect = onSelect,
        trigger = { onOpen ->
            FilledTonalButton(
                onClick = onOpen,
                modifier = modifier.widthIn(max = 170.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (selectedIds.isNotEmpty())
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    "Categories",
                    maxLines = 1,
                    // v1.76: ellipsize instead of hard-clipping ("Catego")
                    // when the database pill leaves little room.
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge
                )
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = "Categories",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    )
}

/**
 * v1.72: global search row — sits just under the top bar on every page
 * except Search itself (which has its own query row). Type a query, tap
 * Search, and the Search page opens with it; the expand menu next to the
 * field stages the full filter set (moods, sort, rating, maturity, cast,
 * exclusions, database scope, notes, voice, length) first.
 */
@Composable
private fun GlobalSearchRow(
    searchVm: SearchViewModel,
    categories: List<Category>,
    databases: List<DbInfo>,
    searchHistory: SearchHistory,
    toast: (String) -> Unit,
    onOpenSearch: () -> Unit,
    manager: DatabaseManager,
    onNickClick: (String) -> Unit
) {
    var optionsExpanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    fun submitGlobal() {
        if (searchVm.query.isBlank()) searchVm.updateFilterBrowse(true)
        // Same history record the Search screen writes on submit.
        searchHistory.add(searchVm.snapshotSearchRecord())
        optionsExpanded = false
        focusManager.clearFocus()
        onOpenSearch()
    }

    // v1.81: tighter — the rows sit closer to the top bar and take up
    // less vertical space (was vertical = 4.dp with a 56.dp row).
    Column(Modifier.padding(vertical = 2.dp)) {
        // v1.80: pill-sized controls — the field and the Search button are
        // 40.dp like the top bar's database/Categories pills (was 56.dp).
        // v1.81: 48.dp row (was 56.dp) so the header block is compact.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // v1.84: BasicTextField with a hand-drawn pill — the M3
            // TextField kept shaving the tops of the glyphs no matter the
            // font size, so the field now centers the text itself with
            // exact padding instead of relying on M3's internals.
            val pillTextStyle = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            BasicTextField(
                value = searchVm.query,
                onValueChange = searchVm::updateQuery,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                singleLine = true,
                textStyle = pillTextStyle,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { submitGlobal() }),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchVm.query.isEmpty()) {
                                Text(
                                    "Search…",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = pillTextStyle.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            innerTextField()
                        }
                        if (searchVm.query.isNotEmpty()) {
                            IconButton(onClick = { searchVm.updateQuery("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                            }
                        } else {
                            // v1.73: mic sits in the trailing slot when there's
                            // nothing to clear — dictated text submits the search.
                            VoiceSearchButton(
                                onResult = {
                                    searchVm.updateQuery(it)
                                    submitGlobal()
                                },
                                onUnavailable = { toast("Voice search not available") }
                            )
                        }
                    }
                }
            )
            // v1.73: badge shows how many search filters are active.
            val filterCount = searchVm.activeFilterCount()
            BadgedBox(
                badge = {
                    if (filterCount > 0) {
                        Badge { Text(filterCount.toString()) }
                    }
                }
            ) {
                IconButton(onClick = { optionsExpanded = !optionsExpanded }) {
                    Icon(
                        if (optionsExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription =
                            if (optionsExpanded) "Hide search options" else "Show search options"
                    )
                }
            }
            Button(
                // v1.80: natural pill height (40.dp) like the top bar's
                // pills — was pinned to 56.dp.
                onClick = { submitGlobal() }
            ) {
                Text("Search")
            }
        }
        // v1.73: nick + category autocomplete under the field.
        QueryAutocomplete(
            query = searchVm.query,
            manager = manager,
            categories = categories,
            onNickClick = onNickClick,
            onCategoryClick = { cat ->
                searchVm.updateCategories(searchVm.categoryIds + cat.id)
                searchVm.updateQuery("") // v1.73: commit the suggestion, dismiss autocomplete
                submitGlobal()
            }
        )
        if (optionsExpanded) {
            SearchFilterPanel(
                vm = searchVm,
                categories = categories,
                databases = databases,
                filtersExpanded = true,
                onFiltersExpandedChange = {},
                toast = toast,
                autoSubmit = false,
                onSubmit = {},
                showFilterHeader = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNav(
    manager: DatabaseManager,
    deepLinkNumber: Int? = null,
    deepLinkDbId: String? = null,
    onDeepLinkConsumed: () -> Unit = {},
    deepLinkTab: String? = null,
    onTabDeepLinkConsumed: () -> Unit = {},
    sharedText: String? = null,
    onSharedTextConsumed: () -> Unit = {},
    folderLink: String? = null,
    onFolderLinkConsumed: () -> Unit = {},
    /** NavHost start destination; default reproduces today's Browse launch. */
    startDestination: String = Routes.BROWSE
) {
    val navController = rememberNavController()

    // Activity-scoped: shared by the top-bar dice action and the Random tab.
    val mainVm: MainViewModel = viewModel(factory = vmFactory { MainViewModel(manager) })
    val randomVm: RandomViewModel = viewModel(factory = vmFactory { RandomViewModel(manager) })
    val playlistVm: PlaylistViewModel = viewModel(factory = vmFactory { PlaylistViewModel(manager) })
    val qotdVm: QotdViewModel = viewModel(factory = vmFactory { QotdViewModel(manager) })
    val catVm: CategoryViewModel = viewModel(factory = vmFactory { CategoryViewModel(manager) })
    // v1.67: Browse/Search VMs hoisted to AppNav scope so the top bar can
    // host the categories dropdown next to the database pill.
    val browseVm: BrowseViewModel = viewModel(factory = vmFactory { BrowseViewModel(manager) })
    val searchVm: SearchViewModel = viewModel(factory = vmFactory { SearchViewModel(manager) })
    val topBarCategories by catVm.categories.collectAsStateWithLifecycle()
    // v1.72: search history hoisted so the global search row (top bar) and
    // the Search screen record into the same store.
    val searchHistory = remember {
        SearchHistory(navController.context.applicationContext)
    }
    // v1.66: user quote lists ("My Collections") — one store for the app.
    val quoteListStore = remember {
        QuoteListStore(navController.context.applicationContext)
    }

    val databases by mainVm.databases.collectAsStateWithLifecycle()
    val currentLabel by mainVm.currentLabel.collectAsStateWithLifecycle()
    // v1.59: DbSwitcher quick theme picker data.
    val dbThemeConfigs by mainVm.dbThemeConfigs.collectAsStateWithLifecycle()
    val customThemes by mainVm.customThemes.collectAsStateWithLifecycle()
    val importMessage by mainVm.importMessage.collectAsStateWithLifecycle()
    val showNumber by mainVm.showQuoteNumber.collectAsStateWithLifecycle()
    val showRating by mainVm.showRating.collectAsStateWithLifecycle()
    val showUrl by mainVm.showUrl.collectAsStateWithLifecycle()
    val shakeToShuffle by mainVm.shakeToShuffle.collectAsStateWithLifecycle()
    val playlistPlaying by playlistVm.playing.collectAsStateWithLifecycle()
    val playlistIndex by playlistVm.index.collectAsStateWithLifecycle()
    val playlistItems by playlistVm.items.collectAsStateWithLifecycle()

    // Random sources drive the top-bar control on the Random tab: the
    // current-database switcher is meaningless there, because the Random
    // pool is drawn from its own source filter (null = all databases).
    val randomSourceIds by randomVm.sourceIds.collectAsStateWithLifecycle()
    val randomDbs by randomVm.databases.collectAsStateWithLifecycle()
    var showRandomSources by remember { mutableStateOf(false) }

    // Which export scope the Export screen should preselect (basket flow).
    var exportInitialScope by remember { mutableStateOf(ExportScope.ALL) }
    // v1.67: which format the Export screen should preselect (the Random
    // page's "Export video" menu item opens it on MP4).
    var exportInitialFormat by remember { mutableStateOf(ExportFormat.TXT) }

    // v1.61.20: per-quote voice-recording management, opened by long-pressing
    // the voice icon on any quote card.
    var voiceManageKey by remember { mutableStateOf<Pair<String, Int>?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val importPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) mainVm.onImportPicked(uri)
        }

    LaunchedEffect(importMessage) {
        importMessage?.let { message ->
            scope.launch { snackbarHostState.showSnackbar(message) }
            mainVm.consumeImportMessage()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var overflowExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val appVersion = remember {
        try {
            val pkg = context.packageManager.getPackageInfo(context.packageName, 0)
            "v${pkg.versionName}"
        } catch (e: Exception) {
            ""
        }
    }

    // Export queue state, shared by every screen's long-press menu.
    val basket by mainVm.basket.collectAsStateWithLifecycle()
    val basketCount by mainVm.basketCount.collectAsStateWithLifecycle()
    // v1.53 feature gates for the overflow menu entries.
    val onThisDayOn by manager.onThisDayEnabled.collectAsStateWithLifecycle()
    val ambientOn by manager.ambientModeEnabled.collectAsStateWithLifecycle()
    fun onToggleBasket(dbId: String, number: Int) {
        val added = com.quotebrowser.app.data.BasketEntry(dbId, number) !in basket
        mainVm.toggleBasket(dbId, number)
        android.widget.Toast.makeText(
            context,
            if (added) "Added to export tray" else "Removed from export tray",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    fun goRandom() {
        navController.navigate(Routes.RANDOM) {
            popUpTo(navController.graph.startDestinationId)
            launchSingleTop = true
        }
        randomVm.newRandom()
    }

    // Configurable quote-row swipe actions, provided once for every screen.
    val swipeLeft by manager.swipeLeft.collectAsStateWithLifecycle()
    val swipeRight by manager.swipeRight.collectAsStateWithLifecycle()
    val swipeHomeEnabled by manager.swipeHomeEnabled.collectAsStateWithLifecycle()
    val swipeRandomEnabled by manager.swipeRandomEnabled.collectAsStateWithLifecycle()
    // v1.49 tap gestures, provided once for every screen.
    val gesturesEnabled by manager.gesturesEnabled.collectAsStateWithLifecycle()
    // v1.53 "Explain this quote" feature gate.
    val explainEnabled by manager.explainQuoteEnabled.collectAsStateWithLifecycle()
    // v1.53 site-styled share cards feature gate.
    val siteShareCardsEnabled by manager.siteShareCardsEnabled.collectAsStateWithLifecycle()
    // v1.53 qdb.us approved/pending counts for the site footer chrome.
    val qdbCounts by produceState(0 to 0) {
        value = try {
            manager.qdbUsCounts()
        } catch (e: Exception) {
            0 to 0
        }
    }
    // v1.55.1: per-database total quote counts for the site footers
    // ("21,096 quotes").
    val dbTotals by produceState(mapOf<String, Int>()) {
        value = try {
            manager.allDbIds().associateWith { id ->
                try {
                    manager.quoteCount(id)
                } catch (e: Exception) {
                    null
                } ?: 0
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
    // v1.53 black page background outside the bordered site quotes.
    val blackOutside by manager.blackOutsideQuotes.collectAsStateWithLifecycle()
    // v1.58: per-quote capture dates for the quiet card footer line
    // (qdb.us sidecar asset; empty when unavailable). Keyed
    // "dbId/number" so other databases' quote numbers can never collide
    // with qdb.us dates.
    val servedDates by produceState(mapOf<String, String>()) {
        value = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val qdbId = com.quotebrowser.app.data.DatabaseManager.QDB_US_ASSET_ID
                manager.qdbUsServedMap().mapKeys { (k, _) -> "$qdbId/$k" }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
    val swipeScope = rememberCoroutineScope()
    val swipeContext = LocalContext.current
    val upNextStore = remember { com.quotebrowser.app.data.QueueStore(swipeContext) }
    // v1.50 rate-as-you-read: one shared personal-ratings store for every
    // screen, plus the feature toggle from Settings.
    val personalRatings = remember { com.quotebrowser.app.data.PersonalRatings(swipeContext) }
    val personalRatingsOn by manager.personalRatings.collectAsStateWithLifecycle()
    // v1.53 report/flag feature toggle.
    val flagQuotesEnabled by manager.flagQuotesEnabled.collectAsStateWithLifecycle()
    // Two-pane Browse/Search on foldables and tablets: any device whose
    // smallest width is at least 600dp shows list + detail side by side.
    // Hoisted above the Scaffold so the database switcher (top bar) can
    // clear the pane on an ordinary switch.
    val wide = LocalConfiguration.current.smallestScreenWidthDp >= 600
    // Quote shown in the right pane on wide screens.
    var paneSelection by remember { mutableStateOf<Pair<String, Int>?>(null) }
    CompositionLocalProvider(
        LocalRowSwipeConfig provides RowSwipeConfig(left = swipeLeft, right = swipeRight),
        LocalGesturesEnabled provides gesturesEnabled,
        // v1.61.10: the bottom-right database tag button jumps to Browse
        // filtered to that database.
        LocalBrowseDatabase provides { dbId: String ->
            manager.setBrowseDbIds(setOf(dbId))
            navController.navigate(Routes.BROWSE) {
                launchSingleTop = true
            }
        },
        // v1.61: quote translations for every card in the tree.
        LocalTranslations provides manager.translations,
        LocalExplainEnabled provides explainEnabled,
        LocalFlagEnabled provides flagQuotesEnabled,
        LocalSiteShareCardsEnabled provides siteShareCardsEnabled,
        LocalQdbCounts provides qdbCounts,
        LocalDbTotals provides dbTotals,
        LocalServedDates provides servedDates,
        LocalBlackOutsideQuotes provides blackOutside,
        LocalPersonalRatings provides personalRatings,
        LocalPersonalRatingsEnabled provides personalRatingsOn,
        // v1.61.20: the voice-recording icon on quote cards — tap jumps to
        // Search filtered to voiced quotes, long-press manages the quote.
        LocalVoiceIconActions provides VoiceIconActions(
            onVoiceSearch = { navController.navigate(Routes.voiceSearch()) },
            onVoiceManage = { dbId, number -> voiceManageKey = dbId to number }
        ),
        LocalRowSwipeHandler provides { action, dbId, quote ->
            when (action) {
                SwipeAction.FAVORITE -> swipeScope.launch {
                    try {
                        manager.repoFor(dbId).toggleFavorite(quote)
                    } catch (_: Exception) {
                    }
                }
                SwipeAction.SHARE -> shareQuote(swipeContext, quote)
                SwipeAction.COPY -> copyQuote(swipeContext, quote)
                SwipeAction.BASKET -> manager.toggleBasket(dbId, quote.number)
                SwipeAction.HIDE -> {
                    manager.setQuoteHidden(dbId, quote.number, true)
                    Toast.makeText(
                        swipeContext,
                        "Quote hidden — see Content Filter to restore",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                SwipeAction.READ_ALOUD ->
                    playlistVm.start(listOf(SourcedQuote(dbId, quote)))
                SwipeAction.UP_NEXT -> {
                    val added = upNextStore.add(dbId, quote.number)
                    Toast.makeText(
                        swipeContext,
                        if (added) "Added to Up Next" else "Already in Up Next",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                SwipeAction.NEXT_RANDOM -> randomVm.newRandom()
                SwipeAction.NONE -> {}
            }
        }
    ) {
    // DevXen "Behind quotes" mode: the artwork sits behind the lists, so
    // the scaffold itself stays transparent and cards go semi-transparent.
    // v1.61.5: the Quote404 theme always draws Clint's Quote404 artwork
    // behind the lists the same way.
    val devXenBehindQuotes =
        LocalDevXenBgMode.current == DevXenBgMode.BEHIND_QUOTES
    val quote404Bg = LocalQuote404Theme.current
    val artBehindQuotes = devXenBehindQuotes || quote404Bg
    Scaffold(
        containerColor = if (artBehindQuotes) Color.Transparent
        else MaterialTheme.colorScheme.surface,
        topBar = {
            // v1.72: the top app bar plus the global search row stacked
            // under it (every page except Search itself).
            Column {
            // v1.79: custom QuoteTopBar instead of M3 TopAppBar — M3
            // measures its title slot inside the fixed bar height AFTER
            // consuming the status-bar inset, so the v1.77 forced 56.dp
            // squished the database/Categories pills below their 40.dp
            // minimum and their labels spilled out of the pill.
            // QuoteTopBar puts the inset outside the 56.dp content row,
            // so the pills always get their natural height.
            QuoteTopBar(
                title = {
                    if (currentRoute == Routes.RANDOM) {
                        // On the Random tab the dropdown picks the random
                        // sources (one, several, or all databases) instead
                        // of switching the current database, which the
                        // Random pool ignores.
                        val srcIds = randomSourceIds
                        val summary = when {
                            srcIds == null -> "All databases"
                            srcIds.size == 1 ->
                                randomDbs.firstOrNull { it.id in srcIds }?.label
                                    ?: "1 selected"
                            else -> "${srcIds.size} databases"
                        }
                        // v1.67: categories sit next to the database pill in the
                        // top bar on every page that filters by category.
                        // v1.77: the database pill is never squeezed — it
                        // keeps its natural width (full label, no clipping)
                        // and the Categories pill yields instead (weight +
                        // ellipsis). Database names are more important to
                        // read than the Categories label.
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { showRandomSources = true }
                            ) {
                                Text(
                                    text = summary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.Filled.ArrowDropDown,
                                    contentDescription = "Choose random sources"
                                )
                            }
                            TopBarCategoriesButton(
                                categories = topBarCategories,
                                selectedIds = randomVm.categoryIds,
                                onSelect = { ids ->
                                    randomVm.updateCategories(
                                        ids,
                                        topBarCategories.filter { it.id in ids }
                                            .map { it.name }.toSet()
                                    )
                                },
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    } else if (currentRoute == Routes.BROWSE) {
                        // v1.57: Browse/home database multi-select — any
                        // subset of databases, not just one or all. Empty
                        // selection = all databases; persists across
                        // restarts via DatabaseManager.browseDbIds.
                        // v1.55: per-database info dialog state (shared with
                        // the single-select switcher below).
                        var infoDbId by remember { mutableStateOf<String?>(null) }
                        var infoStats by remember {
                            mutableStateOf<com.quotebrowser.app.data.DatabaseManager.DatabaseStats?>(null)
                        }
                        LaunchedEffect(infoDbId) {
                            val id = infoDbId
                            if (id != null) {
                                infoStats = null
                                infoStats = manager.databaseStats(id)
                            }
                        }
                        if (infoDbId != null) {
                            DatabaseInfoDialog(
                                stats = infoStats,
                                onDismiss = { infoDbId = null }
                            )
                        }
                        val browseIds by manager.browseDbIds.collectAsStateWithLifecycle()
                        val singleBrowseId = browseIds.singleOrNull()
                        val snap = LocalDbThemeSnapshots.current
                        // v1.57: only the database pill carries the selected
                        // database's accent (Bash.org -> dark gold); mixed/
                        // all selections stay neutral.
                        val pillAccent = remember(snap, singleBrowseId) {
                            dbAccentColor(snap, singleBrowseId)
                        }
                        val pillLabel = when {
                            browseIds.isEmpty() -> "All databases"
                            singleBrowseId != null ->
                                databases.firstOrNull { it.id == singleBrowseId }?.label
                                    ?: "1 selected"
                            else -> "${browseIds.size} databases"
                        }
                        var showBrowseSources by remember { mutableStateOf(false) }
                        // v1.67: categories next to the database pill —
                        // only meaningful for a single database (categories
                        // are per-database), matching the old in-screen row.
                        // v1.77: the database pill is never squeezed — it
                        // keeps its natural width and the Categories pill
                        // yields instead (database names are more important
                        // to read than the Categories label).
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { showBrowseSources = true },
                                colors = if (pillAccent != null)
                                    ButtonDefaults.filledTonalButtonColors(
                                        containerColor = pillAccent,
                                        contentColor = contentOn(pillAccent)
                                    )
                                else ButtonDefaults.filledTonalButtonColors()
                            ) {
                                Text(
                                    text = pillLabel,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.Filled.ArrowDropDown,
                                    contentDescription = "Choose databases"
                                )
                            }
                            if (singleBrowseId != null) {
                                TopBarCategoriesButton(
                                    categories = topBarCategories,
                                    selectedIds = browseVm.categoryIds,
                                    onSelect = { browseVm.updateCategories(it) },
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                        if (showBrowseSources) {
                            DatabaseMultiSelectDialog(
                                title = "Browse databases",
                                databases = databases,
                                selected = browseIds.takeIf { it.isNotEmpty() },
                                onConfirm = {
                                    // v1.56: a database change clears the
                                    // wide detail pane so it can't show a
                                    // stale quote.
                                    paneSelection = null
                                    manager.setBrowseDbIds(it ?: emptySet())
                                },
                                onDismiss = { showBrowseSources = false },
                                onInfoClick = { infoDbId = it },
                            )
                        }
                    } else {
                        // v1.55: per-database info dialog state.
                        var infoDbId by remember { mutableStateOf<String?>(null) }
                        var infoStats by remember {
                            mutableStateOf<com.quotebrowser.app.data.DatabaseManager.DatabaseStats?>(null)
                        }
                        LaunchedEffect(infoDbId) {
                            val id = infoDbId
                            if (id != null) {
                                infoStats = null
                                infoStats = manager.databaseStats(id)
                            }
                        }
                        if (infoDbId != null) {
                            DatabaseInfoDialog(
                                stats = infoStats,
                                onDismiss = { infoDbId = null }
                            )
                        }
                        // v1.67: on the Search page the categories dropdown sits
                        // next to the database switcher in the top bar.
                        val isSearchRoute = currentRoute == Routes.SEARCH ||
                            currentRoute?.startsWith("search?") == true
                        // v1.77: the database pill keeps its natural width
                        // (full label, never clipped — database names are
                        // more important to read) and the Categories pill
                        // yields the remaining width with an ellipsis.
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DbSwitcher(
                                currentLabel = currentLabel.ifEmpty { "Quote Browser" },
                                databases = databases,
                                onSelect = { id ->
                                    // v1.56: an ordinary database switch clears
                                    // the wide detail pane so it can't show a
                                    // stale quote ("stars are wrong"). Deliberate
                                    // opens (cards, deep links) manage the pane
                                    // themselves and don't go through here.
                                    paneSelection = null
                                    mainVm.switchDatabase(id)
                                },
                                onImportClick = { importPicker.launch(arrayOf("*/*")) },
                                onInfoClick = { infoDbId = it },
                                // v1.59: quick per-database theme picker in the switcher.
                                dbThemeConfigs = dbThemeConfigs,
                                customThemes = customThemes,
                                onDbThemeConfigChange = mainVm::setDbThemeConfig,
                            )
                            if (isSearchRoute) {
                                TopBarCategoriesButton(
                                    categories = topBarCategories,
                                    selectedIds = searchVm.categoryIds,
                                    onSelect = { searchVm.updateCategories(it) },
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = ::goRandom) {
                        Icon(Icons.Filled.Casino, contentDescription = "Random quote")
                    }
                    // v1.59.1: Settings gear before the overflow menu —
                    // opens Settings directly (overflow menu kept too).
                    IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = { overflowExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                    // v1.57: rounded, accent-tinted overflow menu.
                    AccentDropdownMenu(
                        expanded = overflowExpanded,
                        onDismissRequest = { overflowExpanded = false }
                    ) {
                        // v1.58: the menu's real contrast color, captured
                        // outside the items (M3's item colors would
                        // otherwise override it inside the slots).
                        val menuContentColor = androidx.compose.material3.LocalContentColor.current
                        // v1.59: grouped overflow menu — Actions / View / App.
                        DropdownMenuItem(
                            text = { Text("Up Next") },
                            leadingIcon = { Icon(Icons.Filled.PlaylistPlay, contentDescription = null) },
                            onClick = {
                                overflowExpanded = false
                                navController.navigate(Routes.UP_NEXT)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Notes") },
                            leadingIcon = { Icon(Icons.Filled.EditNote, contentDescription = null) },
                            onClick = {
                                overflowExpanded = false
                                navController.navigate(Routes.NOTES)
                            },
                            // v1.49: the notes feature can be disabled.
                            enabled = manager.notesEnabled.value
                        )
                        DropdownMenuItem(
                            text = { Text("Games") },
                            leadingIcon = { Icon(Icons.Filled.SportsEsports, contentDescription = null) },
                            onClick = {
                                overflowExpanded = false
                                navController.navigate(Routes.GAMES)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate finder") },
                            leadingIcon = { Icon(Icons.Filled.FindInPage, contentDescription = null) },
                            onClick = {
                                overflowExpanded = false
                                navController.navigate(Routes.DUPLICATES)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Import from photo") },
                            leadingIcon = { Icon(Icons.Filled.AddAPhoto, contentDescription = null) },
                            onClick = {
                                overflowExpanded = false
                                navController.navigate(Routes.OCR_IMPORT)
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (basketCount == 0) "Export"
                                    else "Export tray ($basketCount)"
                                )
                            },
                            leadingIcon = { Icon(Icons.Filled.IosShare, contentDescription = null) },
                            onClick = {
                                overflowExpanded = false
                                navController.navigate(Routes.EXPORT_HUB)
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Top 100") },
                            leadingIcon = { Icon(Icons.Filled.EmojiEvents, contentDescription = null) },
                            onClick = {
                                overflowExpanded = false
                                navController.navigate(Routes.TOP100)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Bottom 100") },
                            leadingIcon = { Icon(Icons.Filled.TrendingDown, contentDescription = null) },
                            onClick = {
                                overflowExpanded = false
                                navController.navigate(Routes.BOTTOM100)
                            }
                        )
                        // v1.66: user quote lists ("My Collections").
                        DropdownMenuItem(
                            text = { Text("My Collections") },
                            leadingIcon = { Icon(Icons.Filled.Layers, contentDescription = null) },
                            onClick = {
                                overflowExpanded = false
                                navController.navigate(Routes.QUOTE_LISTS)
                            }
                        )
                        // v1.53: On this day + Ambient mode, feature-gated.
                        if (onThisDayOn) {
                            DropdownMenuItem(
                                text = { Text("On this day") },
                                leadingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                                onClick = {
                                    overflowExpanded = false
                                    navController.navigate(Routes.ON_THIS_DAY)
                                }
                            )
                        }
                        if (ambientOn) {
                            DropdownMenuItem(
                                text = { Text("Ambient mode") },
                                leadingIcon = { Icon(Icons.Filled.Wallpaper, contentDescription = null) },
                                onClick = {
                                    overflowExpanded = false
                                    navController.navigate(Routes.AMBIENT)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Themes") },
                            leadingIcon = { Icon(Icons.Filled.Palette, contentDescription = null) },
                            onClick = {
                                overflowExpanded = false
                                navController.navigate(Routes.THEMES)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Categories") },
                            leadingIcon = { Icon(Icons.Filled.Category, contentDescription = null) },
                            onClick = {
                                overflowExpanded = false
                                navController.navigate(Routes.CATEGORIES)
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Content Filter") },
                            leadingIcon = { Icon(Icons.Filled.FilterList, contentDescription = null) },
                            onClick = {
                                overflowExpanded = false
                                navController.navigate(Routes.CONTENT_FILTER)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                            onClick = {
                                overflowExpanded = false
                                navController.navigate(Routes.SETTINGS)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Help") },
                            leadingIcon = { Icon(Icons.Filled.Help, contentDescription = null) },
                            onClick = {
                                overflowExpanded = false
                                navController.navigate(Routes.HELP)
                            }
                        )
                        if (appVersion.isNotEmpty()) {
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Quote Browser $appVersion",
                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                        color = menuContentColor
                                    )
                                },
                                onClick = { overflowExpanded = false },
                                enabled = false
                            )
                        }
                    }
                }
            )
            // v1.72: global search row just under the top bar on every page
            // except Search itself, which has its own query row.
            val onSearchPage = currentRoute == Routes.SEARCH ||
                currentRoute?.startsWith("search?") == true
            if (!onSearchPage) {
                GlobalSearchRow(
                    searchVm = searchVm,
                    categories = topBarCategories,
                    databases = databases,
                    searchHistory = searchHistory,
                    toast = { msg ->
                        Toast.makeText(
                            navController.context,
                            msg,
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onOpenSearch = {
                        navController.navigate(Routes.SEARCH) {
                            launchSingleTop = true
                        }
                    },
                    manager = manager,
                    onNickClick = { nick -> navController.navigate(Routes.nickDetail(nick)) }
                )
            }
            } // v1.72 topBar Column
        },
        bottomBar = {
            Column {
                if (playlistPlaying && playlistItems.isNotEmpty()) {
                    PlaylistBar(
                        position = playlistIndex,
                        total = playlistItems.size,
                        onSkip = playlistVm::skip,
                        onStop = playlistVm::stopPlayback
                    )
                }
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    bottomItems.forEach { item ->
                        val selected = currentRoute == item.route ||
                            (item.route == Routes.BROWSE && currentRoute?.startsWith("detail/") == true) ||
                            (item.route == Routes.RANDOM && currentRoute == Routes.DECK) ||
                            (item.route == Routes.SEARCH && currentRoute?.startsWith("nick/") == true) ||
                            (item.route == Routes.SEARCH && currentRoute?.startsWith("search") == true)
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            // Pop back to the tab if it's already on the stack
                            // (saving state of anything above it); otherwise
                            // navigate to it fresh. This avoids relying on the
                            // navigate()+popUpTo(saveState)+restoreState path,
                            // which could leave the visible screen stuck.
                            // Tapping the active tab again re-triggers it: on Random
                            // that shuffles a fresh set of quotes.
                            if (currentRoute == item.route) {
                                if (item.route == Routes.RANDOM) randomVm.newRandom()
                                return@NavigationBarItem
                            }
                            val popped = navController.popBackStack(
                                route = item.route,
                                inclusive = false,
                                saveState = true
                            )
                            if (!popped) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(item.icon, contentDescription = item.label)
                        },
                        label = { Text(item.label) }
                    )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (devXenBehindQuotes) {
                DevXenDimmedBackground(
                    modifier = Modifier.matchParentSize(),
                    scrimAlpha = 0.62f
                )
            }
            if (quote404Bg) {
                Quote404DimmedBackground(
                    modifier = Modifier.matchParentSize(),
                    scrimAlpha = 0.62f
                )
            }
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
            // Quote shown in the right pane on wide screens (declared
            // above the Scaffold).
            // Deep link from the widget / Quote-of-the-Day notification.
            LaunchedEffect(deepLinkNumber, wide) {
                if (deepLinkNumber != null) {
                    // The quote may belong to another database: switch first.
                    if (deepLinkDbId != null) manager.switchDatabase(deepLinkDbId)
                    if (wide) {
                        paneSelection =
                            (deepLinkDbId ?: manager.currentDbId.value) to deepLinkNumber
                    } else {
                        navController.navigate(Routes.detail(deepLinkNumber))
                    }
                    onDeepLinkConsumed()
                }
            }

            // Launcher shortcut: jump straight to Random, the deck, or Search.
            LaunchedEffect(deepLinkTab) {
                val route = when (deepLinkTab) {
                    "random" -> Routes.RANDOM
                    "deck" -> Routes.DECK
                    "search" -> Routes.SEARCH
                    "games" -> Routes.GAMES
                    "who_said" -> Routes.WHO_SAID
                    else -> null
                }
                if (route != null) {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    onTabDeepLinkConsumed()
                }
            }

            // Text shared from another app: stash it for the My Quotes screen,
            // then open My Quotes with the editor prefilled.
            // Text shared from another app: stash it for the My Quotes screen,
            // then open My Quotes with the editor prefilled.
            var inboxText by remember { mutableStateOf<String?>(null) }
            var folderInbox by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(folderLink) {
                if (folderLink != null) {
                    folderInbox = folderLink
                    onFolderLinkConsumed()
                    navController.navigate(Routes.FAVORITES) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
            LaunchedEffect(sharedText) {
                if (sharedText != null) {
                    inboxText = sharedText
                    onSharedTextConsumed()
                    navController.navigate(Routes.MY_QUOTES) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }

            /** Opens a quote: right pane on wide screens, detail route otherwise. */
            fun openQuote(dbId: String, number: Int) {
                // v1.56: the detail keeps the quote's real source database
                // independently — opening a quote from mixed All-databases
                // mode no longer switches the global selection away from All.
                val realId = manager.singleDbId(dbId)
                if (wide) {
                    paneSelection = realId to number
                } else {
                    navController.navigate(Routes.detail(number))
                    navController.currentBackStackEntry
                        ?.savedStateHandle?.set("seedDbId", realId)
                }
            }

            @Composable
            fun MainNavHost(modifier: Modifier = Modifier) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = modifier
                ) {
            composable(Routes.BROWSE) {
                CompositionLocalProvider(
                    LocalRowSwipeConfig provides RowSwipeConfig(
                        left = if (swipeHomeEnabled) swipeLeft else SwipeAction.NONE,
                        right = if (swipeHomeEnabled) swipeRight else SwipeAction.NONE
                    )
                ) {
                    BrowseScreen(
                        vm = browseVm,
                        qotdVm = qotdVm,
                        catVm = catVm,
                        manager = manager,
                        onQuoteClick = { dbId, number -> openQuote(dbId, number) },
                        onQotdClick = { dbId, number -> openQuote(dbId, number) },
                        onQotdHistoryClick = { navController.navigate(Routes.QOTD_HISTORY) },
                        showNumber = showNumber,
                        showRating = showRating,
                        basket = basket,
                        onToggleBasket = ::onToggleBasket,
                        onAddToPlaylist = { playlistVm.append(it) },
                        onReadAloudQuote = { playlistVm.start(listOf(it)) },
                        onNickClick = { nick -> navController.navigate(Routes.nickDetail(nick)) }
                    )
                }
            }
            composable(Routes.QOTD_HISTORY) {
                QotdHistoryScreen(
                    vm = qotdVm,
                    onQuoteClick = { dbId, number -> openQuote(dbId, number) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.SEARCH_PATTERN,
                arguments = listOf(
                    navArgument("voiceOnly") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                // v1.61.20: the card voice icon navigates here with the
                // "Has voice" filter pre-enabled.
                val voiceOnlyArg =
                    backStackEntry.arguments?.getBoolean("voiceOnly") == true
                LaunchedEffect(voiceOnlyArg) {
                    if (voiceOnlyArg) searchVm.updateVoiceOnly(true)
                }
                SearchScreen(vm = searchVm, catVm = catVm, manager = manager, onQuoteClick = { number -> openQuote(manager.currentDbId.value, number) }, onSourcedQuoteClick = { dbId, number -> openQuote(dbId, number) }, showNumber = showNumber, showRating = showRating, basket = basket, onToggleBasket = ::onToggleBasket, onAddToPlaylist = { playlistVm.append(it) }, onReadAloudQuote = { playlistVm.start(listOf(it)) }, onNickClick = { nick -> navController.navigate(Routes.nickDetail(nick)) }, quoteListStore = quoteListStore, searchHistory = searchHistory)
            }
            composable(
                route = Routes.NICK_PATTERN,
                arguments = listOf(navArgument("query") { type = NavType.StringType })
            ) { backStackEntry ->
                val nick = backStackEntry.arguments?.getString("query").orEmpty()
                NickScreen(
                    nick = nick,
                    manager = manager,
                    onQuoteClick = { dbId, number -> openQuote(dbId, number) },
                    onNickClick = { n -> navController.navigate(Routes.nickDetail(n)) },
                    onBack = { navController.popBackStack() },
                    showNumber = showNumber,
                    showRating = showRating
                )
            }
            composable(Routes.NICKS) {
                NickLeaderboardScreen(
                    manager = manager,
                    onNickClick = { nick -> navController.navigate(Routes.nickDetail(nick)) },
                    onCompareClick = { navController.navigate(Routes.NICK_VS_NICK) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.RANDOM) {
                CompositionLocalProvider(
                    LocalRowSwipeConfig provides RowSwipeConfig(
                        left = if (swipeRandomEnabled) swipeLeft else SwipeAction.NONE,
                        right = if (swipeRandomEnabled) swipeRight else SwipeAction.NONE
                    )
                ) {
                    RandomScreen(
                        vm = randomVm,
                        catVm = catVm,
                        manager = manager,
                        onQuoteClick = { dbId, number -> openQuote(dbId, number) },
                        showNumber = showNumber,
                        showRating = showRating,
                        basket = basket,
                        onToggleBasket = ::onToggleBasket,
                        shakeEnabled = shakeToShuffle,
                        onDeckClick = { navController.navigate(Routes.DECK) },
                        onRatingDeckClick = { navController.navigate(Routes.RATING_DECK) },
                        onReadAloudSet = { playlistVm.start(randomVm.quotes.value) },
                        onReadAloudQuote = { playlistVm.start(listOf(it)) },
                        onFlashcardsClick = { navController.navigate(Routes.FLASHCARDS) },
                        onAddToPlaylist = { playlistVm.append(it) },
                        onRadioClick = { navController.navigate(Routes.RADIO) },
                        // v1.67: Export video — the current set's missing
                        // quotes join the export queue, then Export opens
                        // with the basket scope and MP4 preselected.
                        onExportVideo = {
                            scope.launch {
                                for (q in randomVm.quotes.value) {
                                    mainVm.toggleBasketIfAbsent(q.dbId, q.quote.number)
                                }
                                exportInitialScope = ExportScope.BASKET
                                exportInitialFormat = ExportFormat.MP4
                                navController.navigate(Routes.EXPORT)
                            }
                        },
                        onNickClick = { nick -> navController.navigate(Routes.nickDetail(nick)) }
                    )
                }
            }
            composable(Routes.RADIO) {
                RadioScreen(
                    manager = manager,
                    mainVm = mainVm,
                    playlistVm = playlistVm,
                    onBack = { navController.popBackStack() },
                    onOpenQuote = { dbId, number -> openQuote(dbId, number) }
                )
            }
            composable(Routes.DECK) {
                DeckScreen(
                    randomVm = randomVm,
                    manager = manager,
                    onQuoteClick = { dbId, number -> openQuote(dbId, number) },
                    onExit = { navController.popBackStack() },
                    showNumber = showNumber,
                    showRating = showRating
                )
            }
            composable(Routes.RATING_DECK) {
                RatingDeckScreen(
                    randomVm = randomVm,
                    onQuoteClick = { dbId, number -> openQuote(dbId, number) },
                    onExit = { navController.popBackStack() }
                )
            }
            composable(Routes.DUPLICATES) {
                DuplicateScreen(
                    manager = manager,
                    onQuoteClick = { dbId, number -> openQuote(dbId, number) }
                )
            }
            composable(Routes.HELP) {
                HelpScreen()
            }
            composable(Routes.STATS) {
                StatsScreen(
                    manager = manager,
                    onNickClick = { nick -> navController.navigate(Routes.nickDetail(nick)) }
                )
            }
            composable(Routes.FAVORITES) {
                val vm: FavoritesViewModel =
                    viewModel(factory = vmFactory { FavoritesViewModel(manager) })
                FavoritesScreen(vm = vm, manager = manager, onQuoteClick = { number -> openQuote(manager.currentDbId.value, number) }, onSourcedQuoteClick = { id, number -> openQuote(id, number) }, showNumber = showNumber, showRating = showRating, basket = basket, onToggleBasket = ::onToggleBasket, onReadAloud = { playlistVm.start(it) }, onAddToPlaylist = { playlistVm.append(it) }, onReadAloudQuote = { playlistVm.start(listOf(it)) }, initialFolder = folderInbox, onInitialFolderConsumed = { folderInbox = null }, onNickClick = { nick -> navController.navigate(Routes.nickDetail(nick)) })
            }
            composable(Routes.HISTORY) {
                val vm: HistoryViewModel =
                    viewModel(factory = vmFactory { HistoryViewModel(manager) })
                HistoryScreen(vm = vm, manager = manager, onQuoteClick = { number -> openQuote(manager.currentDbId.value, number) }, onSourcedQuoteClick = { id, number -> openQuote(id, number) }, showNumber = showNumber, showRating = showRating, basket = basket, onToggleBasket = ::onToggleBasket, onAddToPlaylist = { playlistVm.append(it) }, onReadAloudQuote = { playlistVm.start(listOf(it)) }, onNickClick = { nick -> navController.navigate(Routes.nickDetail(nick)) })
            }
            composable(Routes.TOP100) {
                val vm: TopBottomViewModel =
                    viewModel(factory = vmFactory { TopBottomViewModel(manager, TopBottomMode.TOP) })
                TopBottomScreen(vm = vm, catVm = catVm, manager = manager, onQuoteClick = { dbId, number -> openQuote(dbId, number) }, showNumber = showNumber, showRating = showRating, basket = basket, onToggleBasket = ::onToggleBasket, onAddToPlaylist = { playlistVm.append(it) }, onReadAloudQuote = { playlistVm.start(listOf(it)) }, onReadAllAloud = { playlistVm.start(it) })
            }
            composable(Routes.BOTTOM100) {
                val vm: TopBottomViewModel =
                    viewModel(factory = vmFactory { TopBottomViewModel(manager, TopBottomMode.BOTTOM) })
                TopBottomScreen(vm = vm, catVm = catVm, manager = manager, onQuoteClick = { dbId, number -> openQuote(dbId, number) }, showNumber = showNumber, showRating = showRating, basket = basket, onToggleBasket = ::onToggleBasket, onAddToPlaylist = { playlistVm.append(it) }, onReadAloudQuote = { playlistVm.start(listOf(it)) }, onReadAllAloud = { playlistVm.start(it) })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    vm = mainVm,
                    onMyQuotesClick = { navController.navigate(Routes.MY_QUOTES) },
                    onStatsClick = { navController.navigate(Routes.STATS) },
                    onDbOrderClick = { navController.navigate(Routes.DB_ORDER) },
                    onDuplicatesClick = { navController.navigate(Routes.DUPLICATES) },
                    onBackupRestore = { navController.navigate(Routes.BACKUP_RESTORE) },
                    // v1.59: theme preview + verify databases.
                    onPreviewThemesClick = { navController.navigate(ThemePreviewRoute) },
                    onVerifyDatabasesClick = { navController.navigate(VERIFY_DATABASES_ROUTE) },
                    onVoiceRecordingsClick = { navController.navigate(Routes.VOICE_RECORDINGS) },
                    onSpeakerVoicesClick = { navController.navigate(Routes.SPEAKER_VOICES) },
                    // v1.83: plugin packs.
                    onPluginsClick = { navController.navigate(Routes.PLUGINS) },
                    onLayoutEditorClick = { navController.navigate(Routes.LAYOUT_EDITOR) }
                )
            }
            // v1.83: plugin pack management.
            composable(Routes.PLUGINS) {
                PluginsScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            // v1.85: custom HTML/CSS style editor.
            composable(Routes.LAYOUT_EDITOR) {
                LayoutEditorScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            // v1.59: live theme preview screen.
            composable(ThemePreviewRoute) {
                ThemePreviewScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            // v1.59: verify databases repair tool.
            composable(VERIFY_DATABASES_ROUTE) {
                VerifyDatabasesScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.CONTENT_FILTER) {
                BlockedWordsScreen(
                    vm = mainVm,
                    onHiddenQuotesClick = { navController.navigate(Routes.HIDDEN_QUOTES) },
                    onMutedNicksClick = { navController.navigate(Routes.MUTED_NICKS) }
                )
            }
            composable(Routes.MUTED_NICKS) {
                MutedNicksScreen(vm = mainVm)
            }
            composable(Routes.HIDDEN_QUOTES) {
                HiddenQuotesScreen(
                    vm = mainVm,
                    onQuoteClick = { dbId, number -> openQuote(dbId, number) }
                )
            }
            composable(Routes.DB_ORDER) {
                DatabaseOrderScreen(vm = mainVm)
            }
            composable(Routes.GAME) {
                val gameVm: GameViewModel =
                    viewModel(factory = vmFactory { GameViewModel(manager) })
                GameScreen(vm = gameVm)
            }
            composable(Routes.GAMES) {
                val dcDone by manager.dcLastDate.collectAsStateWithLifecycle()
                val dcStreak by manager.dcStreak.collectAsStateWithLifecycle()
                val dcBest by manager.dcBestScore.collectAsStateWithLifecycle()
                val todayStr = java.time.LocalDate.now().toString()
                val quoteBattleOn by manager.quoteBattleEnabled.collectAsStateWithLifecycle()
                GamesScreen(
                    quoteBattleEnabled = quoteBattleOn,
                    onHigherOrLower = { navController.navigate(Routes.GAME) },
                    onFlashcards = { navController.navigate(Routes.FLASHCARDS) },
                    onWhoSaid = { navController.navigate(Routes.WHO_SAID) },
                    onRatingGuess = { navController.navigate(Routes.RATING_GUESS) },
                    onQuoteBattle = { navController.navigate(Routes.BATTLE) },
                    onDailyChallenge = { navController.navigate(Routes.DAILY_CHALLENGE) },
                    onFillBlank = { navController.navigate(Routes.FILL_BLANK) },
                    onOddOneOut = { navController.navigate(Routes.ODD_ONE_OUT) },
                    onSortRating = { navController.navigate(Routes.SORT_RATING) },
                    onGuessDb = { navController.navigate(Routes.GUESS_DB) },
                    onSpeedRound = { navController.navigate(Routes.SPEED_ROUND) },
                    onTournament = { navController.navigate(Routes.TOURNAMENT) },
                    onPassAndPlay = { navController.navigate(Routes.PASS_AND_PLAY) },
                    onCompleteQuote = { navController.navigate(Routes.COMPLETE_QUOTE) },
                    onTrueFalse = { navController.navigate(Routes.TRUE_FALSE) },
                    onTop100Game = { navController.navigate(Routes.TOP_100_GAME) },
                    onMemoryMatch = { navController.navigate(Routes.MEMORY_MATCH) },
                    onSurvivor = { navController.navigate(Routes.SURVIVOR) },
                    onLineScramble = { navController.navigate(Routes.LINE_SCRAMBLE) },
                    onBlitzWhoSaidIt = { navController.navigate(Routes.BLITZ_WHO_SAID_IT) },
                    onRatingGuessSlider = { navController.navigate(Routes.RATING_GUESS_SLIDER) },
                    dailySubtitle = if (dcDone == todayStr)
                        "Done today • streak $dcStreak • best $dcBest"
                    else "5 rounds, one scored attempt per day" +
                        if (dcStreak > 0) " • streak $dcStreak" else "",
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.DAILY_CHALLENGE) {
                val dcVm: DailyChallengeViewModel =
                    viewModel(factory = vmFactory { DailyChallengeViewModel(manager) })
                DailyChallengeScreen(
                    vm = dcVm,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.BATTLE) {
                val battleStore = remember {
                    BattleStore(navController.context.applicationContext)
                }
                QuoteBattleScreen(
                    manager = manager,
                    battleStore = battleStore,
                    onQuoteClick = { dbId, number -> openQuote(dbId, number) },
                    onBack = { navController.popBackStack() },
                    // v1.66: Tournament game lives under Quote Arena.
                    onTournament = { navController.navigate(Routes.TOURNAMENT) }
                )
            }
            composable(Routes.WHO_SAID) {
                val whoVm: WhoSaidViewModel =
                    viewModel(factory = vmFactory { WhoSaidViewModel(manager) })
                WhoSaidScreen(vm = whoVm)
            }
            composable(Routes.RATING_GUESS) {
                val rgVm: RatingGuessViewModel =
                    viewModel(factory = vmFactory { RatingGuessViewModel(manager) })
                RatingGuessScreen(vm = rgVm)
            }
            composable(Routes.FILL_BLANK) {
                FillBlankGameScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ODD_ONE_OUT) {
                OddOneOutGameScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SORT_RATING) {
                SortByRatingGameScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.GUESS_DB) {
                GuessDbGameScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SPEED_ROUND) {
                SpeedRoundGameScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.TOURNAMENT) {
                TournamentGameScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.PASS_AND_PLAY) {
                PassAndPlayGameScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.COMPLETE_QUOTE) {
                CompleteQuoteGameScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.TRUE_FALSE) {
                TrueFalseGameScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.TOP_100_GAME) {
                Top100GameScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.MEMORY_MATCH) {
                MemoryMatchGameScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SURVIVOR) {
                SurvivorGameScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.LINE_SCRAMBLE) {
                LineScrambleGameScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.BLITZ_WHO_SAID_IT) {
                BlitzWhoSaidItGameScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.RATING_GUESS_SLIDER) {
                RatingGuessGameScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.NICK_VS_NICK) {
                NickVsNickScreen(
                    manager = manager,
                    onQuoteClick = { dbId, number -> openQuote(dbId, number) },
                    onBack = { navController.popBackStack() },
                    showNumber = showNumber,
                    showRating = showRating,
                    onNickClick = { nick -> navController.navigate(Routes.nickDetail(nick)) }
                )
            }
            composable(Routes.UP_NEXT) {
                UpNextScreen(
                    manager = manager,
                    onQuoteClick = { dbId, number -> openQuote(dbId, number) },
                    onBack = { navController.popBackStack() }
                )
            }
            // v1.61.20: saved voice recordings management.
            composable(Routes.VOICE_RECORDINGS) {
                VoiceRecordingsScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() },
                    onManageQuote = { dbId, number ->
                        voiceManageKey = dbId to number
                    }
                )
            }
            composable(Routes.SPEAKER_VOICES) {
                SpeakerVoicesScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.BACKUP_RESTORE) {
                BackupScreen(
                    manager = manager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.EXPORT_HUB) {
                ExportHubScreen(
                    queueCount = basketCount,
                    onQueueClick = { navController.navigate(Routes.BASKET) },
                    onExportClick = {
                        exportInitialScope = ExportScope.ALL
                        exportInitialFormat = ExportFormat.TXT
                        navController.navigate(Routes.EXPORT)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.NOTES) {
                val notesVm: NotesViewModel =
                    viewModel(factory = vmFactory { NotesViewModel(manager) })
                NotesScreen(
                    vm = notesVm,
                    onQuoteClick = { dbId, number -> openQuote(dbId, number) }
                )
            }
            composable(Routes.FLASHCARDS) {
                val flashVm: FlashcardViewModel =
                    viewModel(factory = vmFactory { FlashcardViewModel(manager) })
                FlashcardScreen(
                    vm = flashVm,
                    fetchBatch = { randomVm.fetchRandomBatch(60) },
                    catVm = catVm
                )
            }
            // v1.53 ambient mode: fullscreen rotating quotes.
            composable(Routes.AMBIENT) {
                AmbientScreen(
                    manager = manager,
                    onClose = { navController.popBackStack() }
                )
            }
            // v1.53 dedicated On-this-day screen.
            composable(Routes.ON_THIS_DAY) {
                OnThisDayScreen(
                    manager = manager,
                    showNumber = showNumber,
                    showRating = showRating,
                    onQuoteClick = { dbId, number -> openQuote(dbId, number) }
                )
            }
            composable(Routes.THEMES) {
                ThemesScreen(vm = mainVm)
            }
            composable(Routes.EXPORT) {
                ExportScreen(vm = mainVm, catVm = catVm, initialScope = exportInitialScope, initialFormat = exportInitialFormat)
            }
            composable(Routes.BASKET) {
                BasketScreen(
                    vm = mainVm,
                    onExportClick = {
                        exportInitialScope = ExportScope.BASKET
                        exportInitialFormat = ExportFormat.TXT
                        navController.navigate(Routes.EXPORT)
                    },
                    onQuoteClick = { dbId, number -> openQuote(dbId, number) }
                )
            }
            // v1.66: user quote lists ("My Collections").
            composable(Routes.QUOTE_LISTS) {
                QuoteListsScreen(
                    manager = manager,
                    listStore = quoteListStore,
                    onQuoteClick = { dbId, number -> openQuote(dbId, number) },
                    onPlay = { quotes -> playlistVm.start(quotes) },
                    onExportList = { list ->
                        scope.launch {
                            for (item in list.items) {
                                mainVm.toggleBasketIfAbsent(item.dbId, item.number)
                            }
                            exportInitialScope = ExportScope.BASKET
                            exportInitialFormat = ExportFormat.TXT
                            navController.navigate(Routes.EXPORT)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.CATEGORIES) {
                CategoriesScreen(
                    catVm = catVm,
                    onBulkCategorize = { navController.navigate(Routes.BULK_CATEGORIZE) },
                    onShareCategory = { cat ->
                        scope.launch {
                            val quotes = kotlinx.coroutines.withContext(
                                kotlinx.coroutines.Dispatchers.IO
                            ) { mainVm.getQuotesForCategory(cat.id) }
                            shareCollection(
                                context,
                                QuoteCollection(
                                    name = cat.name,
                                    source = "category",
                                    quotes = quotes.map {
                                        CollectionQuote(it.number, it.text, it.rating)
                                    }
                                )
                            )
                        }
                    },
                    onReadAllAloud = { playlistVm.start(it) }
                )
            }
            composable(Routes.BULK_CATEGORIZE) {
                val bulkVm: BulkCategorizeViewModel =
                    viewModel(factory = vmFactory { BulkCategorizeViewModel(manager) })
                BulkCategorizeScreen(
                    vm = bulkVm,
                    onBack = { navController.popBackStack() },
                    onOpenQuote = { dbId, number -> openQuote(dbId, number) }
                )
            }
            composable(
                route = Routes.DETAIL_PATTERN,
                arguments = listOf(navArgument("number") { type = NavType.IntType })
            ) { backStackEntry ->
                val number = backStackEntry.arguments?.getInt("number") ?: return@composable
                // v1.56: openQuote seeds the quote's real source database
                // here so the detail doesn't need a global switch to find it.
                val seedDbId = backStackEntry.savedStateHandle.get<String>("seedDbId")
                val vm: DetailViewModel =
                    viewModel(factory = vmFactory { DetailViewModel(manager, number, seedDbId) })
                // Real source database of the on-screen quote (never "all"),
                // so the detail header always names the true source DB.
                val detailDbId by vm.positionDbId.collectAsStateWithLifecycle()
                // v1.56 (Bug A): a deep-linked quote number that doesn't
                // exist must never replace the list with the full-screen
                // error — bounce back to the list with a snackbar instead.
                // (Follow-switch misses go through onFollowMissed; explicit
                // opens always exist, so only unseeded deep links land here.)
                val detailQuote by vm.quote.collectAsStateWithLifecycle()
                val detailLoaded by vm.loaded.collectAsStateWithLifecycle()
                LaunchedEffect(detailLoaded, detailQuote, seedDbId) {
                    if (seedDbId == null && detailLoaded && detailQuote == null) {
                        navController.popBackStack()
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Quote #$number doesn't exist in " +
                                    "${manager.shortLabel(detailDbId)}."
                            )
                        }
                    }
                }
                DetailScreen(
                    vm = vm,
                    catVm = catVm,
                    manager = manager,
                    showUrl = showUrl,
                    sourceLabel = manager.shortLabel(detailDbId),
                    dbId = detailDbId,
                    basket = basket,
                    onToggleBasket = ::onToggleBasket,
                    onNickClick = { nick -> navController.navigate(Routes.nickDetail(nick)) },
                    // v1.66: user quote lists ("My Collections").
                    quoteListStore = quoteListStore,
                    // v1.56 (Bug A): a database switch that lands on a
                    // missing quote number bounces back to the list with a
                    // snackbar instead of dead-ending on the full-screen
                    // "doesn't exist" error.
                    onFollowMissed = { missedDbId, missedNumber ->
                        navController.popBackStack()
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Quote #$missedNumber doesn't exist in " +
                                    "${manager.shortLabel(missedDbId)}."
                            )
                        }
                    }
                )
            }
            composable(Routes.OCR_IMPORT) {
                val vm: MyQuotesViewModel =
                    viewModel(factory = vmFactory { MyQuotesViewModel(manager) })
                OcrImportScreen(
                    myQuotesVm = vm,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.MY_QUOTES) {
                val vm: MyQuotesViewModel =
                    viewModel(factory = vmFactory { MyQuotesViewModel(manager) })
                MyQuotesScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    inboxText = inboxText,
                    onInboxTextConsumed = { inboxText = null }
                )
            }
                } // NavHost
            } // MainNavHost

            @Composable
            fun DetailPane(modifier: Modifier = Modifier) {
                val sel = paneSelection
                if (sel == null) {
                    // The weight modifier MUST be applied here: without it
                    // this unweighted EmptyState (fillMaxSize) would consume
                    // the whole Row and squeeze the list pane to zero width.
                    com.quotebrowser.app.ui.components.EmptyState(
                        "Pick a quote on the left to read it here.",
                        modifier = modifier
                    )
                } else {
                    val paneVm: DetailViewModel = viewModel(
                        key = "detail-pane",
                        factory = vmFactory { DetailViewModel(manager, sel.second) }
                    )
                    LaunchedEffect(sel) { paneVm.showQuote(sel.first, sel.second) }
                    // Real source database of the on-screen quote (never "all").
                    val detailDbId by paneVm.positionDbId.collectAsStateWithLifecycle()
                    DetailScreen(
                        vm = paneVm,
                        catVm = catVm,
                        manager = manager,
                        showUrl = showUrl,
                        sourceLabel = manager.shortLabel(detailDbId),
                        dbId = detailDbId,
                        basket = basket,
                        onToggleBasket = ::onToggleBasket,
                        onNickClick = { nick -> navController.navigate(Routes.nickDetail(nick)) },
                        modifier = modifier,
                        // v1.66: user quote lists ("My Collections").
                        quoteListStore = quoteListStore,
                        // v1.56 (Bug A): clear the stale pane selection and
                        // tell the user, instead of showing the full-screen
                        // "doesn't exist" error in the detail pane.
                        onFollowMissed = { missedDbId, missedNumber ->
                            paneSelection = null
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Quote #$missedNumber doesn't exist in " +
                                        "${manager.shortLabel(missedDbId)}."
                                )
                            }
                        }
                    )
                }
            }

            if (wide) {
                Row(Modifier.fillMaxSize()) {
                    MainNavHost(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    VerticalDivider()
                    DetailPane(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            } else {
                MainNavHost(modifier = Modifier.fillMaxSize())
            }
        }
        }
    }
    } // LocalRowSwipeConfig + LocalRowSwipeHandler

    // v1.61.20: per-quote voice-recording management (long-press the card
    // voice icon, or tap a row on the Voice recordings screen).
    val manageKey = voiceManageKey
    if (manageKey != null) {
        VoiceManageDialog(
            dbId = manageKey.first,
            number = manageKey.second,
            manager = manager,
            onDismiss = { voiceManageKey = null }
        )
    }

    // Random sources picker, opened from the top-bar control on the
    // Random tab. Uses the shared multi-select database dialog, the same
    // one the Search scope chip uses.
    if (showRandomSources) {
        DatabaseMultiSelectDialog(
            title = "Random sources",
            databases = randomDbs,
            selected = randomSourceIds,
            onConfirm = { randomVm.updateSources(it) },
            onDismiss = { showRandomSources = false }
        )
    }
}
