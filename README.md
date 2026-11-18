# DB Comment Search

An IntelliJ Platform plugin that adds a **DB Comment** tab to **Search Everywhere** (double `Shift`)
for finding database tables, views and columns by their **comments**.

The built-in *Go to Database Object* matches object names only. When a schema uses short or abbreviated
names and the meaning lives in comments (often written in Korean, Japanese or Chinese), this plugin lets you
search by that meaning instead.

## Features

- Searches comments of tables, views and columns in every data source of the Database tool window
- Works with any language, including non-Latin text such as Korean
- Space-separated words are AND-matched (`change request`)
- Ranked results: exact comment > comment starts with > comment contains > name match
- Selecting a result navigates to the object in the Database tool window

## Usage

1. Press `Shift` twice and switch to the **DB Comment** tab (`Tab`)
2. Type part of a comment
3. Press `Enter` to jump to the table or column

## Requirements

- IntelliJ IDEA Ultimate / DataGrip 2025.3 (build 253) or later, with the bundled **Database Tools and SQL** plugin
- Comments come from the IDE's introspected metadata. If nothing is found, make sure the data source
  introspection includes comments and refresh it (`Ctrl+F5`).

## Build

```
./gradlew buildPlugin     # build/distributions/DbCommentSearch-<version>.zip
./gradlew verifyPlugin    # IntelliJ Plugin Verifier against the IDEs configured in build.gradle.kts
```

`gradle.properties` points `localIdePath` and `org.gradle.java.home` at a locally installed IntelliJ IDEA Ultimate.
Change these paths to match your machine.

## 한국어

Search Everywhere(`Shift` 두 번)에 **DB Comment** 탭을 추가해, 테이블·뷰·컬럼을 **comment(한글 포함)** 로 검색하고
선택하면 Database 도구 창의 해당 객체로 이동합니다. 공백으로 구분한 여러 단어는 모두 포함된 항목만 표시합니다.

## License

[MIT](LICENSE)
