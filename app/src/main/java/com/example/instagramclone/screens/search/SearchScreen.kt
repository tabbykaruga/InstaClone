package com.example.instagramclone.screens.search

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instagramclone.routes.DestinationScreen
import com.example.instagramclone.screens.BottomNavItem
import com.example.instagramclone.screens.BottomNavigationMenu
import com.example.instagramclone.screens.posts.PostLists
import com.example.instagramclone.sharedUtils.MySearchScreenShimmer
import com.example.instagramclone.sharedUtils.NavParam
import com.example.instagramclone.sharedUtils.navigateTo
import com.example.instagramclone.viewModel.AuthViewModel

@Composable
fun SearchScreen(navController: NavController, vm: AuthViewModel) {

  val searchedPostLoading = vm.searchedPostProgress.value
  val searchedPosts = vm.searchedPosts.value
  var searchTerm by rememberSaveable() { mutableStateOf("") }

  Scaffold(bottomBar = { BottomNavigationMenu(BottomNavItem.SEARCH, navController) }) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
      if (searchedPostLoading) {
        MySearchScreenShimmer()
      } else {
        SearchBar(
            searchTerm,
            onSearchChange = { searchTerm = it },
            onSearch = { vm.searchPost(searchTerm) },
        )
      }
      PostLists(
          isContextLoading = false,
          postLoading = searchedPostLoading,
          posts = searchedPosts,
          modifier = Modifier.fillMaxSize().padding(8.dp).imePadding(),
      ) { post ->
        navigateTo(navController, DestinationScreen.SinglePost, NavParam("post", post))
      }
    }
  }
}

@Composable
fun SearchBar(
    searchTerm: String,
    onSearchChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
  val focusManager = LocalFocusManager.current
  TextField(
      value = searchTerm,
      onValueChange = onSearchChange,
      modifier =
          Modifier.padding(8.dp).fillMaxWidth().border(1.dp, color = Color.LightGray, CircleShape),
      shape = CircleShape,
      keyboardOptions =
          KeyboardOptions(
              keyboardType = KeyboardType.Text,
              imeAction = ImeAction.Search,
          ),
      keyboardActions =
          KeyboardActions(
              onSearch = {
                onSearch()
                focusManager.clearFocus()
              }
          ),
      maxLines = 1,
      singleLine = true,
      colors =
          TextFieldDefaults.colors(
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              focusedTextColor = Color.Black,
              unfocusedTextColor = Color.Black,
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent,
              disabledIndicatorColor = Color.Transparent,
          ),
      trailingIcon = {
        IconButton(
            onClick = {
              onSearch()
              focusManager.clearFocus()
            }
        ) {
          Icon(imageVector = Icons.Filled.Search, contentDescription = "Search Post")
        }
      },
  )
}
