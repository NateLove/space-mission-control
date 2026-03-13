# 🚀 Space Mission Control

A Quarkus demo application for managing space missions, crew assignments, and launch sequences — running on OpenShift with secrets securely managed by CyberArk Conjur.

---

## 🌌 What does it do?

**Space Mission Control** lets you:

- Create and manage space **missions** (Moon, Mars, Europa, and beyond)
- Register **astronaut crew members** with roles like Commander, Pilot, Science Officer
- Assign crew to missions and track readiness through a full **status lifecycle**
- Advance missions from `PLANNING` → `CREW_SELECTION` → `TRAINING` → `LAUNCH_READY` → `IN_FLIGHT` → `COMPLETED`
- Abort missions if something goes wrong 💥
- View live health metrics and Prometheus scraping endpoints

---

## 🏗️ Architecture

```
OpenShift Cluster
│
├── Namespace: $NAMESPACE
│   │
│   ├── Deployment: space-mission-control
│   │   ├── Container: space-mission-control (app)
│   │   │     └── connects to localhost:1433  ← no credentials
│   │   └── Container: secretless-broker (sidecar)
│   │         ├── listens on localhost:1433
│   │         ├── authenticates to Conjur via authn-k8s
│   │         ├── fetches DB credentials from Conjur at connect time
│   │         └── proxies to mssql Service with creds injected (TDS layer)
│   │
│   ├── Deployment: mssql                    (existing)
│   ├── Deployment: conjur-demo-conjur-oss   (existing, Helm chart)
│   ├── ConfigMap:  secretless-config        (secretless.yaml — no secrets)
│   ├── ConfigMap:  conjur-config            (Conjur URLs — no secrets)
│   ├── Service + Route (TLS edge termination)
│   └── HorizontalPodAutoscaler (2–5 replicas)
│
│   ✅ No db-credentials K8s Secret exists
│   ✅ No DB_USERNAME / DB_PASSWORD env vars in the pod
│   ✅ Credentials only exist inside the broker process, transiently
```

---

## 🔐 Conjur Integration — Secretless Broker

Credentials are **never present in the pod** — not in env vars, not in K8s Secrets, not in the filesystem.

### How it works

1. The **Secretless Broker** runs as a sidecar container alongside the app
2. The app connects to `localhost:1433` with **no username or password**
3. The broker intercepts the TDS (SQL Server wire protocol) handshake
4. It authenticates to Conjur using the pod's Kubernetes service account (authn-k8s)
5. It fetches credentials from Conjur and injects them into the connection transparently
6. The proxied connection is forwarded to the real `mssql` Service

The app code is completely unaware of credentials. If you `kubectl exec` into the app container and run `env`, there is no `DB_PASSWORD` to find.

### Apply the Conjur policy

```bash
conjur policy load -b root -f openshift/conjur-policy.yaml
```

### Enable authn-k8s in the Helm chart deployment

```bash
oc set env deployment/conjur-demo-conjur-oss \
  CONJUR_AUTHENTICATORS=authn,authn-k8s/$NAMESPACE \
  -n $NAMESPACE
```

### Populate secrets in Conjur

```bash
conjur variable set -i $NAMESPACE/db/host     -v "mssql.$NAMESPACE.svc.cluster.local"
conjur variable set -i $NAMESPACE/db/port     -v "1433"
conjur variable set -i $NAMESPACE/db/name     -v "MissionControl"
conjur variable set -i $NAMESPACE/db/username -v "missionapp"
conjur variable set -i $NAMESPACE/db/password -v "<strong-password>"
```

---

## 🗄️ Database

Microsoft SQL Server (MSSQL) with **Flyway** migrations run automatically on startup.

| Migration | Description               |
|-----------|---------------------------|
| V1        | Create tables + indexes   |
| V2        | Seed demo missions & crew |

---

## 🚀 Running Locally

### Prerequisites
- Java 17+
- Maven 3.9+
- Docker / Podman

### Start MSSQL

```bash
docker-compose up -d mssql
```

### Run in Dev Mode (hot reload)

```bash
./mvnw quarkus:dev
```

- App:        http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui
- Dev UI:     http://localhost:8080/q/dev
- Health:     http://localhost:8080/q/health

---

## 🛸 Deploying to OpenShift

### 1. Create namespace, RBAC & ConfigMaps

```bash
oc apply -f openshift/rbac-and-configmap.yaml
oc apply -f openshift/secretless-config.yaml
```

### 2. Verify the Conjur service URL

The ConfigMap assumes Conjur is reachable at:
`conjur-demo-conjur-oss.$NAMESPACE.svc.cluster.local`

Confirm with:
```bash
oc get svc -n $NAMESPACE | grep conjur
```
If the service name differs, update `CONJUR_APPLIANCE_URL` and `CONJUR_AUTHN_URL` in `rbac-and-configmap.yaml` before applying.

### 3. Build & push image

```bash
oc new-build --name=space-mission-control \
  --binary=true \
  --strategy=docker \
  -n $NAMESPACE

./mvnw package -DskipTests
oc start-build space-mission-control \
  --from-dir=. \
  --follow \
  -n $NAMESPACE
```

### 4. Deploy

```bash
oc apply -f openshift/deployment.yaml
```

### 5. Get the route

```bash
oc get route space-mission-control -n $NAMESPACE
```

---

## 📡 API Reference

| Method | Path                              | Description                      |
|--------|-----------------------------------|----------------------------------|
| GET    | `/api/missions`                   | List missions (filter by status) |
| POST   | `/api/missions`                   | Create a mission                 |
| GET    | `/api/missions/{id}`              | Get mission by ID                |
| PATCH  | `/api/missions/{id}`              | Update mission details           |
| DELETE | `/api/missions/{id}`              | Delete a mission                 |
| POST   | `/api/missions/{id}/advance`      | Advance mission to next stage    |
| POST   | `/api/missions/{id}/abort`        | Abort a mission                  |
| GET    | `/api/missions/{id}/crew`         | List crew on a mission           |
| POST   | `/api/missions/{id}/crew/{crewId}`| Assign crew member               |
| DELETE | `/api/missions/{id}/crew/{crewId}`| Remove crew member               |
| GET    | `/api/crew`                       | List all crew (filter by role)   |
| POST   | `/api/crew`                       | Register a crew member           |
| GET    | `/api/crew/{id}`                  | Get crew member by ID            |
| PATCH  | `/api/crew/{id}`                  | Update crew member               |
| DELETE | `/api/crew/{id}`                  | Delete a crew member             |

---

## 📊 Observability

| Endpoint           | Description             |
|--------------------|-------------------------|
| `/q/health`        | Combined health (UP/DOWN)|
| `/q/health/live`   | Liveness probe           |
| `/q/health/ready`  | Readiness + DB check     |
| `/q/metrics`       | Prometheus metrics       |

---

## 🛠️ Tech Stack

| Component     | Technology                            |
|---------------|---------------------------------------|
| Framework     | Quarkus 3.8 (fast-jar)                |
| Language      | Java 17                               |
| Database      | Microsoft SQL Server 2022             |
| ORM           | Hibernate ORM + Panache               |
| Migrations    | Flyway                                |
| API Docs      | SmallRye OpenAPI / Swagger UI         |
| Health        | MicroProfile Health                   |
| Metrics       | Micrometer + Prometheus               |
| Secrets       | CyberArk Conjur (authn-k8s)           |
| Platform      | OpenShift 4.x                         |
| Image base    | Red Hat UBI 8 + OpenJDK 17            |
