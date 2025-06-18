-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-ignorewarnings

-keep class javax.net.ssl.** { *; }
-keep class java.net.** { *; }
-keep class sun.net.** { *; }

-keepattributes Signature, InnerClasses, EnclosingMethod
-keep class com.google.gson.** { *; }

-keep class eu.andret.kalendarzswiatnietypowych.entity.** { *; }
-keep class eu.andret.kalendarzswiatnietypowych.util.** { *; }

-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.** { *; }

-keep class org.mozilla.javascript.** { *; }
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }
