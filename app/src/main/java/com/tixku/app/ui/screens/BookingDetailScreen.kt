package com.tixku.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tixku.app.data.BookingStatus
import com.tixku.app.data.TixkuRepository
import com.tixku.app.data.label
import com.tixku.app.util.formatRupiah
import com.tixku.app.util.generateQrBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    bookingId: String,
    onBack: () -> Unit,
    onHome: () -> Unit,
) {
    val bookings by TixkuRepository.bookings.collectAsState()
    val booking = bookings.firstOrNull { it.id == bookingId } ?: return

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("E-Ticket") })
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (booking.status == BookingStatus.CONFIRMED) {
                Icon(
                    Icons.Filled.CheckCircle,
                    null,
                    tint = Color(0xFF43A047),
                    modifier = Modifier.size(56.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text("Booking Terkonfirmasi!", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            } else {
                Text("Booking Dibatalkan", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(20.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (booking.status == BookingStatus.CONFIRMED) {
                        val qr = generateQrBitmap(booking.qrPayload, size = 400)
                        Image(
                            bitmap = qr,
                            contentDescription = "QR code tiket",
                            modifier = Modifier
                                .size(200.dp)
                                .background(Color.White)
                                .padding(8.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Tunjukkan QR ini di pintu masuk", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    } else {
                        Box(
                            Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) { Text("TIKET TIDAK BERLAKU", textAlign = TextAlign.Center) }
                    }

                    Spacer(Modifier.height(16.dp))
                    TicketRow("Kode Booking", booking.id)
                    TicketRow("Film", booking.movieTitle)
                    TicketRow("Bioskop", booking.cinemaName)
                    TicketRow("Jam", "${booking.showTime} • ${booking.format.label()}")
                    TicketRow("Kursi", booking.seatIds.joinToString(", "))
                    TicketRow("Total", formatRupiah(booking.totalPrice))
                }
            }

            Spacer(Modifier.height(24.dp))
            if (booking.status == BookingStatus.CONFIRMED) {
                OutlinedButton(
                    onClick = { TixkuRepository.cancelBooking(booking.id) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Batalkan Booking") }
                Spacer(Modifier.height(8.dp))
            }
            Button(onClick = onHome, modifier = Modifier.fillMaxWidth()) {
                Text("Kembali ke Beranda")
            }
        }
    }
}

@Composable
private fun TicketRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}
