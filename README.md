# Photo Catalog

An Android application that allows users to take pictures, add metadata (location, cost, observations, ratings), and save them to Google Photos while organizing them into projects or catalogs.

## Features

- Take photos using device camera
- Import existing photos
- Auto-upload to Google Photos
- Add metadata to photos:
  - Location
  - Cost information
  - Observations/comments
  - 5-star rating system
- Organize photos into projects/catalogs
- View photos in grid or list layout
- Search and filter capabilities

## Technical Stack

- **Language**: Kotlin
- **Min SDK**: Android 7.0 (API 24)
- **Architecture**: MVVM with Clean Architecture
- **Dependencies**:
  - CameraX for photo capture
  - Google Photos API
  - Google Maps API
  - Firebase/Firestore
  - Hilt for dependency injection
  - Coroutines for async operations
  - AndroidX Navigation

## Setup

1. Clone the repository
```bash
git clone https://github.com/yourusername/PhotoCatalog.git
```

2. Add your Google Cloud configuration:
   - Create a project in Google Cloud Console
   - Enable required APIs:
     - Google Photos API
     - Maps SDK for Android
     - Places API
   - Add google-services.json to the app directory

3. Update API keys in local.properties:
```properties
MAPS_API_KEY=your_maps_api_key
```

4. Build and run the project in Android Studio

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/photocatalog/
│   │   │   ├── data/           # Data layer
│   │   │   ├── di/            # Dependency injection
│   │   │   ├── ui/            # UI components
│   │   │   └── utils/         # Utilities
│   │   └── res/               # Resources
│   └── test/                  # Unit tests
└── build.gradle              # App level build config
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
