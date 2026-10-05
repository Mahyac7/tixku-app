package com.tixku.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tixku.app.data.City
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.CircularProgressIndicator
import com.tixku.app.data.CinemaShows
import com.tixku.app.data.Movie
import com.tixku.app.data.TixkuRepository
import com.tixku.app.data.label
import com.tixku.app.ui.components.MoviePoster
import com.tixku.app.util.formatRupiah

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MovieDetailScreen(
    movieId: String,
    city: City,
    onBack: () -> Unit,
    onShowSelected: (String) -> Unit,
) {
    var movie by remember { mutableStateOf<Movie?>(null) }
    var showsByCinema by remember { mutableStateOf<List<CinemaShows>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(movieId, city.id) {
        loading = true
        movie = runCatching { TixkuRepository.getMovie(movieId) }.getOrNull()
        showsByCinema = runCatching { TixkuRepository.getShowsForMovie(movieId, city.id) }.getOrDefault(emptyList())
        loading = false
    }

    val movieData = movie
    if (movieData == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (loading) CircularProgressIndicator() else Text("Film tidak ditemukan.")
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(movieData.title, maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                }
            },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Row {
                MoviePoster(
                    title = movieData.title,
                    posterColor = movieData.posterColor,
                    modifier = Modifier
                        .width(120.dp)
                        .height(170.dp),
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(movieData.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                        Text(" ${movieData.rating} / 10", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("${movieData.durationMinutes} menit • ${movieData.language}", fontSize = 13.sp)
                    Text("Rating usia: ${movieData.certification}", fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        movieData.genres.forEach { g -> Chip(g) }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Sinopsis", fontWeight = FontWeight.Bold)
            Text(movieData.synopsis, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            Spacer(Modifier.height(8.dp))
            Text("Pemain: ${movieData.cast.joinToString(", ")}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

            Spacer(Modifier.height(20.dp))
            Text("Jadwal Tayang di ${city.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))

            if (showsByCinema.isEmpty()) {
                Text("Belum ada jadwal untuk film ini di ${city.name}.")
            } else {
                showsByCinema.forEach { (cinema, shows) ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Text(cinema.name, fontWeight = FontWeight.SemiBold)
                            Text(cinema.address, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Spacer(Modifier.height(10.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                shows.forEach { show ->
                                    Box(
                                        modifier = Modifier
                                            .padding(bottom = 8.dp)
                                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                            .clickable { onShowSelected(show.id) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(show.startTime, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Text(show.format.label(), fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                            Text(formatRupiah(show.price), fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Chip(text: String) {
    Box(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
