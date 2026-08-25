# Social Wallet

> ### **결제는 순식간, 소통은 자연스럽게**
>
> 금융 거래를 **소셜 콘텐츠로 확장**하고,
> 금융 데이터를 **읽고 즐기는 콘텐츠**로 제공하는 소셜 지갑 서비스

<br>

<p align="center">
  <img src="./images/logo.png" width="300px" alt="Social Wallet Logo">
</p>

<p align="center">
  <b>KB IT's Your Life 7기 · 30반 2팀 · 5성급 개발자</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Vue.js-3-4FC08D?style=for-the-badge&logo=vuedotjs&logoColor=white">
  <img src="https://img.shields.io/badge/Spring-Framework-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">
  <img src="https://img.shields.io/badge/WebSocket-STOMP-010101?style=for-the-badge">
  <img src="https://img.shields.io/badge/OpenAI-GPT--5--nano-412991?style=for-the-badge&logo=openai&logoColor=white">
</p>

<br>

---

# 목차

* [프로젝트 개요](#-프로젝트-개요)
* [기획 배경](#-기획-배경)
* [팀 구성](#-팀-구성)
* [개발 환경 및 기술 스택](#-개발-환경-및-기술-스택)
* [주요 기능](#-주요-기능)
* [시스템 아키텍처](#-시스템-아키텍처)
* [데이터베이스](#-데이터베이스)
* [주요 기술 구현](#-주요-기술-구현)
* [개발 이슈 및 해결](#-개발-이슈-및-해결)
* [서비스 화면](#-서비스-화면)
* [시연 영상](#-시연-영상)
* [향후 개선](#-향후-개선)
* [프로젝트 회고](#-프로젝트- 회고)

---

# 프로젝트 개요

| 구분         | 내용                   |
| ---------- | -------------------- |
| **프로젝트명**  | Social Wallet        |
| **교육 과정**  | KB IT's Your Life 7기 |
| **팀**      | 30반 2팀 · 5성급 개발자     |
| **개발 기간**  | 2026.07 ~ 2026.08    |
| **개발 인원**  | 5명                   |
| **서비스 유형** | 금융 · 소셜 플랫폼          |

### 한 줄 소개

**Social Wallet은 금융 거래와 소셜 활동을 결합한 전자지갑 서비스입니다.**

송금·결제·정산과 같은 금융 활동을 단순한 거래 내역으로 끝내지 않고
**피드 콘텐츠로 확장하여 친구들과 공유하고 소통할 수 있도록 설계했습니다.**

또한 12개월 소비 데이터를 기반으로 AI 소비 분석을 제공하고,
커스텀 카드·이벤트·리워드 등 다양한 콘텐츠를 통해 사용자의 서비스 체류와 참여를 유도합니다.

---

# 기획 배경

### 기존 금융 서비스의 한계

기존 금융 앱은 일반적으로 금융 활동 이후 결제 / 송금 완료와 함께 서비스 이용이 종료되는 구조로, 사용자의 금융 활동이 **거래 완료와 동시에 종료**됩니다.

또한 금융 서비스 특성상 사용자가 서비스를 지속적으로 방문할 수 있는 콘텐츠와 소셜 경험이 부족하다는 점에 주목했습니다.

### Social Wallet의 방향

금융 거래 → 소셜 콘텐츠 → 친구와 공유 → 좋아요 / 댓글 / 소통 → 서비스 재방문

금융 거래를 **소셜 콘텐츠로 확장**하고, 소비 데이터를 **AI 기반 콘텐츠로 제공**하여 사용자의 자연스러운 서비스 이용을 유도하는 것을 목표로 했습니다.

---

# 팀 구성

| 팀원 | 역할 | 주요 담당 |
| :---: | --- | --- |
| **이승진** | 팀장 · 발표 · UI/UX | 전자지갑 · 결제 · 송금 · 더치페이 |
| **박우진** | PM · Git/API 관리 | 소셜 피드 · 커스텀 카드 · 정산 · 알림 |
| **박준우** | DBA · QA | 포인트 지갑 · AI 소비분석 · 상품 추천 · 이벤트 |
| **이효능** | UI/UX · 발표 | 인증 · 회원 · 프로필 · 계좌 연결 |
| **허현정** | UI/UX | 이벤트 서비스 · 커스텀 카드 일부 |

---

# 개발 환경 및 기술 스택

## Frontend
* **Vue 3** (SPA 기반 프론트엔드)
* **Vite** (개발 서버 및 빌드)
* **Pinia** (전역 상태 관리)
* **Vue Router** (페이지 라우팅)
* **Axios** (REST API 통신)

## Backend
* **Java** / **Spring Framework** (REST API 및 서버)
* **Spring Security** (인증 / 인가)
* **MyBatis** (SQL Mapper)
* **JWT** (사용자 인증)
* **Jsoup** (웹 크롤링)

## Database / Infrastructure
* **MySQL** (관계형 데이터베이스)
* **Redis** (좋아요 처리 및 캐싱)
* **Docker** (개발 환경 구성)

## Real-time / AI
* **WebSocket** / **STOMP** / **SockJS** (실시간 통신 및 메시징)
* **OpenAI API** (AI 소비 분석)

## Collaboration
* Git, GitHub, Notion, Discord, IntelliJ IDEA, Visual Studio Code

---

# 주요 기능

## 01. 전자지갑 & 금융 거래
* **계좌 연결 및 관리:** 계좌 인증, 연결/해제, 대표 계좌 설정
* **송금:** 친구 송금, 계좌번호 송금, 간편 비밀번호 인증
* **결제:** 카드 결제, 승인, 취소 및 상태 관리
* **통합 금융 Pipeline:** 송금/결제/정산 완료 시 지갑 잔액 변경 → 소비 내역 생성 → 소셜 피드 생성 → 리워드 지급으로 이어지는 흐름 구축

## 02. Social Feed
금융 거래를 소셜 콘텐츠로 확장하여 송금, 결제, 정산, 이벤트, 카드, 소비 분석 등의 활동을 피드로 공유합니다.
* **공개 범위 설정:** PUBLIC(전체), FRIEND(친구), PRIVATE(본인) 설정에 따라 거래 금액과 민감 정보의 노출 범위를 다르게 처리합니다.

## 03. Redis 기반 좋아요
좋아요 기능에 Redis Set을 적용하여 중복 좋아요를 방지하고 빠른 상태 반영 및 DB 부하를 감소시켰으며, 백그라운드에서 DB와 동기화합니다.

## 04. 실시간 알림
WebSocket + STOMP를 활용하여 좋아요, 댓글, 친구 요청, 정산 요청 등의 이벤트를 사용자별 Queue(`/user/queue/...`)를 통해 실시간으로 전달합니다.

## 05. AI 소비 분석
최근 12개월 소비 데이터를 기반으로 월별/카테고리별 소비 분석 및 AI 칭호를 부여합니다. 가맹점 매핑 테이블(`merchant_category_mapping_tbl`)을 우선 조회하여 GPT API 호출을 최적화했습니다.

## 06. AI 비동기 분석
분석 중 페이지 이동 시 요청이 종료되는 문제를 해결하기 위해 **비동기 분석 요청 + 2초 Polling** 방식을 적용하여 작업과 화면을 분리했습니다.

## 07. 금융 상품 추천
서버 기동 시 Jsoup을 활용해 KB 카드(123건) 및 손해보험(18건) 상품 데이터를 자동 수집하고 사용자에게 추천합니다.

## 08. Custom Card
Canvas 기반 편집 기능과 Pinia 상태 관리를 활용하여 사용자가 단색 배경, 그라데이션, 패턴, 이미지, 텍스트 등을 조합해 나만의 디지털 카드를 제작하고 공유할 수 있습니다.

## 09. 더치페이 정산
균등 및 비율 분할을 지원하는 정산 그룹 생성, 친구 초대, 정산 요청/결제/취소 및 실시간 알림 기능을 제공합니다.

## 10. 이벤트 & 리워드
출석 체크, 랜덤박스, 이벤트 챌린지 등의 포인트를 제공하며, DB의 **UNIQUE KEY 제약 조건**을 활용해 중복 보상 지급을 방지합니다.

---

# 시스템 아키텍처

<p align="center">
  <img src="./images/architecture.png" width="850px" alt="System Architecture">
</p>

---

# 데이터베이스

<p align="center">
  <img src="./images/ERD.png" width="900px" alt="ERD">
</p>

---

# 주요 기술 구현

1. **Redis와 WebSocket의 역할 분리:** 빠른 상태 처리가 필요한 '좋아요'는 Redis를, 실시간 이벤트 전달이 필요한 '알림/정산'은 WebSocket+STOMP로 분리
2. **금융 거래와 부가 기능 분리:** 핵심 송금/결제 거래가 성공한 뒤 피드 생성 및 리워드 처리가 이어지도록 설계하여 거래 안정성 확보
3. **JWT 인증 구조:** Spring Security와 JWT를 활용하고 탈퇴 회원은 익명화(`withdrawn_{userId}_{timestamp}`)하여 제약 충돌 방지

---

# 개발 이슈 및 해결

* **AI 분석 중 페이지 이동 문제:** 비동기 요청과 2초 Polling 방식을 도입하여 화면 이동과 무관하게 분석이 완료되도록 개선
* **GPT 반복 호출 문제:** 가맹점-카테고리 매핑 테이블을 구축하여 기존 분류 결과를 우선 조회함으로써 API 호출 횟수 및 시간 단축
* **Redis READONLY 문제:** Docker Redis Replica 설정을 해제(`REPLICAOF NO ONE`)하여 정상적인 Master 상태로 전환
* **비정형 HTML 크롤링 문제:** 페이지 구조별 5개의 크롤링 규칙을 추가하여 다양한 금융 상품 데이터를 안정적으로 수집

---

# 서비스 화면

| 회원가입 | 소셜 피드 | 송금 & 정산 |
| :---: | :---: | :---: |
| <img src="./images/signup.png" width="220px"> | <img src="./images/feed.png" width="220px"> | <img src="./images/settlement.png" width="220px"> |

| 소비 분석 | 커스텀 카드 |
| :---: | :---: |
| <img src="./images/analysis.png" width="220px"> | <img src="./images/custom-card.png" width="220px"> |

---

# 시연 영상

<p align="center">
  <a href="https://youtu.be/7DNlIi7Jd-M" target="_blank">
    <img src="https://img.youtube.com/vi/7DNlIi7Jd-M/maxresdefault.jpg"
         width="600px"
         alt="Social Wallet 시연 영상">
  </a>
</p>

<p align="center">
  <b>Social Wallet Demo</b>
</p>

---

# 협업 방식

* **Git:** 기능 단위 Branch 작업, 커밋 메시지 규칙 준수, 수시 병합 및 충돌 즉시 해결
* **Notion:** 일일 업무일지, 진행 상황 공유, 오류 및 해결 과정 기록, API 및 개발 문서 관리
* **Communication:** Discord 실시간 소통

---

# 실행 방법

### 1. Repository Clone
```bash
git clone [https://github.com/baguni93/KBProject.git](https://github.com/baguni93/KBProject.git)
