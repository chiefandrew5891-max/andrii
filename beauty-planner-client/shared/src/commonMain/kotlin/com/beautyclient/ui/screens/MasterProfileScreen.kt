package com.beautyclient.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beautyclient.di.AppModule
import com.beautyclient.domain.models.MasterProfile
import com.beautyclient.domain.models.MasterReview

/**
 * Shows the public profile of a master with their rating, bio, and calls-to-action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterProfileScreen(
    masterId: String,
    onBack: () -> Unit,
    onBookClick: () -> Unit,
    onReviewsClick: () -> Unit
) {
    var master by remember { mutableStateOf<MasterProfile?>(null) }
    var recentReviews by remember { mutableStateOf<List<MasterReview>>(emptyList()) }

    LaunchedEffect(masterId) {
        master = AppModule.mastersRepository.getMasterById(masterId)
        recentReviews = AppModule.reviewsRepository.getReviewsForMaster(masterId).take(3)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(master?.displayName ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        master?.let { m ->
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Header
                    Text(m.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        m.category.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("${m.city} · ★ ${m.ratingAvg} (${m.reviewCount} reviews)", style = MaterialTheme.typography.bodySmall)
                }
                if (m.bio.isNotBlank()) {
                    item { Text(m.bio, style = MaterialTheme.typography.bodyMedium) }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onBookClick, modifier = Modifier.weight(1f)) {
                            Text("Book")
                        }
                        OutlinedButton(onClick = onReviewsClick, modifier = Modifier.weight(1f)) {
                            Text("Reviews")
                        }
                    }
                }
                if (recentReviews.isNotEmpty()) {
                    item {
                        Text("Recent reviews", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    items(recentReviews) { review ->
                        ReviewItem(review)
                    }
                    item {
                        TextButton(onClick = onReviewsClick) { Text("See all reviews →") }
                    }
                }
            }
        } ?: Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
internal fun ReviewItem(review: MasterReview) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(review.authorNickname, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text("★ ${review.rating}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            if (review.comment.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(review.comment, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
