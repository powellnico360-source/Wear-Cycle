package com.chichi.project.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.chichi.project.ProjectApplication
import com.chichi.project.models.ClothesRequest
import com.chichi.project.models.CollectionPoint
import com.chichi.project.models.Donation
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onBack: () -> Unit, requestLocationPermission: () -> Unit) {
    val nairobi = LatLng(-1.286389, 36.817223)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(nairobi, 12f)
    }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var collectionPoints by remember { mutableStateOf<List<CollectionPoint>>(emptyList()) }
    var donations by remember { mutableStateOf<List<Donation>>(emptyList()) }
    var requests by remember { mutableStateOf<List<ClothesRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val fallbackPoints = listOf(
        CollectionPoint(name = "Central Collection Hub", latitude = -1.2833, longitude = 36.8167, snippet = "Main donation center"),
        CollectionPoint(name = "Westlands Drop-off", latitude = -1.2633, longitude = 36.8033, snippet = "Open 9am-5pm"),
        CollectionPoint(name = "Karen Community Center", latitude = -1.3333, longitude = 36.7000, snippet = "Weekend collections only"),
        CollectionPoint(name = "Kilimani Recycling Point", latitude = -1.2900, longitude = 36.7800, snippet = "All items welcome")
    )

    LaunchedEffect(Unit) {
        requestLocationPermission()
        try {
            // Fetch user location to center map
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                cameraPositionState.animate(
                    update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.latitude, it.longitude), 13f
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val points = ProjectApplication.supabase.postgrest["collection_points"]
                .select().decodeList<CollectionPoint>()
            collectionPoints = if (points.isEmpty()) fallbackPoints else points

            donations = ProjectApplication.supabase.postgrest["donations"]
                .select().decodeList<Donation>().filter { it.latitude != null && it.longitude != null }
            
            requests = ProjectApplication.supabase.postgrest["requests"]
                .select().decodeList<ClothesRequest>().filter { it.latitude != null && it.longitude != null }

        } catch (e: Exception) {
            e.printStackTrace()
            collectionPoints = fallbackPoints
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Activity") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = true,
                    zoomControlsEnabled = false
                )
            ) {
                // Collection Points (Blue)
                collectionPoints.forEach { point ->
                    Marker(
                        state = rememberMarkerState(position = LatLng(point.latitude, point.longitude)),
                        title = point.name,
                        snippet = point.snippet,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }

                // Donations (Green)
                donations.forEach { donation ->
                    Marker(
                        state = rememberMarkerState(position = LatLng(donation.latitude!!, donation.longitude!!)),
                        title = "Donation: ${donation.description}",
                        snippet = "By: ${donation.donor_email}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                }

                // Requests (Red/Orange)
                requests.forEach { request ->
                    Marker(
                        state = rememberMarkerState(position = LatLng(request.latitude!!, request.longitude!!)),
                        title = "Request: ${request.description}",
                        snippet = "Size: ${request.size}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }
            }
            
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
