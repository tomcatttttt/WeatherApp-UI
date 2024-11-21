# WeatherAppUI

WeatherAppUI is a simple and feature-rich weather application for Android, developed in Kotlin. The app provides users with accurate weather updates and ensures a smooth experience with modern architecture and libraries.

## Features

- **Current Weather Updates**: Displays real-time weather data based on the user's location.
- **Location Permissions**: Handles permissions for accessing GPS data with clear user prompts.
- **Network Check**: Verifies internet connectivity to ensure the app works seamlessly even in offline scenarios.
- **Live Data Integration**: Uses LiveData for observing and reacting to changes in weather data dynamically.
- **Modern UI**: Clean and responsive interface designed with XML layouts and styled for a great user experience.
- **Error Handling**: Provides user-friendly messages in case of GPS or network issues.

## Core Functionalities

1. **GPS Access**:
   - Uses `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` to fetch the user's location.
   - Includes error handling for cases when GPS is disabled or permission is denied.

2. **Internet Check**:
   - Validates network connectivity before making API calls.
   - Displays appropriate error messages if no connection is available.

3. **Weather Data**:
   - Fetches weather data using the **Retrofit** library from a weather API.
   - Supports caching of last fetched data for offline access.

4. **Live Updates**:
   - Weather information updates dynamically using **LiveData** in MVVM architecture.

5. **Image Loading**:
   - Displays weather icons and other images with **Coil** (or Glide).

6. **Navigation**:
   - Seamless navigation between screens using the **Navigation Component**.

## How to run

1. Clone the repository:
Make git clone <url>
