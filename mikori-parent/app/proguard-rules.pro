# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.mikori.parent.data.remote.dto.** { *; }
-keep,includedescriptorclasses class com.mikori.parent.**$$serializer { *; }
-keepclassmembers class com.mikori.parent.** {
    *** Companion;
}
-keepclasseswithmembers class com.mikori.parent.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit
-keepattributes Signature, Exceptions
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
