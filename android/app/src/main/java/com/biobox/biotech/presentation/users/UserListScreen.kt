package com.biobox.biotech.presentation.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.presentation.components.cards.BioTechCard
import com.biobox.biotech.presentation.components.indicators.StatusBadge
import com.biobox.biotech.presentation.components.loading.LoadingView
import com.biobox.biotech.presentation.components.navigation.BioTechTopBar
import com.biobox.biotech.presentation.components.states.ErrorState
import com.biobox.biotech.presentation.components.textfields.BioTechSearchBar
import com.biobox.biotech.presentation.theme.*
import com.biobox.biotech.core.common.SyncStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    viewModel: UserViewModel = hiltViewModel()
) {
    val state by viewModel.users.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            BioTechTopBar(title = "USUARIOS")
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                BioTechSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Buscar usuario por nombre o email..."
                )
            }

            when (val s = state) {
                is UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorState(message = s.message, onRetry = { viewModel.loadUsers() })
                is UiState.Success -> {
                    val filteredUsers = remember(s.data, searchQuery) {
                        if (searchQuery.isBlank()) s.data
                        else s.data.filter { 
                            it.nombre.contains(searchQuery, ignoreCase = true) || 
                            it.apellido.contains(searchQuery, ignoreCase = true) ||
                            it.email.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filteredUsers, key = { it.id }) { user ->
                            UserItem(user = user)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun UserItem(user: com.biobox.biotech.domain.model.User) {
    BioTechCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = PrimaryBlue.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = PrimaryBlue
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.nombre} ${user.apellido}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = SyncStatus.SYNCED) // Placeholder, as User model might not have syncStatus
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = PrimaryCyan.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = user.rol.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = PrimaryCyan,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            IconButton(onClick = { /* Edit or details */ }) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondaryDark)
            }
        }
    }
}
