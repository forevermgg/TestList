package com.forever.testlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.forever.testlist.ui.theme.TestListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestListTheme {
                MyApp()
            }
        }
    }
}

@Composable
fun MyApp() {
    // StickyHeadersHighlightExample()
    Box(modifier = Modifier.fillMaxSize()) {
        // 页面主内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // 给底部按钮留空间
        ) {
            CombinedStickyLists()
        }

        // 底部按钮固定
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight() // 👈 高度根据按钮实际内容自适应
                .align(Alignment.BottomCenter)
                .background(Color.Yellow) // ✅ 可选：加背景确保可见
        ) {
            Column {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = Color.LightGray
                )
                ThreeAlignedButtons(
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StickyHeadersHighlightExample() {
    val sections = (1..5).map { section ->
        "Section $section" to List(10) { "Item $section-$it" }
    }

    val listState = rememberLazyListState()
    val currentStickyIndex = remember { derivedStateOf { getCurrentStickyHeaderIndex(listState, sections) } }

    // 所有选中项（操作整个列表）
    val selectedItems = remember { mutableStateOf(mutableSetOf<String>()) }

    LazyColumn(state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())) {
        sections.forEachIndexed { index, (header, items) ->
            stickyHeader {
                val isCurrentSticky = currentStickyIndex.value == index
                // 动画过渡颜色
                val backgroundColor by animateColorAsState(
                    targetValue = if (isCurrentSticky) Color.Red else Color.LightGray,
                    label = "headerColor"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = header, color = Color.White)

                    // 仅当前 sticky header 显示按钮
                    if (isCurrentSticky) {
                        Row {
                            TextButton(onClick = {
                                // 全选：添加所有 item
                                selectedItems.value = sections.flatMap { it.second }.toMutableSet()
                            }) {
                                Text("全选", color = Color.White)
                            }

                            TextButton(onClick = {
                                val allItems = sections.flatMap { it.second }
                                val current = selectedItems.value
                                selectedItems.value = allItems.mapNotNull {
                                    if (current.contains(it)) null else it
                                }.toMutableSet()
                            }) {
                                Text("反选", color = Color.White)
                            }

                            TextButton(onClick = {
                                selectedItems.value.clear()
                            }) {
                                Text("取消", color = Color.White)
                            }
                        }
                    }
                }
            }

            // 列表项
            items(items) { item ->
                val isSelected = selectedItems.value.contains(item)
                Text(
                    text = if (isSelected) "✅ $item" else item,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(if (isSelected) Color(0xFFE0F7FA) else Color.Transparent)
                )
            }
        }
    }
}

// Helper function to find the current sticky header index
fun getCurrentStickyHeaderIndex(
    listState: LazyListState,
    sections: List<Pair<String, List<String>>>
): Int {
    // Get index of the first visible item
    val firstVisibleIndex = listState.firstVisibleItemIndex

    // Count how many items are before each header
    var runningIndex = 0
    for ((sectionIndex, section) in sections.withIndex()) {
        if (firstVisibleIndex == runningIndex) {
            return sectionIndex
        }
        runningIndex += 1 + section.second.size // 1 for header + number of items
        if (firstVisibleIndex < runningIndex) {
            return sectionIndex
        }
    }
    return 0
}

/**
 * ✅ Header 背景红色仍只用于 Sticky Header
 *
 * ✅ Project 和 Task 分组操作。在project 和 task section的header 都在当前页面显示的情况下，project的header 和task section的header 都可以显示取消。全选 反选按钮。但是针对task section的列表，需要满足，task section的header 在当前页面显示的时候 ，第一个task section的header 显示取消。全选 反选按钮。
 *
 * ✅ LazyColumn 结构不变
 *
 * ✅ 按钮功能：全选 / 反选 / 取消
 *
 * ✅ 支持实时刷新和选中高亮
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CombinedStickyLists() {
    val randomTexts = List(3) { "Random Item ${it + 1}" }
    val randomTextsCount = randomTexts.size
    val projectSections = listOf(
        "Project A" to listOf("Compose", "ViewModel"/*, "Navigation", "Model", "Http", "https", "Curl",
                              "AndroidX", "Camera2", "LiveData", "Page2"*/),
        "Project B" to listOf("LiveData", "Room", "Kotlin Flow")
    )

    val taskSections = listOf(
        "Task Section 1" to List(50) { "Task 1-${it + 1}" },
        "Task Section 2" to List(50) { "Task 2-${it + 1}" },
        "Task Section 3" to List(50) { "Task 2-${it + 1}" }
    )

    val allSections = projectSections + taskSections
    val projectCount = projectSections.size

    val listState = rememberLazyListState()
    val currentStickyIndex = remember { mutableStateOf(0) }
    val visibleHeaderIndices = remember { mutableStateOf(setOf<Int>()) }

    val selectedTasks = remember { mutableStateOf(mutableSetOf<String>()) }
    val selectedProjects = remember {
        mutableStateMapOf<Int, MutableSet<String>>().apply {
            projectSections.forEachIndexed { i, _ -> put(i, mutableSetOf()) }
        }
    }

    val firstVisibleTaskHeaderIndex = remember { mutableStateOf<Int?>(null) }

    // Observe visible headers and update current sticky + visibility
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .collect { layoutInfo ->
                val visibleItems = layoutInfo.visibleItemsInfo
                val visibleHeaders = mutableSetOf<Int>()
                var firstTaskHeaderFound: Int? = null

                visibleItems.forEach { item ->
                    val headerIdx = findHeaderIndexFromItemIndex(item.index, allSections, randomTextsCount)
                    val sectionStart = getItemStartIndexForSection(headerIdx, allSections, randomTextsCount)
                    if (item.index == sectionStart) {
                        visibleHeaders.add(headerIdx)

                        if (headerIdx >= projectCount && firstTaskHeaderFound == null) {
                            firstTaskHeaderFound = headerIdx
                        }
                    }
                }

                currentStickyIndex.value = findHeaderIndexFromItemIndex(
                    layoutInfo.visibleItemsInfo.firstOrNull { it.offset == 0 }?.index ?: 0,
                    allSections,
                    randomTextsCount
                    )

                visibleHeaderIndices.value = visibleHeaders
                firstVisibleTaskHeaderIndex.value = firstTaskHeaderFound
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        // ✅ 显示随机文本项
        items(randomTexts) { item ->
            Text(
                text = item,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color(0xFFF5F5F5))
            )
        }
        allSections.forEachIndexed { index, (header, items) ->
            val isProject = index < projectCount
            val isSticky = index == currentStickyIndex.value
            val isVisible = visibleHeaderIndices.value.contains(index)
            val isFirstVisibleTaskHeader = index == firstVisibleTaskHeaderIndex.value

            val showButtons = when {
                isProject && isVisible -> true
                !isProject && isFirstVisibleTaskHeader -> true
                else -> false
            }

            stickyHeader {
                val bgColor by animateColorAsState(
                    targetValue = if (isSticky) Color.Red else Color.LightGray,
                    label = "headerColor"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .defaultMinSize(minHeight = 56.dp) // ✅ 设置最小高度
                        .background(bgColor)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = header, color = Color.White)

                    if (showButtons) {
                        Row {
                            TextButton(onClick = {
                                if (isProject) {
                                    selectedProjects[index] =
                                        projectSections[index].second.toMutableSet()
                                } else {
                                    selectedTasks.value =
                                        taskSections.flatMap { it.second }.toMutableSet()
                                }
                            }) {
                                Text("全选", color = Color.White)
                            }

                            TextButton(onClick = {
                                if (isProject) {
                                    val itemsInGroup = projectSections[index].second
                                    val current = selectedProjects[index] ?: mutableSetOf()
                                    selectedProjects[index] = itemsInGroup.mapNotNull {
                                        if (current.contains(it)) null else it
                                    }.toMutableSet()
                                } else {
                                    val allItems = taskSections.flatMap { it.second }
                                    val current = selectedTasks.value
                                    selectedTasks.value = allItems.mapNotNull {
                                        if (current.contains(it)) null else it
                                    }.toMutableSet()
                                }
                            }) {
                                Text("反选", color = Color.White)
                            }

                            TextButton(onClick = {
                                if (isProject) {
                                    selectedProjects[index]?.clear()
                                } else {
                                    selectedTasks.value.clear()
                                }
                            }) {
                                Text("取消", color = Color.White)
                            }
                        }
                    }
                }
            }

            items(items) { item ->
                val isSelected = if (isProject) {
                    selectedProjects[index]?.contains(item) == true
                } else {
                    selectedTasks.value.contains(item)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(if (isSelected) Color(0xFFE0F7FA) else Color.Transparent),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            if (isProject) {
                                val currentSet = selectedProjects[index] ?: mutableSetOf()
                                val newSet = currentSet.toMutableSet() // 新集合触发 Compose 重组
                                if (checked) {
                                    newSet.add(item)
                                } else {
                                    newSet.remove(item)
                                }
                                selectedProjects[index] = newSet // 替换原集合
                            } else {
                                val newSet = selectedTasks.value.toMutableSet()
                                if (checked) {
                                    newSet.add(item)
                                } else {
                                    newSet.remove(item)
                                }
                                selectedTasks.value = newSet // 替换原集合
                            }
                        }
                    )
                    Text(
                        text = item,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

// === Utility functions ===
fun findHeaderIndexFromItemIndex(itemIndex: Int, sections: List<Pair<String, List<String>>>, offset: Int): Int {
    var running = offset
    for ((sectionIndex, section) in sections.withIndex()) {
        if (itemIndex == running) return sectionIndex
        running += 1 + section.second.size
        if (itemIndex < running) return sectionIndex
    }
    return 0
}

fun getItemStartIndexForSection(sectionIndex: Int, sections: List<Pair<String, List<String>>>, offset: Int): Int {
    var index = offset
    for (i in 0 until sectionIndex) {
        index += 1 + sections[i].second.size
    }
    return index
}

@Composable
fun ThreeAlignedButtons(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 20.dp), // padding 控制内部间距
    ) {
        // 左侧按钮
        TextButton(
            onClick = { /*TODO*/ },
            modifier = Modifier.align(Alignment.CenterStart),
            colors = ButtonDefaults.textButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Unspecified
            ),
            contentPadding = PaddingValues(
                start = 12.dp,
                top = 8.dp,
                end = 12.dp,
                bottom = 8.dp
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("左边")
            }
        }

        // 中间按钮
        TextButton(
            onClick = { /*TODO*/ },
            modifier = Modifier.align(Alignment.Center),
            colors = ButtonDefaults.textButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Unspecified
            ),
            contentPadding = PaddingValues(
                start = 12.dp,
                top = 8.dp,
                end = 12.dp,
                bottom = 8.dp
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("中间")
            }
        }

        // 右侧按钮
        TextButton(
            onClick = { /*TODO*/ },
            modifier = Modifier.align(Alignment.CenterEnd),
            colors = ButtonDefaults.textButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Unspecified
            ),
            contentPadding = PaddingValues(
                start = 12.dp,
                top = 8.dp,
                end = 12.dp,
                bottom = 8.dp
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("右边")
            }
        }
    }
}
