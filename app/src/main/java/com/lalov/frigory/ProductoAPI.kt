package com.lalov.frigory

import com.google.gson.annotations.SerializedName

// Esta es la respuesta de la búsqueda
data class RespuestaAPI(
    @SerializedName("products") val productos: List<ProductoAPI>
)

// Este es el producto individual
data class ProductoAPI(
    @SerializedName("product_name") val nombre: String?,
    @SerializedName("brands") val marca: String?,
    @SerializedName("image_url") val imagen: String?
)