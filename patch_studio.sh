sed -i '126,192c\
            item(key = "stats_banner") {\
                // Professional Glassmorphic Dashboard Widget\
                GlassCard(\
                    modifier = Modifier.fillMaxWidth(),\
                    shape = RoundedCornerShape(24.dp),\
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f),\
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),\
                    borderWidth = 1.dp\
                ) {\
                    Box(modifier = Modifier.fillMaxSize()) {\
                        // Background ambient glow\
                        Box(\
                            modifier = Modifier\
                                .align(Alignment.TopEnd)\
                                .size(120.dp)\
                                .background(\
                                    Brush.radialGradient(\
                                        colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), Color.Transparent)\
                                    )\
                                )\
                        )\
                        Column(\
                            modifier = Modifier\
                                .fillMaxWidth()\
                                .padding(24.dp),\
                            verticalArrangement = Arrangement.spacedBy(20.dp)\
                        ) {\
                            Row(\
                                modifier = Modifier.fillMaxWidth(),\
                                horizontalArrangement = Arrangement.SpaceBetween,\
                                verticalAlignment = Alignment.CenterVertically\
                            ) {\
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {\
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {\
                                        Icon(\
                                            imageVector = Icons.Default.School,\
                                            contentDescription = null,\
                                            tint = MaterialTheme.colorScheme.primary,\
                                            modifier = Modifier.size(24.dp)\
                                        )\
                                        Text(\
                                            text = "Active Recall",\
                                            style = MaterialTheme.typography.titleLarge,\
                                            fontWeight = FontWeight.ExtraBold,\
                                            color = MaterialTheme.colorScheme.onSurface\
                                        )\
                                    }\
                                    Text(\
                                        text = "Your daily learning progress",\
                                        style = MaterialTheme.typography.bodyMedium,\
                                        color = MaterialTheme.colorScheme.onSurfaceVariant\
                                    )\
                                }\
                                Box(\
                                    modifier = Modifier\
                                        .clip(RoundedCornerShape(12.dp))\
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))\
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))\
                                        .clickable { onViewAllDecksClick() }\
                                        .padding(horizontal = 16.dp, vertical = 8.dp)\
                                ) {\
                                    Text(\
                                        text = "View All",\
                                        style = MaterialTheme.typography.labelSmall,\
                                        fontWeight = FontWeight.Bold,\
                                        color = MaterialTheme.colorScheme.primary\
                                    )\
                                }\
                            }\
                            // Stats Row\
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {\
                                StatItem(title = "Decks", value = "${decks.size}")\
                                StatItem(title = "Cards", value = "${decks.sumOf { it.cardCount }}")\
                                StatItem(title = "Streak", value = "12", suffix = "days")\
                            }\
                        }\
                    }\
                }\
            }' app/src/main/java/com/example/ui/screens/StudioScreen.kt
