package com.chichi.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chichi.project.ProjectApplication
import com.chichi.project.models.ClothesRequest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestedItemsScreen(onBack: () -> Unit, requestLocationPermission: () -> Unit) {
    var requests by remember { mutableStateOf<List<ClothesRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedRequestId by remember { mutableStateOf<Int?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val mockRequests = listOf(
        ClothesRequest(id = -1, description = "Warm sweater for 5yr old", size = "Age 5-6", quantity = "1", requester_email = "Local Shelter", phone_number = "0733333333"),
        ClothesRequest(id = -2, description = "Men's formal shoes", size = "Size 42", quantity = "1 pair", requester_email = "Job Seeker Support", phone_number = "0744444444"),
        ClothesRequest(id = -3, description = "School uniform (Blue)", size = "Small", quantity = "2 sets", requester_email = "Primary School", phone_number = "0755555555")
    )

    LaunchedEffect(Unit) {
        requestLocationPermission()
        try {
            val response = ProjectApplication.supabase.postgrest["requests"]
                .select().decodeList<ClothesRequest>()
            requests = mockRequests + response
        } catch (e: Exception) {
            requests = mockRequests
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Requested Items") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onSecondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Text(
                text = "See what the community is looking for.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            if (requests.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No requests at the moment.")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(requests) { request ->
                    RequestItemCard(
                        clothesRequest = request,
                        isSelected = selectedRequestId == request.id,
                        onSelect = {
                            if (selectedRequestId == null) {
                                selectedRequestId = request.id
                                showSuccessDialog = true
                            }
                        },
                        isDisabled = selectedRequestId != null && selectedRequestId != request.id
                    )
                }
            }
        }
    }

    if (showSuccessDialog) {
        val selectedRequest = requests.find { it.id == selectedRequestId }
        val dialogText = if (selectedRequest != null && (selectedRequest.id ?: 0) < 0) {
            "You have chosen to help with: ${selectedRequest.description}. Please deliver it to the nearest collection point. Check the map for locations!"
        } else {
            "You have chosen to help with: ${selectedRequest?.description}. You can find the requester's location on the map."
        }
        
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("Thank You for Helping!") },
            text = { Text(dialogText) },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green) }
        )
    }
}

@Composable
fun RequestItemCard(
    clothesRequest: ClothesRequest,
    isSelected: Boolean,
    onSelect: () -> Unit,
    isDisabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = clothesRequest.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Requested by: ${clothesRequest.requester_email}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Phone: ${clothesRequest.phone_number}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                        Text("Size: ${clothesRequest.size}", modifier = Modifier.padding(horizontal = 4.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                        Text("Qty: ${clothesRequest.quantity}", modifier = Modifier.padding(horizontal = 4.dp))
                    }
                    if (clothesRequest.latitude != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("Location shared", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            
            Button(
                onClick = onSelect,
                enabled = !isDisabled && !isSelected,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Color.Gray else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Help")
            }
        }
    }
}
