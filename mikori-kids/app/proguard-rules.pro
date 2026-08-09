-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.mikori.kids.data.remote.dto.** { *; }
-keep,includedescriptorclasses class com.mikori.kids.**$$serializer { *; }
-keepclassmembers class com.mikori.kids.** {
    *** Companion;
}
-keepclasseswithmembers class com.mikori.kids.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepattributes Signature, Exceptions
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
