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
fun TaskDetailsScreen(navController: NavController, taskId: String) {
    val context = LocalContext.current
    val isEditMode = taskId != null && taskId.isNotEmpty()

    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var repeat by remember { mutableStateOf("Once") }
    var description by remember { mutableStateOf("") }
    var timeForSave by remember { mutableStateOf("") } // Internal "HH:mm" for logic/storage


    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var categories by remember { mutableStateOf(listOf<String>()) }
    var selectedCategory by remember { mutableStateOf("") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val repeatOptions = listOf("Once", "Daily", "Weekly", "Monthly")
    var repeatMenuExpanded by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            date = "$year/${month + 1}/$dayOfMonth"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            // Determine if it's AM or PM
            val isPM = hourOfDay >= 12
            val displayHour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
            val amPm = if (isPM) "PM" else "AM"
            val displayTime = "%02d:%02d %s".format(displayHour, minute, amPm)
            // Save for display/input
            time = displayTime

            // Always save/compare 24-hour version for internal usage
            val trueHour = when {
                isPM && displayHour < 12 -> displayHour + 12
                !isPM && displayHour == 12 -> 0
                else -> displayHour
            }
            val time24 = "%02d:%02d".format(trueHour, minute)
            // Save time24 to Firestore for due logic and notification
            timeForSave = time24 // Declare this as a separate state variable
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false // <-- FALSE enables 12-hour mode with AM/PM
    )


    // Listen to user's categories from Firestore
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId)
                .collection("categories")
                .addSnapshotListener { snapshot, _ ->
                    val list = snapshot?.documents?.mapNotNull { it.getString("name") } ?: emptyList()
                    categories = list
                    if (selectedCategory.isEmpty() && list.isNotEmpty()) {
                        selectedCategory = list.first()
                    }
                }
        }
    }
    LaunchedEffect(taskId) {
        if (isEditMode && userId.isNotEmpty()) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId)
                .collection("tasks").document(taskId)
                .get()
                .addOnSuccessListener { doc ->
                    doc.data?.let { task ->
                        title = task["title"] as? String ?: ""
                        date = task["date"] as? String ?: ""
                        // For time, if it's stored as "HH:mm", you may want to convert it for display
                        val savedTime = task["time"] as? String ?: ""
                        timeForSave = savedTime // for save and notification
                        // convert to AM/PM for display
                        val t = try {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).parse(savedTime)
                        } catch(e: Exception) { null }
                        if (t != null) {
                            time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(t)
                        } else {
                            time = savedTime
                        }
                        repeat = task["repeat"] as? String ?: "Once"
                        description = task["description"] as? String ?: ""
                        selectedCategory = task["category"] as? String ?: ""
                    }
                }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Task" else "Task Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = {
                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(userId)
                                .collection("tasks").document(taskId)
                                .delete()
                                .addOnSuccessListener {
                                    android.widget.Toast.makeText(
                                        context, "Task deleted!", android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()
                                }
                                .addOnFailureListener {
                                    android.widget.Toast.makeText(
                                        context, "Failed to delete task.", android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                Modifier.padding(start = 16.dp, top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("← Back", fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(Modifier.width(16.dp))
                Box(
                    Modifier
                        .padding(horizontal = 16.dp) // padding for the ComboBox itself
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFFF1F1F1), RoundedCornerShape(12.dp))
                        .clickable(enabled = categories.isNotEmpty()) { categoryDropdownExpanded = true }
                ) {
                    Text(
                        text = if (selectedCategory.isEmpty()) "Select category" else selectedCategory,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        fontSize = 16.sp,
                        color = if (selectedCategory.isEmpty()) Color.Gray else Color.Black
                    )
                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false },
                        modifier = Modifier
                            .widthIn(max = 330.dp)         // Set a max width, do NOT use align!
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(16.dp))
            // CALENDAR ROW
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray)
                Spacer(Modifier.width(12.dp))
                Box(
                    Modifier
                        .weight(1f)
                        .clickable { datePickerDialog.show() } // only input area clickable
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = {},
                        label = { Text("Date") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            // TIME ROW
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Gray)
                Spacer(Modifier.width(12.dp))
                Box(
                    Modifier
                        .weight(1f)
                        .clickable { timePickerDialog.show() } // only input area clickable
                ) {
                    OutlinedTextField(
                        value = time,
                        onValueChange = {},
                        label = { Text("Time") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            // REPEAT ROW (Dropdown)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Autorenew, contentDescription = null, tint = Color.Gray)
                Spacer(Modifier.width(12.dp))
                Box(
                    Modifier
                        .weight(1f)
                        .clickable { repeatMenuExpanded = true }
                ) {
                    OutlinedTextField(
                        value = repeat,
                        onValueChange = {},
                        label = { Text("Repeat") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = repeatMenuExpanded,
                        onDismissRequest = { repeatMenuExpanded = false }
                    ) {
                        repeatOptions.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    repeat = it
                                    repeatMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Description", fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = 16.dp),
                maxLines = 6
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (userId.isNotEmpty() && title.isNotBlank()) {
                        val db = FirebaseFirestore.getInstance()
                        val data = mapOf(
                            "title" to title,
                            "date" to date,
                            "time" to timeForSave,
                            "repeat" to repeat,
                            "description" to description,
                            "category" to selectedCategory,
                            "createdAt" to System.currentTimeMillis()
                        )
                        val collection = db.collection("users").document(userId).collection("tasks")

                        if (isEditMode) {
                            // Update existing document
                            collection.document(taskId)
                                .set(data)
                                .addOnSuccessListener {
                                    android.widget.Toast.makeText(
                                        context, "Task updated!", android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()
                                }
                                .addOnFailureListener {
                                    android.widget.Toast.makeText(
                                        context, "Failed to update task.", android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            // Create new document
                            collection
                                .add(data)
                                .addOnSuccessListener { docRef ->
                                    scheduleTaskNotification(
                                        context = context,
                                        title = title,
                                        description = description,
                                        date = date,
                                        time = timeForSave,
                                        taskId = docRef.id
                                    )
                                    android.widget.Toast.makeText(
                                        context,
                                        "Task added successfully!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()
                                }
                                .addOnFailureListener { e ->
                                    android.widget.Toast.makeText(
                                        context,
                                        "Failed to add task. Try again.",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) { Text(if (isEditMode) "Update Task" else "Save Task") }

        }
    }
}
fun scheduleTaskNotification(
    context: Context,
    title: String,
    description: String,
    date: String,
    time: String,
    taskId: String
) {
    val sdf = java.text.SimpleDateFormat("yyyy/M/d HH:mm", java.util.Locale.getDefault())
    val triggerAtMillis = try {
        sdf.parse("$date $time")?.time ?: return
    } catch (e: Exception) { return }

    val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
        putExtra("title", title)
        putExtra("description", description)
        putExtra("taskId", taskId)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    manager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        triggerAtMillis,
        pendingIntent
    )
}
