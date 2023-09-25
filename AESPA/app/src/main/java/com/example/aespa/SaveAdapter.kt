package com.example.aespa

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream
fun setEllipsizeText(textView: TextView, originalText: String, maxLength: Int) {
    if (originalText.length > maxLength) {
        val truncatedText = originalText.substring(0, maxLength - 3) + "..."
        textView.text = truncatedText
    } else {
        textView.text = originalText
    }
}
class SaveAdapter(private val viewModel4: SaveViewModel,
                  private val imageSelectedListener: ImageSelectedListener) : RecyclerView.Adapter<SaveAdapter.ViewHolder4>(){
    companion object{
        var lastSelectedPosition = -1
    }
    private val databaseReference = FirebaseDatabase.getInstance().reference



    fun deleteItem(position: Int) {
        if (position >= 0 && position < viewModel4.items.size) {
            val itemToDelete = viewModel4.items[position] // 삭제할 아이템
            val user_name = itemToDelete.user_name // 사용자 이름 (또는 필요한 식별자)
            val date = itemToDelete.date // 아이템의 날짜 (또는 필요한 식별자)

            // Firebase 데이터베이스에서 해당 아이템 삭제
            val reference = databaseReference.child("user").child("$user_name")
            reference.orderByChild("date").equalTo(date).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        snapshot.ref.removeValue() // 해당 아이템을 삭제합니다.
                    }
                    // Firebase에서 삭제가 완료되면 RecyclerView의 아이템을 삭제
                    viewModel4.deleteItem(position)
                    notifyItemRemoved(position)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "onCancelled", databaseError.toException())
                }
            })
        }
    }


    inner class ViewHolder4(private val view: View) : RecyclerView.ViewHolder(view) {
        // 세팅 뷰들
        val imgv = view.findViewById<ImageView>(R.id.imageView10)
        val namet = view.findViewById<TextView>(R.id.name)
        val datet = view.findViewById<TextView>(R.id.date)
        val user_namet = view.findViewById<TextView>(R.id.username)
        val previewt = view.findViewById<TextView>(R.id.preview)

        private fun showPopupMenu(view: View, position: Int) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_delete -> {
                        // 삭제 메뉴 클릭 시 팝업 메뉴에서 아이템 삭제 작업 수행
                        deleteItem(position)
                        true
                    }
                    // 다른 메뉴 아이템에 대한 처리 추가 가능
                    else -> false
                }
            }

            // 팝업 메뉴가 닫히도록 추가
            popupMenu.setOnDismissListener {
                // 팝업 메뉴가 닫힐 때 실행될 코드 (옵셔널)
            }

            popupMenu.show()
        }

        fun bind(data: saveData) {
            namet.text = "노트 제목 : ${data.name}" // data.name 대신 fileName 사용
            datet.text = "날짜 : ${data.date}"
            user_namet.text = "작성자 : ${data.user_name}"
            previewt.text = "미리 보기 : ${data.content}"
            setEllipsizeText(previewt, previewt.text.toString(), 20)
        }

        private fun fetchImageFromGallery(position: Int) {
            val item = viewModel4.items[position]
            val context = imgv.context

            val cursor = item.img?.let {
                context.contentResolver.query(
                    it, // 이미지의 Uri를 사용합니다.
                    null,
                    null,
                    null,
                    null
                )
            }

            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                val imagePath = cursor.getString(columnIndex)

                // imagePath를 사용하여 이미지를 로드하고 ImageView에 설정합니다.
                // 이후 cursor를 닫아야 합니다.
                cursor.close()
            }
        }

        init {
            imgv.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    SaveAdapter.lastSelectedPosition = position
                    fetchImageFromGallery(position) // position을 전달해 줍니다.
                }
                notifyDataSetChanged()
            }
        }

        fun setContents(pos: Int) {
            with(viewModel4.items[pos]) {
                fetchImageFromGallery(pos)
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder4 {
        val layoutInflater = LayoutInflater.from(viewGroup.context)
        val view = layoutInflater.inflate(R.layout.recycler_save_view,viewGroup,false)
        val viewHolder = ViewHolder4(view)

        view.setOnClickListener {
            viewModel4.itemClickEvent.value = viewHolder.adapterPosition
        }

        return viewHolder
    }
    override fun getItemCount() = viewModel4.items.size

    override fun onBindViewHolder(viewHolder: SaveAdapter.ViewHolder4, position: Int) {
        viewHolder.setContents(position)
        viewHolder.bind(viewModel4.items[position])
    }




}

