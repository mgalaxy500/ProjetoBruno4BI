package com.example.myapplication

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

data class Movie(
    val id: Int = 0,
    val title: String,
    val year: Int,
    val tags: String,
    val plannedAt: Long,
    val watched: Boolean = false,
    val rating: Int? = null
)

class MainActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Música de fundo
        mediaPlayer = MediaPlayer.create(this, R.raw.halloween_theme).apply {
            isLooping = true
            start()
        }

        setContent {
            HorrorMovieListApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorrorMovieListApp() {
    var title by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Slasher") }
    var plannedDate by remember { mutableStateOf("") }
    var movies by remember { mutableStateOf(listOf<Movie>()) }
    var selectedFilter by remember { mutableStateOf<String?>(null) }

    var showRatingDialog by remember { mutableStateOf(false) }
    var movieToRate by remember { mutableStateOf<Movie?>(null) }
    var ratingText by remember { mutableStateOf("5") }

    val categories = listOf("Slasher", "Clássico", "Psicológico")

    val halloweenFont = FontFamily.Cursive
    val listFont = FontFamily.SansSerif

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFF8C00),
            onPrimary = Color.Black,
            background = Color(0xFF1A1A1A),
            onBackground = Color(0xFFFF8C00)
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A1A1A), Color(0xFF0D0D0D))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text(
                    "🎃 Lista de Filmes de Terror 👻",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = halloweenFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = Color(0xFFFF8C00)
                    )
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Ano") },
                    modifier = Modifier.fillMaxWidth()
                )

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFFFF8C00),
                            unfocusedBorderColor = Color(0xFFFF8C00),
                            focusedTrailingIconColor = Color(0xFFFF8C00)
                        ),
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = plannedDate,
                    onValueChange = { plannedDate = it },
                    label = { Text("Data planejada (ex: 31/10/2025)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val newMovie = Movie(
                                id = (movies.maxOfOrNull { it.id } ?: 0) + 1,
                                title = title,
                                year = year.toIntOrNull() ?: 0,
                                tags = category,
                                plannedAt = parseDateToMillis(plannedDate)
                            )
                            movies = movies + newMovie
                            title = ""
                            year = ""
                            category = "Slasher"
                            plannedDate = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C00))
                ) {
                    Text("Adicionar Filme", color = Color.Black, fontFamily = halloweenFont)
                }

                Spacer(Modifier.height(16.dp))
                FilterSection(selectedFilter = selectedFilter, onSelect = { selectedFilter = it })

                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val filteredMovies = movies.filter {
                        selectedFilter == null || it.tags.contains(selectedFilter!!, ignoreCase = true)
                    }
                    items(filteredMovies) { movie ->
                        MovieCard(
                            movie = movie,
                            onMarkWatched = { m ->
                                movieToRate = m
                                showRatingDialog = true
                            },
                            halloweenFont = halloweenFont,
                            listFont = listFont
                        )
                    }
                }

                if (showRatingDialog && movieToRate != null) {
                    AlertDialog(
                        onDismissRequest = { showRatingDialog = false },
                        title = { Text("Dê uma nota para ${movieToRate!!.title}") },
                        text = {
                            OutlinedTextField(
                                value = ratingText,
                                onValueChange = { ratingText = it },
                                label = { Text("Nota (0 a 10)") }
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                val rating = ratingText.toIntOrNull()?.coerceIn(0, 10)
                                if (rating != null) {
                                    movies = movies.map {
                                        if (it.id == movieToRate!!.id)
                                            it.copy(watched = true, rating = rating)
                                        else it
                                    }
                                }
                                showRatingDialog = false
                            }) {
                                Text("Salvar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRatingDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterSection(selectedFilter: String?, onSelect: (String?) -> Unit) {
    val options = listOf("Todos", "Slasher", "Clássico", "Psicológico")
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        options.forEach { opt ->
            FilterChip(
                selected = (opt == "Todos" && selectedFilter == null) || selectedFilter?.equals(opt, true) == true,
                onClick = { onSelect(if (opt == "Todos") null else opt) },
                label = { Text(opt) }
            )
        }
    }
}

@Composable
fun MovieCard(
    movie: Movie,
    onMarkWatched: (Movie) -> Unit,
    halloweenFont: FontFamily,
    listFont: FontFamily
) {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateStr = if (movie.plannedAt > 0) formatter.format(Date(movie.plannedAt)) else "-"

    val cardColor = Color(0xFFFF8C00)
    val watchedColor = Color(0xFF14542F)

    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1A1A1A), Color(0xFF0D0D0D))
                )
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(Modifier.padding(8.dp)) {
            Text(
                "${movie.title} (${movie.year}) 🎃",
                fontFamily = listFont,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text("Categoria: ${movie.tags} 👻", color = Color.Black, fontFamily = listFont)
            Text("Data planejada: $dateStr 🕸️", color = Color.Black, fontFamily = listFont)

            if (movie.watched) {
                Text("✅ Assistido - Nota: ${movie.rating ?: "-"}", color = watchedColor, fontFamily = listFont)
            } else {
                Button(
                    onClick = { onMarkWatched(movie) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Marcar como assistido 🧛", color = cardColor, fontFamily = halloweenFont)
                }
            }
        }
    }
}

fun parseDateToMillis(dateStr: String): Long {
    return try {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formatter.parse(dateStr)?.time ?: 0
    } catch (e: Exception) {
        0
    }
}
