package com.tixku.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tixku.app.data.City
import com.tixku.app.data.MockData
import com.tixku.app.ui.screens.BookingDetailScreen
import com.tixku.app.ui.screens.CitySelectScreen
import com.tixku.app.ui.screens.HomeScreen
import com.tixku.app.ui.screens.MovieDetailScreen
import com.tixku.app.ui.screens.MyBookingsScreen
import com.tixku.app.ui.screens.SeatSelectionScreen
import com.tixku.app.ui.theme.TixkuTheme

object Routes {
    const val CITY = "city"
    const val HOME = "home"
    const val MOVIE = "movie/{movieId}"
    const val SEATS = "seats/{showId}"
    const val BOOKING = "booking/{bookingId}"
    const val MY_BOOKINGS = "myBookings"

    fun movie(movieId: String) = "movie/$movieId"
    fun seats(showId: String) = "seats/$showId"
    fun booking(bookingId: String) = "booking/$bookingId"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TixkuTheme {
                TixkuApp()
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun TixkuApp() {
    val navController = rememberNavController()
    var selectedCity by remember { mutableStateOf<City?>(null) }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.CITY,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.CITY) {
                CitySelectScreen(
                    onCitySelected = { city ->
                        selectedCity = city
                        navController.navigate(Routes.HOME)
                    }
                )
            }
            composable(Routes.HOME) {
                HomeScreen(
                    city = selectedCity ?: MockData.cities.first(),
                    onChangeCity = { navController.navigate(Routes.CITY) },
                    onMovieClick = { navController.navigate(Routes.movie(it)) },
                    onMyBookings = { navController.navigate(Routes.MY_BOOKINGS) },
                )
            }
            composable(Routes.MOVIE) { backStack ->
                val movieId = backStack.arguments?.getString("movieId") ?: return@composable
                MovieDetailScreen(
                    movieId = movieId,
                    city = selectedCity ?: MockData.cities.first(),
                    onBack = { navController.popBackStack() },
                    onShowSelected = { navController.navigate(Routes.seats(it)) },
                )
            }
            composable(Routes.SEATS) { backStack ->
                val showId = backStack.arguments?.getString("showId") ?: return@composable
                SeatSelectionScreen(
                    showId = showId,
                    onBack = { navController.popBackStack() },
                    onBookingConfirmed = { bookingId ->
                        navController.navigate(Routes.booking(bookingId)) {
                            popUpTo(Routes.HOME)
                        }
                    },
                )
            }
            composable(Routes.MY_BOOKINGS) {
                MyBookingsScreen(
                    onBack = { navController.popBackStack() },
                    onBookingClick = { navController.navigate(Routes.booking(it)) },
                )
            }
            composable(Routes.BOOKING) { backStack ->
                val bookingId = backStack.arguments?.getString("bookingId") ?: return@composable
                BookingDetailScreen(
                    bookingId = bookingId,
                    onBack = { navController.popBackStack() },
                    onHome = {
                        navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
                    },
                )
            }
        }
    }
}
