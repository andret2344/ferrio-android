-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Entity classes used by Gson and Room
-keep class eu.andret.kalendarzswiatnietypowych.entity.** { *; }

# Gson TypeAdapter
-keep class eu.andret.kalendarzswiatnietypowych.util.LocalDateTimeAdapter { *; }
