-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-ignorewarnings

-keep class javax.net.ssl.** { *; }
-keep class java.net.** { *; }
-keep class sun.net.** { *; }

# Gson serialization
-keepattributes Signature, InnerClasses, EnclosingMethod
-keep class com.google.gson.** { *; }

# Entity classes used by Gson and Room
-keep class eu.andret.kalendarzswiatnietypowych.entity.** { *; }

# Gson TypeAdapter
-keep class eu.andret.kalendarzswiatnietypowych.util.LocalDateTimeAdapter { *; }

# Credentials play services
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }
