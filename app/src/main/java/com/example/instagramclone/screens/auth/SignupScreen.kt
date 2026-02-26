package com.example.instagramclone.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.instagramclone.R
import com.example.instagramclone.routes.DestinationScreen
import com.example.instagramclone.sharedUtils.CheckSignedIn
import com.example.instagramclone.sharedUtils.CommonProgressSpinner
import com.example.instagramclone.sharedUtils.navigateTo
import com.example.instagramclone.viewModel.AuthViewModel

@Composable
fun SignUpScreen(navController: NavController, vm: AuthViewModel) {
  // navigation to FeedScreen
  CheckSignedIn(navController, vm)

  // dismiss the keyboard
  val focus = LocalFocusManager.current

  val usernameState = remember { mutableStateOf(TextFieldValue()) }
  val emailState = remember { mutableStateOf(TextFieldValue()) }
  val passwordState = remember { mutableStateOf(TextFieldValue()) }

  Box(modifier = Modifier.fillMaxSize().imePadding()) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
          painter = painterResource(id = R.drawable.ig_icon),
          contentDescription = null,
          modifier = Modifier.width(250.dp).padding(top = 120.dp).padding(8.dp),
      )
      Text(
          "Signup",
          modifier = Modifier.padding(8.dp),
          fontSize = 30.sp,
          fontFamily = FontFamily.SansSerif,
      )
      OutlinedTextField(
          value = usernameState.value,
          onValueChange = { usernameState.value = it },
          modifier = Modifier.padding(8.dp),
          label = { Text("Username") },
      )
      OutlinedTextField(
          value = emailState.value,
          onValueChange = { emailState.value = it },
          modifier = Modifier.padding(8.dp),
          label = { Text("Email") },
      )
      OutlinedTextField(
          value = passwordState.value,
          onValueChange = { passwordState.value = it },
          modifier = Modifier.padding(8.dp),
          label = { Text("Password") },
          visualTransformation = PasswordVisualTransformation(),
      )
      Button(
          onClick = {
            focus.clearFocus(force = true)

            vm.onSignUp(
                usernameState.value.text,
                emailState.value.text,
                passwordState.value.text,
            )
          },
          modifier = Modifier.padding(8.dp),
      ) {
        Text("SIGN UP")
      }
      Text(
          "Already a User? Go to Login ",
          color = Color.Blue,
          modifier =
              Modifier.padding(8.dp).clickable {
                navigateTo(navController, DestinationScreen.Login)
              },
      )
    }
    val isLoading = vm.inProgress.value
    if (isLoading) CommonProgressSpinner()
  }
}
