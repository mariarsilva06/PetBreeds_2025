# PetBreeds - Android App

Android application for browsing cat and dog breeds using MVVM architecture, Jetpack Compose, and modern Android development practices.

## Getting Started

### Prerequisites
- Android Studio Iguana (2023.2.1) or newer
- JDK 17
- Android SDK 34
- Kotlin 1.9.22

### API Keys Setup

1. Get your free API keys:
    - Cat API: https://thecatapi.com/signup
    - Dog API: https://thedogapi.com/signup

2. Create `local.properties` file in the project root (if not exists)

3. Add your API keys:
```properties
CAT_API_KEY=your_cat_api_key_here
DOG_API_KEY=your_dog_api_key_here
```

**Note**: `local.properties` is gitignored. See `apikeys.properties.example` for reference.

## Architecture

This app follows MVVM (Model-View-ViewModel) architecture with:
- **Jetpack Compose** for UI
- **Hilt** for dependency injection
- **Room** for local database
- **Retrofit** for networking
- **Coroutines & Flow** for async operations
- **Navigation Compose** for navigation

## Features

- Choose between Cat or Dog person on first launch
- Browse breeds with images
- Search breeds by name
- Mark breeds as favorites
- View favorite breeds with average lifespan calculation
- Offline support with local caching
- Detailed breed information

## Tech Stack

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Gradle**: 8.6
- **AGP**: 8.3.0
- **Kotlin**: 1.9.22
- **Compose BOM**: 2024.02.01

## Project Structure

```
com.example.petbreeds/
├── data/               # Data layer
│   ├── api/           # Retrofit services
│   ├── local/         # Room database
│   ├── model/         # Data models
│   └── repository/    # Repository implementations
├── presentation/       # UI layer
│   ├── onboarding/    # Pet type selection
│   ├── breeds/        # Breeds list
│   ├── favorites/     # Favorites screen
│   ├── details/       # Breed details
│   └── theme/         # Compose theme
├── di/                # Dependency injection
└── utils/             # Utilities
```

## Build & Run

1. Clone the repository
2. Set up your API keys (see above)
3. Sync project with Gradle files
4. Run on emulator or device (API 24+)

## Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## License

This project was created as a technical challenge for Sword Health Trainee Program 2025.