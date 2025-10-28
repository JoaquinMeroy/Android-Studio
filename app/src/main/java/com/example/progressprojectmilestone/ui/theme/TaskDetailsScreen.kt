package com.example.progressprojectmilestone.ui.theme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.*
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.progressprojectmilestone.TaskNotificationReceiver
import java.text.SimpleDateFormat


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(navController: NavController, taskId: String?) {
    // State
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var reminder by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf(0f) }
    var priority by remember { mutableStateOf("None") }
    var sendNotification by remember { mutableStateOf(false) }
    var categories by remember { mutableStateOf(listOf("NoList")) }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var categoryExpanded by remember { mutableStateOf(false) }

    // UI
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task Name", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Task Description", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(Modifier.height(18.dp))
        Text("Pick Deadline", fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { /* Show date picker */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Deadline", color = Color.Gray)
            }
            Button(
                onClick = { /* Show reminder picker */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Reminder", color = Color.Gray)
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Progress", fontWeight = FontWeight.Medium)
        Slider(
            value = progress,
            onValueChange = { progress = it },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(disabledActiveTrackColor = Color(0xFFE0E0E0)),
            steps = 9, // 10 steps
            valueRange = 0f..1f
        )
        Spacer(Modifier.height(12.dp))
        Text("Priority", fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val priorities = listOf("High", "Medium", "Low")
            priorities.forEach {
                Button(
                    onClick = { priority = it },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (priority == it) Color(0xFFD1D1D1) else Color(0xFFE0E0E0)
                    ),
                    modifier = Modifier.width(80.dp),
                    enabled = true
                ) {
                    Text(it)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Send Notifications", fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = sendNotification,
                onCheckedChange = { sendNotification = it }
            )
        }
        Spacer(Modifier.height(18.dp))
        // Category Dropdown
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selectedCategory,
                onValueChange = {},
                label = { Text("List") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categories.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            selectedCategory = item
                            categoryExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = false // Show as disabled in UI wireframe
        ) {
            Text("Create Now")
        }
    }
}