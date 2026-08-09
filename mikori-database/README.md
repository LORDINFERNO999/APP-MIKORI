# MIKORI Database

Esquema y documentación de la base de datos (MySQL 8).

## Archivos
- [`schema.sql`](./schema.sql) — DDL completo (tablas V1 + tablas preparadas para V2/V3).
- [`erd.md`](./erd.md) — diagrama entidad-relación (Mermaid).

## Uso rápido (local)
```bash
mysql -u root -p -e "CREATE DATABASE mikori CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p mikori < schema.sql
```

> En producción, el esquema se gestionará mediante **migraciones de Laravel** dentro de `mikori-api/`. Este `schema.sql` es la referencia canónica del diseño.

## Principios de diseño
- Normalizado (3NF), InnoDB, utf8mb4.
- Aislamiento por dueño: las FKs cuelgan de `users`/`children` con `ON DELETE CASCADE`.
- Índices en las consultas calientes (`app_usage(device_id, usage_date)`).
