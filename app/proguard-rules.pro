# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile
-ignorewarnings

-keepattributes Signature
-keepclassmembers class eu.andret.kalendarzswiatnietypowych.entity.UnusualCalendar {
    <init>();
    <fields>;
}

-keepclassmembers class eu.andret.kalendarzswiatnietypowych.entity.HolidayDay {
    <init>();
    <fields>;
}

-keepclassmembers class eu.andret.kalendarzswiatnietypowych.entity.Holiday {
    <init>();
    <fields>;
}

-keepclassmembers class eu.andret.kalendarzswiatnietypowych.entity.FloatingHoliday {
    <init>();
    <fields>;
}

-keep class org.mozilla.javascript.** { *; }

-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}
