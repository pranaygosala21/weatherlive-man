package com.example.weatherapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.ui.theme.WeatherAppTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.clickable
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.SearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.SolidColor
import org.json.JSONObject
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    LocationScreen()
                }
            }
        }
    }
}

class FavoritesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("Favorites", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _favoritesFlow = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val favoritesFlow: StateFlow<List<Pair<String, String>>> = _favoritesFlow

    init {
        _favoritesFlow.value = getFavorites()
    }

    fun getFavorites(): List<Pair<String, String>> {
        val favoritesJson = sharedPreferences.getString("favorites", null)
        return if (favoritesJson != null) {
            gson.fromJson(favoritesJson, object : TypeToken<List<Pair<String, String>>>() {}.type)
        } else {
            emptyList()
        }
    }

    fun addFavorite(city: String, state: String) {
        val favorites = getFavorites().toMutableList()
        favorites.add(Pair(city, state))
        saveFavorites(favorites)
        _favoritesFlow.value = favorites
    }

    fun removeFavorite(city: String, state: String) {
        val favorites = getFavorites().toMutableList()
        favorites.removeAll { it.first == city && it.second == state }
        saveFavorites(favorites)
        _favoritesFlow.value = favorites
    }

    fun isFavorite(city: String, state: String): Boolean {
        return getFavorites().contains(Pair(city, state))
    }

    private fun saveFavorites(favorites: List<Pair<String, String>>) {
        val favoritesJson = gson.toJson(favorites)
        sharedPreferences.edit().putString("favorites", favoritesJson).apply()
    }

    fun refreshFavorites() {
        _favoritesFlow.value = getFavorites()
    }
}

object WeatherMappings {
    val statusCodeMapping = mapOf(
        0 to "Unknown",
        1000 to "Clear",
        1100 to "Mostly Clear",
        1101 to "Partly Cloudy",
        1102 to "Mostly Cloudy",
        1001 to "Cloudy",
        2000 to "Fog",
        2100 to "Light Fog",
        4000 to "Drizzle",
        4001 to "Rain",
        4200 to "Light Rain",
        4201 to "Heavy Rain",
        5000 to "Snow",
        5001 to "Flurries",
        5100 to "Light Snow",
        5101 to "Heavy Snow",
        6000 to "Freezing Drizzle",
        6001 to "Freezing Rain",
        6200 to "Light Freezing Rain",
        6201 to "Heavy Freezing Rain",
        7000 to "Ice Pellets",
        7101 to "Heavy Ice Pellets",
        7102 to "Light Ice Pellets",
        8000 to "Thunderstorm"
    )

    val weatherIconMapping = mapOf(
        "0" to "unknown",
        "1000" to "clear_day",
        "1001" to "cloudy",
        "1100" to "mostly_clear_day",
        "1101" to "partly_cloudy_day",
        "1102" to "mostly_cloudy",
        "2000" to "fog",
        "2100" to "light_fog",
        "3000" to "light_wind",
        "3001" to "wind",
        "3002" to "strong_wind",
        "4000" to "drizzle",
        "4001" to "rain",
        "4200" to "rain_light",
        "4201" to "rain_heavy",
        "5000" to "snow",
        "5001" to "flurries",
        "5100" to "light_snow",
        "5101" to "heavy_snow",
        "6000" to "freezing_drizzle",
        "6001" to "freezing_rain",
        "6200" to "light_freezing_rain",
        "6201" to "heavy_freezing_rain",
        "7000" to "ice_pellets",
        "7101" to "heavy_ice_pellets",
        "7102" to "light_ice_pellets",
        "8000" to "thunderstorm"
    )

    fun getWeatherDescription(statusCode: Int): String {
        return statusCodeMapping[statusCode] ?: "Unknown"
    }

    fun getWeatherIconResourceName(statusCode: String): String {
        return weatherIconMapping[statusCode] ?: "unknown"
    }
}

val highchartsHtml = """
    <!DOCTYPE html>
    <html>
    <head>
        <script src="https://code.highcharts.com/highcharts.js"></script>
        <script src="https://code.highcharts.com/modules/arearange.js"></script>
    </head>
    <body style="margin: 0; padding: 0;">
        <div id="container" style="width: 100%; height: 100%;"></div>
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                Highcharts.chart('container', {
                    chart: {
                        type: 'arearange',
                        zoomType: 'x'
                    },
                    title: {
                        text: 'Temperature Range (Next 5 Days)'
                    },
                    xAxis: {
                        categories: ['Day 1', 'Day 2', 'Day 3', 'Day 4', 'Day 5']
                    },
                    yAxis: {
                        title: {
                            text: 'Temperature (°C)'
                        }
                    },
                    series: [{
                        name: 'Temperature Range',
                        data: [
                            [10, 20], [12, 22], [8, 18], [15, 25], [10, 20]
                        ]
                    }]
                });
            });
        </script>
    </body>
    </html>
"""


private fun fetchPlaceSuggestions(query: String, context: Context, onResult: (List<String>) -> Unit) {
    val queue = Volley.newRequestQueue(context)
    val baseUrl = "https://maps.googleapis.com/maps/api/place/autocomplete/json"
    val apiKey = "AIzaSyBHhiBN7fJCpKwkGTkK7rXxXWb3PKwH09Y"

    val url = "$baseUrl?input=$query&types=(cities)&key=$apiKey"

    val request = JsonObjectRequest(
        Request.Method.GET, url, null,
        { response ->
            val suggestions = mutableListOf<String>()
            val predictions = response.getJSONArray("predictions")

            for (i in 0 until predictions.length()) {
                val prediction = predictions.getJSONObject(i)
                suggestions.add(prediction.getString("description"))
            }

            onResult(suggestions)
        },
        { error ->
            Log.e("Places API", "Error: ${error.message}")
            onResult(emptyList())
        }
    )

    queue.add(request)
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LocationScreen() {
    var userLocation by remember { mutableStateOf("Fetching location...") }
    var latLong by remember { mutableStateOf("Fetching coordinates...") }
    var weatherData by remember { mutableStateOf<List<WeatherItem>>(emptyList()) }
    var showDetailedView by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf(listOf<String>()) }
    var isSearchResult by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(0) }
    var isSearchResultView by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val favoritesManager = remember { FavoritesManager(context) }
    val favorites by favoritesManager.favoritesFlow.collectAsState()
    val pagerState = rememberPagerState { 1 + favorites.size }

    LaunchedEffect(Unit) {
        fetchInitialLocation(context) { location, coordinates, weather ->
            userLocation = location
            latLong = coordinates
            weatherData = weather
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            currentPage = page
        }
    }

    LaunchedEffect(selectedLocation, favorites) {
        val parts = selectedLocation.split(",")
        if (parts.size >= 2) {
            val city = parts[0].trim()
            val state = parts[1].trim()
            isFavorite = favoritesManager.isFavorite(city, state)
        }
    }

    LaunchedEffect(favorites) {
        if (pagerState.currentPage >= favorites.size + 1) {
            pagerState.animateScrollToPage(favorites.size)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showDetailedView) {
            DetailedView(
                onBackClick = { showDetailedView = false },
                location = if (isSearchResult) selectedLocation else userLocation,
                currentTemperature = weatherData.firstOrNull()?.temperatureHigh?.toInt() ?: 0,
                weatherItem = weatherData.first(),
                weatherData = weatherData
            )
        } else {
            Scaffold(
                floatingActionButton = {
                    if (isSearchResult) {
                        FloatingActionButton(
                            onClick = {
                                val parts = selectedLocation.split(",")
                                if (parts.size >= 2) {
                                    val city = parts[0].trim()
                                    val state = parts[1].trim()
                                    if (isFavorite) {
                                        favoritesManager.removeFavorite(city, state)
                                        Toast.makeText(context, "$city removed from favorites", Toast.LENGTH_SHORT).show()
                                    } else {
                                        favoritesManager.addFavorite(city, state)
                                        Toast.makeText(context, "$city added to favorites", Toast.LENGTH_SHORT).show()
                                    }
                                    isFavorite = !isFavorite
                                    favoritesManager.refreshFavorites()
                                }
                            },
                            shape = CircleShape,
                            containerColor = Color.White
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isFavorite) R.drawable.rem_fav else R.drawable.add_fav
                                ),
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = Color.Black
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSearchResultView) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.arrow_left),
                                        contentDescription = "Back",
                                        modifier = Modifier
                                            .clickable {
                                                isSearchResultView = false
                                                isSearchResult = false
                                                selectedLocation = ""
                                            }
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = selectedLocation,
                                        fontSize = 24.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(15.dp))
                                Text(
                                    text = "Search Result",
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f) // Optional styling
                                )
                            }
                        }else {
                            Text(
                                text = "WeatherApp",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.map_search_outline),
                            contentDescription = "Search",
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    showSearchBar = true
                                    if (selectedLocation.isNotEmpty()) {
                                        searchQuery = selectedLocation
                                    }
                                }
                        )
                    }

                    if (!isSearchResultView) {
                        DotIndicator(totalDots = 1 + favorites.size, selectedIndex = currentPage)
                    }

                    if (isSearchResultView) {
                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (weatherData.isNotEmpty()) {
                            Column {
                                LocationDisplay(
                                    location = selectedLocation,
                                    latLong = "",
                                    weatherData = weatherData,
                                    onInfoClick = { showDetailedView = true },
                                    onCardClick = { showDetailedView = true }
                                )
                            }
                        }
                    } else {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            when {
                                page == 0 -> {
                                    if (weatherData.isNotEmpty()) {
                                        Column {
                                            LocationDisplay(
                                                location = userLocation,
                                                latLong = latLong,
                                                weatherData = weatherData,
                                                onInfoClick = { showDetailedView = true },
                                                onCardClick = { showDetailedView = true }
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                                else -> {
                                    val favoriteIndex = page - 1
                                    val (city, state) = favorites[favoriteIndex]
                                    FavoriteLocationDisplay(city, state)
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showSearchBar,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { query ->
                    searchQuery = query
                    if (query.length >= 2) {
                        fetchPlaceSuggestions(query, context) { results ->
                            suggestions = results
                        }
                    }
                },
                onSearch = {
                    if (selectedLocation.isNotEmpty()) {
                        fetchWeatherForLocation(selectedLocation, context) { weather ->
                            weatherData = weather
                            isSearchResult = true
                            showSearchBar = false
                            searchQuery = ""
                            suggestions = emptyList()
                            isSearchResultView = true
                        }
                    }
                },
                active = showSearchBar,
                onActiveChange = { isActive ->
                    showSearchBar = isActive
                    if (!isActive) {
                        searchQuery = ""
                        suggestions = emptyList()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                placeholder = {
                    Text(
                        "Search for a city",
                        color = Color.White
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_left),
                        contentDescription = "Back",
                        modifier = Modifier.clickable {
                            showSearchBar = false
                            searchQuery = ""
                            suggestions = emptyList()
                            selectedLocation = ""
                            isSearchResult = false
                            isSearchResultView = false
                        }
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.clickable {
                            searchQuery = ""
                            suggestions = emptyList()
                        }
                    )
                }
            ) {
                suggestions.forEach { suggestion ->
                    ListItem(
                        headlineContent = { Text(suggestion) },
                        modifier = Modifier.clickable {
                            selectedLocation = suggestion
                            searchQuery = suggestion
                            isLoading = true
                            fetchWeatherForLocation(suggestion, context) { weather ->
                                weatherData = weather
                                isSearchResult = true
                                showSearchBar = false
                                searchQuery = ""
                                suggestions = emptyList()
                                isSearchResultView = true
                                isLoading = false
                            }
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun FavoriteLocationDisplay(city: String, state: String) {
    var weatherData by remember { mutableStateOf<List<WeatherItem>>(emptyList()) }
    var showDetailedView by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val favoritesManager = remember { FavoritesManager(context) }

    LaunchedEffect(city, state) {
        fetchWeatherForLocation("$city, $state", context) { weather ->
            weatherData = weather
        }
    }

    Box {
        if (showDetailedView) {
            DetailedView(
                onBackClick = { showDetailedView = false },
                location = "$city, $state",
                currentTemperature = weatherData.firstOrNull()?.temperatureHigh?.toInt() ?: 0,
                weatherItem = weatherData.firstOrNull() ?: WeatherItem(
                    date = "",
                    status = 0,
                    temperatureHigh = 0.0,
                    temperatureLow = 0.0,
                    windSpeed = 0.0,
                    humidity = 0.0,
                    cloudCover = 0.0,
                    visibility = 0.0,
                    pressure = 0.0
                ),
                weatherData = weatherData
            )
        } else {
            if (weatherData.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.padding(top = 16.dp)  // Adds padding from the top
                    ) {
                        LocationDisplay(
                            location = "$city, $state",
                            latLong = "",
                            weatherData = weatherData,
                            onInfoClick = { showDetailedView = true },
                            onCardClick = { showDetailedView = true }
                        )
                    }

                    FloatingActionButton(
                        onClick = {
                            favoritesManager.removeFavorite(city, state)
                            Toast.makeText(context, "$city removed from favorites", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .size(56.dp),
                        shape = CircleShape,
                        containerColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 12.dp
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.rem_fav),
                            contentDescription = "Remove from favorites",
                            tint = Color.Black
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}



@Composable
fun DotIndicator(totalDots: Int, selectedIndex: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(totalDots) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (index == selectedIndex) Color.White
                        else Color.White.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .padding(4.dp)
            )
            if (index < totalDots - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}


private fun checkIsFavorite(city: String, state: String, context: Context, onResult: (Boolean) -> Unit) {
    val queue = Volley.newRequestQueue(context)
    val url = "https://weatherapp-backend-772294567985.us-west1.run.app/api/favorites"

    val request = JsonArrayRequest(
        Request.Method.GET,
        url,
        null,
        { response ->
            val isFav = (0 until response.length()).any { i ->
                val favorite = response.getJSONObject(i)
                favorite.getString("city").equals(city, ignoreCase = true) &&
                        favorite.getString("state").equals(state, ignoreCase = true)
            }
            onResult(isFav)
        },
        { error ->
            Log.e("Favorites", "Error: ${error.message}")
            onResult(false)
        }
    )
    queue.add(request)
}

private fun addFavorite(city: String, state: String, context: Context, onResult: (Boolean) -> Unit) {
    val queue = Volley.newRequestQueue(context)
    val url = "https://weatherapp-backend-772294567985.us-west1.run.app/api/favorites"

    val jsonBody = JSONObject().apply {
        put("city", city)
        put("state", state)
    }

    val request = JsonObjectRequest(
        Request.Method.POST,
        url,
        jsonBody,
        { response ->
            onResult(true)
        },
        { error ->
            Log.e("Favorites", "Error: ${error.message}")
            onResult(false)
        }
    )
    queue.add(request)
}

private fun removeFavorite(city: String, context: Context, onResult: (Boolean) -> Unit) {
    val queue = Volley.newRequestQueue(context)
    val url = "https://weatherapp-backend-772294567985.us-west1.run.app/api/favorites/$city"

    val request = JsonObjectRequest(
        Request.Method.DELETE,
        url,
        null,
        { response ->
            onResult(true)
        },
        { error ->
            Log.e("Favorites", "Error: ${error.message}")
            onResult(false)
        }
    )
    queue.add(request)
}

private fun fetchWeatherForLocation(location: String, context: Context, onResult: (List<WeatherItem>) -> Unit) {
    val queue = Volley.newRequestQueue(context)
    val geocodingUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=${Uri.encode(location)}&key=AIzaSyARB0lVlj9JsGNHBWzapTor7Glre_f-P6s"

    val request = JsonObjectRequest(
        Request.Method.GET, geocodingUrl, null,
        { response ->
            val results = response.getJSONArray("results")
            if (results.length() > 0) {
                val location = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location")
                val lat = location.getDouble("lat")
                val lng = location.getDouble("lng")
                fetchWeatherData("$lat,$lng", context, onResult)
            }
        },
        { error ->
            Log.e("Geocoding", "Error: ${error.message}")
        }
    )
    queue.add(request)
}

private fun fetchWeatherData(coordinates: String, context: Context, onResult: (List<WeatherItem>) -> Unit) {
    val queue = Volley.newRequestQueue(context)
    val weatherUrl = "https://weatherapp-backend-450539621978.us-west1.run.app/api/weather?loc=$coordinates"

    val request = JsonArrayRequest(
        Request.Method.GET, weatherUrl, null,
        { response ->
            val weatherList = mutableListOf<WeatherItem>()
            for (i in 0 until response.length()) {
                val item = response.getJSONObject(i)
                weatherList.add(
                    WeatherItem(
                        date = item.getString("date"),
                        status = item.getInt("status"),
                        temperatureHigh = item.getDouble("temperatureHigh"),
                        temperatureLow = item.getDouble("temperatureLow"),
                        windSpeed = item.getDouble("windSpeed"),
                        humidity = item.getDouble("humidity"),
                        cloudCover = item.getDouble("cloudCover"),
                        visibility = item.getDouble("visibility"),
                        pressure = item.getDouble("pressure")
                    )
                )
            }
            onResult(weatherList)
        },
        { error ->
            Log.e("Weather", "Error: ${error.message}")
        }
    )
    queue.add(request)
}

private fun fetchInitialLocation(
    context: Context,
    onResult: (location: String, coordinates: String, weather: List<WeatherItem>) -> Unit
) {
    val queue = Volley.newRequestQueue(context)
    val ipinfoUrl = "https://ipinfo.io/json?token=28f772be3811cd"

    val locationRequest = JsonObjectRequest(
        Request.Method.GET, ipinfoUrl, null,
        { response ->
            try {
                val city = response.optString("city", "Unknown")
                val region = response.optString("region", "Unknown")
                val country = response.optString("country", "Unknown")
                val loc = response.optString("loc", "Unknown")
                val latLongArray = loc.split(",")
                val latitude = latLongArray.getOrNull(0) ?: "Unknown"
                val longitude = latLongArray.getOrNull(1) ?: "Unknown"

                val location = "$city, $region, $country"
                val coordinates = "Latitude: $latitude, Longitude: $longitude"

                val weatherUrl = "https://weatherapp-backend-450539621978.us-west1.run.app/api/weather?loc=$latitude,$longitude"

                val weatherRequest = JsonArrayRequest(
                    Request.Method.GET, weatherUrl, null,
                    { weatherResponse ->
                        val weatherList = mutableListOf<WeatherItem>()
                        for (i in 0 until weatherResponse.length()) {
                            val weatherItem = weatherResponse.getJSONObject(i)
                            weatherList.add(
                                WeatherItem(
                                    date = weatherItem.getString("date"),
                                    status = weatherItem.getInt("status"),
                                    temperatureHigh = weatherItem.getDouble("temperatureHigh"),
                                    temperatureLow = weatherItem.getDouble("temperatureLow"),
                                    windSpeed = weatherItem.getDouble("windSpeed"),
                                    humidity = weatherItem.getDouble("humidity"),
                                    cloudCover = weatherItem.getDouble("cloudCover"),
                                    visibility = weatherItem.getDouble("visibility"),
                                    pressure = weatherItem.getDouble("pressure")
                                )
                            )
                        }
                        onResult(location, coordinates, weatherList)
                    },
                    { error ->
                        Log.e("Weather", "Error fetching weather: ${error.message}")
                        onResult("Error", "Error", emptyList())
                    }
                )
                queue.add(weatherRequest)

            } catch (e: Exception) {
                Log.e("Location", "Error parsing location: ${e.message}")
                onResult("Error", "Error", emptyList())
            }
        },
        { error ->
            Log.e("Location", "Error fetching location: ${error.message}")
            onResult("Error", "Error", emptyList())
        }
    )
    queue.add(locationRequest)
}


@Composable
fun LocationDisplay(location: String, latLong: String, weatherData: List<WeatherItem>, onInfoClick: () -> Unit, onCardClick: () -> Unit) {
    val currentWeather = weatherData.first()
    location.split(",").take(2).joinToString(",")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true),
                onClick = onCardClick
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF272727),
            contentColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxWidth(),contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = getDrawableResourceByName(WeatherMappings.getWeatherIconResourceName(currentWeather.status.toString()))),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "${currentWeather.temperatureHigh.toInt()}°F",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = WeatherMappings.getWeatherDescription(currentWeather.status),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = location.split(",").take(2).joinToString(", "),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.information_outline),
                contentDescription = "Information",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(24.dp)
                    .clickable(onClick = onInfoClick),
                tint = Color.Black
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF272727),
            contentColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetailItem(R.drawable.humidity, "${currentWeather.humidity.toInt()}%", "Humidity")
                WeatherDetailItem(R.drawable.wind, "${currentWeather.windSpeed}mph", "Wind Speed")
                WeatherDetailItem(R.drawable.visibility, "${currentWeather.visibility}mi", "Visibility")
                WeatherDetailItem(R.drawable.pressure, "${currentWeather.pressure}inHg", "Pressure")
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF272727),
            contentColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            weatherData.forEach { weather ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = weather.date.substring(0, 10),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(45.dp))
                    Image(
                        painter = painterResource(id = getDrawableResourceByName(WeatherMappings.getWeatherIconResourceName(weather.status.toString()))),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "${weather.temperatureLow.toInt()}",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${weather.temperatureHigh.toInt()}",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
                if (weather != weatherData.last()) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun WeatherDetailItem(iconRes: Int, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(55.dp),
            tint = Color.Black
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            fontSize = 19.sp
        )
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun getDrawableResourceByName(name: String): Int {
    return LocalContext.current.resources.getIdentifier(name, "drawable", LocalContext.current.packageName)
}

data class WeatherItem(
    val date: String,
    val status: Int,
    val temperatureHigh: Double,
    val temperatureLow: Double,
    val windSpeed: Double,
    val humidity: Double,
    val cloudCover: Double,
    val visibility: Double,
    val pressure: Double
)

@Composable
fun DetailedView(onBackClick: () -> Unit, location: String, currentTemperature: Int, weatherItem: WeatherItem, weatherData: List<WeatherItem>) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val cityState = location.split(",").take(2).joinToString(", ").trim()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
            .background(Color(0xFF121212))
    ) {
        Spacer(modifier = Modifier.height(15.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 25.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_left),
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = cityState,
                fontSize = 22.sp,
                modifier = Modifier.padding(start = 16.dp).padding(end = 32.dp),
                maxLines = 1
            )
            IconButton(
                onClick = {
                    createTwitterIntent(context, cityState, currentTemperature)
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF121212))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.twitter),
                    contentDescription = "Share on Twitter",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified
                )
            }
        }
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Black,
            contentColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.today),
                        contentDescription = "Today",
                        tint = Color.White
                    )
                },
                text = { Text("TODAY", color = Color.White) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.weekly_tab),
                        contentDescription = "Weekly",
                        tint = Color.White
                    )
                },
                text = { Text("WEEKLY", color = Color.White) }
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.thermometer),
                        contentDescription = "Weather Data",
                        tint = Color.White
                    )
                },
                text = { Text("WEATHER DATA", color = Color.White) }
            )
        }

        // Tab content
        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(top = 0.dp)
            .padding(start = 0.dp)
            .padding(end = 0.dp)
        ) {
            when (selectedTabIndex) {
                0 -> TodayTabContent(weatherItem)
                1 -> TemperatureRangeTab()
                2 -> WeatherDataTab()
            }
        }
    }
}

@Composable
fun TemperatureRangeTab() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Temperature Range",
            fontSize = 24.sp,
            color = Color.White,
            modifier = Modifier.padding(top = 80.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Image(
            painter = painterResource(id = R.drawable.chart_temp),
            contentDescription = "Temperature Chart",
            modifier = Modifier.fillMaxWidth()
                .height(600.dp)
        )
    }
}


@Composable
fun WeatherDataTab() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Weather Data",
            fontSize = 24.sp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 80.dp)
        )
        Spacer(modifier = Modifier.height(30.dp))

        Image(
            painter = painterResource(id = R.drawable.stat_summary),
            contentDescription = "Weather Data Tab",
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
        )
    }
}


fun createTwitterIntent(context: Context, cityState: String, temperature: Int) {
    val tweetText = "Check out $cityState's weather! It is $temperature°F! #CSCI571WeatherSearch"
    val tweetUrl = "https://twitter.com/intent/tweet?text=${Uri.encode(tweetText)}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl))
    context.startActivity(intent)
}

@Composable
fun TodayTabContent(weatherItem: WeatherItem) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(9) { index ->
            WeatherCard(weatherItem, index)
        }
    }
}

@Composable
private fun WeatherCard(weatherItem: WeatherItem, index: Int) {
    val (icon, value, label) = when (index) {
        0 -> Triple(R.drawable.wind, "${weatherItem.windSpeed}mph", "Wind Speed")
        1 -> Triple(R.drawable.pressure, "29.96inHg", "Pressure")
        2 -> Triple(R.drawable.weather_pouring, "0%", "Precipitation")
        3 -> Triple(R.drawable.thermometer, "${weatherItem.temperatureHigh}°F", "Temperature")
        4 -> Triple(
            getDrawableResourceByName(WeatherMappings.getWeatherIconResourceName(weatherItem.status.toString())),
            WeatherMappings.getWeatherDescription(weatherItem.status),
            ""
        )
        5 -> Triple(R.drawable.humidity, "${weatherItem.humidity}%", "Humidity")
        6 -> Triple(R.drawable.visibility, "${weatherItem.visibility}mi", "Visibility")
        7 -> Triple(R.drawable.cloud_cover, "${weatherItem.cloudCover}%", "Cloud Cover")
        else -> Triple(R.drawable.uv, "1", "Ozone")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF272727),
            contentColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp)
                .padding(horizontal = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (index == 4) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = label,
                    modifier = Modifier.size(70.dp)
                )
            } else {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = label,
                    modifier = Modifier.size(70.dp),
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LocationDisplayPreview() {
    WeatherAppTheme {
        LocationDisplay(
            location = "Sample City, Sample Region, Sample Country",
            latLong = "Latitude: 12.34, Longitude: 56.78",
            weatherData = listOf(),
            onInfoClick = {},
            onCardClick = {}
        )
    }
}