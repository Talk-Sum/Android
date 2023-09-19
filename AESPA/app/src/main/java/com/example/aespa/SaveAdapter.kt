package com.example.aespa

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SaveAdapter(private val viewModel4: SaveViewModel,
                  private val imageSelectedListener: ImageSelectedListener) : RecyclerView.Adapter<SaveAdapter.ViewHolder4>(){
    companion object{
        var lastSelectedPosition = -1
    }
    inner class ViewHolder4(private val view: View) : RecyclerView.ViewHolder(view) {
        //세팅v
        val imgv = view.findViewById<ImageView>(R.id.imageView10)
        val namet = view.findViewById<TextView>(R.id.name)
        val datet = view.findViewById<TextView>(R.id.date)
        val user_namet = view.findViewById<TextView>(R.id.username)
        val previewt = view.findViewById<TextView>(R.id.preview)
        fun bind(data: saveData) {
            namet.text = data.name // data.name 대신 fileName 사용
        }
        private fun fetchImageFromGallery(position: Int) {
            imageSelectedListener.onImageSelected(position)
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
                imgv.setImageURI(this.img)
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

