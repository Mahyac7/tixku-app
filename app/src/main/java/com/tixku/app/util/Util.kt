package com.tixku.app.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(amount: Int): String {
    val nf = NumberFormat.getNumberInstance(Locale("in", "ID"))
    return "Rp " + nf.format(amount)
}

/** Generates a QR code [ImageBitmap] from the given payload for e-tickets. */
fun generateQrBitmap(content: String, size: Int = 512): ImageBitmap {
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    return bmp.asImageBitmap()
}
