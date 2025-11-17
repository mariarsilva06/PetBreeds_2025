package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlin.run

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCarousel(
    images: List<String>,
    petName: String = "Pet",
    modifier: Modifier = Modifier,
    onImageClick: ((Int) -> Unit)? = null,
) {
    if (images.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { images.size })
    var showFullScreenViewer by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableIntStateOf(0) }

    Column(modifier = modifier) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(300.dp),
            shape = RoundedCornerShape(0.dp),
        ) {
            Box {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    AsyncImage(
                        model = images[page],
                        contentDescription = "$petName image ${page + 1}",
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .clickable {
                                    selectedImageIndex = page
                                    onImageClick?.invoke(page) ?: run {
                                        showFullScreenViewer = true
                                    }
                                },
                        contentScale = ContentScale.Crop,
                    )
                }

                // Page indicators
                if (images.size > 1) {
                    Row(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        repeat(images.size) { iteration ->
                            val color =
                                if (pagerState.currentPage == iteration) {
                                    Color.White
                                } else {
                                    Color.White.copy(alpha = 0.5f)
                                }
                            Box(
                                modifier =
                                    Modifier
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(8.dp),
                            )
                        }
                    }
                }

                // Click hint overlay for first time users
                if (pagerState.currentPage == 0 && images.isNotEmpty()) {
                    Card(
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.6f),
                            ),
                    ) {
                        Text(
                            text = "Tap to expand",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }

                // Image counter in top-left
                if (images.size > 1) {
                    Card(
                        modifier =
                            Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.6f),
                            ),
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1}/${images.size}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }

    // Full Screen Image Viewer
    if (showFullScreenViewer) {
        FullScreenImageViewer(
            images = images,
            initialIndex = selectedImageIndex,
            petName = petName,
            onDismiss = { showFullScreenViewer = false },
        )
    }
}
