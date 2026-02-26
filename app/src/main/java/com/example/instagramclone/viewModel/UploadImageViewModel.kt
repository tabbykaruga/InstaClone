import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.instagramclone.sharedUtils.handleException
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class UploadImageViewModel
@Inject
constructor(
    val storage: FirebaseStorage,
) : ViewModel() {
  val inProgress = mutableStateOf(false)

  private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
    inProgress.value = true
    val storageRef = storage.reference
    val uuid = UUID.randomUUID()
    val imageRef = storageRef.child("images/$uuid")
    val uploadTask = imageRef.putFile(uri)

    uploadTask
        .addOnSuccessListener {
          val result = it.metadata?.reference?.downloadUrl
          result?.addOnSuccessListener { onSuccess }
        }
        .addOnFailureListener { e ->
          handleException(e, "")
          inProgress.value = false
        }
  }

  fun uploadProfileImage(
      uri: Uri,
  ) {
    uploadImage(uri) {}
  }
}
