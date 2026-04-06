package com.example.instagramclone.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.instagramclone.R
import com.example.instagramclone.routes.DestinationScreen
import com.example.instagramclone.sharedUtils.CheckSignedIn
import com.example.instagramclone.sharedUtils.navigateTo
import com.example.instagramclone.viewModel.AuthViewModel

@Composable
fun LoginScreen(navController: NavController, vm: AuthViewModel) {
  // navigation to FeedScreen
  CheckSignedIn(navController, vm)

  // dismiss the keyboard
  val focus = LocalFocusManager.current

  val emailState = remember { mutableStateOf(TextFieldValue()) }
  val passwordState = remember { mutableStateOf(TextFieldValue()) }
  var passwordVisible = remember { mutableStateOf(false) }
  var errorMessage = remember { mutableStateOf("") }

  Box(modifier = Modifier.fillMaxSize().imePadding().background(Color(0xFFF0F0F0))) {
    Column(
        modifier =
            Modifier.fillMaxWidth().wrapContentHeight().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
          painter = painterResource(id = R.drawable.ig_icon),
          contentDescription = null,
          modifier = Modifier.width(150.dp).padding(top = 100.dp).padding(8.dp),
      )
      Text(
          "InstaClone",
          modifier = Modifier.padding(8.dp),
          fontSize = 45.sp,
          fontFamily = FontFamily.Cursive,
      )
      Card(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
      ) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(top = 25.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          OutlinedTextField(
              value = emailState.value,
              onValueChange = {
                emailState.value = it
                errorMessage.value = ""
              },
              modifier = Modifier.fillMaxWidth().padding(8.dp),
              label = { Text("Email") },
              isError = errorMessage.value.isNotEmpty() && emailState.value.text.isEmpty(),
              supportingText = {
                if (errorMessage.value.isNotEmpty() && emailState.value.text.isEmpty()) {
                  Text("Email is required", color = Color.Red)
                }
              },
          )
          OutlinedTextField(
              value = passwordState.value,
              onValueChange = {
                passwordState.value = it
                errorMessage.value = ""
              },
              modifier = Modifier.fillMaxWidth().padding(8.dp),
              label = { Text("Password") },
              visualTransformation =
                  if (passwordVisible.value) VisualTransformation.None
                  else PasswordVisualTransformation(),
              trailingIcon = {
                IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                  Icon(
                      imageVector =
                          if (passwordVisible.value) Icons.Default.VisibilityOff
                          else Icons.Default.Visibility,
                      contentDescription =
                          if (passwordVisible.value) "Hide password" else "Show password",
                  )
                }
              },
              isError = errorMessage.value.isNotEmpty() && passwordState.value.text.isEmpty(),
              supportingText = {
                if (errorMessage.value.isNotEmpty() && passwordState.value.text.isEmpty()) {
                  Text("Password is required", color = Color.Red)
                }
              },
          )
          val isLoading = vm.inProgress.value
          Button(
              onClick = {
                if (emailState.value.text.isEmpty() || passwordState.value.text.isEmpty()) {
                  errorMessage.value = "error"
                } else {
                  errorMessage.value = ""
                  focus.clearFocus(force = true)
                  vm.onLogin(emailState.value.text, passwordState.value.text)
                }
              },
              enabled = !isLoading,
              modifier = Modifier.padding(top = 15.dp).height(52.dp),
              shape = RoundedCornerShape(6.dp),
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                  color = Color.Blue,
                  modifier = Modifier.size(24.dp),
                  strokeWidth = 2.dp,
              )
            } else {
              Text("LOG IN", fontSize = 16.sp, fontWeight = FontWeight.W500)
            }
          }
          Text(
              "New Here? Go to SignUp ",
              fontWeight = FontWeight.W500,
              fontSize = 15.sp,
              fontStyle = FontStyle.Italic,
              color = Color.Blue,
              modifier =
                  Modifier.padding(top = 28.dp, bottom = 20.dp).clickable {
                    navigateTo(navController, DestinationScreen.Signup)
                  },
          )
        }
      }
    }
  }
}
