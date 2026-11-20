package com.example.ui.screens.owner

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.Game
import com.example.data.model.Product
import com.example.ui.components.MsrScrollableColumn
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.SubtlePurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.OwnerViewModel
import com.example.ui.viewmodel.StoreFormatters

@Composable
fun OwnerGamesAndCategoriesTab(
    ownerViewModel: OwnerViewModel,
    games: List<Game>,
    categories: List<Category>
) {
    var editingGame by remember { mutableStateOf<Game?>(null) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

    MsrScrollableColumn {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Games & Services", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Button(
                onClick = { editingGame = Game(displayOrder = (games.size + 1).toLong()) },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = PitchBlack),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("owner_add_game_button")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add Game")
            }
        }
        Text(
            text = "Add Free Fire, PUBG, Minecraft, YouTube, Netflix, or any new service here. Services remain hidden until Active is ON.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        games.forEach { game ->
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${game.name} (#${game.displayOrder})",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = if (game.active) "ACTIVE (Visible to Customers)" else "HIDDEN (Draft)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (game.active) SuccessGreen else ErrorRed
                        )
                    }
                    Switch(
                        checked = game.active,
                        onCheckedChange = { ownerViewModel.saveGame(game.copy(active = it)) }
                    )
                    IconButton(onClick = { editingGame = game }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit game", tint = ElectricCyan)
                    }
                    IconButton(onClick = { ownerViewModel.deleteGame(game.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete game", tint = ErrorRed)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Categories", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Button(
                onClick = {
                    editingCategory = Category(
                        gameId = games.firstOrNull()?.id ?: "game_roblox",
                        displayOrder = (categories.size + 1).toLong()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SubtlePurple, contentColor = TextPrimary),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("owner_add_category_button")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add Category")
            }
        }

        categories.forEach { cat ->
            val gameName = games.firstOrNull { it.id == cat.gameId }?.name ?: cat.gameId
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cat.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text(
                            text = "$gameName • ${cat.type} • Inputs: ${cat.requiredInputFields.joinToString()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = cat.active,
                        onCheckedChange = { ownerViewModel.saveCategory(cat.copy(active = it)) }
                    )
                    IconButton(onClick = { editingCategory = cat }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit category", tint = ElectricCyan)
                    }
                    IconButton(onClick = { ownerViewModel.deleteCategory(cat.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete category", tint = ErrorRed)
                    }
                }
            }
        }
    }

    if (editingGame != null) {
        GameEditorDialog(
            initial = editingGame!!,
            onDismiss = { editingGame = null },
            onSave = {
                ownerViewModel.saveGame(it)
                editingGame = null
            }
        )
    }

    if (editingCategory != null) {
        CategoryEditorDialog(
            initial = editingCategory!!,
            games = games,
            onDismiss = { editingCategory = null },
            onSave = {
                ownerViewModel.saveCategory(it)
                editingCategory = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OwnerProductsTab(
    ownerViewModel: OwnerViewModel,
    games: List<Game>,
    categories: List<Category>,
    products: List<Product>
) {
    var selectedCatFilter by remember { mutableStateOf<String?>(null) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    MsrScrollableColumn {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Products & Subscriptions", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Button(
                onClick = {
                    val cat = categories.firstOrNull { it.id == selectedCatFilter } ?: categories.firstOrNull()
                    editingProduct = Product(
                        gameId = cat?.gameId ?: games.firstOrNull()?.id ?: "game_roblox",
                        categoryId = cat?.id ?: "",
                        categoryType = cat?.type ?: CategoryType.CURRENCY.name,
                        displayOrder = (products.size + 1).toLong()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = PitchBlack),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("owner_add_product_button")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add Product")
            }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(
                selected = selectedCatFilter == null,
                onClick = { selectedCatFilter = null },
                label = { Text("All (${products.size})") }
            )
            categories.forEach { cat ->
                FilterChip(
                    selected = selectedCatFilter == cat.id,
                    onClick = { selectedCatFilter = cat.id },
                    label = { Text(cat.name) }
                )
            }
        }

        val filtered = products.filter { selectedCatFilter == null || it.categoryId == selectedCatFilter }
        filtered.forEach { prod ->
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Text(
                                text = "${prod.quantityLabel} • ${StoreFormatters.formatBdt(prod.priceBdt)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = MonoFontFamily),
                                color = ElectricCyan
                            )
                        }
                        IconButton(onClick = { editingProduct = prod }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit product", tint = ElectricCyan)
                        }
                        IconButton(onClick = { ownerViewModel.deleteProduct(prod.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete product", tint = ErrorRed)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Available", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Spacer(Modifier.width(6.dp))
                            Switch(
                                checked = prod.available,
                                onCheckedChange = { ownerViewModel.saveProduct(prod.copy(available = it)) }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Active", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Spacer(Modifier.width(6.dp))
                            Switch(
                                checked = prod.active,
                                onCheckedChange = { ownerViewModel.saveProduct(prod.copy(active = it)) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (editingProduct != null) {
        ProductEditorDialog(
            initial = editingProduct!!,
            games = games,
            categories = categories,
            onDismiss = { editingProduct = null },
            onSave = {
                ownerViewModel.saveProduct(it)
                editingProduct = null
            }
        )
    }
}

@Composable
private fun GameEditorDialog(initial: Game, onDismiss: () -> Unit, onSave: (Game) -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var desc by remember { mutableStateOf(initial.description) }
    var imageUrl by remember { mutableStateOf(initial.imageUrl) }
    var order by remember { mutableStateOf(initial.displayOrder.toString()) }
    var active by remember { mutableStateOf(initial.active) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(if (initial.id.isBlank()) "Add New Game / Service" else "Edit ${initial.name}", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Game / Service Name *") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL (optional)") })
                OutlinedTextField(
                    value = order,
                    onValueChange = { order = it },
                    label = { Text("Display Order") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Active (Visible to Customers)", color = TextPrimary, modifier = Modifier.weight(1f))
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            initial.copy(
                                name = name.trim(),
                                description = desc.trim(),
                                imageUrl = imageUrl.trim(),
                                displayOrder = order.toLongOrNull() ?: 1L,
                                active = active
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = PitchBlack)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryEditorDialog(
    initial: Category,
    games: List<Game>,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var desc by remember { mutableStateOf(initial.description) }
    var selectedGameId by remember { mutableStateOf(initial.gameId) }
    var selectedType by remember { mutableStateOf(CategoryType.fromRaw(initial.type)) }
    var inputsCsv by remember { mutableStateOf(initial.requiredInputFields.joinToString(", ")) }
    var active by remember { mutableStateOf(initial.active) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(if (initial.id.isBlank()) "Add Category" else "Edit Category", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Category Name *") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                Text("Game", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    games.forEach { g ->
                        FilterChip(
                            selected = selectedGameId == g.id,
                            onClick = { selectedGameId = g.id },
                            label = { Text(g.name) }
                        )
                    }
                }
                Text("Type", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CategoryType.entries.forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { selectedType = t },
                            label = { Text(t.name) }
                        )
                    }
                }
                OutlinedTextField(
                    value = inputsCsv,
                    onValueChange = { inputsCsv = it },
                    label = { Text("Required Customer Fields (comma separated)") },
                    placeholder = { Text("e.g. Roblox Username or Player UID") }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Active", color = TextPrimary, modifier = Modifier.weight(1f))
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val fields = inputsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        onSave(
                            initial.copy(
                                gameId = selectedGameId,
                                name = name.trim(),
                                description = desc.trim(),
                                type = selectedType.name,
                                requiredInputFields = fields.ifEmpty { listOf("Account ID") },
                                active = active
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = PitchBlack)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductEditorDialog(
    initial: Product,
    games: List<Game>,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var subtitle by remember { mutableStateOf(initial.subtitle) }
    var quantityLabel by remember { mutableStateOf(initial.quantityLabel) }
    var priceText by remember { mutableStateOf(if (initial.priceBdt > 0) initial.priceBdt.toString() else "") }
    var amountText by remember { mutableStateOf(initial.amountValue.toString()) }
    var desc by remember { mutableStateOf(initial.description) }
    var imageUrl by remember { mutableStateOf(initial.imageUrl) }
    var selectedCategoryId by remember { mutableStateOf(initial.categoryId) }
    var available by remember { mutableStateOf(initial.available) }
    var active by remember { mutableStateOf(initial.active) }
    var featured by remember { mutableStateOf(initial.featured) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(if (initial.id.isBlank()) "Add Product / Subscription" else "Edit ${initial.name}", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name *") })
                OutlinedTextField(value = quantityLabel, onValueChange = { quantityLabel = it }, label = { Text("Quantity / Duration Label *") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Price BDT *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Numeric Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text("Subtitle") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL (optional)") })
                Text("Category", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(cat.name) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Available", color = TextPrimary)
                        Spacer(Modifier.width(4.dp))
                        Switch(checked = available, onCheckedChange = { available = it })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Active", color = TextPrimary)
                        Spacer(Modifier.width(4.dp))
                        Switch(checked = active, onCheckedChange = { active = it })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toLongOrNull() ?: 0L
                    if (name.isNotBlank() && price > 0L) {
                        val chosenCat = categories.firstOrNull { it.id == selectedCategoryId }
                        onSave(
                            initial.copy(
                                gameId = chosenCat?.gameId ?: initial.gameId.ifBlank { games.firstOrNull()?.id ?: "game_roblox" },
                                categoryId = chosenCat?.id ?: initial.categoryId,
                                categoryType = chosenCat?.type ?: initial.categoryType,
                                name = name.trim(),
                                subtitle = subtitle.trim(),
                                quantityLabel = quantityLabel.ifBlank { name }.trim(),
                                amountValue = amountText.toLongOrNull() ?: 0L,
                                priceBdt = price,
                                description = desc.trim(),
                                imageUrl = imageUrl.trim(),
                                available = available,
                                active = active,
                                featured = featured
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = PitchBlack)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}
