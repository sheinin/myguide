package android.myguide

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlin.jvm.java

@Entity(tableName = "items", primaryKeys = ["id","parent"])
data class Item(
    val parent: String,
    val id: String,
    val drawable: Int?,
    val info: String?
)

class ViewModel(private val repository: Repository) : ViewModel() {

    private val _allUsers = MutableLiveData<List<Item>>()
    val allUsers: LiveData<List<Item>> get() = _allUsers


    fun fetchItems() {
        repository.getItems { users ->
            qqq("u"+users)
            // Post the result back to the LiveData
            _allUsers.postValue(users)
        }
    }
}


class Repository(private val itemDao: ItemDao) {


    fun getItems(callback: (List<Item>) -> Unit) {
        // Run query operation in a separate thread
        Thread {
            val users = itemDao.items()
            // Pass the results back to the callback
            callback(users)
        }.start()
    }
}


@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY id DESC")
    fun items(): List<Item>
}

// Define the database with entities and version
@Database(entities = [Item::class], version = 1, exportSchema = false)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, UserDatabase::class.java, "store_database")
                    .createFromAsset("store_database")
                    .build()
                /*
                    Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "store_database"
                ).build()

                 */

                INSTANCE = instance
                instance
            }
        }
    }
}

