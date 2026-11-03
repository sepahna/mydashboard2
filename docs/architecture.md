# Architecture

## Android
- Presentation: Compose screens and UI state.
- Data: Room entities and DAO.
- Domain: planned use-case/repository boundary for expansion.
- Local-first: Room is the source of truth for the initial offline MVP.

## Backend
Django REST Framework exposes owner-scoped CRUD endpoints. JWT authentication is configured globally. Add token obtain/refresh routes before production client integration.

## Sync design (next milestone)
Use stable client UUIDs, updated timestamps, tombstones for deletes, per-record sync state, and explicit conflict resolution. Do not use last-write-wins silently for private notes.
