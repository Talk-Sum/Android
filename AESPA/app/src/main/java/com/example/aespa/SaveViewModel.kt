package com.example.aespa

import android.net.Uri
import androidx.lifecycle.MutableLiveData


data class saveData(
    var imgStr : String = "",
    var img: Uri? = null,
    var date: String? = null,
    var name: String? = null,
    var user_name: String? = null,
    var content: String? = null
)



enum class event { ADD, UPDATE, DELETE }

class SaveViewModel {
    var itemsEventPos = -1 //
    var itemLongClick = -1 //길게 클릭
    var itemsEvent = event.ADD // 아이템 이벤트
    val itemsListData = MutableLiveData<ArrayList<saveData>>()
    //저장된 아이템의 변화 감지
    val items = ArrayList<saveData>()
    //아이템들이 들어있는 리스트
    val itemClickEvent = MutableLiveData<Int>()
    fun clearItems() {
        items.clear()
    }
    fun addItem(uri: Uri?, date: String, name : String, user_name : String, content : String,urlstr : String ) {
        itemsEvent = event.ADD
        itemsEventPos = items.size
        items.add(saveData(urlstr,uri,date,name,user_name,content))
        itemsListData.value = items
    }

    val itemSize get() = items.size
    //이벤트 발생 감지
    fun getItem(pos:Int) = items[pos]

    fun deleteItem(pos: Int) {
        itemsEvent = event.DELETE
        itemsEventPos = pos
        items.removeAt(pos)
        itemsListData.value = items
    }
    fun updateItem(pos: Int, uri: Uri?) {
        itemsEvent = event.UPDATE
        itemsEventPos = pos
        val item = saveData(items[pos].imgStr,uri,items[pos].date,items[pos].name,items[pos].user_name,items[pos].content)
        items[pos] = item
        itemsListData.value = items // 옵저버에게 라이브데이터가 변경된 것을 알리기 위해
    }

}