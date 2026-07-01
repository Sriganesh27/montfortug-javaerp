# API Reference
*Status: Verified*

## Authentication
* `POST /api/auth/login` (Public): Returns JWT.
* `POST /api/auth/register` (Protected: SUPER_ADMIN)

## School
* `GET /api/branches` (Protected): Lists master data.
* `POST /api/branches` (Protected: SUPER_ADMIN)

## Admission
* `POST /api/public/applications` (Public): Intakes forms.

## SuperAdmin
* `GET /api/superadmin/users` (Protected): User directory.
