-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-ignorewarnings

# Reguły URLConnection (SSL)
-keep class javax.net.ssl.** { *; }
-keep class java.net.** { *; }
-keep class sun.net.** { *; }

# Gson
-keepattributes Signature, InnerClasses, EnclosingMethod
-keep class com.google.gson.** { *; }

# Twoje klasy modelowe encji używane przez Gson
-keep class eu.andret.kalendarzswiatnietypowych.entity.** { *; }

# Inne Twoje klasy użytkowe (już obecne w konfiguracji)
-keep class eu.andret.kalendarzswiatnietypowych.util.** { *; }

# Firebase i PlayServices (już obecne w konfiguracji)
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.** { *; }

# Pozostałe obecne reguły
-keep class org.mozilla.javascript.** { *; }
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }
