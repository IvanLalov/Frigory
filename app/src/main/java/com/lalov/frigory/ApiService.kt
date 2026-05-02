package com.lalov.frigory

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    // Búsqueda de productos filtrada por región (España) y orden de popularidad
    @GET("cgi/search.pl?action=process&json=true&cc=es&sort_by=unique_scans_n")
    suspend fun buscarAlimento(
        @Query("search_terms") nombre: String
    ): RespuestaAPI
}