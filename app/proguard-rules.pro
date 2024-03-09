# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile
-ignorewarnings

-keepattributes Signature
-keepclassmembers class eu.andret.kalendarzswiatnietypowych.entity.UnusualCalendar {
 !transient <fields>;
}

-keepclassmembers class eu.andret.kalendarzswiatnietypowych.entity.HolidayDay {
 !transient <fields>;
}

-keepclassmembers class eu.andret.kalendarzswiatnietypowych.entity.Holiday {
 !transient <fields>;
}

-keepclassmembers class eu.andret.kalendarzswiatnietypowych.entity.FloatingHoliday {
 !transient <fields>;
}

-keep class org.mozilla.javascript.** { *; }
