package com.example.progressprojectmilestone
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.foundation.clickable
import java.util.Locale
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextDecoration


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var showTaskModal by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Map<String, Any>?>(null) }
    // Dynamic category list from Firebase
    var categories by remember { mutableStateOf(listOf<String>()) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategory by remember { mutableStateOf("") }
    var addError by remember { mutableStateOf("") }
    var tasks by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    val categoryTabs = listOf("All") + categories
    val todayString by remember {
        mutableStateOf(SimpleDateFormat("yyyy/M/d", Locale.getDefault()).format(Date()))
    }
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val scrollState = rememberScrollState()

    val todayTasks = tasks.filter { (it["date"] as? String) == todayString }

    val nowMillis = currentTimeMillis
    val dueTasks = todayTasks.filter {
        val dateString = it["date"] as? String ?: ""
        val timeString = it["time"] as? String ?: ""
        val sdf = SimpleDateFormat("yyyy/M/d HH:mm", Locale.getDefault())
        val taskMillis = runCatching { sdf.parse("$dateString $timeString")?.time }.getOrNull()
        taskMillis != null && taskMillis <= nowMillis
    }
    val hasDueTasks = dueTasks.isNotEmpty()




    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            kotlinx.coroutines.delay(1 * 1000) // every 1 minute, or use 5*1000 for 5 seconds
        }
    }

    // Firestore listener for categories (live updates)
    // Fetch categories
    LaunchedEffect(userId) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .collection("categories")
            .addSnapshotListener { snapshot, _ ->
                categories = snapshot?.documents?.mapNotNull { it.getString("name") } ?: listOf()
            }
    }
// Fetch tasks (with id)
    LaunchedEffect(userId) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .collection("tasks")
            .addSnapshotListener { snapshot, _ ->
                tasks = snapshot?.documents?.map { doc ->
                    doc.data!!.plus("id" to doc.id)
                } ?: listOf()
            }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(260.dp)
            ) {
                Text(
                    "Menu",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F1F1))
                ) {
                    Text("Logout", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    )
    {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "APPNAME", fontWeight = FontWeight.Bold, letterSpacing = 2.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showAddCategoryDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Category")
                        }
                        IconButton(onClick = { /* search */ }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { /* show notification center, if any */ }) {
                            BadgedBox(badge = {
                                if (hasDueTasks) {
                                    Badge(
                                        containerColor = Color.Red
                                    )
                                }
                            }) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { navController.navigate("task_details") }, shape = CircleShape, containerColor = Color(0xFFF1F1F1)) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = true,
                        onClick = { /* Home */ },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { /* Calendar */ },
                        icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Calendar") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { /* Stats */ },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("profile") },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Spacer(Modifier.height(12.dp))
                // Dynamic Categories
                Row(Modifier.padding(horizontal = 16.dp).horizontalScroll(scrollState)) {
                    categoryTabs.forEach { cat ->
                        val isSelected = (cat == "All" && selectedCategory == null) || (cat == selectedCategory)
                        Surface(
                            onClick = { selectedCategory = if (cat == "All") null else cat },
                            color = if (isSelected) Color(0xFFF1F1F1) else Color(0xFFF8F8F8),
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = if (isSelected) 4.dp else 0.dp,
                            modifier = Modifier
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                cat,
                                modifier = Modifier
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Task Overview", modifier = Modifier.padding(start = 16.dp, bottom = 8.dp), fontWeight = FontWeight.Medium)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(100.dp)
                            .background(Color(0xFFF1F1F1), RoundedCornerShape(16.dp))
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .height(100.dp)
                            .background(Color(0xFFF1F1F1), RoundedCornerShape(16.dp))
                    )
                }
                Spacer(Modifier.height(20.dp))
                val todayTasks = tasks.filter {
                    val isToday = (it["date"] as? String) == todayString
                    val cat = it["category"] as? String ?: ""
                    isToday && (selectedCategory == null || cat == selectedCategory)
                }
                // Use SimpleDateFormat to parse dates
                val sdf = SimpleDateFormat("yyyy/M/d", Locale.getDefault())
                val todayDate = sdf.parse(todayString)
                val upcomingTasks = tasks
                    .filter {
                        val t = it["date"] as? String
                        t != null && runCatching { sdf.parse(t)!! > todayDate }.getOrDefault(false)
                    }
                    .sortedBy { it["date"] as? String }
                if (todayTasks.isEmpty()) {
                    Text("No tasks for today.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                    if (upcomingTasks.isNotEmpty()) {
                        Text("Next scheduled tasks:", fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 16.dp))
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(upcomingTasks.take(3)) { task -> // show up to 3 upcoming tasks
                                val dateString = task["date"] as? String ?: ""
                                val timeString = task["time"] as? String ?: ""
                                val sdfTime = SimpleDateFormat("yyyy/M/d HH:mm", Locale.getDefault())
                                val taskMillis = runCatching { sdfTime.parse("$dateString $timeString")?.time }.getOrNull()
                                val isDue = taskMillis != null && taskMillis <= currentTimeMillis

                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .background(
                                            if (isDue) Color(0xFFFFCDD2) else Color(0xFFEAEAEA),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            val taskId = task["id"]?.toString() ?: return@clickable
                                            navController.navigate("task_details/$taskId")
                                        }
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(task["title"]?.toString() ?: "", fontWeight = FontWeight.Bold)
                                        Text(task["description"]?.toString() ?: "", maxLines = 1, fontSize = 12.sp)
                                        Text(
                                            "Scheduled: ${task["date"]} ${task["time"]}",
                                            fontSize = 12.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text("You have no upcoming tasks.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                    }
                }else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize())
                        {
                            items(todayTasks) { task ->
                                val dateString = task["date"] as? String ?: ""
                                val timeString = task["time"] as? String ?: ""
                                val sdf = SimpleDateFormat("yyyy/M/d HH:mm", Locale.getDefault())
                                val taskMillis = runCatching { sdf.parse("$dateString $timeString")?.time }.getOrNull()
                                val isDue = taskMillis != null && taskMillis <= currentTimeMillis

                                // Add isCompleted state
                                val isCompleted = (task["isCompleted"] as? Boolean) ?: false
                                val taskId = task["id"]?.toString() ?: return@items
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .background(
                                            when {
                                                isCompleted -> Color(0xFFE0E0E0)
                                                isDue -> Color(0xFFFFCDD2)
                                                else -> Color(0xFFEAEAEA)
                                            },
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { navController.navigate("task_details/$taskId") }
                                        .padding(12.dp)
                                ) {
                                    Checkbox(
                                        checked = isCompleted,
                                        onCheckedChange = { checked ->
                                            val db = FirebaseFirestore.getInstance()
                                            db.collection("users").document(userId)
                                                .collection("tasks").document(taskId)
                                                .update("isCompleted", checked)
                                        }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.Center // <-- ensures text lines up to center!
                                    ) {
                                        Text(
                                            task["title"]?.toString() ?: "",
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                            color = if (isCompleted) Color.Gray else Color.Unspecified
                                        )
                                        Text(
                                            text = task["description"]?.toString().takeIf { !it.isNullOrBlank() } ?: "No description available",
                                            maxLines = 1,
                                            fontSize = 12.sp,
                                            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                            color = if (isCompleted) Color.Gray else Color.DarkGray
                                        )

                                    }
                                }

                            }

                        }
                    if (showTaskModal && selectedTask != null) {
                        AlertDialog(
                            onDismissRequest = { showTaskModal = false },
                            confirmButton = {
                                TextButton(onClick = { showTaskModal = false }) { Text("Close") }
                            },
                            title = { Text(selectedTask?.get("title")?.toString() ?: "") },
                            text = {
                                Column {
                                    Text("Description: ${selectedTask?.get("description") ?: ""}")
                                    Text("Category: ${selectedTask?.get("category") ?: ""}")
                                    Text("Date: ${selectedTask?.get("date") ?: ""}")
                                    Text("Time: ${selectedTask?.get("time") ?: ""}")
                                    Text("Repeat: ${selectedTask?.get("repeat") ?: ""}")
                                }
                            }
                        )
                    }

                }
            }
            // Category add modal
            if (showAddCategoryDialog) {
                AlertDialog(
                    onDismissRequest = { showAddCategoryDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            if (newCategory.isBlank()) {
                                addError = "Category name can't be empty"
                                return@TextButton
                            }
                            // Firestore: categories/<userId>/categories with name field
                            FirebaseFirestore.getInstance()
                                .collection("users").document(userId)
                                .collection("categories").document(newCategory)
                                .set(mapOf("name" to newCategory))
                            newCategory = ""
                            showAddCategoryDialog = false
                            addError = ""
                        }) { Text("Add") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showAddCategoryDialog = false
                            newCategory = ""
                            addError = ""
                        }) { Text("Cancel") }
                    },
                    title = { Text("Add Category") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newCategory,
                                onValueChange = {
                                    newCategory = it
                                    addError = ""
                                },
                                label = { Text("Category Name") },
                                singleLine = true
                            )
                            if (addError.isNotEmpty()) {
                                Text(addError, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                )
            }
        }
    }
}
