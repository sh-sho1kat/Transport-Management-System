# Transport Management System — Spring Boot migration

The active Node.js API has been migrated to Spring Boot. Both React frontend source trees are unchanged.

- `backend/`: Java 21 / Spring Boot 3.5.16 application, Maven wrapper, tests and environment example.
- `frontend/admin-app/` and `frontend/user-app/`: original frontend files.
- `backend-node-reference/`: original Node source retained for comparison; not needed to run the migrated app.
- `MIGRATION.md`: setup, API contracts, database notes, original limitations and component mapping.
- `VALIDATION.md`: recorded verification results and reproduction commands.
- `README.original.md`: original project README for reference.

## Quick start

Install a JDK 21 or newer and start MongoDB, or use your existing MongoDB deployment. Set `JAVA_HOME` to the JDK directory, not a Java runtime-only installation.

```sh
cd backend
cp .env.example .env
# Edit MONGO_URL in .env to select your MongoDB database.
./mvnw spring-boot:run
```

On Windows, copy the file using `Copy-Item .env.example .env` and run `./mvnw.cmd spring-boot:run` from PowerShell.

The API listens on **http://localhost:8000**, matching both unchanged frontends. Stop the old Node backend before starting Java on that port.

Start the frontend apps in separate terminals:

```sh
cd frontend/admin-app
npm ci
npm run dev -- --port 5173
```

```sh
cd frontend/user-app
npm ci
npm run dev -- --port 5174
```

Run these frontend commands from the extracted project's root in separate terminals. Bundled `node_modules`, original secrets, and generated build directories are excluded from the delivery.

For a packaged backend:

```sh
cd backend
./mvnw clean verify
java -jar target/transport-backend-1.0.0.jar
```

Read [MIGRATION.md](MIGRATION.md) before pointing the application at an existing database. No schema conversion is needed. Reinitializing a trip's seats clears its bookings, matching the original API.
