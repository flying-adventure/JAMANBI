# JAMANBI (자만비)

자격증 준비생을 위한 통합 Android 앱 — 자격증 검색부터 커뮤니티 활동, 합격 운세까지 한 곳에서.

> 25-1 모바일프로그래밍 팀 프로젝트

## 팀원

| 학번 | 이름 | 이메일 |
|------|------|--------|
| 202235070 | 안수빈 | soobin6870@gachon.ac.kr |
| 202332707 | 유준혁 | somethinaws@gmail.com |
| 202135722 | 김명수 | meong0325@gachon.ac.kr |
| 202135701 | 강동호 | ind07316@naver.com |

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Kotlin |
| Platform | Android (minSdk 24 / targetSdk 35) |
| Auth & DB | Firebase Authentication, Firebase Firestore |
| Storage | Firebase Storage |
| Network | Retrofit2, OkHttp3 |
| Image | Glide |
| UI | Android Views (XML) + Material 3, NavigationDrawer, BottomNavigation |
| Calendar | kizitonwose/calendar-view |
| Open API | Q-Net 국가자격증 API, Naver Blog API |

---

## 주요 기능

### 인증
- Firebase 이메일/비밀번호 로그인
- 3단계 Fragment 기반 회원가입 (이름·생년월일·성별 → 관심 분야 → 완료)
- 회원정보 수정 / 회원탈퇴

### 자격증 검색
- Q-Net 국가자격증 API 연동 (전체 자격증 목록 조회)
- 분야별 필터(Spinner) + 이름 키워드 검색
- 관심 분야 자동 선택
- 자격증 저장/관리 (Firestore `savedCerts`, `acquiredCerts` 컬렉션)

### 커뮤니티 게시판
- Firestore 기반 게시글 CRUD
- 최신순 / 좋아요순 정렬
- 내 게시글 보기 (NavigationDrawer)
- 게시글 상세 조회 및 좋아요

### 블로그 연동
- 자격증 이름으로 Naver Blog API 검색
- 관련 블로그 포스트 목록 표시

### 합격 운세
- 저장한 자격증을 선택하면 합격 예측 메시지 랜덤 출력

### 프로필
- Firebase Storage 프로필 이미지 업로드 및 Glide 로딩
- 이름, 생년월일, 성별, 관심 분야 표시 및 수정

---

## 설치 및 실행

### 사전 요구사항
- Android Studio Hedgehog 이상
- JDK 11
- Firebase 프로젝트 및 `google-services.json`

### 실행 방법

```bash
# 1. 저장소 클론
git clone https://github.com/flying-adventure/JAMANBI.git

# 2. jamanbi/ 디렉토리를 Android Studio로 열기

# 3. google-services.json을 jamanbi/app/ 에 배치
#    (Firebase Console에서 다운로드)

# 4. Run 'app' (Shift+F10)
```

> Firebase Console에서 Authentication (이메일/비밀번호), Firestore Database, Storage를 활성화해야 합니다.

---

## 디렉토리 구조

```
jamanbi/
└── app/src/main/
    ├── java/com/example/jamanbi/
    │   ├── MainActivity.kt          # 로그인 여부에 따라 화면 분기
    │   ├── SplashActivity.kt
    │   ├── LoginActivity.kt
    │   ├── SignUp/                  # 3단계 회원가입 (Fragment)
    │   ├── ProfileActivity.kt
    │   ├── EditProfileActivity.kt
    │   ├── SearchCertActivity.kt    # 자격증 검색 (Q-Net API)
    │   ├── SavedCertListActivity.kt
    │   ├── AcquiredCertActivity.kt
    │   ├── PostListActivity.kt      # 커뮤니티 게시판
    │   ├── PostViewActivity.kt
    │   ├── PostWriteActivity.kt
    │   ├── BlogPostListActivity.kt  # Naver 블로그 검색 결과
    │   ├── FortuneActivity.kt       # 합격 운세
    │   ├── naverapi/                # Naver Blog API Retrofit 클라이언트
    │   ├── repository/              # 관심 분야 데이터
    │   └── viewmodel/
    └── res/
        ├── layout/                  # XML 레이아웃
        ├── drawable/                # 이미지 및 커스텀 배경
        └── values/                  # 색상, 문자열, 테마
```

---

## 사용 Open API

- [Q-Net 국가자격증 목록 API](https://www.data.go.kr/data/15003024/openapi.do)
- [Q-Net 자격증 상세 API](https://www.data.go.kr/data/15003029/openapi.do)
- Naver Blog Search API
