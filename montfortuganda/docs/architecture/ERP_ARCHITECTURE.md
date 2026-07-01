# ERP Architecture

*Status: Verified*

## Overall Architecture
Monolithic Spring Boot backend with a REST API and server-rendered Vanilla JS frontend.
* Layer Responsibilities: Controllers (API routing), Services (Business logic/Transactions), Repositories (Data access).

## Dependency Flow
Frontend -> Controller -> Service -> Repository -> MySQL

## Request Lifecycle
HTTP Request -> JwtAuthenticationFilter -> Controller -> Service -> JPA -> DB.
