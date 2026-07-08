# SRM Credit Engine Frontend

Scaffold Angular 22 para a interface operacional do SRM Credit Engine.

## Estrutura

O app segue a organizacao definida em `docs/specs/06-architecture.md` e `docs/adr/ADR-005-frontend-stack.md`:

```text
src/app/
|-- core/
|-- shared/
|-- features/
`-- models/
```

## Comandos

Desenvolvimento local:

```bash
npm install
npm start
```

Build:

```bash
npm run build
```

Testes:

```bash
npm test -- --watch=false
```

## Runtime config

O frontend carrega `ANGULAR_API_BASE_URL` via `assets/config/runtime-config.json`.

Valor padrao local:

```text
http://localhost:8080/api
```

No container, o arquivo final e gerado em runtime pelo entrypoint do Nginx.
