package android.myguide

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.widget.Toolbar
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.jvm.java

@Entity(tableName = "items", primaryKeys = ["id","parent"])
data class Item(
    val parent: String,
    val id: String,
    val drawable: Int?,
    val info: String?
)

interface ListInterface {
    val title: String
    val subtitle: String?
    val description: String?
}

class ViewModel(private val repository: Repository) : ViewModel() {

    class Cycler {
        val isMap = MutableLiveData(true)
        val size = MutableLiveData(0)
        data class Item(
            val title: String,
            val subtitle: String?,
            val description: String?,
            var x: Dp,
            var y: Dp,
            val w: Dp,
            val h: Dp
        )
        data class CyclerState(
            val isMap: Boolean = true,
            val items: List<Item> = emptyList(),
            val hiddenIndices: Set<Int> = emptySet()
        )
        val item =
            Item(
                title = "",
                subtitle = "",
                description = "",
                x = 0.dp,
                y = 0.dp,
                w = 0.dp,
                h = 0.dp
            )
        private val _items = MutableStateFlow<List<Item>>(emptyList())
        val items = _items.asStateFlow()

        val hidden = List(batch) { MutableLiveData(false) }
        //val items = List(batch) { MutableLiveData<Item>().apply { postValue(item) } }

      //  var items = mutableStateListOf<Item>()
        //    private set

        // Example: Initialize with some data
        init {
            repeat(batch) {
                _items.value += item
            }
        }



        fun updateItem(index: Int, item: Item) {
            _items.update {
                it.mapIndexed { ix, it ->
                    if (ix == index)
                        it.copy(
                            title = item.title,
                            subtitle = item.subtitle,
                            description = item.description,
                            x = item.x,
                            y = item.y,
                            w = item.w,
                            h = item.h
                        )
                    else it
                }
            }
        }



      //  val its: List<LiveData<MutableLiveData<Item>>> get() = items

        //private val _items = mutableStateListOf<Item>(item,item,item,item,item,item,item,item,item,item,item,item,item,item,item,item,)
        //val items: SnapshotStateList<Item> = _items
    }

    class Screen {
        val isMap = MutableLiveData(false)
        val cycler = Cycler()
        val x = MutableLiveData(0.dp)
        val y = MutableLiveData(0.dp)
        val w = MutableLiveData(0.dp)
        val h = MutableLiveData(0.dp)
    }
    val current = MutableLiveData<Boolean?>(null)
    val toolbar = Toolbar()
    val showSplash = MutableLiveData(true)

    val mapShowing = MutableLiveData(true)
    val screen = mapOf(false to Screen(), true to Screen())
    private val _allItems = MutableLiveData<List<Item>>()
    val allItems: LiveData<List<Item>> get() = _allItems
    fun fetchItems() {
        repository.getItems {
            _allItems.postValue(it)
        }
    }

    val allProducts: LiveData<List<ListInterface>> = MutableLiveData<List<ListInterface>>()

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

