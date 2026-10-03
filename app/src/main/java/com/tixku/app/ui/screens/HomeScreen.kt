package com.tixku.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tixku.app.data.City
import com.tixku.app.data.Movie
import com.tixku.app.data.TixkuRepository
import com.tixku.app.ui.components.MoviePoster

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    city: City,
    onChangeCity: () -> Unit,
    onMovieClick: (String) -> Unit,
    onMyBookings: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val movies: List<Movie> = TixkuRepository.searchMovies(city.id, query)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = Color.White,
            ),
            title = {
                Column {
                    Text("Tixku", fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onChangeCity() },
                    ) {
                        Icon(Icons.Filled.LocationOn, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text(city.name, color = Color.White, fontSize = 12.sp)
                        Icon(Icons.Filled.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            },
            actions = {
                IconButton(onClick = onMyBookings) {
                    Icon(Icons.Filled.ConfirmationNumber, "Booking saya", tint = Color.White)
                }
            },
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Cari film, genre, atau bahasa") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            singleLine = true,
        )

        Text(
            "Sedang Tayang di ${city.name}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))

        if (movies.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tidak ada film yang cocok.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(movies) { movie ->
                    Column(modifier = Modifier.clickable { onMovieClick(movie.id) }) {
                        MoviePoster(
                            title = movie.title,
                            posterColor = movie.posterColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.7f),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(movie.title, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Star,
                                null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                " ${movie.rating} • ${movie.genres.first()}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
            }
        }
    }
}
