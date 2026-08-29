package com.sepfol.app.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Renders high-fidelity PDF Document Pages formatted as clean, official academic syllabus/notes
 * matching the visual layout in the screenshot (CBSE / NEP Mathematics Class X 2026-27).
 */
@Composable
fun PdfPageContent(
    pageIndex: Int,
    documentTitle: String,
    modifier: Modifier = Modifier,
    isThumbnail: Boolean = false
) {
    val scaleFactor = if (isThumbnail) 0.35f else 1.0f
    val basePadding = if (isThumbnail) 8.dp else 24.dp
    val headerFontSize = if (isThumbnail) 6.sp else 15.sp
    val subHeaderFontSize = if (isThumbnail) 5.sp else 12.sp
    val bodyFontSize = if (isThumbnail) 3.5.sp else 10.sp
    val lineHeight = if (isThumbnail) 4.5.sp else 14.sp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (isThumbnail) 8.dp else 16.dp))
            .shadow(
                elevation = if (isThumbnail) 4.dp else 12.dp,
                shape = RoundedCornerShape(if (isThumbnail) 8.dp else 16.dp),
                spotColor = Color.Black.copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(if (isThumbnail) 8.dp else 16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(basePadding),
            verticalArrangement = Arrangement.spacedBy(if (isThumbnail) 4.dp else 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (pageIndex) {
                1 -> Page1Content(headerFontSize, subHeaderFontSize, bodyFontSize, lineHeight, isThumbnail)
                2 -> Page2Content(headerFontSize, subHeaderFontSize, bodyFontSize, lineHeight, isThumbnail)
                3 -> Page3Content(headerFontSize, subHeaderFontSize, bodyFontSize, lineHeight, isThumbnail)
                4 -> Page4Content(headerFontSize, subHeaderFontSize, bodyFontSize, lineHeight, isThumbnail)
                5 -> Page5Content(headerFontSize, subHeaderFontSize, bodyFontSize, lineHeight, isThumbnail)
                6 -> Page6Content(headerFontSize, subHeaderFontSize, bodyFontSize, lineHeight, isThumbnail)
                7 -> Page7Content(headerFontSize, subHeaderFontSize, bodyFontSize, lineHeight, isThumbnail)
                8 -> Page8Content(headerFontSize, subHeaderFontSize, bodyFontSize, lineHeight, isThumbnail)
                9 -> Page9Content(headerFontSize, subHeaderFontSize, bodyFontSize, lineHeight, isThumbnail)
                else -> GenericPageContent(pageIndex, documentTitle, headerFontSize, subHeaderFontSize, bodyFontSize, lineHeight, isThumbnail)
            }
        }
    }
}

@Composable
private fun Page1Content(
    headerSize: androidx.compose.ui.unit.TextUnit,
    subHeaderSize: androidx.compose.ui.unit.TextUnit,
    bodySize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    isThumbnail: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (isThumbnail) 3.dp else 8.dp)
    ) {
        Text(
            text = "Mathematics",
            fontWeight = FontWeight.Bold,
            fontSize = headerSize,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Subject Code – 041 & 241\nClass – X (2026-27)",
            fontWeight = FontWeight.Bold,
            fontSize = subHeaderSize,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = if (isThumbnail) 6.sp else 16.sp
        )

        Spacer(modifier = Modifier.height(if (isThumbnail) 2.dp else 8.dp))

        Text(
            text = "The Mathematics curriculum for the Secondary stage has been redesigned in alignment with the National Education Policy 2020 and the National Curriculum Framework for School Education (NCF – SE) 2023, prioritizing deep conceptual understanding and logical reasoning. The revised syllabus places strong emphasis on developing core mathematical competencies, including problem-solving, visualisation, mathematical modelling, mathematical communication, computational thinking, and data analytics. The syllabus integrates Indian Knowledge System with contemporary mathematical knowledge, highlighting the rich contributions of Indian mathematicians to foster a sense of pride and historical context.",
            fontSize = bodySize,
            color = Color(0xFF1E1E1E),
            textAlign = TextAlign.Justify,
            lineHeight = lineHeight
        )

        Text(
            text = "At the secondary stage, the curriculum focuses on developing essential global mathematical competencies, including mathematical representation through quantities and relations, mathematical modelling and algorithm building, and effective mathematical communication. The study of the number system, algebra, geometry, mensuration, statistics and probability is designed to build a strong foundation for higher education while enhancing functional life skills.",
            fontSize = bodySize,
            color = Color(0xFF1E1E1E),
            textAlign = TextAlign.Justify,
            lineHeight = lineHeight
        )

        if (!isThumbnail) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Objectives:",
                    fontWeight = FontWeight.Bold,
                    fontSize = bodySize,
                    color = Color.Black
                )
                val bulletPoints = listOf(
                    "Develop logical thinking, critical reasoning, and a structured approach to problem-solving;",
                    "Build the ability to recognise, analyse, and solve diverse problems with confidence and adaptability;",
                    "Communicate mathematical ideas effectively using appropriate language, symbols, and representations;",
                    "Appreciate the beauty, history, and real-life relevance of Mathematics as a discipline;"
                )
                for (bullet in bulletPoints) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "•", fontSize = bodySize, color = Color.Black)
                        Text(
                            text = bullet,
                            fontSize = bodySize,
                            color = Color(0xFF1E1E1E),
                            lineHeight = lineHeight
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Page2Content(
    headerSize: androidx.compose.ui.unit.TextUnit,
    subHeaderSize: androidx.compose.ui.unit.TextUnit,
    bodySize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    isThumbnail: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(if (isThumbnail) 3.dp else 8.dp)
    ) {
        Text(
            text = "Course Structure & Learning Framework",
            fontWeight = FontWeight.Bold,
            fontSize = headerSize,
            color = Color.Black
        )
        Text(
            text = "Pedagogical Approaches & Indian Knowledge System",
            fontWeight = FontWeight.SemiBold,
            fontSize = subHeaderSize,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(if (isThumbnail) 2.dp else 6.dp))

        Text(
            text = "1. Experiential and Discovery-based Learning: Transition from rote formula memorization towards understanding foundational mathematical axioms and algebraic structures through hands-on geometric transformations.\n\n" +
                    "2. Historical Contributions of Indian Mathematics: The syllabus explicitly explores Aryabhata’s sine tables, Brahmagupta’s rules for arithmetic with zero, Bhaskara II’s Lilavati problems, and Madhava’s infinite series expansions.\n\n" +
                    "3. Computational Thinking and Algorithmic Logic: Incorporating flowcharting, pseudo-code logic, and iterative approximations into Class X algebra problem-solving exercises.",
            fontSize = bodySize,
            color = Color(0xFF1E1E1E),
            textAlign = TextAlign.Justify,
            lineHeight = lineHeight
        )

        if (!isThumbnail) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Assessment Framework Guidelines:",
                        fontWeight = FontWeight.Bold,
                        fontSize = bodySize,
                        color = Color.Black
                    )
                    Text(
                        text = "• Pen-paper Periodic Tests: 10 Marks\n• Portfolio & Subject Enrichment: 5 Marks\n• Practical Lab Experiments & Viva: 5 Marks\n• Annual Board Examination: 80 Marks",
                        fontSize = bodySize,
                        color = Color(0xFF2E2E2E),
                        lineHeight = lineHeight
                    )
                }
            }
        }
    }
}

@Composable
private fun Page3Content(
    headerSize: androidx.compose.ui.unit.TextUnit,
    subHeaderSize: androidx.compose.ui.unit.TextUnit,
    bodySize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    isThumbnail: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (isThumbnail) 3.dp else 8.dp)
    ) {
        Text(
            text = "COURSE STRUCTURE CLASS – X",
            fontWeight = FontWeight.Bold,
            fontSize = headerSize,
            color = Color.Black
        )

        // Structure Table
        TableComponent(
            headers = listOf("Units", "Unit Name", "Marks"),
            rows = listOf(
                listOf("I", "NUMBER SYSTEMS", "06"),
                listOf("II", "ALGEBRA", "20"),
                listOf("III", "COORDINATE GEOMETRY", "06"),
                listOf("IV", "GEOMETRY", "15"),
                listOf("V", "TRIGONOMETRY", "12"),
                listOf("VI", "MENSURATION", "10"),
                listOf("VII", "STATISTICS & PROBABILITY", "11"),
                listOf("", "TOTAL", "80")
            ),
            bodySize = bodySize,
            isThumbnail = isThumbnail
        )

        if (!isThumbnail) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "UNIT I: NUMBER SYSTEMS\n1. REAL NUMBERS: Fundamental Theorem of Arithmetic – statements after reviewing work done earlier and after illustrating and motivating through examples, Proofs of irrationality of √2, √3, √5.",
                fontSize = bodySize,
                color = Color(0xFF1E1E1E),
                lineHeight = lineHeight
            )
        }
    }
}

@Composable
private fun Page4Content(
    headerSize: androidx.compose.ui.unit.TextUnit,
    subHeaderSize: androidx.compose.ui.unit.TextUnit,
    bodySize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    isThumbnail: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(if (isThumbnail) 3.dp else 8.dp)
    ) {
        Text(
            text = "UNIT II: ALGEBRA",
            fontWeight = FontWeight.Bold,
            fontSize = headerSize,
            color = Color.Black
        )

        TableComponent(
            headers = listOf("S.No.", "Topic", "Competencies & Core Concepts"),
            rows = listOf(
                listOf("1", "Polynomials", "Zeros of a polynomial. Relationship between zeros and coefficients of quadratic polynomials."),
                listOf("2", "Pair of Linear Equations", "Graphical method of solution, consistency/inconsistency. Algebraic solutions: substitution, elimination."),
                listOf("3", "Quadratic Equations", "Standard form ax² + bx + c = 0. Solution by factorisation and quadratic formula. Discriminant nature of roots."),
                listOf("4", "Arithmetic Progressions", "Derivation of the nth term and sum of the first n terms of A.P. Real life daily applications.")
            ),
            bodySize = bodySize,
            isThumbnail = isThumbnail
        )
    }
}

@Composable
private fun Page5Content(
    headerSize: androidx.compose.ui.unit.TextUnit,
    subHeaderSize: androidx.compose.ui.unit.TextUnit,
    bodySize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    isThumbnail: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(if (isThumbnail) 3.dp else 8.dp)
    ) {
        Text(
            text = "UNIT III & IV: COORDINATE GEOMETRY & GEOMETRY",
            fontWeight = FontWeight.Bold,
            fontSize = headerSize,
            color = Color.Black
        )

        Text(
            text = "1. Coordinate Geometry: Review concepts of coordinate geometry. Graphs of linear equations. Distance formula. Section formula (internal division).\n\n" +
                    "2. Triangles: Definitions, examples, counter examples of similar triangles. (Prove) If a line is drawn parallel to one side of a triangle intersecting other two sides, the other two sides are divided in same ratio.\n\n" +
                    "3. Circles: Tangent to a circle at, point of contact. (Prove) The tangent at any point of a circle is perpendicular to the radius through the point of contact. (Prove) The lengths of tangents drawn from an external point to a circle are equal.",
            fontSize = bodySize,
            color = Color(0xFF1E1E1E),
            textAlign = TextAlign.Justify,
            lineHeight = lineHeight
        )
    }
}

@Composable
private fun Page6Content(
    headerSize: androidx.compose.ui.unit.TextUnit,
    subHeaderSize: androidx.compose.ui.unit.TextUnit,
    bodySize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    isThumbnail: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(if (isThumbnail) 3.dp else 8.dp)
    ) {
        Text(
            text = "UNIT V: TRIGONOMETRY",
            fontWeight = FontWeight.Bold,
            fontSize = headerSize,
            color = Color.Black
        )

        Text(
            text = "1. Introduction to Trigonometry: Trigonometric ratios of an acute angle of a right-angled triangle. Proof of their existence. Values of trigonometric ratios of 30°, 45° and 60°. Relationships between the ratios.\n\n" +
                    "2. Trigonometric Identities: Proof and applications of the identity sin²A + cos²A = 1. Only simple identities to be given.\n\n" +
                    "3. Heights and Distances: Angles of elevation / depression. Simple problems on heights and distances. Problems should not involve more than two right triangles. Angles should be only 30°, 45°, and 60°.",
            fontSize = bodySize,
            color = Color(0xFF1E1E1E),
            textAlign = TextAlign.Justify,
            lineHeight = lineHeight
        )
    }
}

@Composable
private fun Page7Content(
    headerSize: androidx.compose.ui.unit.TextUnit,
    subHeaderSize: androidx.compose.ui.unit.TextUnit,
    bodySize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    isThumbnail: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(if (isThumbnail) 3.dp else 8.dp)
    ) {
        Text(
            text = "UNIT VI: MENSURATION",
            fontWeight = FontWeight.Bold,
            fontSize = headerSize,
            color = Color.Black
        )

        Text(
            text = "1. Areas Related to Circles: Motivate the area of a circle; area of sectors and segments of a circle. Problems based on areas and perimeter / circumference of the above said plane figures (In calculating area of segment, problems should be restricted to central angle of 60°, 90° and 120°).\n\n" +
                    "2. Surface Areas and Volumes: Surface areas and volumes of combinations of any two of the following: cubes, cuboids, spheres, hemispheres and right circular cylinders/cones.",
            fontSize = bodySize,
            color = Color(0xFF1E1E1E),
            textAlign = TextAlign.Justify,
            lineHeight = lineHeight
        )
    }
}

@Composable
private fun Page8Content(
    headerSize: androidx.compose.ui.unit.TextUnit,
    subHeaderSize: androidx.compose.ui.unit.TextUnit,
    bodySize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    isThumbnail: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(if (isThumbnail) 3.dp else 8.dp)
    ) {
        Text(
            text = "UNIT VII: STATISTICS & PROBABILITY",
            fontWeight = FontWeight.Bold,
            fontSize = headerSize,
            color = Color.Black
        )

        Text(
            text = "1. Statistics: Mean, median and mode of grouped data (bimodal situation to be avoided). Mean by Direct method and Assumed Mean Method.\n\n" +
                    "2. Probability: Classical definition of probability. Simple problems on finding the probability of an event.",
            fontSize = bodySize,
            color = Color(0xFF1E1E1E),
            textAlign = TextAlign.Justify,
            lineHeight = lineHeight
        )
    }
}

@Composable
private fun Page9Content(
    headerSize: androidx.compose.ui.unit.TextUnit,
    subHeaderSize: androidx.compose.ui.unit.TextUnit,
    bodySize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    isThumbnail: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(if (isThumbnail) 3.dp else 8.dp)
    ) {
        Text(
            text = "QUESTION PAPER DESIGN - CLASS X",
            fontWeight = FontWeight.Bold,
            fontSize = headerSize,
            color = Color.Black
        )

        TableComponent(
            headers = listOf("Typology of Questions", "Total Marks", "% Weightage"),
            rows = listOf(
                listOf("Remembering: Exhibit memory of previously learned material", "43", "54%"),
                listOf("Applying: Solve problems to new situations by applying acquired knowledge", "19", "24%"),
                listOf("Analysing & Evaluating: Examine and break information into parts", "18", "22%"),
                listOf("Total", "80", "100%")
            ),
            bodySize = bodySize,
            isThumbnail = isThumbnail
        )
    }
}

@Composable
private fun GenericPageContent(
    pageIndex: Int,
    documentTitle: String,
    headerSize: androidx.compose.ui.unit.TextUnit,
    subHeaderSize: androidx.compose.ui.unit.TextUnit,
    bodySize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    isThumbnail: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(if (isThumbnail) 3.dp else 8.dp)
    ) {
        Text(
            text = "$documentTitle • Page $pageIndex",
            fontWeight = FontWeight.Bold,
            fontSize = headerSize,
            color = Color.Black
        )
        Text(
            text = "Internal Assessment & Laboratory Work Breakdown",
            fontWeight = FontWeight.SemiBold,
            fontSize = subHeaderSize,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(if (isThumbnail) 2.dp else 6.dp))

        Text(
            text = "Guidelines for Laboratory Activity in Mathematics Class X:\n" +
                    "1. Verification of the formula for the sum of first n natural numbers by graphical grid.\n" +
                    "2. To verify Basic Proportionality Theorem using parallel lines board.\n" +
                    "3. To find the ratio of areas of two similar triangles through coordinate cutting.\n" +
                    "4. To make a mathematical clinometer to measure the elevation of a tall tree or school building.\n" +
                    "5. Surface area verification of sphere via unwinding cylinder string coils.",
            fontSize = bodySize,
            color = Color(0xFF1E1E1E),
            textAlign = TextAlign.Justify,
            lineHeight = lineHeight
        )
    }
}

@Composable
private fun TableComponent(
    headers: List<String>,
    rows: List<List<String>>,
    bodySize: androidx.compose.ui.unit.TextUnit,
    isThumbnail: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black, RoundedCornerShape(2.dp))
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F0F0))
                .padding(if (isThumbnail) 2.dp else 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (header in headers) {
                Text(
                    text = header,
                    fontWeight = FontWeight.Bold,
                    fontSize = bodySize,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        HorizontalDivider(thickness = 1.dp, color = Color.Black)

        // Rows
        for ((idx, row) in rows.withIndex()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isThumbnail) 1.dp else 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (cell in row) {
                    Text(
                        text = cell,
                        fontWeight = if (idx == rows.size - 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = bodySize,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            if (idx < rows.size - 1) {
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            }
        }
    }
}
