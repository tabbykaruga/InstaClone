package com.example.instagramclone.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.instagramclone.viewModel.AuthViewModel

@Composable
fun SearchScreen(navController: NavController, vm: AuthViewModel) {
  Column(modifier = Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
    Column(modifier = Modifier.weight(1f)) { Text("SEARCH SCREEN") }

    BottomNavigationMenu(BottomNavItem.SEARCH, navController)
  }
}
