package com.passportphoto.app.model

enum class LayoutOption(val label: String, val photoCount: Int) {
    FULL_12("12 Photos (Full Sheet)", 12),
    HALF_6("6 Photos (Half Sheet)", 6)
}

enum class PhotoOrientation {
    PORTRAIT, LANDSCAPE
}

enum class BackgroundChoice(val label: String, val colorArgb: Int) {
    WHITE("White", 0xFFFFFFFF.toInt()),
    LIGHT_BLUE("Light Blue", 0xFFBFDCEF.toInt())
}
