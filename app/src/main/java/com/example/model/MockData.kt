package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AccentCyber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentFrosted
import com.example.ui.theme.AccentMidnight
import com.example.ui.theme.AccentNebula
import com.example.ui.theme.AccentSunset

data class VaultItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: VaultItemType,
    val iconName: String
)

enum class VaultItemType {
    DOCUMENT,
    PDF,
    IMAGE,
    FOLDER
}

data class Flashcard(
    val id: String,
    val front: String,
    val back: String,
    val topic: String,
    val tags: List<String> = emptyList()
)

data class FlashcardDeck(
    val id: String,
    val title: String,
    val description: String,
    val cardCount: Int,
    val lastReviewed: String,
    val progress: Float, // 0.0 to 1.0
    val iconName: String,
    val categoryColor: Color,
    val cards: List<Flashcard>,
    val tags: List<String> = emptyList(),
    val isAiGenerated: Boolean = false
)

enum class VisualEngine {
    CLASSIC_OBSIDIAN,
    LIQUID_GLASS_3D
}

enum class BrightnessMode(val label: String, val description: String) {
    DARK("Dark Mode", "Deep void canvas & eye safety"),
    LIGHT("Dark Mode", "Deep void canvas & eye safety"),
    SYSTEM("System Default", "Follows device system settings")
}

enum class AccentTheme(
    val label: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentGlowColor: Color,
    val buttonGradientColors: List<Color>,
    val buttonTextColor: Color = Color.White,
    val orbColors: List<Color>
) {
    // 1. Bio Matrix / Emerald (Default matching Reference Designs)
    BIO_MATRIX(
        label = "Bio Matrix",
        subtitle = "Emerald & Mint",
        primaryColor = Color(0xFF4EDEA3), // Emerald 400 (#4edea3)
        secondaryColor = Color(0xFF10B981), // Emerald 500 (#10b981)
        accentGlowColor = Color(0xFF6FFBBE), // Light Emerald Glow (#6ffbbe)
        buttonGradientColors = listOf(Color(0xFFFF7886), Color(0xFF8B5CF6)), // Tertiary to Indigo glow
        buttonTextColor = Color(0xFF003824),
        orbColors = listOf(Color(0xFF4EDEA3), Color(0xFF818CF8), Color(0xFFD946EF), Color(0xFF10B981))
    ),

    // 2. Cyber Core / Indigo
    CYBER_CORE(
        label = "Cyber Core",
        subtitle = "Indigo & Purple",
        primaryColor = Color(0xFFC0C1FF), // Periwinkle Indigo (#c0c1ff)
        secondaryColor = Color(0xFF818CF8), // Indigo 400
        accentGlowColor = Color(0xFFD946EF), // Fuchsia Glow
        buttonGradientColors = listOf(Color(0xFF6366F1), Color(0xFFA855F7)),
        buttonTextColor = Color(0xFF1000A9),
        orbColors = listOf(Color(0xFFC0C1FF), Color(0xFF818CF8), Color(0xFFD946EF), Color(0xFF3131C0))
    ),

    // 3. Deep Velvet / Coral Rose
    DEEP_VELVET(
        label = "Deep Velvet",
        subtitle = "Coral & Rose",
        primaryColor = Color(0xFFFFB2B7), // Rose (#ffb2b7)
        secondaryColor = Color(0xFFFF7886), // Coral Pink (#ff7886)
        accentGlowColor = Color(0xFF780021), // Deep Wine
        buttonGradientColors = listOf(Color(0xFFFF7886), Color(0xFFE11D48)),
        buttonTextColor = Color(0xFF67001B),
        orbColors = listOf(Color(0xFFFFB2B7), Color(0xFFFF7886), Color(0xFF991B1B), Color(0xFFBE123C))
    ),

    // 4. Neon Electric / Cyber Cyan
    NEON_ELECTRIC(
        label = "Neon Electric",
        subtitle = "Cyan & Electric Blue",
        primaryColor = Color(0xFF06B6D4), // Cyan 500
        secondaryColor = Color(0xFF3B82F6), // Blue 500
        accentGlowColor = Color(0xFF1D4ED8), // Deep Sea Blue
        buttonGradientColors = listOf(Color(0xFF0891B2), Color(0xFF2563EB)),
        buttonTextColor = Color.White,
        orbColors = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6), Color(0xFF1D4ED8), Color(0xFF6366F1))
    ),

    // 5. Solar Flare / Gold Amber
    SOLAR_FLARE(
        label = "Solar Flare",
        subtitle = "Amber & Sunset Gold",
        primaryColor = Color(0xFFF59E0B), // Amber 500
        secondaryColor = Color(0xFFEAB308), // Yellow 500
        accentGlowColor = Color(0xFFEA580C), // Sunset Amber
        buttonGradientColors = listOf(Color(0xFFD97706), Color(0xFFEAB308)),
        buttonTextColor = Color.White,
        orbColors = listOf(Color(0xFFF59E0B), Color(0xFFEAB308), Color(0xFFEA580C), Color(0xFFDC2626))
    ),

    // 6. Pure AMOLED Void / Monochrome
    AMOLED_MONOCHROME(
        label = "AMOLED Void",
        subtitle = "Monochrome & Silver",
        primaryColor = Color(0xFFDBE2FD), // Ice White (#dbe2fd)
        secondaryColor = Color(0xFF9CA3AF), // Neutral Gray
        accentGlowColor = Color(0xFFE2E8F0), // Silver
        buttonGradientColors = listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9)),
        buttonTextColor = Color(0xFF0B1326),
        orbColors = listOf(Color(0xFFE2E8F0), Color(0xFF9CA3AF), Color(0xFF64748B), Color(0xFFCBD5E1))
    );

    // Compatibility accessor for previous property usages
    val color: Color get() = primaryColor

    companion object {
        fun fromStorageKey(key: String?): AccentTheme {
            if (key == null) return BIO_MATRIX
            return when (key.uppercase()) {
                "BIO_MATRIX", "EMERALD_AURORA" -> BIO_MATRIX
                "CYBER_CORE", "CYBER_AMOLED" -> CYBER_CORE
                "DEEP_VELVET", "NEBULA_VIOLET" -> DEEP_VELVET
                "NEON_ELECTRIC", "MIDNIGHT_CYAN" -> NEON_ELECTRIC
                "SOLAR_FLARE", "SUNSET_EMBER" -> SOLAR_FLARE
                "AMOLED_MONOCHROME", "FROSTED_VELVET" -> AMOLED_MONOCHROME
                else -> BIO_MATRIX
            }
        }
    }
}

object MockDataSource {
    val recentFiles = listOf(
        VaultItem("1", "Maths_SecP1X_2026_27.pdf", "2 mins ago", VaultItemType.PDF, "picture_as_pdf"),
        VaultItem("2", "Synaptic Pruning Notes.md", "3 mins ago", VaultItemType.DOCUMENT, "description"),
        VaultItem("3", "Biology Overview.md", "5 mins ago", VaultItemType.DOCUMENT, "description"),
        VaultItem("4", "Physics_Mechanics_Handbook.pdf", "1 hr ago", VaultItemType.PDF, "picture_as_pdf")
    )

    val folders = listOf(
        VaultItem("f1", "University", "12 items", VaultItemType.FOLDER, "folder"),
        VaultItem("f2", "Work", "28 items", VaultItemType.FOLDER, "folder"),
        VaultItem("f3", "Personal", "9 items", VaultItemType.FOLDER, "folder"),
        VaultItem("f4", "Flashcards", "6 decks", VaultItemType.FOLDER, "folder")
    )

    val neuralPlasticityCards = listOf(
        Flashcard(
            id = "c1",
            front = "What is the central premise of Neural Plasticity?",
            back = "The brain's ability to reorganize itself by forming new neural connections throughout life.",
            topic = "Neural Plasticity"
        ),
        Flashcard(
            id = "c2",
            front = "What is Synaptic Pruning?",
            back = "The process where extra neurons and synaptic connections are eliminated to increase the efficiency of neuronal transmissions.",
            topic = "Neural Plasticity"
        ),
        Flashcard(
            id = "c3",
            front = "Explain Hebbian Theory ('Cells that fire together...')",
            back = "Simultaneous activation of cells leads to pronounced increases in synaptic strength between those cells ('wire together').",
            topic = "Neural Plasticity"
        ),
        Flashcard(
            id = "c4",
            front = "What role does BDNF play in neurogenesis?",
            back = "Brain-Derived Neurotrophic Factor supports the survival of existing neurons and encourages the growth of new neurons and synapses.",
            topic = "Neural Plasticity"
        )
    )

    val decks = listOf(
        FlashcardDeck(
            id = "d1",
            title = "Neural Plasticity",
            description = "Advanced concepts in neurobiology and synaptic adaptation mechanisms.",
            cardCount = 112,
            lastReviewed = "2h ago",
            progress = 0.75f,
            iconName = "psychology",
            categoryColor = Color(0xFFC0C1FF), // Periwinkle Indigo matching reference
            cards = neuralPlasticityCards
        ),
        FlashcardDeck(
            id = "d2",
            title = "Quantum Mechanics",
            description = "Postulates, wave functions, and Heisenberg's uncertainty principle.",
            cardCount = 86,
            lastReviewed = "Yesterday",
            progress = 0.45f,
            iconName = "calculate",
            categoryColor = Color(0xFFFFB2B7), // Coral Rose matching reference
            cards = listOf(
                Flashcard("q1", "What is Heisenberg's Uncertainty Principle?", "States that position and momentum cannot be measured simultaneously with arbitrary precision.", "Quantum Mechanics")
            )
        ),
        FlashcardDeck(
            id = "d3",
            title = "JLPT N2 Vocabulary",
            description = "High-frequency kanji and vocabulary for the JLPT N2 exam.",
            cardCount = 450,
            lastReviewed = "3 days ago",
            progress = 0.30f,
            iconName = "translate",
            categoryColor = Color(0xFF4EDEA3), // Emerald matching reference
            cards = listOf(
                Flashcard("j1", "What does 考慮 (kouryo) mean?", "Consideration, taking into account.", "JLPT N2 Vocabulary")
            )
        ),
        FlashcardDeck(
            id = "d4",
            title = "System Architecture",
            description = "Design patterns, microservices, and scalable infrastructure concepts.",
            cardCount = 52,
            lastReviewed = "1 week ago",
            progress = 0.90f,
            iconName = "terminal",
            categoryColor = Color(0xFFC0C1FF), // Indigo matching reference
            cards = listOf(
                Flashcard("s1", "What is CAP Theorem?", "In a distributed data store, you can only simultaneously provide two of Consistency, Availability, and Partition tolerance.", "System Architecture")
            )
        )
    )
}
