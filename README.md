# 스카이 캐슬 (Fabric 1.21.11)

## 빌드
1. JDK 21 설치
2. `gradle wrapper --gradle-version 9.2.1` (Gradle이 없으면 Fabric 템플릿의 gradlew, gradle/ 폴더를 복사)
3. `./gradlew build` → `build/libs/skycastle-0.1.0.jar`
4. `./gradlew runClient` 로 바로 테스트

## 사용법
- 크리에이티브 > 전투 탭에서 무기 4종 획득
- 무기를 든 채로 R 키(설정에서 변경 가능) → 소드 스킬 발동

## GitHub으로 자동 빌드 (설치 없이 jar 받기)
1. github.com에서 새 저장소(Repository) 생성
2. 이 폴더의 모든 파일(.github 폴더 포함)을 업로드
3. 저장소의 Actions 탭 > build 완료 후 > Artifacts의 skycastle-mod 다운로드
4. 압축을 풀면 나오는 skycastle-0.1.0.jar를 mods 폴더에 넣기
