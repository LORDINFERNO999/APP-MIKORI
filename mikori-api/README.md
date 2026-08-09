# MIKORI API

Backend REST de MIKORI (PHP 8.2+ / Laravel + MySQL 8).

## Stack
- PHP 8.2+, Laravel
- MySQL 8.x
- Autenticación por tokens (Sanctum o JWT access+refresh)
- Firebase Admin SDK (envío de FCM)
- HTTPS obligatorio

## Arquitectura
Controller → Service → Repository → Model (Eloquent). Autorización estricta por `user_id`: un padre nunca accede a datos de otro usuario.

## Módulos
Auth · Users · Children · Devices · Linking · Stats · Limits · (V2) Rules/Schedules · (V3) Commands/Notifications

## Seguridad
Prepared statements, rate limiting, validación (Form Requests), HTTPS forzado, hash de contraseñas (bcrypt/argon2), expiración de códigos de vinculación, logs de eventos.

> Endpoints detallados en [`docs/01-arquitectura.md`](../docs/01-arquitectura.md) §10.

_Pendiente de scaffolding del proyecto Laravel (Fase 1 del plan V1)._
