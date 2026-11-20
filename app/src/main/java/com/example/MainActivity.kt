package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.InitialCatalog
import com.example.data.repository.GoogleAuthManager
import com.example.data.repository.StoreRepository
import com.example.ui.components.MsrGlobalHeader
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BonusOffersScreen
import com.example.ui.screens.CategoryPackagesScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OrderConfirmationScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.owner.OwnerDashboardScreen
import com.example.ui.screens.owner.OwnerTab
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.MsrTopUpTheme
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.OwnerViewModel
import com.example.ui.viewmodel.StoreViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.auth.FirebaseUser

object Routes {
    const val HOME = "home"
    const val CATEGORY_PACKAGES = "category_packages/{gameId}/{categoryId}"
    const val BONUS_OFFERS = "bonus_offers"
    const val ORDERS = "orders"
    const val SUPPORT = "support"
    const val ACCOUNT = "account"
    const val CHECKOUT = "checkout"
    const val ORDER_CONFIRMATION = "order_confirmation"
    const val NOTIFICATIONS = "notifications"
    const val OWNER_DASHBOARD = "owner_dashboard"
    const val OWNER_ORDERS = "owner_orders"

    fun categoryPackages(gameId: String, categoryId: String): String =
        "category_packages/$gameId/$categoryId"
}

private enum class BottomNavTab(
    val route: String,
    val label: String,
    val headerTitle: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(
        route = Routes.HOME,
        label = "Home",
        headerTitle = "MSR Game Store",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    ORDERS(
        route = Routes.ORDERS,
        label = "Orders",
        headerTitle = "My Orders & Tracking",
        selectedIcon = Icons.Filled.ReceiptLong,
        unselectedIcon = Icons.Outlined.ReceiptLong
    ),
    SUPPORT(
        route = Routes.SUPPORT,
        label = "Support",
        headerTitle = "Customer Support & WhatsApp",
        selectedIcon = Icons.Filled.HeadsetMic,
        unselectedIcon = Icons.Outlined.HeadsetMic
    ),
    ACCOUNT(
        route = Routes.ACCOUNT,
        label = "My Account",
        headerTitle = "Account, Points & VIP Tier",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.BLACK),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.BLACK)
        )
        setContent {
            MsrTopUpTheme {
                MsrAppRoot()
            }
        }
    }
}

@Composable
private fun MsrAppRoot() {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    val authState by GoogleAuthManager.authStateFlow().collectAsStateWithLifecycle(initialValue = null)

    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
        return
    }

    val currentUser = authState
    if (currentUser == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(currentUser.uid) {
            AuthenticatedStoreRoot(currentUser = currentUser)
        }
    }
}

@Composable
private fun AuthenticatedStoreRoot(currentUser: FirebaseUser) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val repository = remember(currentUser.uid) { StoreRepository(appContext) }
    val isVerifiedOwner = GoogleAuthManager.isVerifiedOwnerUser(currentUser)

    val storeViewModel: StoreViewModel = viewModel(
        key = "store_vm_${currentUser.uid}",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StoreViewModel(repository, currentUser, isVerifiedOwner) as T
            }
        }
    )

    val ownerViewModel: OwnerViewModel = viewModel(
        key = "owner_vm_${currentUser.uid}",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OwnerViewModel(repository) as T
            }
        }
    )

    val catalogState by storeViewModel.catalogState.collectAsStateWithLifecycle()
    val userProfileState by storeViewModel.userProfileState.collectAsStateWithLifecycle()
    val ordersState by storeViewModel.ordersState.collectAsStateWithLifecycle()
    val pointsHistoryState by storeViewModel.pointsHistoryState.collectAsStateWithLifecycle()
    val notificationsState by storeViewModel.notificationsState.collectAsStateWithLifecycle()
    val serverOwnerVerified by storeViewModel.serverOwnerVerified.collectAsStateWithLifecycle()

    val selectedGameId by storeViewModel.selectedGameId.collectAsStateWithLifecycle()
    val checkoutSelection by storeViewModel.checkoutSelection.collectAsStateWithLifecycle()
    val lastConfirmedOrder by storeViewModel.lastConfirmedOrder.collectAsStateWithLifecycle()
    val isSubmittingOrder by storeViewModel.isSubmittingOrder.collectAsStateWithLifecycle()
    val snackbarMessage by storeViewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            storeViewModel.clearMessage()
        }
    }

    val effectiveOwner = isVerifiedOwner && serverOwnerVerified
    val ownerOrderMessagesState: UiState<List<com.example.data.model.OwnerOrderMessage>> =
        if (effectiveOwner) {
            ownerViewModel.ownerOrderMessagesState.collectAsStateWithLifecycle().value
        } else {
            UiState.Success(emptyList())
        }

    val userProfile = (userProfileState as? UiState.Success)?.data
    val catalogData = (catalogState as? UiState.Success)?.data
    val storeConfig = catalogData?.storeConfig ?: InitialCatalog.defaultStoreConfig
    val faqs = catalogData?.faqs ?: InitialCatalog.defaultFaqs
    val unreadCustomerNotifications = (notificationsState as? UiState.Success)?.data?.count { !it.read } ?: 0
    val unreadOwnerOrderMessages = (ownerOrderMessagesState as? UiState.Success)?.data?.count { !it.read } ?: 0
    val unreadNotifications = unreadCustomerNotifications + if (effectiveOwner) unreadOwnerOrderMessages else 0

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.HOME
    val activeTab = BottomNavTab.entries.firstOrNull { it.route == currentRoute }
    val isTopLevelTab = activeTab != null

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 840.dp

        Box(modifier = Modifier.fillMaxSize()) {
            if (isTopLevelTab) {
                Scaffold(
                    containerColor = DeepNavyBg,
                    topBar = {
                        MsrGlobalHeader(
                            pageTitle = activeTab?.headerTitle,
                            unreadNotificationCount = unreadNotifications,
                            onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                            isOwner = effectiveOwner,
                            onOwnerDashboardClick = { navController.navigate(Routes.OWNER_DASHBOARD) }
                        )
                    },
                    bottomBar = {
                        if (!useNavigationRail) {
                            NavigationBar(
                                containerColor = PitchBlack,
                                contentColor = TextPrimary
                            ) {
                                BottomNavTab.entries.forEach { tab ->
                                    val selected = currentRoute == tab.route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateToTab(tab.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = tab.label,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = PureWhite,
                                            selectedTextColor = PureWhite,
                                            indicatorColor = DarkSurfaceElevated,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary
                                        ),
                                        modifier = Modifier.testTag("nav_${tab.route}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useNavigationRail) {
                            NavigationRail(
                                containerColor = DarkSurface,
                                contentColor = TextPrimary
                            ) {
                                BottomNavTab.entries.forEach { tab ->
                                    val selected = currentRoute == tab.route
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { navigateToTab(tab.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        },
                                        label = { Text(tab.label, maxLines = 1) },
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = PureWhite,
                                            selectedTextColor = PureWhite,
                                            indicatorColor = DarkSurfaceElevated,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary
                                        ),
                                        modifier = Modifier.testTag("rail_${tab.route}")
                                    )
                                }
                            }
                        }

                        StoreNavHost(
                            navController = navController,
                            storeViewModel = storeViewModel,
                            ownerViewModel = ownerViewModel,
                            currentUser = currentUser,
                            catalogState = catalogState,
                            catalogData = catalogData,
                            userProfile = userProfile,
                            ordersState = ordersState,
                            pointsHistoryState = pointsHistoryState,
                            notificationsState = notificationsState,
                            ownerOrderMessagesState = ownerOrderMessagesState,
                            unreadOwnerOrderMessages = unreadOwnerOrderMessages,
                            storeConfig = storeConfig,
                            faqs = faqs,
                            unreadNotifications = unreadNotifications,
                            effectiveOwner = effectiveOwner,
                            selectedGameId = selectedGameId,
                            checkoutSelection = checkoutSelection,
                            lastConfirmedOrder = lastConfirmedOrder,
                            isSubmittingOrder = isSubmittingOrder,
                            onNavigateTab = ::navigateToTab,
                            onSignOut = {
                                GoogleAuthManager.signOut(credentialManager, scope)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                StoreNavHost(
                    navController = navController,
                    storeViewModel = storeViewModel,
                    ownerViewModel = ownerViewModel,
                    currentUser = currentUser,
                    catalogState = catalogState,
                    catalogData = catalogData,
                    userProfile = userProfile,
                    ordersState = ordersState,
                    pointsHistoryState = pointsHistoryState,
                    notificationsState = notificationsState,
                    ownerOrderMessagesState = ownerOrderMessagesState,
                    unreadOwnerOrderMessages = unreadOwnerOrderMessages,
                    storeConfig = storeConfig,
                    faqs = faqs,
                    unreadNotifications = unreadNotifications,
                    effectiveOwner = effectiveOwner,
                    selectedGameId = selectedGameId,
                    checkoutSelection = checkoutSelection,
                    lastConfirmedOrder = lastConfirmedOrder,
                    isSubmittingOrder = isSubmittingOrder,
                    onNavigateTab = ::navigateToTab,
                    onSignOut = {
                        GoogleAuthManager.signOut(credentialManager, scope)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = if (isTopLevelTab && !useNavigationRail) 84.dp else 12.dp, start = 12.dp, end = 12.dp)
            )
        }
    }
}

@Composable
private fun StoreNavHost(
    navController: androidx.navigation.NavHostController,
    storeViewModel: StoreViewModel,
    ownerViewModel: OwnerViewModel,
    currentUser: FirebaseUser,
    catalogState: UiState<com.example.ui.viewmodel.StoreCatalogData>,
    catalogData: com.example.ui.viewmodel.StoreCatalogData?,
    userProfile: com.example.data.model.UserProfile?,
    ordersState: UiState<List<com.example.data.model.Order>>,
    pointsHistoryState: UiState<List<com.example.data.model.PointsEntry>>,
    notificationsState: UiState<List<com.example.data.model.AppNotification>>,
    ownerOrderMessagesState: UiState<List<com.example.data.model.OwnerOrderMessage>>,
    unreadOwnerOrderMessages: Int,
    storeConfig: com.example.data.model.StoreConfig,
    faqs: List<com.example.data.model.FaqItem>,
    unreadNotifications: Int,
    effectiveOwner: Boolean,
    selectedGameId: String?,
    checkoutSelection: com.example.ui.viewmodel.CheckoutSelection?,
    lastConfirmedOrder: com.example.data.model.Order?,
    isSubmittingOrder: Boolean,
    onNavigateTab: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = {
            fadeIn(animationSpec = tween(220)) + slideInHorizontally(
                animationSpec = tween(240),
                initialOffsetX = { it / 12 }
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(180))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(200))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(180)) + slideOutHorizontally(
                animationSpec = tween(220),
                targetOffsetX = { it / 12 }
            )
        },
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                catalogState = catalogState,
                selectedGameId = selectedGameId,
                userProfile = userProfile,
                isVerifiedOwner = effectiveOwner,
                unreadOwnerOrderMessages = unreadOwnerOrderMessages,
                onSelectGame = storeViewModel::selectGame,
                onOpenCategory = { gameId, categoryId ->
                    navController.navigate(Routes.categoryPackages(gameId, categoryId))
                },
                onOpenBonusOffers = {
                    navController.navigate(Routes.BONUS_OFFERS)
                },
                onOpenAccount = { onNavigateTab(Routes.ACCOUNT) },
                onOpenOwnerDashboard = { navController.navigate(Routes.OWNER_DASHBOARD) },
                onOpenOwnerOrders = { navController.navigate(Routes.OWNER_ORDERS) }
            )
        }

        composable(Routes.BONUS_OFFERS) {
            BonusOffersScreen(
                catalogState = catalogState,
                isVerifiedOwner = effectiveOwner,
                unreadNotifications = unreadNotifications,
                onBack = { navController.popBackStack() },
                onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onOpenOwnerDashboard = { navController.navigate(Routes.OWNER_DASHBOARD) },
                onBuyOffer = { product, game, category, discountedPrice, offerTitle ->
                    storeViewModel.startCheckout(product, game, category, discountedPrice, offerTitle)
                    navController.navigate(Routes.CHECKOUT)
                }
            )
        }

        composable(
            route = Routes.CATEGORY_PACKAGES,
            arguments = listOf(
                navArgument("gameId") { type = NavType.StringType },
                navArgument("categoryId") { type = NavType.StringType }
            )
        ) { entry ->
            val gameIdArg = entry.arguments?.getString("gameId") ?: InitialCatalog.GAME_ROBLOX_ID
            val categoryIdArg = entry.arguments?.getString("categoryId") ?: InitialCatalog.CAT_ROBUX_ID
            CategoryPackagesScreen(
                catalogState = catalogState,
                storeConfig = storeConfig,
                gameId = gameIdArg,
                categoryId = categoryIdArg,
                userProfile = userProfile,
                isVerifiedOwner = effectiveOwner,
                unreadNotifications = unreadNotifications,
                isSubmitting = isSubmittingOrder,
                onBack = { navController.popBackStack() },
                onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onOpenOwnerDashboard = { navController.navigate(Routes.OWNER_DASHBOARD) },
                onToggleFavorite = { productId ->
                    storeViewModel.toggleFavorite(productId, userProfile?.favoriteProductIds ?: emptyList())
                },
                onCopyFeedback = storeViewModel::showMessage,
                onSubmitOrder = { product, game, category, label, input, method, recipient, sender, trxId ->
                    storeViewModel.submitDirectOrder(
                        game = game,
                        categoryName = category?.name ?: "Roblox Top-Up",
                        product = product,
                        targetAccountLabel = label,
                        targetAccountInput = input,
                        paymentMethod = method,
                        paymentRecipientNumber = recipient,
                        paymentSenderNumber = sender,
                        transactionId = trxId,
                        onSuccess = { order ->
                            storeViewModel.showMessage("Order ${order.orderCode} submitted! Status: Pending")
                            navController.navigate(Routes.ORDERS) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            )
        }

        composable(Routes.ORDERS) {
            OrdersScreen(
                ordersState = ordersState,
                onShopNow = { onNavigateTab(Routes.HOME) },
                onCopyFeedback = storeViewModel::showMessage
            )
        }

        composable(Routes.SUPPORT) {
            SupportScreen(
                storeConfig = storeConfig,
                faqs = faqs,
                onGoToOrders = { onNavigateTab(Routes.ORDERS) },
                onCopyFeedback = storeViewModel::showMessage
            )
        }

        composable(Routes.ACCOUNT) {
            AccountScreen(
                firebaseUser = currentUser,
                userProfile = userProfile,
                storeConfig = storeConfig,
                catalog = catalogData,
                pointsHistoryState = pointsHistoryState,
                unreadNotifications = unreadNotifications,
                isVerifiedOwner = effectiveOwner,
                onOpenOwnerDashboard = { navController.navigate(Routes.OWNER_DASHBOARD) },
                onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onBuyFavorite = { product, game, category ->
                    storeViewModel.startCheckout(product, game, category)
                    navController.navigate(Routes.CHECKOUT)
                },
                onDeleteAccount = {
                    storeViewModel.deleteAccount {
                        onSignOut()
                    }
                },
                onSignOut = onSignOut,
                onCopyFeedback = storeViewModel::showMessage
            )
        }

        composable(Routes.CHECKOUT) {
            CheckoutScreen(
                selection = checkoutSelection,
                storeConfig = storeConfig,
                isSubmitting = isSubmittingOrder,
                isVerifiedOwner = effectiveOwner,
                unreadNotifications = unreadNotifications,
                onBack = { navController.popBackStack() },
                onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onOpenOwnerDashboard = { navController.navigate(Routes.OWNER_DASHBOARD) },
                onCopyFeedback = storeViewModel::showMessage,
                onSubmitOrder = { label, input, method, recipient, sender, trxId ->
                    storeViewModel.submitOrder(
                        targetAccountLabel = label,
                        targetAccountInput = input,
                        paymentMethod = method,
                        paymentRecipientNumber = recipient,
                        paymentSenderNumber = sender,
                        transactionId = trxId,
                        onSuccess = { order ->
                            storeViewModel.showMessage("Order ${order.orderCode} submitted! Status: Pending")
                            navController.navigate(Routes.ORDERS) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            )
        }

        composable(Routes.ORDER_CONFIRMATION) {
            OrderConfirmationScreen(
                order = lastConfirmedOrder,
                isVerifiedOwner = effectiveOwner,
                unreadNotifications = unreadNotifications,
                onTrackOrders = {
                    navController.popBackStack(Routes.HOME, inclusive = false)
                    onNavigateTab(Routes.ORDERS)
                },
                onBackToHome = {
                    navController.popBackStack(Routes.HOME, inclusive = false)
                    onNavigateTab(Routes.HOME)
                },
                onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onOpenOwnerDashboard = { navController.navigate(Routes.OWNER_DASHBOARD) }
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                notificationsState = notificationsState,
                isVerifiedOwner = effectiveOwner,
                ownerOrderMessagesState = ownerOrderMessagesState,
                onBack = { navController.popBackStack() },
                onMarkRead = storeViewModel::markNotificationRead,
                onMarkOrderMessageRead = ownerViewModel::markOrderMessageRead,
                onOpenOwnerDashboard = { navController.navigate(Routes.OWNER_DASHBOARD) },
                onOpenOwnerOrders = { navController.navigate(Routes.OWNER_ORDERS) }
            )
        }

        composable(Routes.OWNER_DASHBOARD) {
            OwnerDashboardScreen(
                ownerViewModel = ownerViewModel,
                catalogState = catalogState,
                isVerifiedOwner = effectiveOwner,
                initialTab = OwnerTab.OVERVIEW,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.OWNER_ORDERS) {
            OwnerDashboardScreen(
                ownerViewModel = ownerViewModel,
                catalogState = catalogState,
                isVerifiedOwner = effectiveOwner,
                initialTab = OwnerTab.ORDERS,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
