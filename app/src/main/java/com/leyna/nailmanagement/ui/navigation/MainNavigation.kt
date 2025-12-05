package com.leyna.nailmanagement.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.leyna.nailmanagement.ui.screens.EditGelContent
import com.leyna.nailmanagement.ui.screens.EditNailContent
import com.leyna.nailmanagement.ui.screens.GelDetailContent
import com.leyna.nailmanagement.ui.screens.GelScreenContent
import com.leyna.nailmanagement.ui.screens.NailDetailContent
import com.leyna.nailmanagement.ui.screens.NailScreenContent
import com.leyna.nailmanagement.ui.viewmodel.GelViewModel
import com.leyna.nailmanagement.ui.viewmodel.NailStyleViewModel

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
        const val NAIL_ADD = "nail_add"
        const val NAIL_DETAIL = "nail_detail"
        const val NAIL_EDIT = "nail_edit"
    }

    // Route patterns for NavHost composable registration
    const val GEL_ADD = Base.GEL_ADD
    const val GEL_DETAIL = "${Base.GEL_DETAIL}/{${Args.GEL_ID}}?${Args.FROM_LABEL}={${Args.FROM_LABEL}}"
    const val GEL_EDIT = "${Base.GEL_EDIT}/{${Args.GEL_ID}}"
    const val NAIL_ADD = Base.NAIL_ADD
    const val NAIL_DETAIL = "${Base.NAIL_DETAIL}/{${Args.NAIL_ID}}?${Args.FROM_LABEL}={${Args.FROM_LABEL}}"
    const val NAIL_EDIT = "${Base.NAIL_EDIT}/{${Args.NAIL_ID}}"

    // Route builders for navigation
    fun gelDetail(gelId: Long, fromLabel: Boolean = false) =
        "${Base.GEL_DETAIL}/$gelId?${Args.FROM_LABEL}=$fromLabel"

    fun gelEdit(gelId: Long) =
        "${Base.GEL_EDIT}/$gelId"

    fun nailDetail(nailId: Long, fromLabel: Boolean = false) =
        "${Base.NAIL_DETAIL}/$nailId?${Args.FROM_LABEL}=$fromLabel"

    fun nailEdit(nailId: Long) =
        "${Base.NAIL_EDIT}/$nailId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    modifier: Modifier = Modifier,
    gelViewModel: GelViewModel = viewModel(),
    nailStyleViewModel: NailStyleViewModel = viewModel()
) {
    val navController = rememberNavController()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val bottomNavItems = listOf(
        BottomNavItem.Gel,
        BottomNavItem.Nail
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // Determine if we're on a main tab screen (show bottom bar)
    val isMainScreen = currentRoute in listOf(BottomNavItem.Gel.route, BottomNavItem.Nail.route)

    // Determine title based on current route
    val currentTitle = when (currentRoute) {
        BottomNavItem.Gel.route -> "Gel"
        BottomNavItem.Nail.route -> "Nail"
        Routes.GEL_ADD -> "Add Gel"
        Routes.GEL_DETAIL -> "Gel Details"
        Routes.GEL_EDIT -> "Edit Gel"
        Routes.NAIL_ADD -> "Add Nail Style"
        Routes.NAIL_DETAIL -> "Nail Details"
        Routes.NAIL_EDIT -> "Edit Nail Style"
        else -> "Gel"
    }

    // Collect data from ViewModels
    val gels by gelViewModel.allGels.collectAsState()
    val gelsWithNailStyles by nailStyleViewModel.allGelsWithNailStyles.collectAsState()
    val nailStylesWithGels by nailStyleViewModel.allNailStylesWithGels.collectAsState()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = currentTitle) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    if (!isMainScreen) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    if (isMainScreen) {
                        IconButton(onClick = { /* Search */ }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
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
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isMainScreen) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                            label = { Text(text = item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
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
            modifier = Modifier.padding(innerPadding)
        ) {
            // Gel routes
            composable(route = BottomNavItem.Gel.route) {
                GelScreenContent(
                    gelsWithNailStyles = gelsWithNailStyles,
                    onGelClick = { gelWithNailStyles ->
                        navController.navigate(Routes.gelDetail(gelWithNailStyles.gel.id))
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(route = Routes.GEL_ADD) {
                EditGelContent(
                    gel = null,
                    onSave = { name, price, colorCode, imageUri, _ ->
                        gelViewModel.insertGel(name, price, colorCode, imageUri)
                        navController.popBackStack()
                    },
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
                        onNailStyleClick = { nailStyleId ->
                            navController.navigate(Routes.nailDetail(nailStyleId, fromLabel = true))
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
                        onSave = { name, price, colorCode, imageUri, existingImagePath ->
                            gelViewModel.updateGel(gelId, name, price, colorCode, imageUri, existingImagePath)
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

            // Nail routes
            composable(route = BottomNavItem.Nail.route) {
                NailScreenContent(
                    nailStylesWithGels = nailStylesWithGels,
                    onNailStyleClick = { nailStyleWithGels ->
                        navController.navigate(Routes.nailDetail(nailStyleWithGels.nailStyle.id))
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(route = Routes.NAIL_ADD) {
                EditNailContent(
                    nailStyleWithGels = null,
                    allGels = gels,
                    onSave = { name, steps, gelIds, mainImageUri, _ ->
                        nailStyleViewModel.insertNailStyle(name, steps, gelIds, mainImageUri)
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
                        onEditClick = {
                            navController.navigate(Routes.nailEdit(nailId))
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
                        onSave = { name, steps, gelIds, mainImageUri, existingMainImagePath ->
                            nailStyleViewModel.updateNailStyle(nailId, name, steps, gelIds, mainImageUri, existingMainImagePath)
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
        }
    }
}