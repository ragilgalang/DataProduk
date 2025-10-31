// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Plugin Android Application (digunakan oleh modul app)
    alias(libs.plugins.android.application) apply false

    // WAJIB: Plugin Google Services (memungkinkan modul app mengimpor Firebase SDK)
    alias(libs.plugins.google.gms.google.services) apply false
}