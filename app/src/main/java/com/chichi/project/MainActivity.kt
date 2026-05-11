package com.chichi.project

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chichi.project.ui.screens.AvailableItemsScreen
import com.chichi.project.ui.screens.DonateScreen
import com.chichi.project.ui.screens.ForgotPasswordScreen
import com.chichi.project.ui.screens.LoginScreen
import com.chichi.project.ui.screens.MapScreen
import com.chichi.project.ui.screens.RequestScreen
import com.chichi.project.ui.screens.RequestedItemsScreen
import com.chichi.project.ui.screens.SelectionScreen
import com.chichi.project.ui.screens.SignUpScreen
import com.chichi.project.ui.screens.SplashScreen
import com.chichi.project.ui.screens.SuccessScreen
import com.chichi.project.ui.theme.PROJECTTheme

class MainActivity : ComponentActivity() {
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
            }
            else -> {
                // No location access granted.
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PROJECTTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        requestLocationPermission = {
                            locationPermissionRequest.launch(arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    requestLocationPermission: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier
    ) {
        composable("splash") {
            SplashScreen(onTimeout = {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("login") {
            LoginScreen(
                onNavigateToSignUp = { navController.navigate("signup") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") },
                onLoginSuccess = {
                    navController.navigate("selection") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("signup") {
            SignUpScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onSignUpSuccess = {
                    navController.navigate("selection") {
                        popUpTo("signup") { inclusive = true }
                    }
                }
            )
        }
        composable("forgot_password") {
            ForgotPasswordScreen(
                onNavigateToLogin = { navController.navigate("login") }
            )
        }
        composable("selection") {
            SelectionScreen(
                onDonateSelected = { navController.navigate("donate") },
                onRequestSelected = { navController.navigate("request") },
                onViewMapSelected = { navController.navigate("map") },
                onBrowseItemsSelected = { navController.navigate("available_items") },
                onViewRequestsSelected = { navController.navigate("requested_items") }
            )
        }
        composable("available_items") {
            AvailableItemsScreen(
                onBack = { navController.popBackStack() },
                requestLocationPermission = requestLocationPermission
            )
        }
        composable("requested_items") {
            RequestedItemsScreen(
                onBack = { navController.popBackStack() },
                requestLocationPermission = requestLocationPermission
            )
        }
        composable("donate") {
            DonateScreen(
                onDonateSubmitted = { 
                    navController.navigate("donation_success") {
                        popUpTo("selection") 
                    }
                },
                onBack = { navController.popBackStack() },
                requestLocationPermission = requestLocationPermission
            )
        }
        composable("donation_success") {
            SuccessScreen(
                title = "Donation Received!",
                message = "Thank you for your generosity! Your donation has been posted and will help someone in need.",
                onContinue = { navController.popBackStack("selection", inclusive = false) }
            )
        }
        composable("request") {
            RequestScreen(
                onRequestSubmitted = { 
                    navController.navigate("request_success") {
                        popUpTo("selection")
                    }
                },
                onBack = { navController.popBackStack() },
                requestLocationPermission = requestLocationPermission
            )
        }
        composable("request_success") {
            SuccessScreen(
                title = "Request Submitted!",
                message = "Your request has been posted successfully. Others can now see what you need on the map.",
                onContinue = { navController.popBackStack("selection", inclusive = false) }
            )
        }
        composable("map") {
            MapScreen(
                onBack = { navController.popBackStack() },
                requestLocationPermission = requestLocationPermission
            )
        }
    }
}
