# R8 keep rules for the release build (isMinifyEnabled = true).
#
# No custom keep rules are needed: the app has no reflection, no JSON/Gson/Moshi model classes,
# no Retrofit interfaces, no JavascriptInterface, no JNI, and no Room/Hilt. Enum constants are
# read only via `.name` (see DataStoreThemeRepository.themeModeOf), which R8 preserves by
# default without a keep rule because the name is a constructor-time String literal, not an
# obfuscatable identifier. chesslib (:core's only external dependency) is a plain JitPack jar
# with no reflection and ships no consumer rules of its own — checked its class files directly.
