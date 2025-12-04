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
    const val GEL_LIST = "gel"
    const val NAIL_LIST = "nail"
    const val GEL_ADD = "gel_add"
    const val GEL_DETAIL = "gel_detail/{gelId}?fromLabel={fromLabel}"
    const val GEL_EDIT = "gel_edit/{gelId}"
    const val NAIL_ADD = "nail_add"
    const val NAIL_DETAIL = "nail_detail/{nailId}?fromLabel={fromLabel}"
    const val NAIL_EDIT = "nail_edit/{nailId}"

    fun gelDetail(gelId: Long, fromLabel: Boolean = false) = "gel_detail/$gelId?fromLabel=$fromLabel"
    fun gelEdit(gelId: Long) = "gel_edit/$gelId"
    fun nailDetail(nailId: Long, fromLabel: Boolean = false) = "nail_detail/$nailId?fromLabel=$fromLabel"
    fun nailEdit(nailId: Long) = "nail_edit/$nailId"
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
    val currentTitle = when {
        currentRoute == BottomNavItem.Gel.route -> "Gel"
        currentRoute == BottomNavItem.Nail.route -> "Nail"
        currentRoute == Routes.GEL_ADD -> "Add Gel"
        currentRoute == Routes.GEL_DETAIL -> "Gel Details"
        currentRoute == Routes.GEL_EDIT -> "Edit Gel"
        currentRoute == Routes.NAIL_ADD -> "Add Nail Style"
        currentRoute == Routes.NAIL_DETAIL -> "Nail Details"
        currentRoute == Routes.NAIL_EDIT -> "Edit Nail Style"
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
                    navArgument("gelId") { type = NavType.LongType },
                    navArgument("fromLabel") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val gelId = backStackEntry.arguments?.getLong("gelId") ?: 0L
                val fromLabel = backStackEntry.arguments?.getBoolean("fromLabel") ?: false
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
                arguments = listOf(navArgument("gelId") { type = NavType.LongType })
            ) { backStackEntry ->
                val gelId = backStackEntry.arguments?.getLong("gelId") ?: 0L
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
                    navArgument("nailId") { type = NavType.LongType },
                    navArgument("fromLabel") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val nailId = backStackEntry.arguments?.getLong("nailId") ?: 0L
                val fromLabel = backStackEntry.arguments?.getBoolean("fromLabel") ?: false
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
                arguments = listOf(navArgument("nailId") { type = NavType.LongType })
            ) { backStackEntry ->
                val nailId = backStackEntry.arguments?.getLong("nailId") ?: 0L
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