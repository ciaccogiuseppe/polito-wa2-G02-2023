# polito-wa2-G02-2023

# Installation

## General

- Import project in IntelliJ IDEA as ```File > New > Project from Version Control...```

## Docker JIB deploy
- If running Postgres and Server on the same host edit ```application.properties``` file, changing ```spring.datasource.url=jdbc:postgresql://localhost:5432/ticketing``` to ```spring.datasource.url=jdbc:postgresql://host.docker.internal:5432/ticketing```
- Run gradle task ```jibDockerBuild``` to deploy locally
- A working build can be found on [DockerHub](https://hub.docker.com/repository/docker/ciaccogiuseppe/polito_wa2_g02_2023/general)

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
### Profiles
- **METHOD** `GET` **URL**: `/API/profiles/{email}`

  - **Description**: Get profile, if existing, with email corresponding to `email`
  - **Permissions allowed**:
  - **Request query parameter**: `email` to retrieve the corresponding profile
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `404 Not Found` (profile with email `email` not existing)
    - `422 Unprocessable Entity` (wrong format for `email`)
    - `500 Internal Server Error`
  - **Response body**: profile corresponding to `email` / Error message in case of error
  ```
  {
    "id": <id>, //not returned
    "email": <email>,
    "name": <name>,
    "surname": <surname>
  }
  ```
| Field     | Content                   |
|-----------|---------------------------|
| `id`      | profile id //not returned |
| `name`    | profile name              |
| `surname` | profile surname           |
| `email`   | profile email             |


- **METHOD** `POST` **URL**: `/API/profiles`

  - **Description**: Create new profile, given its properties
  - **Permissions allowed**:
  - **Request query parameter**: _None_
  - **Request body**: Profile to be created

    ```
    {
      "email": <email>,
      "name": <name>,
      "surname": <surname>,
    }
    ```
    - **Response**: `201 Created` (success)
    - **Error responses**:
      - `400 Bad Request`
      - `422 Unprocessable Entity` (wrong format for request body, email already used)
      - `500 Internal Server Error`
    - **Response body**: _None_ / Error message in case of errors


| Field     | Content         |
|-----------|-----------------|
| `name`    | profile name    |
| `surname` | profile surname |
| `email`   | profile email   |


- **METHOD** `PUT` **URL**: `/API/profiles/{email}`

  - **Description**: Change data of given profile
  - **Permissions allowed**:
  - **Request query parameter**:`email` to retrieve the corresponding profile
  - **Request body**: Update information of profile

    ```
    {
      "email": <email>
      "name": <name>,
      "surname": <surname>
    }
    ```

  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `404 Not Found` (user associated with `email` not existing)
    - `422 Unprocessable Entity`  (wrong format for request body)
    - `500 Internal Server Error`
  - **Response body**: _None_ / Error message in case of error

| Field     | Content             |
|-----------|---------------------|
| `name`    | new profile name    |
| `surname` | new profile surname |
| `email`   | new profile email   |

### Products
- **METHOD** `GET` **URL**: `/API/products/`

  - **Description**: Get all products in the DB
  - **Permissions allowed**:
  - **Request query parameter**: _None_
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `500 Internal Server Error`
  - **Response body**: list of all products / Error message in case of error
  ```
  {
    [
      {
        "productId": <id>,
        "name": <name>,
        "brand": <brand>
      }, {...}, ...
    ]
  }
  ```
| Field       | Content              |
|-------------|----------------------|
| `productId` | product id           |
| `name`      | product name         |
| `brand`     | brand of the product |

- **METHOD** `GET` **URL**: `/API/products/{productId}`

  - **Description**: Get details of product with id `{productId}`
  - **Permissions allowed**:
  - **Request query parameter**: `{productId}` to get corresponding product
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `404 Not Found` (product with id `productId` not existing)
    - `422 Unprocessable Entity` (wrong format for `productId`)
    - `500 Internal Server Error`
  - **Response body**: product details / Error message in case of error
  ```
  {
    "productId": <id>,
    "name": <name>,
    "brand": <brand>
  }
  ```
| Field       | Content              |
|-------------|----------------------|
| `productId` | product id           |
| `name`      | product name         |
| `brand`     | brand of the product |

### Ticketing
- **METHOD** `GET` **URL**: `/API/ticketing/{ticketId}`

  - **Description**: Get ticket, if existing, with id corresponding to parameter `ticketId`
  - **Permissions allowed**:
  - **Request query parameter**: `ticketId` to retrieve the corresponding ticket
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `404 Not Found` (profile with email `email` not existing)
    - `422 Unprocessable Entity` (wrong format for `email`)
    - `500 Internal Server Error`
  - **Response body**: ticket corresponding to ticketId / Error message in case of error
  ```
  {
    "ticketId": <ticketId>,
    "title": <title>,
    "description": <description>,
    "priority": <priority>,   
    "productId": <productId>, 
    "customerId": <customerId>,  
    "expertId": <expertId>, 
    "status": <status>, 
    "createdDate": <createdDate> 
  }
  ```
| Field         | Content                         |
|---------------|---------------------------------|
| `ticketId`    | ticket id                       |
| `title`       | title of the ticket             |
| `description` | description of the ticket       |
| `priority`    | priority of the ticket          |
| `productId`   | id of the product of the ticket |
| `customerId`  | customer who created the ticket |
| `expertId`    | expert assigned to the ticket   |
| `status`      | status of the ticket            |
| `createdDate` | timestamp of ticket creation    |

- **METHOD** `GET` **URL**: `/API/ticketing/history/{ticketId}`

  - **Description**: Get ticket history about ticket with id corresponding to parameter `ticketId`, if existing
  - **Permissions allowed**:
  - **Request query parameter**: `ticketId` to retrieve the corresponding ticket
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `404 Not Found` (ticket with id `ticketId` not existing)
    - `422 Unprocessable Entity` (wrong format for `ticketId`)
    - `500 Internal Server Error`
  - **Response body**: array of ticket history events, regarding ticket corresponding to ticketId / Error message in case of error
  ```
  {
    [
      {
        "historyId": <historyId>,
        "ticketId": <ticketId>,
        "oldState": <oldState>,
        "newState": <newState>,
        "userId": <userId>,
        "expertId": <expertId>,
        "timestamp": <timestamp>
      }
      ,
      {...}
    ]
  }
  ```

| Field       | Content                   |
|-------------|---------------------------|
| `historyId` | history event id          |
| `ticketId`  | ticket id of the event    |
| `oldState`  | old state of ticket       |
| `newState`  | new state of ticket       |
| `userId`    | user who updated ticket   |
| `expertId`  | expert assigned to ticket |
| `timestamp` | timestamp of update       |


- **METHOD** `GET` **URL**: `/API/ticketing/filter`

  - **Description**: Get all tickets satisfying the given filters
  - **Permissions allowed**:
  - **Request query parameter**: _None_
  - **Request body**: Required filters (only needed ones to be specified)
  ```
  {
    "customerId": <customerId>,
    "minPriority": <minPriority>,
    "maxPriority": <maxPriority>,
    "productId": <productId>,
    "createdAfter": <createdAfter>,
    "createdBefore": <createdBefore>,
    "expertId": <expertId>,
    "status": [<status1>, <status2>, <status3>...]    
  }
  ```
 - 
 - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `404 Not Found` (ticket with id `ticketId` not existing)
    - `422 Unprocessable Entity` (wrong format for `ticketId`)
    - `500 Internal Server Error`
  - **Response body**: list of tickets satisfying the given filtering conditions / Error message in case of error
  ```
  {
    [
      <ticket_1>
      ,
      <ticket_2>
      ,
      ...
    ]
  }
  ```
| Field           | Content                                                |
|-----------------|--------------------------------------------------------|
| `customerId`    | get tickets created by customer with `id = customerId` |
| `minPriority`   | minimum priority of required tickets                   |
| `maxPriority`   | maximum priority of required tickets                   |
| `productId`     | get tickets regarding product `productId`              |
| `createdAfter`  | get tickets with `createdTime >= createdAfter`         |
| `createdBefore` | get tickets with `createdTime <= createdBefore`        |
| `expertId`      | get tickets assigned to expert `expertId`              |
| `status`        | get tickets with status in `status` list               |

- **METHOD** `POST` **URL**: `/API/ticketing/`

  - **Description**: Create new ticket, given its properties
  - **Permissions allowed**:
  - **Request query parameter**: _None_
  - **Request body**: Ticket to be inserted

    ```
    {
      "title": <title>,
      "description": <description>,
      "productId": <productId>,
    }
    ```
    - **Response**: `201 Created` (success)
    - **Error responses**:
      - `400 Bad Request`
      - `404 Not Found` (product with id `productId` not existing)
      - `422 Unprocessable Entity` (wrong format for request body)
      - `500 Internal Server Error`
    - **Response body**: id assigned to the created ticket / Error message in case of errors
    ```
      {
        "ticketId": <ticketId>
      }
    ```
    - **Note**: `userId` is obtained from the session

| Field          | Content                                          |
|----------------|--------------------------------------------------|
| `title`        | ticket title (textual field)                     |
| `description`  | ticket description (textual field)               |
| `productId`    | product id of the product linked to the ticket   |



    
- **METHOD** `PUT` **URL**: `/API/ticketing/assign`

  - **Description**: Assign ticket to expert
  - **Permissions allowed**:
  - **Request query parameter**: _None_
  - **Request body**: Assignment information of ticket

    ```
    {
      "ticketId": <ticketId>,
      "expertId": <expertId>,
      "priority": <priority>
    }
    ```

  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `404 Not Found` (non-existing fields in request body)
    - `422 Unprocessable Entity`  (wrong format for request body)
    - `500 Internal Server Error`
  - **Response body**: _None_ / Error message in case of error
  - **Note**: `userId` is obtained from the session

| Field        | Content                                |
|--------------|----------------------------------------|
| `ticketId`   | ticket to assign                       |
| `expertId`   | expert to which the ticket is assigned |
| `priority`   | priority assigned to the ticket        |


- **METHOD** `PUT` **URL**: `/API/ticketing/update`

  - **Description**: Change state of given ticket
  - **Permissions allowed**:
  - **Request query parameter**: _None_
  - **Request body**: Update information of ticket

    ```
    {
      "ticketId": <ticketId>,
      "newState": <newState>
    }
    ```

  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `404 Not Found` (non-existing fields in request body)
    - `422 Unprocessable Entity`  (wrong format for request body)
    - `500 Internal Server Error`
  - **Response body**: _None_ / Error message in case of error
  - **Note**: `userId` is obtained from the session

| Field      | Content                      |
|------------|------------------------------|
| `ticketId` | ticket to update             |
| `newState` | new state assigned to ticket |


### Chat
- **METHOD** `GET` **URL**: `/API/chat/{ticketId}`

  - **Description**: Get messages of chat linked to `ticketId`
  - **Permissions allowed**:
  - **Request query parameter**: `ticketId` to retrieve the corresponding ticket
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `404 Not Found` (ticket with id `ticketId` not existing)
    - `422 Unprocessable Entity` (wrong format for `ticketId`)
    - `500 Internal Server Error`
  - **Response body**: list of messages of chat corresponding to ticketId / Error message in case of error
  ```
  {
    [
      {
        "messageId": <messageId>,
        "ticketId": <ticketId>,
        "senderId": <senderId>,
        "text": <text>,
        "timestamp": <timestamp>,
        "attachments":[<attachment1>, <attachment2>...]
      },
      {...},
      ...
    ]
  }
  ```
| Field          | Content                                  |
|----------------|------------------------------------------|
| `messageId`    | id of the chat message                   |
| `ticketId`     | id of the ticket                         |
| `senderId`     | id of the sender user                    |
| `text`         | textual content of the message           |
| `timestamp`    | timestamp of the message                 |
| `attachments`  | IDs of attachments linked to the message |


- **METHOD** `POST` **URL**: `/API/chat/{ticketId}`

  - **Description**: Add message to chat linked to `ticketId`
  - **Permissions allowed**:
  - **Request query parameter**: `ticketId` to retrieve the corresponding ticket
  - **Request body**: Message to be added to chat
  ```
  {
    "text": <text>,
    "attachments": [<attachment1>, <attachment2>,...]
  }
  ```
  - **Response**: `201 CREATED` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `404 Not Found` (ticket with id `ticketId` not existing)
    - `422 Unprocessable Entity` (wrong format for `ticketId` or request body)
    - `500 Internal Server Error`
  - **Response body**: id of the added message / Error message in case of error
  - **Note**: `senderId` is obtained from the session
  ```
  {
    "messageId": <messageId>
  }
  ```
| Field          | Content                           |
|----------------|-----------------------------------|
| `messageId`    | id of the chat message            |
| `ticketId`     | id of the ticket                  |
| `text`         | textual content of the message    |
| `timestamp`    | timestamp of the message          |
| `attachments`  | attachments linked to the message |

### Attachment
- **METHOD** `GET` **URL**: `/API/attachment/{attachmentId}`

  - **Description**: Get attachment linked to `attachmentId`
  - **Permissions allowed**:
  - **Request query parameter**: `attachmentId` to retrieve the corresponding attachment
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `404 Not Found` (attachment with id `attachmentId` not existing)
    - `422 Unprocessable Entity` (wrong format for `attachmentId`)
    - `500 Internal Server Error`
  - **Response body**: attachment corresponding to attachmentId / Error message in case of error
  ```
  {
    "attachmentId" : <attachmentId>
    "name" : <name>,
    "attachment": <attachment>
  }
  ```
| Field            | Content                        |
|------------------|--------------------------------|
| `attachmentId`   | id of the chat attachment      |
| `name`           | name of the attachment         |
| `attachment`     | attachment data (binary array) |
