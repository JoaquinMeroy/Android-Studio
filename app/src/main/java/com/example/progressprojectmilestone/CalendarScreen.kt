package com.example.progressprojectmilestone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController) {
    val horizontalPadding = 20.dp // More balanced/ample padding

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding, vertical = 12.dp)
    ) {
        // Top bar: Back arrow and section label
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
                text = "Calendar",
                color = Color(0xFFB0B0B0),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        // Large gray calendar square (larger)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Makes a large square that fills width
                .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(16.dp))
        ) {
            // Canvas or future calendar widget goes here
        }
        Spacer(Modifier.height(32.dp))
        // Task placeholders with even horizontal padding
        repeat(3) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Checkbox(checked = false, onCheckedChange = {}, enabled = false)
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .height(20.dp)
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(6.dp))
                )
            }
        }
    }
}
