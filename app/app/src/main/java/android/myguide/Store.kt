package android.myguide

import android.content.Context
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
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
    val pic: String?,
    val title: String?,
    val origin: String?,
    val description: String?,
    val drawable: Int?,
)


@Entity(tableName = "shops", primaryKeys = ["id"])
data class Shop(
    val id: String,
    val title: String,
    val description: String,
    val origin: String,
    val lat: Double,
    val lng: Double,
    val drawable: Int?,
)

@Entity(tableName = "shop_items", primaryKeys = ["shop", "item"])
data class ShopItems(
    val shop: String,
    val item: String
)

interface ListInterface {
    val description: String?
    val drawable: Int?
    val id: String?
    val origin: String?
    val title: String?
}

class ViewModel(private val repository: Repository) : ViewModel() {

    class Cycler {
        val isMap = MutableLiveData(true)
        val size = MutableLiveData(0)
        data class Item(
            val id: String,
            val title: String,
            val subtitle: String?,
            val description: String?,
            var x: Dp,
            var y: Dp,
            val w: Dp,
            val h: Dp
        )
        val item =
            Item(
                id = "",
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
        //val hidden = List(batch) { MutableLiveData(false) }
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
                            id = item.id,
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
    private val _allItems = MutableLiveData<List<ListInterface>>()
    val allItems: LiveData<List<ListInterface>> get() = _allItems
    fun fetchItems() {
        repository.getItems {
            _allItems.postValue(it)
        }
    }
    fun fetchItems(id: String) {
        repository.getItems {
            _allItems.postValue(it)
        }
    }
    private val _allShops = MutableLiveData<List<ListInterface>>()
    val allShops: LiveData<List<ListInterface>> get() = _allShops
    fun fetchShops() {
        repository.getShops {
            _allShops.postValue(it)
        }
    }
    fun fetchShops(id: String) {
        repository.getShops(id) {
            _allShops.postValue(it)
        }
    }
}


class Repository(private val storeDao: StoreDao) {
    fun getItems(callback: (List<ListInterface>) -> Unit) {
        Thread {
            callback(storeDao.items().map { it.toInterface() }.toList())
        }.start()
    }
    fun getItems(id: String, callback: (List<ListInterface>) -> Unit) {
        Thread {
            callback(storeDao.items(id).map { it.toInterface() }.toList())
        }.start()
    }
    fun getShops(callback: (List<ListInterface>) -> Unit) {
        Thread {
            callback(storeDao.shops().map { it.toInterface() }.toList())
        }.start()
    }
    fun getShops(id: String, callback: (List<ListInterface>) -> Unit) {
        Thread {
            callback(storeDao.shops(id).map { it.toInterface() }.toList())
        }.start()
    }
}


@Dao
@RewriteQueriesToDropUnusedColumns
interface StoreDao {
    @Query("SELECT * FROM items WHERE title IS NOT NULL ORDER BY title DESC")
    fun items(): List<Item>
    @Query("SELECT * FROM items WHERE id IN (SELECT item from shop_items where shop = :id) ORDER BY title DESC")
    fun items(id: String): List<Item>
    @Query("SELECT * FROM shops ORDER BY title DESC")
    fun shops(): List<Shop>
    @Query("SELECT * FROM shops WHERE id IN (SELECT shop from shop_items where item = :id) ORDER BY title DESC")
    fun shops(id: String): List<Shop>
}


@Database(entities = [Item::class, Shop::class, ShopItems::class], version = 1, exportSchema = false)
abstract class StoreDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao

    companion object {
        @Volatile
        private var INSTANCE: StoreDatabase? = null

        fun getDatabase(context: Context): StoreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        StoreDatabase::class.java,
                        "store_database"
                    )
                    .createFromAsset("store_database")
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


fun Item.toInterface(): ListInterface {
    return object : ListInterface {
        override val id = this@toInterface.id
        override val description = this@toInterface.description
        override val drawable = this@toInterface.drawable
        override val origin= this@toInterface.origin
        override val title = this@toInterface.title
    }
}


fun Shop.toInterface(): ListInterface {
    return object : ListInterface {
        override val id = this@toInterface.id
        override val description = this@toInterface.description
        override val drawable = this@toInterface.drawable
        override val origin= this@toInterface.origin
        override val title = this@toInterface.title
    }
}