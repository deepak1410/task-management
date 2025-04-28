## Steps to Run
* Run the following commands below from the identity-service and task-service directories

```
cd identity-service
mvn clean package -DskipTests
cd ../task-service
mvn clean package -DskipTests
cd ..
```

* Run the following commands to execute docker instructions

`docker-compose --env-file .env up --build`

* To access PgAdmin, use URL http://localhost:6070/browser/ with credentials defined in .env file.

### ✅ To **Start** All Containers (Detached Mode)

Run this from project root:

```bash
docker-compose --env-file .env up -d
```

- `-d` = **detached mode**, runs everything in the background
- You don’t need to keep the terminal open

---

### ✅ To **Stop** All Containers

When you're done and want to shut things down:

```bash
docker-compose down
```

This:
- Stops all services
- Removes containers, but **not** volumes or images

---

### ✅ To **Start Again Later**

Just run this again (detached mode preferred):

```bash
docker-compose --env-file .env up -d
```

No need to rebuild unless you changed the code or Dockerfiles. If you **did** make code changes, do:

```bash
docker-compose --env-file .env up -d --build
```

---

### 🧼 Optional: To Clean Everything

If you ever want to remove everything (containers, networks, volumes):

```bash
docker-compose down -v
```

⚠️ This will delete your `postgres_data` volume and all your data.

---

### 🕵️‍♂️ To Check Status of Running Containers

```bash
docker ps
```

And to see logs for a service:

```bash
docker logs identity-service
```

Or stream logs live:

```bash
docker-compose logs -f
```

