package com.tixku.app.data

/**
 * In-memory seed data standing in for the Primary Database (PostgreSQL/MySQL)
 * described in the system design. In a real deployment these would be served
 * by the Movie / Theatre / Show microservices behind the API Gateway.
 */
object MockData {

    val cities = listOf(
        City("jkt", "Jakarta"),
        City("bdg", "Bandung"),
        City("sby", "Surabaya"),
        City("yog", "Yogyakarta"),
    )

    val movies = listOf(
        Movie(
            id = "m1",
            title = "Langit Senja",
            genres = listOf("Drama", "Romance"),
            language = "Indonesia",
            durationMinutes = 118,
            rating = 8.4,
            synopsis = "Kisah dua orang asing yang bertemu di stasiun tua dan menemukan arti pulang.",
            cast = listOf("Nadia Putri", "Reza Mahendra", "Sari Wulandari"),
            posterColor = 0xFFE53935,
            certification = "R13+",
        ),
        Movie(
            id = "m2",
            title = "Protocol Zero",
            genres = listOf("Action", "Thriller"),
            language = "English",
            durationMinutes = 134,
            rating = 7.9,
            synopsis = "Seorang analis siber harus menghentikan serangan global dalam waktu 24 jam.",
            cast = listOf("Chris Dalton", "Mei Lin", "Omar Said"),
            posterColor = 0xFF3949AB,
            certification = "D17+",
        ),
        Movie(
            id = "m3",
            title = "Petualangan Si Kancil",
            genres = listOf("Animation", "Family"),
            language = "Indonesia",
            durationMinutes = 96,
            rating = 8.1,
            synopsis = "Kancil cerdik berpetualang menyelamatkan hutan dari kekeringan besar.",
            cast = listOf("Dubbing Studio Nusantara"),
            posterColor = 0xFF43A047,
            certification = "SU",
        ),
        Movie(
            id = "m4",
            title = "Nebula Rising",
            genres = listOf("Sci-Fi", "Adventure"),
            language = "English",
            durationMinutes = 142,
            rating = 8.7,
            synopsis = "Awak pesawat kolonisasi menemukan sebuah peradaban di tepi galaksi.",
            cast = listOf("Ava Thompson", "Rian Pradana", "Keiko Sato"),
            posterColor = 0xFF8E24AA,
            certification = "R13+",
        ),
        Movie(
            id = "m5",
            title = "Tawa Terakhir",
            genres = listOf("Comedy"),
            language = "Indonesia",
            durationMinutes = 101,
            rating = 7.3,
            synopsis = "Seorang pelawak tua mencoba comeback dengan satu pertunjukan terakhir.",
            cast = listOf("Budi Santoso", "Lina Marlina"),
            posterColor = 0xFFF9A825,
            certification = "R13+",
        ),
        Movie(
            id = "m6",
            title = "Bayangan di Lorong",
            genres = listOf("Horror", "Mystery"),
            language = "Indonesia",
            durationMinutes = 108,
            rating = 7.6,
            synopsis = "Sebuah keluarga pindah ke rumah tua dan menemukan rahasia yang terkubur.",
            cast = listOf("Dewi Anggraini", "Fajar Nugroho"),
            posterColor = 0xFF00897B,
            certification = "D17+",
        ),
    )

    val cinemas = listOf(
        Cinema("c1", "Tixku Grand Plaza", "jkt", "Jl. Jenderal Sudirman No. 1, Jakarta"),
        Cinema("c2", "Tixku Central Park", "jkt", "Jl. Letjen S. Parman, Jakarta"),
        Cinema("c3", "Tixku Paris Van Java", "bdg", "Jl. Sukajadi No. 137, Bandung"),
        Cinema("c4", "Tixku Tunjungan", "sby", "Jl. Basuki Rahmat, Surabaya"),
        Cinema("c5", "Tixku Malioboro", "yog", "Jl. Malioboro No. 52, Yogyakarta"),
    )

    // Shows are generated per movie+cinema to keep seed data compact.
    val shows: List<Show> = buildList {
        val times = listOf("12:30", "15:15", "18:00", "20:45")
        var counter = 0
        for (cinema in cinemas) {
            for (movie in movies) {
                // Each cinema shows 3 of the 6 movies (rotating) to feel realistic.
                if ((movie.id.last().code + cinema.id.last().code) % 2 == 0) continue
                for ((i, t) in times.withIndex()) {
                    counter++
                    val format = when (i) {
                        0 -> ShowFormat.TWO_D
                        1 -> ShowFormat.TWO_D
                        2 -> ShowFormat.THREE_D
                        else -> ShowFormat.IMAX
                    }
                    val base = when (format) {
                        ShowFormat.TWO_D -> 45000
                        ShowFormat.THREE_D -> 60000
                        ShowFormat.IMAX -> 85000
                    }
                    add(
                        Show(
                            id = "s$counter",
                            movieId = movie.id,
                            cinemaId = cinema.id,
                            startTime = t,
                            format = format,
                            price = base,
                        )
                    )
                }
            }
        }
    }
}
