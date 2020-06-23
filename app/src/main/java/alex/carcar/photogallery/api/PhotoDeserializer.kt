package alex.carcar.photogallery.api

import alex.carcar.photogallery.GalleryItem
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

class PhotoDeserializer : JsonDeserializer<PhotoResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PhotoResponse {
        val photoResponse = PhotoResponse()
        val galleryItems = mutableListOf<GalleryItem>()
        (json as JsonObject).getAsJsonObject("photos").getAsJsonArray("photo")?.forEach {
            val galleryItem = GalleryItem()
            val jsonObject = it.asJsonObject
            galleryItem.id = jsonObject["id"].toString()
            galleryItem.title = jsonObject["title"].toString()
            galleryItem.url = jsonObject["url_s"].toString()
            galleryItems.add(galleryItem)
        }
        photoResponse.galleryItems = galleryItems
        return photoResponse
    }
}