package com.example.progressprojectmilestone
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var tasks by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    // Add states for modal title add dialog
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf("") }

    // Firestore listener for tasks
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
                modifier = Modifier.width(260.dp),
                drawerContainerColor = Color.White,
                // tonalElevation = 6.dp // Uncomment for a shadow between drawer and content
            ) {
                Spacer(Modifier.height(24.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("profile") }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.Gray)
                    Spacer(Modifier.width(8.dp))
                    Text("My Profile", fontWeight = FontWeight.Medium)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray)
                }
                Divider(Modifier.padding(vertical = 4.dp, horizontal = 8.dp))
                Text(
                    "Main Options",
                    color = Color(0xFFB0B0B0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("progress")
                            scope.launch { drawerState.close() }}
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Progress",
                        tint = Color.Gray
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Progress", fontWeight = FontWeight.Medium)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("calendar")
                            scope.launch { drawerState.close() }
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Calendar",
                        tint = Color.Gray
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Calendar", fontWeight = FontWeight.Medium)
                }
            }
        }

    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("TaskTimer") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showAddTaskDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("task_details") },
                    shape = CircleShape,
                    containerColor = Color(0xFFE3E1DF),
                    contentColor = Color.DarkGray
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            },
            containerColor = Color.White,
            bottomBar = {}
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Spacer(Modifier.height(12.dp))
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(tasks) { task ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        ) {
                            Checkbox(
                                checked = (task["isCompleted"] as? Boolean) ?: false,
                                onCheckedChange = { checked ->
                                    val db = FirebaseFirestore.getInstance()
                                    db.collection("users").document(userId)
                                        .collection("tasks")
                                        .document(task["id"] as String)
                                        .update("isCompleted", checked)
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                Modifier
                                    .height(20.dp)
                                    .fillMaxWidth()
                                    .background(
                                        color = Color(0xFFE0E0E0),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                            )
                        }
                    }
                }
            }

            // TITLE ADD MODAL DIALOG
            if (showAddTaskDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showAddTaskDialog = false
                        newTaskTitle = ""
                        titleError = ""
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (newTaskTitle.isBlank()) {
                                titleError = "Title can't be empty"
                                return@TextButton
                            }
                            // Add to Firestore!
                            FirebaseFirestore.getInstance()
                                .collection("users").document(userId)
                                .collection("tasks")
                                .add(mapOf(
                                    "title" to newTaskTitle,
                                    "isCompleted" to false
                                ))
                            showAddTaskDialog = false
                            newTaskTitle = ""
                            titleError = ""
                        }) { Text("Add") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showAddTaskDialog = false
                            newTaskTitle = ""
                            titleError = ""
                        }) { Text("Cancel") }
                    },
                    title = { Text("Add Task Title") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newTaskTitle,
                                onValueChange = {
                                    newTaskTitle = it
                                    titleError = ""
                                },
                                label = { Text("Title") },
                                singleLine = true
                            )
                            if (titleError.isNotEmpty()) {
                                Text(titleError, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                )
            }
        }
    }
}


