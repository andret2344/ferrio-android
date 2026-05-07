-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Entity classes used by Gson and Room
-keep class eu.andret.kalendarzswiatnietypowych.entity.** { *; }

# Preserve enum constant names so Gson name()/valueOf() round-trips survive R8.
-keepclassmembers enum eu.andret.kalendarzswiatnietypowych.entity.** { *; }

# Gson TypeAdapter
-keep class eu.andret.kalendarzswiatnietypowych.util.LocalDateTimeAdapter { *; }
