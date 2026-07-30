# SecureBank — 3-Tier Banking App (No Spring, No Database)

**Trainer:** Rushi | **Course:** DevOps with Multi-Cloud Training
**Stack:** Java 17 · Jakarta Servlets 6 · JSP + JSTL · Apache Tomcat 10.1
**Database:** none — all data lives in JVM memory

This is the framework-free, database-free build of SecureBank. Same URLs, same
screens, same features as the Spring Boot version — but nothing to install and
nothing to configure before it runs.

---

## What changed from the Spring Boot version

| Spring Boot build | This build |
| --- | --- |
| `@SpringBootApplication` | plain `@WebServlet` classes |
| `@Controller` + `@GetMapping` | `doGet()` / `doPost()` |
| Spring Security filter chain | `AuthFilter` (one servlet filter) |
| `BCryptPasswordEncoder` | `PasswordUtil` — PBKDF2-HMAC-SHA256 from the JDK |
| Spring Data JPA repositories | `InMemoryBankStore` (ConcurrentHashMap) |
| MySQL 8 + `application.properties` | *deleted — nothing to configure* |
| Thymeleaf templates | JSP + JSTL (identical CSS and layout) |
| ~50 MB WAR | ~250 KB WAR |

**No `application.properties`. No `schema.sql`. No JDBC driver. No MySQL.**

---

## Quick start

```bash
mvn clean package
```

Produces `target/securebank.war`. Deploy it:

```bash
# Local Tomcat 10.1
cp target/securebank.war $CATALINA_HOME/webapps/ROOT.war
$CATALINA_HOME/bin/startup.sh
```

Open <http://localhost:8080/> → you land on the login page. Click
**Open New Account**, register, and you are in.

> Tomcat **10.1 or newer is required**. Tomcat 9 and below use the old
> `javax.servlet` namespace and will not run this WAR.

---

## Docker

```bash
mvn clean package
docker build -t securebank:1.0.0 .
docker run -p 8080:8080 securebank:1.0.0
```

## Kubernetes

```bash
kubectl apply -f k8s/deployment.yml
kubectl apply -f k8s/service.yml
kubectl get svc securebank-service
```

`/health` returns `200 OK` and is wired up as the readiness and liveness probe.

---

## Routes

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/login` | login page |
| POST | `/login` | authenticate, start session |
| GET | `/register` | account opening form |
| POST | `/register` | create customer |
| GET | `/dashboard` | balance + deposit / withdraw / transfer |
| POST | `/deposit` | credit funds |
| POST | `/withdraw` | debit funds |
| POST | `/transfer` | send money to another customer |
| GET | `/transactions` | full activity log |
| GET | `/logout` | end session |
| GET | `/health` | probe endpoint |

Everything except `/login`, `/register` and `/health` requires a session.

---

## Project layout

```
src/main/java/com/rushi/securebank/
├── model/     Customer, TransactionHistory        (data objects)
├── store/     InMemoryBankStore                   (DATA TIER)
├── service/   CustomerService, BankingException   (BUSINESS TIER)
├── web/       *Servlet                            (PRESENTATION TIER)
├── filter/    AuthFilter                          (authentication gate)
└── util/      PasswordUtil                        (PBKDF2 hashing)

src/main/webapp/
├── index.jsp                  redirects to /login
└── WEB-INF/
    ├── web.xml                session config, welcome file
    └── views/                 login, register, dashboard, transactions
```

---

## Read this before you demo

**Data disappears on restart.** Every account, balance and transaction lives in
JVM memory. Restarting Tomcat, rebuilding the image, or letting Kubernetes
reschedule the pod wipes everything. That is the tradeoff for having no
database — it is expected behaviour, not a bug.

**Run exactly one instance.** Two pods means two separate sets of customers, and
a user who registers on one cannot log in on the other. `k8s/deployment.yml` is
set to `replicas: 1` for this reason.

**The data tier is now in-process.** The three logical tiers are still there
(`web` → `service` → `store`), but presentation, business and data all run
inside one JVM. If your assignment requires a physically separate data tier,
this build does not demonstrate that — the Spring Boot + MySQL version does.

**Not production code.** No CSRF tokens, no rate limiting, no account lockout,
no audit trail, no HTTPS enforcement. It is a teaching demo.

---

## Tests

```bash
mvn test
```

Ten JUnit 5 tests cover registration, password hashing, duplicate usernames,
authentication, deposit, overdraft rejection, invalid amounts, transfers,
transfer validation and history ordering. JaCoCo writes coverage to
`target/site/jacoco/`.
