# Social Wallet

> **"결제는 순식간, 소통은 자연스럽게"**
>
> 금융 거래를 소셜 콘텐츠로 확장하고,
> 금융 데이터를 읽고 즐기는 콘텐츠로 제공하는 소셜 지갑 서비스

<br>

<p align="center">
  <img src="./images/logo.png" width="180px" alt="Social Wallet Logo">
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

## 목차

- [프로젝트 개요](#프로젝트-개요)
- [기획 배경](#기획-배경)
- [팀 구성](#팀-구성)
- [개발 환경 및 기술 스택](#개발-환경-및-기술-스택)
- [주요 기능](#주요-기능)
- [시스템 아키텍처](#시스템-아키텍처)
- [데이터베이스](#데이터베이스)
- [주요 기술 구현](#주요-기술-구현)
- [개발 이슈 및 해결](#개발-이슈-및-해결)
- [서비스 화면](#서비스-화면)
- [시연 영상](#시연-영상)
- [향후 개선](#향후-개선)
- [프로젝트 회고](#프로젝트-회고)

---

## 프로젝트 개요

| 구분 | 내용 |
|---|---|
| **프로젝트명** | Social Wallet |
| **교육 과정** | KB IT's Your Life 7기 |
| **팀** | 30반 2팀 · 5성급 개발자 |
| **개발 기간** | 2026.07 ~ 2026.08 |
| **개발 인원** | 5명 |
| **서비스 유형** | 금융 · 소셜 플랫폼 |

### 한 줄 소개

**Social Wallet은 금융 거래와 소셜 활동을 결합한 전자지갑 서비스입니다.**

송금·결제·정산과 같은 금융 활동을 단순한 거래 내역으로 끝내지 않고
**피드 콘텐츠로 확장하여 친구들과 공유하고 소통할 수 있도록 설계했습니다.**

또한 12개월 소비 데이터를 기반으로 AI 소비 분석을 제공하고,
커스텀 카드·이벤트·리워드 등 다양한 콘텐츠를 통해 사용자의 서비스 체류와 참여를 유도합니다.

---

## 기획 배경

### 기존 금융 서비스의 한계

기존 금융 앱은 일반적으로

```text
금융 활동
   ↓
결제 / 송금 완료
   ↓
서비스 이용 종료
