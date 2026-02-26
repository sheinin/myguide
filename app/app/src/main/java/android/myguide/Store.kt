package android.myguide

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    private val _allItems = MutableLiveData<List<Item>>()
    val allItems: LiveData<List<Item>> get() = _allItems

    fun fetchItems() {
        repository.getItems {
            _allItems.postValue(it)
        }
    }
}


class Repository(private val storeDao: StoreDao) {
    fun getItems(callback: (List<Item>) -> Unit) {
        Thread {
            callback(storeDao.items())
        }.start()
    }
}


@Dao
interface StoreDao {
    @Query("SELECT * FROM items ORDER BY id DESC")
    fun items(): List<Item>
}


@Database(entities = [Item::class], version = 1, exportSchema = false)
abstract class StoreDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao

    companion object {
        @Volatile
        private var INSTANCE: StoreDatabase? = null

        fun getDatabase(context: Context): StoreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, StoreDatabase::class.java, "store_database")
                    .createFromAsset("store_database")
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

