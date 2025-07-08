# Jetpack POS - Offline First Android Application

## Overview

Jetpack POS is a modern, offline-first Point of Sale (POS) application built entirely with Jetpack Compose and leveraging Android's latest architecture components. It's designed to provide essential POS functionalities for small businesses, operating seamlessly without a constant internet connection by using a local Room database.

The application follows the MVVM (Model-View-ViewModel) architecture pattern, ensuring a scalable, maintainable, and testable codebase.

## Core Features

*   **Offline First**: All data is stored locally using Room Database, allowing full functionality without an internet connection.
*   **Inventory Management**:
    *   Add, edit, and delete products (name, price, SKU, quantity, category, description, image URI).
    *   Live product list with search functionality (by name/SKU).
    *   Low stock indicators.
    *   (Conceptual) Placeholders for barcode/QR scanning, image picking from gallery/camera, and export to Excel.
*   **Sales / POS System**:
    *   Modern POS interface for adding products to a cart.
    *   Adjust quantity in cart.
    *   Cart counter indicator in the toolbar.
    *   Dedicated cart details screen.
    *   (Conceptual) Placeholder for product scanning.
*   **Checkout Process**:
    *   Generates sale records (orders and order items).
    *   Deducts stock from inventory upon successful sale.
    *   Calculates subtotal, tax (configurable in settings), and total.
*   **Transaction History**:
    *   List of past sales with date, items sold, and total amount.
    *   Detailed view for individual transactions.
    *   Search functionality for orders.
    *   (Conceptual) Placeholders for printing receipts (PDF/Thermal).
*   **Customer Management**:
    *   Create, view, edit, and delete customers (name, phone, email, address).
    *   Search functionality for customers.
*   **Reporting (Basic)**:
    *   Screen for viewing sales reports.
    *   Filter transactions by "Today", "This Week", "This Month", "This Year".
    *   Displays a list of transactions based on the selected filter.
    *   (Conceptual) Placeholder for chart visualization.
*   **Settings**:
    *   **Shop Information**: View and edit shop details (name, contact, email, address, currency symbol, tax percentage).
    *   **Category Management**: Full CRUD operations for product categories.
    *   **Database Reset**: Option to wipe all application data (with confirmation).
    *   (Conceptual) Placeholders for Payment Methods management and Data Backup.
*   **Modern UI**: Built with Jetpack Compose for a declarative and responsive user interface.
*   **Home Screen Dashboard**: Quick access to key modules like Inventory, Customers, Reports, All Orders, etc.

## Tech Stack & Architecture

*   **UI**: Jetpack Compose
*   **Architecture**: MVVM (Model-View-ViewModel)
*   **Language**: Kotlin
*   **Asynchronous Programming**: Coroutines & Kotlin Flow
*   **Dependency Injection**: Hilt
*   **Database**: Room Persistence Library (SQLite)
*   **Navigation**: Jetpack Navigation Compose
*   **ViewModels**: AndroidX ViewModel with `SavedStateHandle`
*   **State Management**: `StateFlow` and `SharedFlow`

## Setup Instructions

1.  **Clone the repository**:
    ```bash
    git clone <repository-url>
    ```
2.  **Open in Android Studio**:
    *   Open Android Studio (latest stable version recommended).
    *   Select "Open an Existing Project" and navigate to the cloned repository folder.
3.  **Build the project**:
    *   Allow Android Studio to sync Gradle files and download dependencies.
    *   Build the project using the "Build" menu or by running the app on an emulator or physical device.
    *   The command line build can be done using:
        ```bash
        ./gradlew assembleDebug
        ```
        or
        ```bash
        ./gradlew installDebug
        ```
4.  **Run the application**:
    *   Select an emulator or connect a physical device.
    *   Run the 'app' configuration from Android Studio.

## Conceptual Features / Future Enhancements

The current version includes UI placeholders or conceptual notes for the following advanced features, which are not yet functionally implemented:

*   **Barcode/QR Code Scanning**: For quick product lookup or adding to cart.
*   **Product Image Handling**: Picking images from Gallery/Camera and displaying them.
*   **Data Export/Import**: Exporting product lists or sales data to Excel/CSV.
*   **Printing**: Generating PDF receipts or printing directly to thermal printers.
*   **Advanced Reporting**: Visual charts and more detailed report generation.
*   **Payment Method Management**: Adding and configuring various payment types.
*   **Data Backup & Restore**: Options to backup data to Google Drive or local storage.
*   **User Authentication**: Local PIN-based login or more advanced authentication.
*   **Cloud Sync**: (Major enhancement) Optional synchronization with a cloud backend.

## Folder Structure (Simplified)

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/jetpackpos/
│   │   │   ├── data/                  # Data layer: Models, DAOs, Repositories, Database
│   │   │   │   ├── db/                # Room Database, DAOs
│   │   │   │   ├── model/             # Entities and other data classes
│   │   │   │   └── repository/        # Repositories
│   │   │   ├── di/                    # Hilt Dependency Injection modules
│   │   │   ├── ui/                    # UI layer: Compose screens, ViewModels, Navigation
│   │   │   │   ├── navigation/        # Navigation graph and route definitions
│   │   │   │   ├── screens/           # Composable screens
│   │   │   │   │   └── common/        # Shared UI components
│   │   │   │   ├── theme/             # Compose theme (Color, Shape, Typography)
│   │   │   │   └── viewmodel/         # ViewModels
│   │   │   ├── utils/                 # Utility classes/extensions (if any)
│   │   │   ├── MainActivity.kt        # Main activity
│   │   │   └── PosApplication.kt      # Hilt Application class
│   │   ├── res/                       # Android resources (drawables, values, etc.)
│   │   └── AndroidManifest.xml
│   └── test/                          # Unit tests
│   └── androidTest/                   # Instrumented tests
└── build.gradle                       # App-level Gradle file
build.gradle                           # Project-level Gradle file
settings.gradle
...
```

---

This README provides a good starting point. It can be further expanded with more details as the project evolves.
