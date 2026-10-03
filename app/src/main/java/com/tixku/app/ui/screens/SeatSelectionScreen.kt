package com.tixku.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tixku.app.data.SeatStatus
import com.tixku.app.data.TixkuRepository
import com.tixku.app.data.label
import com.tixku.app.util.formatRupiah
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelectionScreen(
    showId: String,
    onBack: () -> Unit,
    onBookingConfirmed: (String) -> Unit,
) {
    val show = TixkuRepository.getShow(showId) ?: return
    val movie = TixkuRepository.getMovie(show.movieId) ?: return
    val cinema = TixkuRepository.getCinema(show.cinemaId) ?: return

    var selected by remember { mutableStateOf(setOf<String>()) }
    // Recompute the seat map whenever selection changes; selected seats render as held-by-me.
    val seats = remember(selected) { TixkuRepository.getSeatMap(showId, selected) }

    var showPaymentDialog by remember { mutableStateOf(false) }
    var processing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val total = show.price * selected.size

    Column(Modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                Column {
                    Text(movie.title, maxLines = 1, fontWeight = FontWeight.Bold)
                    Text(
                        "${cinema.name} • ${show.startTime} • ${show.format.label()}",
                        fontSize = 12.sp,
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                }
            },
        )

        // Screen indicator
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        )
        Text(
            "LAYAR",
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )

        Spacer(Modifier.height(16.dp))

        // Seat grid
        val rows = seats.groupBy { it.row }
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            rows.forEach { (rowLabel, rowSeats) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(rowLabel.toString(), modifier = Modifier.width(20.dp), fontSize = 12.sp)
                    rowSeats.forEach { seat ->
                        val isSelected = seat.id in selected
                        val color = when {
                            seat.status == SeatStatus.BOOKED -> Color(0xFFBDBDBD)
                            seat.status == SeatStatus.LOCKED && !isSelected -> Color(0xFFFFB300)
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Box(
                            modifier = Modifier
                                .padding(3.dp)
                                .size(26.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(color)
                                .clickable(
                                    enabled = seat.status != SeatStatus.BOOKED &&
                                        !(seat.status == SeatStatus.LOCKED && !isSelected)
                                ) {
                                    selected = if (isSelected) selected - seat.id else selected + seat.id
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                seat.number.toString(),
                                fontSize = 9.sp,
                                color = if (isSelected || seat.status == SeatStatus.BOOKED) Color.White
                                else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        LegendRow()

        Spacer(Modifier.height(16.dp))
        Column(Modifier.padding(horizontal = 16.dp)) {
            if (selected.isNotEmpty()) {
                Text("Kursi: ${selected.sorted().joinToString(", ")}", fontWeight = FontWeight.SemiBold)
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Total", fontSize = 14.sp)
                Text(formatRupiah(total), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    // Booking Service: atomically lock seats before payment.
                    val locked = TixkuRepository.lockSeats(showId, selected)
                    if (locked) {
                        showPaymentDialog = true
                    } else {
                        errorMessage = "Maaf, salah satu kursi baru saja diambil orang lain. Silakan pilih kursi lain."
                        selected = emptySet()
                    }
                },
                enabled = selected.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Kunci Kursi & Bayar")
            }
        }
        Spacer(Modifier.height(16.dp))
    }

    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!processing) {
                    TixkuRepository.releaseSeats(showId, selected)
                    showPaymentDialog = false
                }
            },
            title = { Text("Pembayaran") },
            text = {
                Column {
                    Text("Film: ${movie.title}")
                    Text("Bioskop: ${cinema.name}")
                    Text("Jam: ${show.startTime} • ${show.format.label()}")
                    Text("Kursi: ${selected.sorted().joinToString(", ")}")
                    Spacer(Modifier.height(8.dp))
                    Text("Total: ${formatRupiah(total)}", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    if (processing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Memproses pembayaran...")
                        }
                    } else {
                        Text(
                            "Kursi dikunci selama 5 menit. Simulasi pembayaran via Payment Gateway.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !processing,
                    onClick = {
                        processing = true
                        scope.launch {
                            delay(1200) // simulate payment gateway round-trip
                            val booking = TixkuRepository.confirmBooking(showId, selected, paymentSuccess = true)
                            processing = false
                            showPaymentDialog = false
                            if (booking != null) {
                                onBookingConfirmed(booking.id)
                            } else {
                                errorMessage = "Pembayaran gagal. Kursi telah dilepas kembali."
                                selected = emptySet()
                            }
                        }
                    },
                ) { Text("Bayar Sekarang") }
            },
            dismissButton = {
                TextButton(
                    enabled = !processing,
                    onClick = {
                        TixkuRepository.releaseSeats(showId, selected)
                        showPaymentDialog = false
                    },
                ) { Text("Batal") }
            },
        )
    }

    errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            confirmButton = { TextButton(onClick = { errorMessage = null }) { Text("OK") } },
            title = { Text("Info") },
            text = { Text(msg) },
        )
    }
}

@Composable
private fun LegendRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        LegendItem(MaterialTheme.colorScheme.surfaceVariant, "Tersedia")
        LegendItem(MaterialTheme.colorScheme.primary, "Pilihanmu")
        LegendItem(Color(0xFFFFB300), "Dikunci")
        LegendItem(Color(0xFFBDBDBD), "Terisi")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color),
        )
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp)
    }
}
