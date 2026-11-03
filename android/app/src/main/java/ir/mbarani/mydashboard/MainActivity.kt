package ir.mbarani.mydashboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import ir.mbarani.mydashboard.data.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var dao: DashboardDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dao = AppDatabase.get(this).dashboardDao()
        setContent { DashboardApp(dao) }
    }
}

private enum class Tab(val label: String) {
    HOME("خانه"), NOTES("یادداشت‌ها"), TASKS("وظایف"), LINKS("لینک‌ها")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardApp(dao: DashboardDao) {
    var selected by remember { mutableStateOf(Tab.HOME) }
    val dark = isSystemInDarkTheme()
    val colors = if (dark) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colors) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("MyDashboard", fontWeight = FontWeight.Bold)
                            Text("داشبورد شخصی شما", style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    actions = {
                        IconButton(onClick = { selected = Tab.HOME }) {
                            Icon(Icons.Default.Home, contentDescription = "خانه")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selected == tab,
                            onClick = { selected = tab },
                            icon = {
                                Icon(
                                    when (tab) {
                                        Tab.HOME -> Icons.Default.Dashboard
                                        Tab.NOTES -> Icons.Default.StickyNote2
                                        Tab.TASKS -> Icons.Default.CheckCircle
                                        Tab.LINKS -> Icons.Default.Link
                                    }, contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (selected) {
                    Tab.HOME -> HomeScreen(dao, onNavigate = { selected = it })
                    Tab.NOTES -> NotesScreen(dao)
                    Tab.TASKS -> TasksScreen(dao)
                    Tab.LINKS -> LinksScreen(dao)
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(dao: DashboardDao, onNavigate: (Tab) -> Unit) {
    val notes by dao.observeNotes().collectAsState(initial = emptyList())
    val tasks1 by dao.observeTasks(1).collectAsState(initial = emptyList())
    val tasks2 by dao.observeTasks(2).collectAsState(initial = emptyList())
    val links by dao.observeLinks().collectAsState(initial = emptyList())
    val now = remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            now.longValue = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }

    val date = remember(now.longValue) {
        SimpleDateFormat("EEEE، d MMMM yyyy", Locale("fa", "IR")).format(Date(now.longValue))
    }
    val time = remember(now.longValue) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(now.longValue))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(time, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    Text(date, style = MaterialTheme.typography.titleMedium)
                    Text(
                        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now.longValue)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        item { SectionHeader("خلاصه امروز", "مشاهده وظایف") { onNavigate(Tab.TASKS) } }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryCard("فهرست اول", "${tasks1.count { !it.completed }} باقی‌مانده", Modifier.weight(1f))
                SummaryCard("فهرست دوم", "${tasks2.count { !it.completed }} باقی‌مانده", Modifier.weight(1f))
            }
        }
        item { SectionHeader("یادداشت‌های اخیر", "همه یادداشت‌ها") { onNavigate(Tab.NOTES) } }
        if (notes.isEmpty()) item { EmptyHint("هنوز یادداشتی ثبت نکرده‌اید") }
        items(notes.take(3)) { note ->
            Card {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    Text(note.title, fontWeight = FontWeight.SemiBold)
                    if (note.body.isNotBlank()) Text(note.body, maxLines = 2, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { SectionHeader("لینک‌های سریع", "همه لینک‌ها") { onNavigate(Tab.LINKS) } }
        if (links.isEmpty()) item { EmptyHint("لینک‌های مهمتان را اضافه کنید") }
        items(links.take(4)) { link ->
            Card {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, null)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(link.title, fontWeight = FontWeight.Medium)
                        Text(link.url, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        TextButton(onClick = onClick) { Text(action) }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(14.dp)) {
        Text(text, Modifier.fillMaxWidth().padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesScreen(dao: DashboardDao) {
    val scope = rememberCoroutineScope()
    val notes by dao.observeNotes().collectAsState(initial = emptyList())
    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<NoteEntity?>(null) }
    var query by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("جستجو در یادداشت‌ها") }, leadingIcon = { Icon(Icons.Default.Search, null) })
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("یادداشت‌ها", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Button(onClick = { editing = null; showEditor = true }) {
                Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text("یادداشت جدید")
            }
        }
        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(notes.filter { it.title.contains(query, true) || it.body.contains(query, true) }) { note ->
                Card {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(note.title, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            if (note.pinned) Icon(Icons.Default.PushPin, "پین شده", tint = MaterialTheme.colorScheme.primary)
                            IconButton(onClick = { scope.launch { dao.updateNote(note.copy(pinned = !note.pinned, updatedAt = System.currentTimeMillis())) } }) {
                                Icon(Icons.Default.PushPin, "تغییر پین")
                            }
                            IconButton(onClick = { editing = note; showEditor = true }) { Icon(Icons.Default.Edit, "ویرایش") }
                            IconButton(onClick = { scope.launch { dao.deleteNote(note.id) } }) { Icon(Icons.Default.Delete, "حذف") }
                        }
                        Text(note.body, maxLines = 4, style = MaterialTheme.typography.bodyMedium)
                        if (note.locked) Text("قفل‌شده", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }

    if (showEditor) {
        NoteEditorDialog(
            initial = editing,
            onDismiss = { showEditor = false },
            onSave = { title, body, pinned ->
                scope.launch {
                    val old = editing
                    if (old == null) dao.insertNote(NoteEntity(title = title, body = body, pinned = pinned))
                    else dao.updateNote(old.copy(title = title, body = body, pinned = pinned, updatedAt = System.currentTimeMillis()))
                    showEditor = false
                }
            }
        )
    }
}

@Composable
private fun NoteEditorDialog(
    initial: NoteEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean) -> Unit
) {
    var title by remember(initial) { mutableStateOf(initial?.title ?: "") }
    var body by remember(initial) { mutableStateOf(initial?.body ?: "") }
    var pinned by remember(initial) { mutableStateOf(initial?.pinned ?: false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "یادداشت جدید" else "ویرایش یادداشت") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("عنوان") }, singleLine = true)
                OutlinedTextField(body, { body = it }, label = { Text("متن یادداشت") }, minLines = 4)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(pinned, { pinned = it })
                    Text("سنجاق‌کردن")
                }
            }
        },
        confirmButton = { TextButton(onClick = { if (title.isNotBlank()) onSave(title.trim(), body, pinned) }) { Text("ذخیره") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasksScreen(dao: DashboardDao) {
    var listId by remember { mutableIntStateOf(1) }
    val scope = rememberCoroutineScope()
    val tasks by dao.observeTasks(listId).collectAsState(initial = emptyList())
    var newTask by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("مدیریت وظایف", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(selected = listId == 1, onClick = { listId = 1 }, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text("فهرست اول") }
            SegmentedButton(selected = listId == 2, onClick = { listId = 2 }, shape = SegmentedButtonDefaults.itemShape(1, 2)) { Text("فهرست دوم") }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = newTask, onValueChange = { newTask = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("وظیفه جدید") },
            trailingIcon = {
                IconButton(onClick = {
                    if (newTask.isNotBlank()) {
                        scope.launch { dao.insertTask(TaskEntity(listId = listId, title = newTask.trim())) }
                        newTask = ""
                    }
                }) { Icon(Icons.Default.Add, "افزودن") }
            }
        )
        Spacer(Modifier.height(12.dp))
        val done = tasks.count { it.completed }
        Text("$done از ${tasks.size} وظیفه انجام شده", style = MaterialTheme.typography.labelLarge)
        if (tasks.isNotEmpty()) {
            LinearProgressIndicator(
                progress = { done.toFloat() / tasks.size.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tasks) { task ->
                Card {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(task.completed, { checked -> scope.launch { dao.updateTask(task.copy(completed = checked)) } })
                        Column(Modifier.weight(1f)) {
                            Text(task.title, fontWeight = FontWeight.Medium)
                            if (task.details.isNotBlank()) Text(task.details, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { scope.launch { dao.updateTask(task.copy(pinned = !task.pinned)) } }) {
                            Icon(Icons.Default.PushPin, "پین")
                        }
                        IconButton(onClick = { scope.launch { dao.deleteTask(task.id) } }) {
                            Icon(Icons.Default.Delete, "حذف")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LinksScreen(dao: DashboardDao) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val links by dao.observeLinks().collectAsState(initial = emptyList())
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("عمومی") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("لینک‌های من", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("عنوان لینک") }, singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(url, { url = it }, Modifier.fillMaxWidth(), label = { Text("آدرس اینترنتی") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(category, { category = it }, Modifier.fillMaxWidth(), label = { Text("دسته‌بندی") }, singleLine = true)
        Spacer(Modifier.height(8.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url.trim() else "https://${url.trim()}"
                if (title.isNotBlank() && normalized.contains(".") && normalized.length > 8) {
                    scope.launch {
                        dao.insertLink(QuickLinkEntity(title = title.trim(), url = normalized, category = category.trim().ifBlank { "عمومی" }))
                        title = ""; url = ""
                    }
                }
            }
        ) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("افزودن لینک") }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(links) { link ->
                Card {
                    Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, null)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(link.title, fontWeight = FontWeight.SemiBold)
                            Text(link.url, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            Text(link.category, style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))
                            }
                        }) { Icon(Icons.Default.OpenInBrowser, "بازکردن") }
                        IconButton(onClick = { scope.launch { dao.deleteLink(link.id) } }) { Icon(Icons.Default.Delete, "حذف") }
                    }
                }
            }
        }
    }
}
