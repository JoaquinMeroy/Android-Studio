package com.example.progressprojectmilestone

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(navController: NavController) {
    val horizontalPadding = 20.dp

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding, vertical = 12.dp)
    ) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFB0B0B0)
                )
            }
            Text(
                text = "Progress",
                color = Color(0xFFB0B0B0),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Card for Pie Chart
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFFF8F8F8), shape = RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Pie: 75% completed (gray), 25% incompleted (lighter gray)
                val completedSweep = 270f
                val incompletedSweep = 90f
                val diameter = size.minDimension
                drawArc(
                    color = Color(0xFFD7D7D7),
                    startAngle = 180f,
                    sweepAngle = completedSweep,
                    useCenter = true,
                    size = Size(diameter, diameter)
                )
                drawArc(
                    color = Color(0xFFF5F5F5),
                    startAngle = 90f,
                    sweepAngle = incompletedSweep,
                    useCenter = true,
                    size = Size(diameter, diameter)
                )
            }
            // Overlay labels
            Text(
                "Completed",
                color = Color(0xFFB0B0B0),
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 24.dp, y = 14.dp)
            )
            Text(
                "Incompleted",
                color = Color(0xFFB0B0B0),
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-32).dp, y = (-24).dp)
            )
        }

    }
}
