package com.example.jetpackpos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check // Changed from Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.jetpackpos.ui.viewmodel.AddEditProductEvent
import com.example.jetpackpos.ui.viewmodel.AddEditProductViewModel
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects


// Helper function to create image URI for camera
/*fun createImageUriOLd(context: android.content.Context): Uri {
    val imageFolder = File(context.cacheDir, "images")
    imageFolder.mkdirs()
    val file = File(imageFolder, "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}_.jpg")
    return FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        BuildConfig.APPLICATION_ID + ".provider", file
    )
}*/

fun createImageUri(context: android.content.Context, authority: String): Uri {
    val imageFolder = File(context.cacheDir, "images")
    imageFolder.mkdirs()
    val file = File(imageFolder, "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}_.jpg")
    return FileProvider.getUriForFile(context, authority, file)
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    navController: NavController,
    // Removed productId from here as ViewModel handles it via SavedStateHandle
    viewModel: AddEditProductViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onImageUriChange(it.toString())
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            tempImageUri?.let { // Use the URI that was provided to the camera
                viewModel.onImageUriChange(it.toString())
            }
        }
    }

    // Permission launchers
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
           // tempImageUri = createImageUri(context) // Create URI before launching camera
            tempImageUri = createImageUri(context, "com.example.jetpackpos.provider")
            cameraLauncher.launch(tempImageUri)
        } else {
            // Handle permission denial - e.g., show a snackbar
            // For simplicity, this is not handled with a snackbar here, but should be in a real app
        }
    }
    // READ_EXTERNAL_STORAGE or READ_MEDIA_IMAGES permission for gallery
     val requestGalleryPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            // Handle permission denial
        }
    }


    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditProductEvent.ProductSaved -> {
                    navController.navigateUp()
                }
                is AddEditProductEvent.Error -> {
                     snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (formState.isEditing) "Edit Product" else "Add New Product") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.saveProduct() },
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(Icons.Filled.Check, contentDescription = if (formState.isEditing) "Save Changes" else "Save Product")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        // Show loading indicator when fetching product for editing
        if (formState.isLoading && formState.isEditing && formState.currentProductId != null) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply padding from Scaffold
                    .padding(horizontal = 16.dp, vertical = 8.dp) // Additional content padding
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp) // Spacing between form fields
            ) {
                formState.generalError?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                OutlinedTextField(
                    value = formState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Product Name*") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = formState.nameError != null,
                    singleLine = true,
                    supportingText = { formState.nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                OutlinedTextField(
                    value = formState.sku,
                    onValueChange = viewModel::onSkuChange,
                    label = { Text("SKU (Stock Keeping Unit)*") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = formState.skuError != null,
                    singleLine = true,
                    supportingText = { formState.skuError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = formState.price,
                        onValueChange = viewModel::onPriceChange,
                        label = { Text("Price*") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = formState.priceError != null,
                        singleLine = true,
                        supportingText = { formState.priceError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                    )
                    OutlinedTextField(
                        value = formState.quantity,
                        onValueChange = viewModel::onQuantityChange,
                        label = { Text("Quantity*") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = formState.quantityError != null,
                        singleLine = true,
                        supportingText = { formState.quantityError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                    )
                }

                var categoryDropdownExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = formState.category,
                        onValueChange = viewModel::onCategoryChange, // Allows typing new category
                        label = { Text("Category*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(), // Important for positioning the dropdown
                        isError = formState.categoryError != null,
                        supportingText = { formState.categoryError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded && formState.categories.isNotEmpty() && !formState.isLoadingCategories,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        formState.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    viewModel.onCategoryChange(category.name)
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                        if (formState.isLoadingCategories) {
                            DropdownMenuItem(text = { Text("Loading categories...") }, onClick = {}, enabled = false)
                        }
                    }
                }

                OutlinedTextField(
                    value = formState.imageUri ?: "",
                    onValueChange = viewModel::onImageUriChange,
                    label = { Text("Image URL (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Description (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    maxLines = 5
                )

                // Image Preview and Picker Buttons
                Spacer(modifier = Modifier.height(8.dp))
                Text("Product Image (Optional)", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))

                AsyncImage(
                    model = formState.imageUri,
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .clickable {
                            // Optionally allow clicking the image to change it
                            // For now, use buttons below
                        },
                    contentScale = ContentScale.Crop,
                    /*error = { // Display a placeholder if imageUri is null or loading fails
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Image, "No image selected", modifier = Modifier.size(48.dp))
                        }
                    }*/
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { requestGalleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES) /* Or READ_EXTERNAL_STORAGE for older APIs */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = "Gallery", modifier = Modifier.padding(end = 4.dp))
                        Text("Gallery")
                    }
                    Button(
                        onClick = { requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "Camera", modifier = Modifier.padding(end = 4.dp))
                        Text("Camera")
                    }
                }
                 Button(
                    onClick = { viewModel.onImageUriChange(null) }, // Clear image
                    enabled = formState.imageUri != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Text("Remove Image")
                }


                // Placeholder for Scan button (remains conceptual for now)

                 Button(
                    onClick = {
                        Toast.makeText(context, "Scan Product Code - Not Implemented", Toast.LENGTH_SHORT).show()
                     },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Scan Product Code (Future)")
                }


                // Show loading indicator during save operation (for new or edit)
                if (formState.isLoading && (formState.currentProductId == null || !formState.isEditing) ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                Spacer(modifier = Modifier.height(72.dp)) // Space for FAB to not overlap content
            }
        }
    }
}
