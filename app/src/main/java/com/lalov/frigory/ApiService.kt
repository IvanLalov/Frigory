package com.lalov.frigory

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface ApiService {
    // Buscamos productos por nombre y filtramos por España
    @GET("cgi/search.pl?action=process&json=true&cc=es")
    suspend fun buscarAlimento(@Query("search_terms") nombre: String): RespuestaAPI
}