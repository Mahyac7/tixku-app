package com.tixku.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tixku.app.data.BookingStatus
import com.tixku.app.data.TixkuRepository
import com.tixku.app.data.label
import com.tixku.app.util.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    onBack: () -> Unit,
    onBookingClick: (String) -> Unit,
) {
    val bookings by TixkuRepository.bookings.collectAsState()

    // Refresh from the backend (or local store) each time this screen opens.
    LaunchedEffect(Unit) { runCatching { TixkuRepository.refreshBookings() } }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Booking Saya") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                }
            },
        )
        if (bookings.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada booking.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(bookings) { b ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBookingClick(b.id) },
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(b.movieTitle, fontWeight = FontWeight.Bold)
                                Text(
                                    if (b.status == BookingStatus.CONFIRMED) "AKTIF" else "DIBATALKAN",
                                    color = if (b.status == BookingStatus.CONFIRMED) Color(0xFF43A047) else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                )
                            }
                            Text("${b.cinemaName} • ${b.showTime} • ${b.format.label()}", fontSize = 13.sp)
                            Text("Kursi: ${b.seatIds.joinToString(", ")}", fontSize = 13.sp)
                            Text("${b.id} • ${formatRupiah(b.totalPrice)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}
