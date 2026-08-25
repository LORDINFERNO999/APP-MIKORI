package com.mikori.parent.navigation

/**
 * Rutas de navegación de MIKORI Parent.
 */
object Routes {
    // Auth
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Main
    const val DASHBOARD = "dashboard"
    const val FAMILY = "family"
    const val SETTINGS = "settings"
    const val ADD_CHILD = "add_child"

    // Con parámetro childId
    const val CHILD_DETAIL = "child/{childId}"
    const val STATS = "child/{childId}/stats"
    const val LIMITS = "child/{childId}/limits"
    const val LINKING = "child/{childId}/linking"

    // V2
    const val CONTROL = "child/{childId}/control"
    const val APP_RULES = "child/{childId}/app-rules"
    const val SCHEDULES = "child/{childId}/schedules"

    fun childDetail(id: Long) = "child/$id"
    fun stats(id: Long) = "child/$id/stats"
    fun limits(id: Long) = "child/$id/limits"
    fun linking(id: Long) = "child/$id/linking"
    fun control(id: Long) = "child/$id/control"
    fun appRules(id: Long) = "child/$id/app-rules"
    fun schedules(id: Long) = "child/$id/schedules"
}
