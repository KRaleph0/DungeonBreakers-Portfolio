# Dungeon Breakers

libGDX(Java)로 만든 2D 로그라이크 던전 액션 게임입니다.
분기형 맵에서 경로를 선택하며 전투·상점·보상·휴식 노드를 거쳐 보스를 공략합니다.

> ⚠️ 서드파티 에셋은 라이선스 문제로 저장소에 포함되어 있지 않습니다.
> **플레이는 [Releases](../../releases)에서 jar를 받아 실행**하세요. ([CREDITS](CREDITS.md))

## 실행

Java 17 이상 필요

```bash
java -jar DungeonBreakers-1.0.0.jar
```

## 주요 기능

- **분기형 던전 맵** — 전투 / 상점 / 상자 / 휴식 / 보스 노드, 노드 잠금·해금 상태 관리 (`MapScreen`, `DungeonMapManager`, `MapNode`)
- **실시간 전투** — Box2D 물리 기반 충돌 처리, 투사체, 데미지 텍스트 (`GameScreen`, `GameContactListener`, `Projectile`)
- **캐릭터 & 전직** — 직업별 플레이어 클래스, NPC를 통한 전직·캐릭터 해금 (`AbstractPlayer`, `Archer`, `Knight`, `JobChangeUI`, `UnlockUI`)
- **적 & 보스 AI** — 공통 추상 클래스 기반 몬스터·보스 패턴 (`AbstractEnemy`, `ArmoredSkeleton`, `SkeletonArcher`, `Boss`)
- **아이템 & 인벤토리** — 장비 능력치 적용, 드랍 아이템, 상점, 퀵슬롯 (`ItemManager`, `InventoryUI`, `ShopUI`)
- **Tiled 맵** — `.tmx` 맵 로딩 및 스폰 트리거 (`GameMapManager`, `SpawnTrigger`)
- **세이브 슬롯** — `Preferences` 기반 저장/불러오기 (`SaveManager`)

## 기술 스택

- Java, libGDX 1.13.1 (LWJGL3 backend)
- Box2D, FreeType, Tiled Map
- Gradle 8.14

## 프로젝트 구조

```
core/     게임 로직 (플랫폼 공통)
lwjgl3/   데스크톱 런처
assets/   게임 리소스 (저장소 미포함)
```

## 소스에서 빌드

에셋이 없으면 빌드는 되지만 실행 시 리소스를 찾지 못합니다.

```bash
./gradlew lwjgl3:jar     # lwjgl3/build/libs/ 에 jar 생성
```
