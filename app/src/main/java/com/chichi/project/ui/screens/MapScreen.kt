package com.chichi.project.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.chichi.project.ProjectApplication
import com.chichi.project.models.ClothesRequest
import com.chichi.project.models.CollectionPoint
import com.chichi.project.models.Donation
import com.google.android.gms.location.LocationServices
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onBack: () -> Unit, requestLocationPermission: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Initialize OSMDroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
    }

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

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(12.0)
            controller.setCenter(GeoPoint(-1.286389, 36.817223))
        }
    }

    val myLocationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
        }
    }

    LaunchedEffect(Unit) {
        requestLocationPermission()
        
        // Fetch Data
        try {
            val points = ProjectApplication.supabase.postgrest["collection_points"]
                .select().decodeList<CollectionPoint>()
            collectionPoints = if (points.isEmpty()) fallbackPoints else points

            donations = ProjectApplication.supabase.postgrest["donations"]
                .select().decodeList<Donation>().filter { it.latitude != null && it.longitude != null }
            
            requests = ProjectApplication.supabase.postgrest["requests"]
                .select().decodeList<ClothesRequest>().filter { it.latitude != null && it.longitude != null }

        } catch (e: Exception) {
            collectionPoints = fallbackPoints
        } finally {
            isLoading = false
        }

        // Try to get current location and center map
        try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                mapView.controller.animateTo(GeoPoint(it.latitude, it.longitude), 14.0, 1000L)
            }
        } catch (e: Exception) {
            // Ignore location errors
        }
    }

    // Update markers when data changes
    LaunchedEffect(collectionPoints, donations, requests) {
        mapView.overlays.clear()
        mapView.overlays.add(myLocationOverlay)

        // Collection Points
        collectionPoints.forEach { point ->
            val marker = Marker(mapView)
            marker.position = GeoPoint(point.latitude, point.longitude)
            marker.title = point.name
            marker.subDescription = point.snippet
            // You can set custom icons here if needed
            mapView.overlays.add(marker)
        }

        // Donations
        donations.forEach { donation ->
            val marker = Marker(mapView)
            marker.position = GeoPoint(donation.latitude!!, donation.longitude!!)
            marker.title = "Donation: ${donation.description}"
            marker.subDescription = "Size: ${donation.size}, Qty: ${donation.quantity}\nDonor: ${donation.donor_email}\nPhone: ${donation.phone_number}"
            marker.icon = ResourcesCompat.getDrawable(context.resources, org.osmdroid.library.R.drawable.marker_default, null)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(marker)
        }

        // Requests
        requests.forEach { request ->
            val marker = Marker(mapView)
            marker.position = GeoPoint(request.latitude!!, request.longitude!!)
            marker.title = "Request: ${request.description}"
            marker.subDescription = "Size: ${request.size}, Qty: ${request.quantity}\nPhone: ${request.phone_number}"
            mapView.overlays.add(marker)
        }
        
        mapView.invalidate()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pick-up & Drop-off Points") },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        val location = fusedLocationClient.lastLocation.await()
                        location?.let {
                            mapView.controller.animateTo(GeoPoint(it.latitude, it.longitude), 15.0, 1000L)
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            )
            
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
