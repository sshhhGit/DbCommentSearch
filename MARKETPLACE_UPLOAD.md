# JetBrains Marketplace 업로드 입력값

업로드 페이지: https://plugins.jetbrains.com/plugin/add  (JetBrains 계정 로그인 → 개발자 약관 동의 필요)

## 1. Upload plugin 폼

| 항목 | 입력값 |
|---|---|
| **Vendor** | 개인 계정(Personal) 선택. 처음이면 vendor 프로필 생성 — 표시 이름 `sshhhdev` |
| **Plugin file** | `build/distributions/DbCommentSearch-1.0.0.zip` |
| **License** | `MIT License` 선택 (또는 소스 저장소의 LICENSE URL) |
| **Tags** | `Database`, `Search`, `Navigation` |
| **Source code URL** | `https://github.com/sshhhGit/DbCommentSearch` |
| **Channel** | `Stable` (기본값) |
| **Hide until approved / visibility** | 공개로 두면 승인 직후 공개됨 |

> 이름·설명·변경 내역·아이콘·호환 버전은 zip 안의 `plugin.xml`에서 자동으로 읽어 오므로 따로 입력하지 않는다.

## 2. zip에서 자동으로 채워지는 값 (확인용)

| 항목 | 값 |
|---|---|
| Plugin ID | `com.sshhhdev.dbcommentsearch` (한 번 올리면 변경 불가) |
| Vendor | `sshhhdev` (url: `https://github.com/sshhhGit/DbCommentSearch`) |
| Name | `DB Comment Search` |
| Version | `1.0.0` |
| Compatibility | build 253+ (2025.3 이상), 상한 없음 |
| Dependencies | `com.intellij.database` → Ultimate / DataGrip 등 유료 IDE 전용으로 표시됨 |
| Icon | `META-INF/pluginIcon.svg`, `pluginIcon_dark.svg` |
| Description / Change notes | `plugin.xml`의 `<description>`, `<change-notes>` (영문 + 한국어) |

## 3. 승인 후 플러그인 페이지에서 추가하면 좋은 것 (선택)

| 항목 | 내용 |
|---|---|
| **Screenshots** | `Shift` 두 번 → DB Comment 탭에서 한글 comment 검색 결과 화면. 회사 테이블명·스키마가 보이지 않는 샘플 DB로 캡처 |
| **Issue tracker** | `https://github.com/sshhhGit/DbCommentSearch/issues` |
| **Website** | `https://github.com/sshhhGit/DbCommentSearch` (plugin.xml vendor url로도 들어가 있음) |
| **Contact (vendor email)** | vendor 프로필에서 입력. `plugin.xml`에는 넣지 않았음 |

## 4. 올리기 전 체크리스트

- [ ] 실제 IntelliJ에 `Install Plugin from Disk`로 설치해 검색·이동 동작 확인
- [ ] `./gradlew verifyPlugin` 결과에 Compatibility problems 없음
- [ ] 스크린샷에 회사 DB 정보가 노출되지 않는지 확인

## 5. 다음 버전부터 (CLI 업로드)

1. Marketplace → 프로필 → **My Tokens**에서 토큰 발급
2. `build.gradle.kts`의 `intellijPlatform { publishing { token = providers.environmentVariable("PUBLISH_TOKEN") } }` 추가
3. `build.gradle.kts`의 `version`과 `plugin.xml`의 `change-notes` 갱신 후 실행:

```
$env:PUBLISH_TOKEN="<토큰>"; ./gradlew publishPlugin
```
