package com.passportphoto.app.model

/**
 * Defines the physical dimensions and compliance rules for a passport/ID photo.
 * Add more presets as needed (India, UK, Schengen, etc.).
 */
data class PassportSpec(
    val name: String,
    val photoWidthMm: Float,
    val photoHeightMm: Float,
    val headHeightRatio: Float,   // head height as a fraction of total photo height
    val eyeLineRatioFromTop: Float // where the eye-line should sit, fraction from top
) {
    val aspectRatio: Float get() = photoWidthMm / photoHeightMm

    companion object {
        val US_2X2 = PassportSpec(
            name = "US Passport (2x2 in)",
            photoWidthMm = 50.8f,
            photoHeightMm = 50.8f,
            headHeightRatio = 0.69f,
            eyeLineRatioFromTop = 0.56f
        )
        val SCHENGEN_35X45 = PassportSpec(
            name = "Schengen Visa (35x45 mm)",
            photoWidthMm = 35f,
            photoHeightMm = 45f,
            headHeightRatio = 0.75f,
            eyeLineRatioFromTop = 0.50f
        )
        val INDIA_35X45 = PassportSpec(
            name = "India Passport (35x45 mm)",
            photoWidthMm = 35f,
            photoHeightMm = 45f,
            headHeightRatio = 0.70f,
            eyeLineRatioFromTop = 0.55f
        )

        val ALL = listOf(US_2X2, SCHENGEN_35X45, INDIA_35X45)
    }
}
