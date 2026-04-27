package com.lalov.frigory

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    // Buscamos productos por nombre, filtramos por España (cc=es)
    // y ordenamos por popularidad (unique_scans_n)
    @GET("cgi/search.pl?action=process&json=true&cc=es&sort_by=unique_scans_n")
    suspend fun buscarAlimento(
        @Query("search_terms") nombre: String
    ): RespuestaAPI
}