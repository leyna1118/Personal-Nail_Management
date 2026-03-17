package com.leyna.nailmanagement.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.leyna.nailmanagement.R
import com.leyna.nailmanagement.data.database.AppDatabase
import com.leyna.nailmanagement.data.repository.BackupRepository
import com.leyna.nailmanagement.ui.screens.EditGelContent
import com.leyna.nailmanagement.ui.screens.EditNailContent
import com.leyna.nailmanagement.ui.screens.GelDetailContent
import com.leyna.nailmanagement.ui.screens.GelInventoryContent
import com.leyna.nailmanagement.ui.screens.GelScreenContent
import com.leyna.nailmanagement.ui.screens.GelSuggestions
import com.leyna.nailmanagement.ui.screens.NailDetailContent
import com.leyna.nailmanagement.ui.screens.NailScreenContent
import com.leyna.nailmanagement.ui.screens.SettingsScreenContent
import com.leyna.nailmanagement.ui.viewmodel.GelViewModel
import com.leyna.nailmanagement.ui.viewmodel.NailStyleViewModel
import com.leyna.nailmanagement.ui.viewmodel.ViewModelFactory

object Routes {
    // Argument keys - exposed for use in navArgument and argument extraction
    object Args {
        const val GEL_ID = "gelId"
        const val NAIL_ID = "nailId"
        const val FROM_LABEL = "fromLabel"
    }

    // Base route names
    private object Base {
        const val GEL_ADD = "gel_add"
        const val GEL_DETAIL = "gel_detail"
        const val GEL_EDIT = "gel_edit"
        const val GEL_INVENTORY = "gel_inventory"
        const val NAIL_ADD = "nail_add"
        const val NAIL_DETAIL = "nail_detail"
        const val NAIL_EDIT = "nail_edit"
    }

    // Route patterns for NavHost composable registration
    const val GEL_ADD = Base.GEL_ADD
    const val GEL_DETAIL = "${Base.GEL_DETAIL}/{${Args.GEL_ID}}?${Args.FROM_LABEL}={${Args.FROM_LABEL}}"
    const val GEL_EDIT = "${Base.GEL_EDIT}/{${Args.GEL_ID}}"
    const val GEL_INVENTORY = "${Base.GEL_INVENTORY}/{${Args.GEL_ID}}"
    const val NAIL_ADD = Base.NAIL_ADD
    const val NAIL_DETAIL = "${Base.NAIL_DETAIL}/{${Args.NAIL_ID}}?${Args.FROM_LABEL}={${Args.FROM_LABEL}}"
    const val NAIL_EDIT = "${Base.NAIL_EDIT}/{${Args.NAIL_ID}}"

    // Route builders for navigation
    fun gelDetail(gelId: Long, fromLabel: Boolean = false) =
        "${Base.GEL_DETAIL}/$gelId?${Args.FROM_LABEL}=$fromLabel"

    fun gelEdit(gelId: Long) =
        "${Base.GEL_EDIT}/$gelId"

    fun gelInventory(gelId: Long) =
        "${Base.GEL_INVENTORY}/$gelId"

    fun nailDetail(nailId: Long, fromLabel: Boolean = false) =
        "${Base.NAIL_DETAIL}/$nailId?${Args.FROM_LABEL}=$fromLabel"

    fun nailEdit(nailId: Long) =
        "${Base.NAIL_EDIT}/$nailId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    themePreferences: com.leyna.nailmanagement.ui.theme.ThemePreferences,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModelFactory = ViewModelFactory(context)
    val gelViewModel: GelViewModel = viewModel(factory = viewModelFactory)
    val nailStyleViewModel: NailStyleViewModel = viewModel(factory = viewModelFactory)

    val navController = rememberNavController()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val database = AppDatabase.getDatabase(context)
    val backupRepository = remember { BackupRepository(context, database.gelDao(), database.nailStyleDao(), database.gelInventoryDao()) }

    val bottomNavItems = listOf(
        BottomNavItem.Gel,
        BottomNavItem.Nail,
        BottomNavItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // Determine if we're on a main tab screen (show bottom bar)
    val isMainScreen = currentRoute in listOf(BottomNavItem.Gel.route, BottomNavItem.Nail.route, BottomNavItem.Settings.route)

    // Determine title based on current route
    val currentTitle = when (currentRoute) {
        BottomNavItem.Gel.route -> stringResource(R.string.nav_gel)
        BottomNavItem.Nail.route -> stringResource(R.string.nav_nail)
        BottomNavItem.Settings.route -> stringResource(R.string.nav_settings)
        Routes.GEL_ADD -> stringResource(R.string.title_add_gel)
        Routes.GEL_DETAIL -> stringResource(R.string.title_gel_details)
        Routes.GEL_EDIT -> stringResource(R.string.title_edit_gel)
        Routes.GEL_INVENTORY -> stringResource(R.string.title_inventory)
        Routes.NAIL_ADD -> stringResource(R.string.title_add_nail_style)
        Routes.NAIL_DETAIL -> stringResource(R.string.title_nail_details)
        Routes.NAIL_EDIT -> stringResource(R.string.title_edit_nail_style)
        else -> stringResource(R.string.nav_gel)
    }

    // Selection state (lifted from list screens)
    var gelSelectedIds by remember { mutableStateOf(setOf<Long>()) }
    var nailSelectedIds by remember { mutableStateOf(setOf<Long>()) }
    var selectionModeActive by remember { mutableStateOf(false) }
    val isSelectionMode = selectionModeActive || when (currentRoute) {
        BottomNavItem.Gel.route -> gelSelectedIds.isNotEmpty()
        BottomNavItem.Nail.route -> nailSelectedIds.isNotEmpty()
        else -> false
    }

    // Search state
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Collect data from ViewModels
    val gels by gelViewModel.allGels.collectAsState()
    val gelsWithNailStyles by nailStyleViewModel.allGelsWithNailStyles.collectAsState()
    val nailStylesWithGels by nailStyleViewModel.allNailStylesWithGels.collectAsState()

    // Autocomplete suggestions
    val brands by gelViewModel.distinctBrands.collectAsState()
    val seriesList by gelViewModel.distinctSeries.collectAsState()
    val categories by gelViewModel.distinctCategories.collectAsState()
    val stores by gelViewModel.distinctStores.collectAsState()
    val gelSuggestions = GelSuggestions(brands, seriesList, categories, stores)

    // Filtered data based on search
    val filteredGelsWithNailStyles = remember(gelsWithNailStyles, searchQuery, isSearchActive) {
        if (!isSearchActive || searchQuery.isBlank()) gelsWithNailStyles
        else {
            val q = searchQuery.trim().lowercase()
            gelsWithNailStyles.filter { item ->
                item.gel.name.lowercase().contains(q) ||
                    item.gel.colorCode.lowercase().contains(q) ||
                    item.nailStyles.any { it.name.lowercase().contains(q) }
            }
        }
    }
    val filteredNailStylesWithGels = remember(nailStylesWithGels, searchQuery, isSearchActive) {
        if (!isSearchActive || searchQuery.isBlank()) nailStylesWithGels
        else {
            val q = searchQuery.trim().lowercase()
            nailStylesWithGels.filter { item ->
                item.nailStyle.name.lowercase().contains(q) ||
                    item.gels.any { it.name.lowercase().contains(q) }
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive && isMainScreen && currentRoute != BottomNavItem.Settings.route) {
                        val focusRequester = remember { FocusRequester() }
                        LaunchedEffect(Unit) { focusRequester.requestFocus() }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.search_placeholder),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    } else {
                        Text(text = currentTitle)
                    }
                },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    if (isSearchActive && isMainScreen && currentRoute != BottomNavItem.Settings.route) {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_close_search)
                            )
                        }
                    } else if (!isMainScreen) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                    }
                },
                actions = {
                    if (isMainScreen && currentRoute != BottomNavItem.Settings.route) {
                        if (isSearchActive) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.cd_clear_search)
                                    )
                                }
                            }
                        } else {
                            IconButton(
                                onClick = { isSearchActive = true },
                                enabled = !isSelectionMode
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = stringResource(R.string.cd_search)
                                )
                            }
                            IconButton(
                                onClick = { selectionModeActive = true },
                                enabled = !isSelectionMode
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = stringResource(R.string.cd_select_items)
                                )
                            }
                            IconButton(
                                onClick = {
                                    when (currentRoute) {
                                        BottomNavItem.Gel.route -> {
                                            navController.navigate(Routes.GEL_ADD)
                                        }
                                        BottomNavItem.Nail.route -> {
                                            navController.navigate(Routes.NAIL_ADD)
                                        }
                                    }
                                },
                                enabled = !isSelectionMode
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.cd_add)
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isMainScreen) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val label = stringResource(item.labelResId)
                        NavigationBarItem(
                            icon = { Icon(imageVector = item.icon, contentDescription = label) },
                            label = { Text(text = label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            ),
                            onClick = {
                                gelSelectedIds = emptySet()
                                nailSelectedIds = emptySet()
                                selectionModeActive = false
                                isSearchActive = false
                                searchQuery = ""
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Gel.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            popEnterTransition = { fadeIn(animationSpec = tween(200)) },
            popExitTransition = { fadeOut(animationSpec = tween(200)) },
        ) {
            // Gel routes
            composable(route = BottomNavItem.Gel.route) {
                GelScreenContent(
                    gelsWithNailStyles = filteredGelsWithNailStyles,
                    onGelClick = { gelWithNailStyles ->
                        navController.navigate(Routes.gelDetail(gelWithNailStyles.gel.id))
                    },
                    selectedIds = gelSelectedIds,
                    onSelectedIdsChange = {
                        gelSelectedIds = it
                        if (it.isNotEmpty()) selectionModeActive = true
                    },
                    isSelectionMode = isSelectionMode,
                    onDeleteGels = { ids ->
                        gelViewModel.deleteGels(ids)
                        gelSelectedIds = emptySet()
                        selectionModeActive = false
                    },
                    onExitSelectionMode = {
                        gelSelectedIds = emptySet()
                        selectionModeActive = false
                    },
                    isSearchActive = isSearchActive,
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(route = Routes.GEL_ADD) {
                EditGelContent(
                    gel = null,
                    onSave = { name, price, colorCode, imageUri, _, brand, series, category, store, storeNote, notes ->
                        gelViewModel.insertGel(name, price, colorCode, imageUri, brand, series, category, store, storeNote, notes)
                        navController.popBackStack()
                    },
                    suggestions = gelSuggestions,
                    onLookupStoreNote = { gelViewModel.getStoreNoteByStore(it) },
                    onUpdateStoreNoteForAll = { s, n -> gelViewModel.updateStoreNoteForStore(s, n) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(
                route = Routes.GEL_DETAIL,
                arguments = listOf(
                    navArgument(Routes.Args.GEL_ID) { type = NavType.LongType },
                    navArgument(Routes.Args.FROM_LABEL) { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val gelId = backStackEntry.arguments?.getLong(Routes.Args.GEL_ID) ?: 0L
                val fromLabel = backStackEntry.arguments?.getBoolean(Routes.Args.FROM_LABEL) ?: false
                val gelWithNailStyles by nailStyleViewModel.getGelWithNailStylesById(gelId)
                    .collectAsState(initial = null)

                gelWithNailStyles?.let { gelItem ->
                    GelDetailContent(
                        gelWithNailStyles = gelItem,
                        onEditClick = {
                            navController.navigate(Routes.gelEdit(gelId))
                        },
                        onDeleteClick = {
                            gelViewModel.deleteGels(listOf(gelId))
                            navController.popBackStack()
                        },
                        onNailStyleClick = { nailStyleId ->
                            navController.navigate(Routes.nailDetail(nailStyleId, fromLabel = true))
                        },
                        onInventoryClick = {
                            navController.navigate(Routes.gelInventory(gelId))
                        },
                        showEditButton = !fromLabel,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: run {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            composable(
                route = Routes.GEL_EDIT,
                arguments = listOf(navArgument(Routes.Args.GEL_ID) { type = NavType.LongType })
            ) { backStackEntry ->
                val gelId = backStackEntry.arguments?.getLong(Routes.Args.GEL_ID) ?: 0L
                val gel by gelViewModel.getGelById(gelId).collectAsState(initial = null)

                gel?.let { gelItem ->
                    EditGelContent(
                        gel = gelItem,
                        onSave = { name, price, colorCode, imageUri, existingImagePath, brand, series, category, store, storeNote, notes ->
                            gelViewModel.updateGel(gelId, name, price, colorCode, imageUri, existingImagePath, brand, series, category, store, storeNote, notes)
                            navController.popBackStack()
                        },
                        suggestions = gelSuggestions,
                        onLookupStoreNote = { gelViewModel.getStoreNoteByStore(it) },
                        onUpdateStoreNoteForAll = { s, n -> gelViewModel.updateStoreNoteForStore(s, n) },
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: run {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            composable(
                route = Routes.GEL_INVENTORY,
                arguments = listOf(navArgument(Routes.Args.GEL_ID) { type = NavType.LongType })
            ) { backStackEntry ->
                val gelId = backStackEntry.arguments?.getLong(Routes.Args.GEL_ID) ?: 0L
                val inventoryList by gelViewModel.getInventoryByGelId(gelId).collectAsState(initial = emptyList())

                GelInventoryContent(
                    inventoryList = inventoryList,
                    onAdd = { inventory -> gelViewModel.insertInventory(inventory.copy(gelId = gelId)) },
                    onUpdate = { inventory -> gelViewModel.updateInventory(inventory) },
                    onDelete = { id -> gelViewModel.deleteInventory(id) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Nail routes
            composable(route = BottomNavItem.Nail.route) {
                NailScreenContent(
                    nailStylesWithGels = filteredNailStylesWithGels,
                    onNailStyleClick = { nailStyleWithGels ->
                        navController.navigate(Routes.nailDetail(nailStyleWithGels.nailStyle.id))
                    },
                    selectedIds = nailSelectedIds,
                    onSelectedIdsChange = {
                        nailSelectedIds = it
                        if (it.isNotEmpty()) selectionModeActive = true
                    },
                    isSelectionMode = isSelectionMode,
                    onDeleteNailStyles = { ids ->
                        nailStyleViewModel.deleteNailStyles(ids)
                        nailSelectedIds = emptySet()
                        selectionModeActive = false
                    },
                    onExitSelectionMode = {
                        nailSelectedIds = emptySet()
                        selectionModeActive = false
                    },
                    isSearchActive = isSearchActive,
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(route = Routes.NAIL_ADD) {
                EditNailContent(
                    nailStyleWithGels = null,
                    allGels = gels,
                    onSave = { name, steps, mainImageUri, _ ->
                        nailStyleViewModel.insertNailStyle(name, steps, mainImageUri)
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(
                route = Routes.NAIL_DETAIL,
                arguments = listOf(
                    navArgument(Routes.Args.NAIL_ID) { type = NavType.LongType },
                    navArgument(Routes.Args.FROM_LABEL) { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val nailId = backStackEntry.arguments?.getLong(Routes.Args.NAIL_ID) ?: 0L
                val fromLabel = backStackEntry.arguments?.getBoolean(Routes.Args.FROM_LABEL) ?: false
                val nailStyleWithGels by nailStyleViewModel.getNailStyleWithGelsById(nailId)
                    .collectAsState(initial = null)

                nailStyleWithGels?.let { nailStyle ->
                    NailDetailContent(
                        nailStyleWithGels = nailStyle,
                        allGels = gels,
                        onEditClick = {
                            navController.navigate(Routes.nailEdit(nailId))
                        },
                        onDeleteClick = {
                            nailStyleViewModel.deleteNailStyles(listOf(nailId))
                            navController.popBackStack()
                        },
                        onGelClick = { gelId ->
                            navController.navigate(Routes.gelDetail(gelId, fromLabel = true))
                        },
                        showEditButton = !fromLabel,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: run {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            composable(
                route = Routes.NAIL_EDIT,
                arguments = listOf(navArgument(Routes.Args.NAIL_ID) { type = NavType.LongType })
            ) { backStackEntry ->
                val nailId = backStackEntry.arguments?.getLong(Routes.Args.NAIL_ID) ?: 0L
                val nailStyleWithGels by nailStyleViewModel.getNailStyleWithGelsById(nailId)
                    .collectAsState(initial = null)

                nailStyleWithGels?.let { nailStyle ->
                    EditNailContent(
                        nailStyleWithGels = nailStyle,
                        allGels = gels,
                        onSave = { name, steps, mainImageUri, existingMainImagePath ->
                            nailStyleViewModel.updateNailStyle(nailId, name, steps, mainImageUri, existingMainImagePath)
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: run {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Settings route
            composable(route = BottomNavItem.Settings.route) {
                SettingsScreenContent(
                    backupRepository = backupRepository,
                    themePreferences = themePreferences,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
