package com.sepfol.app.ui.viewer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sepfol.app.ui.folder.FolderItem

/**
 * Dedicated, rich canvas and visual rendering for Images in the Image Viewer.
 */
@Composable
fun DedicatedImageViewerCanvas(
    item: FolderItem,
    modifier: Modifier = Modifier
) {
    val isBio = item.name.contains("cell", ignoreCase = true) || item.name.contains("bio", ignoreCase = true)
    val isArch = item.name.contains("arch", ignoreCase = true) || item.name.contains("system", ignoreCase = true)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 680.dp)
            .aspectRatio(if (isBio) 1.05f else if (isArch) 1.25f else 1.15f)
            .clip(RoundedCornerShape(18.dp))
            .shadow(20.dp, RoundedCornerShape(18.dp), spotColor = Color.Black.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF131522),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        when {
            isBio -> CellDiagramCanvas(title = item.name)
            isArch -> ArchitectureDiagramCanvas(title = item.name)
            else -> GenericImageCanvas(item = item)
        }
    }
}

/**
 * High-definition Biology Cell Diagram Canvas with interactive labels
 */
@Composable
private fun CellDiagramCanvas(title: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F111E))
            .padding(14.dp)
    ) {
        // Diagram Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF10B981).copy(alpha = 0.15f)
            ) {
                Text(
                    text = "2400 x 2400 px",
                    color = Color(0xFF34D399),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Vector Drawn Cell Organelles Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF171A2E))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val outerRadius = size.minDimension * 0.42f
                val innerRadius = size.minDimension * 0.38f

                // Outer Cell Wall
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF065F46), Color(0xFF047857)),
                        center = center,
                        radius = outerRadius
                    ),
                    center = center,
                    radius = outerRadius
                )

                // Plasma Membrane
                drawCircle(
                    color = Color(0xFF059669),
                    center = center,
                    radius = innerRadius,
                    style = Stroke(width = 4.dp.toPx())
                )

                // Cytoplasm base
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF064E3B), Color(0xFF022C22)),
                        center = center,
                        radius = innerRadius
                    ),
                    center = center,
                    radius = innerRadius - 2.dp.toPx()
                )

                // Nucleus at center
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF8B5CF6), Color(0xFF5B21B6)),
                        center = center,
                        radius = 45.dp.toPx()
                    ),
                    center = center,
                    radius = 45.dp.toPx()
                )
                // Nucleolus
                drawCircle(
                    color = Color(0xFFDDD6FE),
                    center = center,
                    radius = 18.dp.toPx()
                )

                // Mitochondria 1 (Top Right)
                drawRoundRect(
                    color = Color(0xFFF59E0B),
                    topLeft = Offset(center.x + innerRadius * 0.4f, center.y - innerRadius * 0.5f),
                    size = Size(32.dp.toPx(), 18.dp.toPx()),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )

                // Mitochondria 2 (Bottom Left)
                drawRoundRect(
                    color = Color(0xFFF59E0B),
                    topLeft = Offset(center.x - innerRadius * 0.65f, center.y + innerRadius * 0.3f),
                    size = Size(36.dp.toPx(), 20.dp.toPx()),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )

                // Vacuole (Top Left)
                drawCircle(
                    color = Color(0xFF0EA5E9).copy(alpha = 0.5f),
                    center = Offset(center.x - innerRadius * 0.45f, center.y - innerRadius * 0.4f),
                    radius = 28.dp.toPx()
                )

                // Ribosomes (Tiny dots)
                val dotColor = Color(0xFFF43F5E)
                listOf(
                    Offset(center.x + 30f, center.y - 70f),
                    Offset(center.x + 80f, center.y - 30f),
                    Offset(center.x - 60f, center.y + 70f),
                    Offset(center.x + 40f, center.y + 80f),
                    Offset(center.x - 80f, center.y - 60f)
                ).forEach { dot ->
                    drawCircle(color = dotColor, center = dot, radius = 3.dp.toPx())
                }
            }

            // Overlay Labels
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OrganelleBadge(name = "Vacuole", color = Color(0xFF38BDF8))
                    OrganelleBadge(name = "Mitochondria", color = Color(0xFFFBBF24))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OrganelleBadge(name = "Ribosomes", color = Color(0xFFFB7185))
                    OrganelleBadge(name = "Nucleus (DNA)", color = Color(0xFFC084FC))
                    OrganelleBadge(name = "Cell Wall", color = Color(0xFF34D399))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Diagram Footer notes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Figure 4.2 • Eukaryotic Cell Structure & Organelles",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 11.sp
            )
            Text(
                text = "RGB • 300 DPI",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/**
 * System Architecture Diagram Canvas
 */
@Composable
private fun ArchitectureDiagramCanvas(title: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F111E))
            .padding(14.dp)
    ) {
        // Diagram Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF3B82F6).copy(alpha = 0.15f)
            ) {
                Text(
                    text = "3200 x 1800 px",
                    color = Color(0xFF60A5FA),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Multi-tier Architecture Blocks
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF171A2E))
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Layer 1: UI Presentation
            ArchitectureLayerBlock(
                title = "UI Presentation Layer (Jetpack Compose & M3)",
                subtitle = "FolderScreen, DocumentViewer, Material 3 Theme, Navigation",
                accentColor = Color(0xFFC084FC)
            )

            // Flow arrow
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("▼ StateFlow / Intent Triggers", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
            }

            // Layer 2: Domain & ViewModels
            ArchitectureLayerBlock(
                title = "State & ViewModel Layer (Kotlin Coroutines)",
                subtitle = "FolderViewModel, VaultRepository, Search Filter Engine",
                accentColor = Color(0xFF38BDF8)
            )

            // Flow arrow
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("▼ DAO Queries & Offline Cache", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
            }

            // Layer 3: Persistence & Storage
            ArchitectureLayerBlock(
                title = "Data Persistence Layer (Room Database & Storage)",
                subtitle = "Room SQLite Entity, File Store, Encrypted SharedPreferences",
                accentColor = Color(0xFF34D399)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Clean Architecture Architecture • Unidirectional Data Flow",
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ArchitectureLayerBlock(
    title: String,
    subtitle: String,
    accentColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF22263C),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = title,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun GenericImageCanvas(item: FolderItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F111E))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF06B6D4), Color(0xFF8B5CF6))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = item.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${item.extension.uppercase()} Image Document • Multi-touch Zoom & Pan Active",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        item.contentData?.let { details ->
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E2238)
            ) {
                Text(
                    text = details,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun OrganelleBadge(name: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF0A0C16).copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = name,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
