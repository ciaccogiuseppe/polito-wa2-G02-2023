# polito-wa2-G02-2023

# Installation

## General

- Import project in IntelliJ IDEA as ```File > New > Project from Version Control...```

## Database

- Install Docker and deploy Postgres
- Run ```docker run --name postgres -v postgres-vol:/var/lib/postgresql/data -p 5432:5432 -e POSTGRES_PASSWORD=password postgres```
- Connect to PostgreSQL in IDEA Database section with username ```postgres``` and password ```password```
- Open a Query Console for the selected Database
- Run ```create database ticketing``` command
- Select ```ticketing``` Database as main Database
- Run query in ```server/main/resources/schema.sql```
- Run query in ```server/main/resources/records.sql```
- Database should now be running correctly