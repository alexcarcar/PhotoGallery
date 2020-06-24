package alex.carcar.photogallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "PhotoGalleryFragment"
private const val PAGE_SIZE = 25

class PhotoGalleryFragment : Fragment() {
    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var pageBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photoGalleryViewModel = ViewModelProviders.of(this).get(PhotoGalleryViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)
        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        photoRecyclerView.layoutManager = GridLayoutManager(context, 3)
        pageBar = view.findViewById(R.id.pageBar)
        pageBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                photoGalleryViewModel.progress = progress
                photoGalleryViewModel.galleryItemLiveData.removeObservers(viewLifecycleOwner)
                photoRecyclerView.adapter = null
                photoRecyclerView.adapter = PhotoAdapter(
                    photoGalleryViewModel.galleryItemLiveData.value!!,
                    photoGalleryViewModel.progress * PAGE_SIZE
                )
                pageBar.max = photoGalleryViewModel.galleryItemLiveData.value!!.size / PAGE_SIZE - 1
                pageBar.progress = photoGalleryViewModel.progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoGalleryViewModel.galleryItemLiveData.observe(
            viewLifecycleOwner,
            Observer { galleryItems ->
                photoRecyclerView.adapter =
                    PhotoAdapter(galleryItems, photoGalleryViewModel.progress * PAGE_SIZE)
                pageBar.max = galleryItems.size / PAGE_SIZE - 1
                pageBar.progress = photoGalleryViewModel.progress
            }
        )

    }

    private class PhotoHolder(itemTextView: TextView) : RecyclerView.ViewHolder(itemTextView) {
        val bindTitle: (CharSequence) -> Unit = itemTextView::setText
    }

    private class PhotoAdapter(
        private val galleryItems: List<GalleryItem>,
        val offset: Int
    ) :
        RecyclerView.Adapter<PhotoHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val textView = TextView(parent.context)
            return PhotoHolder(textView)
        }

        override fun getItemCount(): Int = galleryItems.size

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            if (offset + position >= galleryItems.size) return
            val galleryItem = galleryItems[offset + position]
            holder.bindTitle(galleryItem.title)
        }

    }

    companion object {
        fun newInstance() = PhotoGalleryFragment()
    }
}