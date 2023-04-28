# polito-wa2-G02-2023

# Installation

## General

- Import project in IntelliJ IDEA as ```File > New > Project from Version Control...```

## Database

- Install Docker and deploy Postgres
- Run ```docker run --name postgres -v postgres-vol:/var/lib/postgresql/data -p 5432:5432 -e POSTGRES_PASSWORD=<password> postgres```
- Connect to PostgreSQL in IDEA Database section with username ```postgres``` and password ```<password>```
- Open a Query Console for the selected Database
- Run ```create database ticketing``` command
- Select ```ticketing``` Database as main Database
- Run query in ```server/main/resources/schema.sql```
- Run query in ```server/main/resources/records.sql```
- Database should now be running correctly

## Client

To run the frontend (requires database and Spring Boot running for the APIs to work):

- `cd client`
- `npm install`
- `npm run start`

To package the React application:

- `cd client`
- `npm install`
- `npm run build`
- copy the content of the generated `client/build` folder in `server/src/main/resources/static`
- the client app can now be loaded opening `localhost:8080` and it can properly interact with the API server

## API Reference

- METHOD `GET` URL: `/API/tickets/{ticket_id}`

  - Description: Get ticket, if existing, with id corresponding to parameter `ticket_id`
  - Permissions allowed:
  - Request query parameter: `ticket_id` to retrieve the corresponding ticket
  - Request body: _None_
  - Response: `200 OK` (success)
  - Error responses: `404 ...` (...), `422 ...` (...), `400 ...` (...), `500 ...` (...)
  - Response body: ticket corresponding to ticket it
  ```
  {(Insert JSON)}
  ```

- METHOD `GET` URL: `/API/tickets/{ticket_id}`

  - Description: Get ticket, if existing, with id corresponding to parameter `ticket_id`
  - Permissions allowed:
  - Request query parameter: `ticket_id` to retrieve the corresponding ticket
  - Request body: _None_

    ```
    {}
    ```

  - Response: `200 OK` (success)
  - Error responses: `404 ...` (...), `422 ...` (...), `400 ...` (...), `500 ...` (...)
  - Response body: ticket corresponding to ticket it
  ```
  {(Insert JSON)}
  ```