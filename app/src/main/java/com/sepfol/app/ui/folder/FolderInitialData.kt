package com.sepfol.app.ui.folder

object FolderInitialData {
    fun getStarterItems(): List<FolderItem> = listOf(
        // Root Folders
        FolderItem(
            id = "folder_university",
            name = "University",
            isDirectory = true,
            parentId = null,
            lastModified = System.currentTimeMillis() - 3600000 * 2,
            itemCount = 3
        ),
        FolderItem(
            id = "folder_work",
            name = "Work",
            isDirectory = true,
            parentId = null,
            lastModified = System.currentTimeMillis() - 3600000 * 5,
            itemCount = 2
        ),
        FolderItem(
            id = "folder_personal",
            name = "Personal",
            isDirectory = true,
            parentId = null,
            lastModified = System.currentTimeMillis() - 86400000 * 2,
            itemCount = 1
        ),
        FolderItem(
            id = "folder_flashcards",
            name = "Flashcards & Decks",
            isDirectory = true,
            parentId = null,
            lastModified = System.currentTimeMillis() - 86400000 * 3,
            itemCount = 2
        ),

        // Inside University
        FolderItem(
            id = "folder_neuro",
            name = "Neurobiology",
            isDirectory = true,
            parentId = "folder_university",
            lastModified = System.currentTimeMillis() - 3600000,
            itemCount = 2
        ),
        FolderItem(
            id = "file_bio10",
            name = "Bio Lecture 10.md",
            isDirectory = false,
            extension = "md",
            mimeType = "text/markdown",
            parentId = "folder_university",
            contentData = "# Bio Lecture 10: Cellular Energetics\n\n- Key concepts of membrane potential and ATP synthesis.\n- Electron transport chain in the inner mitochondrial membrane.\n- Chemiosmosis and electrochemical proton gradients.",
            sizeBytes = 2450L,
            lastModified = System.currentTimeMillis() - 7200000
        ),
        FolderItem(
            id = "file_assign",
            name = "Assignment Checklist.md",
            isDirectory = false,
            extension = "md",
            mimeType = "text/markdown",
            parentId = "folder_university",
            contentData = "# University Tasks\n- [x] Submit Neurobiology Lab Report\n- [ ] Literature review for Cognitive Psychology\n- [ ] Practice JLPT deck for 30 minutes",
            sizeBytes = 320L,
            lastModified = System.currentTimeMillis() - 14400000
        ),

        // Inside Neurobiology subfolder
        FolderItem(
            id = "file_synaptic",
            name = "Synaptic Pruning Notes.md",
            isDirectory = false,
            extension = "md",
            mimeType = "text/markdown",
            parentId = "folder_neuro",
            contentData = "# Synaptic Pruning\n\nThe brain eliminates extra synapses to increase cognitive efficiency.\nOccurs predominantly during late childhood and early adolescence.\n\n### Key Mechanisms:\n1. Microglia engulfment of inactive dendritic spines.\n2. Complement cascade (C1q, C3) tagging.",
            sizeBytes = 1420L,
            lastModified = System.currentTimeMillis() - 120000
        ),
        FolderItem(
            id = "file_hebbian",
            name = "Hebbian Learning.md",
            isDirectory = false,
            extension = "md",
            mimeType = "text/markdown",
            parentId = "folder_neuro",
            contentData = "# Hebbian Theory\n\n*\"Neurons that fire together, wire together.\"*\n\nDescribes how the adaptation of neurons occurs in the brain during the learning process.",
            sizeBytes = 890L,
            lastModified = System.currentTimeMillis() - 3600000
        ),

        // Inside Work
        FolderItem(
            id = "file_q3",
            name = "Q3 Product Architecture.md",
            isDirectory = false,
            extension = "md",
            mimeType = "text/markdown",
            parentId = "folder_work",
            contentData = "# Q3 Client System Architecture\n\n- Offline-first local SQLite cache with Room.\n- Reactive Flow streams for instant UI re-render.\n- Liquid glass UI components with zero latency.",
            sizeBytes = 3100L,
            lastModified = System.currentTimeMillis() - 3600000 * 4
        ),
        FolderItem(
            id = "file_retro",
            name = "Sprint Retrospective.md",
            isDirectory = false,
            extension = "md",
            mimeType = "text/markdown",
            parentId = "folder_work",
            contentData = "# Sprint Retro\n- Faster compose state reconciliation.\n- Clean breadcrumb hierarchy navigation.\n- Validated modal inputs.",
            sizeBytes = 1200L,
            lastModified = System.currentTimeMillis() - 86400000
        ),

        // Inside Personal
        FolderItem(
            id = "file_books",
            name = "Reading List 2026.md",
            isDirectory = false,
            extension = "md",
            mimeType = "text/markdown",
            parentId = "folder_personal",
            contentData = "# 2026 Reading List\n1. Gödel, Escher, Bach - Douglas Hofstadter\n2. The Master and His Emissary - Iain McGilchrist\n3. Structure and Interpretation of Computer Programs",
            sizeBytes = 410L,
            lastModified = System.currentTimeMillis() - 86400000 * 2
        ),

        // Inside Flashcards folder
        FolderItem(
            id = "file_qm",
            name = "Quantum Mechanics Summary.md",
            isDirectory = false,
            extension = "md",
            mimeType = "text/markdown",
            parentId = "folder_flashcards",
            contentData = "# Quantum Mechanics Postulates\n\n1. State vector |ψ⟩ in Hilbert space.\n2. Observables represented by Hermitian operators.\n3. Probability given by Born rule P = |⟨φ|ψ⟩|².",
            sizeBytes = 1850L,
            lastModified = System.currentTimeMillis() - 86400000 * 3
        ),
        FolderItem(
            id = "file_jlpt",
            name = "JLPT N2 Vocabulary List.md",
            isDirectory = false,
            extension = "md",
            mimeType = "text/markdown",
            parentId = "folder_flashcards",
            contentData = "# JLPT N2 Target Kanji\n- 考慮 (こうりょ) : Consideration\n- 把握 (はあく) : Grasp / comprehension\n- 促進 (そくしん) : Promotion / acceleration",
            sizeBytes = 5600L,
            lastModified = System.currentTimeMillis() - 86400000 * 4
        ),

        // Root Files (PDFs, Images & Notes)
        FolderItem(
            id = "file_maths_pdf",
            name = "Maths_SecP1X_2026_27.pdf",
            isDirectory = false,
            extension = "pdf",
            mimeType = "application/pdf",
            parentId = null,
            contentData = "Mathematics Subject Code 041 & 241 Class X (2026-27) Comprehensive Syllabus & Course Structure with 10 Pages",
            sizeBytes = 2450000L,
            lastModified = System.currentTimeMillis() - 60000
        ),
        FolderItem(
            id = "file_physics_pdf",
            name = "Physics_Mechanics_Handbook.pdf",
            isDirectory = false,
            extension = "pdf",
            mimeType = "application/pdf",
            parentId = null,
            contentData = "Complete mechanics formulas, diagrams, vectors, and Newtonian physics questions",
            sizeBytes = 3800000L,
            lastModified = System.currentTimeMillis() - 3600000
        ),
        FolderItem(
            id = "file_cell_img",
            name = "Biology_Cell_Diagram.png",
            isDirectory = false,
            extension = "png",
            mimeType = "image/png",
            parentId = null,
            contentData = "High-resolution diagram of plant & animal cell organelles and mitochondria",
            sizeBytes = 1950000L,
            lastModified = System.currentTimeMillis() - 7200000
        ),
        FolderItem(
            id = "file_arch_img",
            name = "System_Architecture_Diagram.png",
            isDirectory = false,
            extension = "png",
            mimeType = "image/png",
            parentId = "folder_work",
            contentData = "High level client architecture diagram with offline-first Room cache",
            sizeBytes = 2800000L,
            lastModified = System.currentTimeMillis() - 14400000
        ),
        FolderItem(
            id = "file_root_bio",
            name = "Biology Notes.md",
            isDirectory = false,
            extension = "md",
            mimeType = "text/markdown",
            parentId = null,
            contentData = "# High Level Biology Notes\nComprehensive overview of neurobiology, synaptic plasticity, and biological systems.",
            sizeBytes = 1600L,
            lastModified = System.currentTimeMillis() - 120000
        ),
        FolderItem(
            id = "file_root_report",
            name = "Q3 Report.md",
            isDirectory = false,
            extension = "md",
            mimeType = "text/markdown",
            parentId = null,
            contentData = "# Q3 Executive Overview\nSummary of quarterly performance and study metrics.",
            sizeBytes = 2800L,
            lastModified = System.currentTimeMillis() - 3600000
        ),
        FolderItem(
            id = "file_root_neuro",
            name = "Neuro Systems.md",
            isDirectory = false,
            extension = "md",
            mimeType = "text/markdown",
            parentId = null,
            contentData = "# Central vs Peripheral Nervous System\nStructural mappings and synaptic pathway diagrams.",
            sizeBytes = 2100L,
            lastModified = System.currentTimeMillis() - 86400000 * 3
        )
    )
}
