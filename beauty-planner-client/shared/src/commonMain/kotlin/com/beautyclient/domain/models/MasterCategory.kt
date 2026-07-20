package com.beautyclient.domain.models

/**
 * Specialty categories for filtering masters.
 * New categories can be added as the catalog grows.
 */
enum class MasterCategory(val displayKey: String) {
    HAIR("category_hair"),
    NAILS("category_nails"),
    BROWS("category_brows"),
    LASHES("category_lashes"),
    MAKEUP("category_makeup"),
    SKINCARE("category_skincare"),
    MASSAGE("category_massage"),
    OTHER("category_other")
}
