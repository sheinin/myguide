package android.myguide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import android.myguide.ui.theme.MyGuideTheme
import androidx.room.Room

class MainActivity : ComponentActivity() {

    private lateinit var vm: ViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()



        val dao = UserDatabase.getDatabase(application).userDao()
        val repository = Repository(dao)
        vm = ViewModel(repository)

        // Fetch all users when the activity starts

        vm.allUsers.observe(this) { users ->
            users.map { qqq("users: $it") }
            //userAdapter.submitList(users) // Use submitList here
        }
        vm.fetchItems()
        setContent {
            MyGuideTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyGuideTheme {
        Greeting("Android")
    }
}


fun qqq(q: String) { println("qqq $q") }