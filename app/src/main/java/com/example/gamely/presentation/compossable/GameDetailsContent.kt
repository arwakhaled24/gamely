package com.example.gamely.presentation.compossable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImage
import com.example.gamely.presentation.viewmodel.gamedetails.GameDetailsState
@Composable
 fun GameDetailsContent(
    state: GameDetailsState,
    onBack: () -> Unit,
    onToggleDescription: () -> Unit
) {
    val game = state.gameDetails ?: return
    val scrollState = rememberScrollState()
    val description = HtmlCompat.fromHtml(
        game.description,
        HtmlCompat.FROM_HTML_MODE_COMPACT
    ).toString()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box {
            AsyncImage(
                model = game.backgroundImageAdditional?: game.backgroundImage,
                contentDescription = "${game.name} background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(16.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }
        Row(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = game.backgroundImage,
                contentDescription = "cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = game.name,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text("Released: ${game.released}")
                Spacer(Modifier.height(2.dp))
                Text("Rating: ${game.rating}")
                Spacer(Modifier.height(2.dp))
                Text("Playtime: ${game.playtime} hrs")
            }
        }
        Spacer(Modifier.height(8.dp))
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Description",
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (state.isDescriptionExpanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis
            )
            if (description.length > 200) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (state.isDescriptionExpanded) "See Less" else "See More",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onToggleDescription() }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        if (game.genres.isNotEmpty()) {
            GenresSection(genres = game.genres)
        }
        Spacer(Modifier.height(16.dp))
    }
}