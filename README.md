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
- Edit ```application.properties``` file, changing ```spring.jpa.hibernate.ddl-auto=validate``` to ```spring.jpa.hibernate.ddl-auto=create```
- Run Server Application
- Set again ```spring.jpa.hibernate.ddl-auto``` to ```validate```
- Run query in ```server/main/resources/records.sql```
- Database should now be running correctly

## Docker JIB deploy
- Run gradle task ```jibDockerBuild``` to deploy locally
  - If Authorization Error rises, remove the ```"credsStore"``` entry in Docker ```config.json``` (found in ```<user>/.docker``` path)
  -  A working build can be found on [DockerHub](https://hub.docker.com/repository/docker/ciaccogiuseppe/polito_wa2_g02_2023/general)

## Docker Compose
### Using local JIB image
- Build local JIB image
- Run ```docker-compose up``` in ```server/src/main/docker/``` directory
### Using remote docker image
- Run ```docker-compose -f docker-remote.yml up``` in ```server/src/main/docker/``` directory


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
- **METHOD** `GET` **URL**: `/API/manager/profiles/{email}`

  - **Description**: Get profile, if existing, with email corresponding to `email`
  - **Permissions allowed**: Manager
  - **Request path parameter**: `email` to retrieve the corresponding profile
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `403 Forbidden`
    - `404 Not Found` (profile with email `email` not existing)
    - `422 Unprocessable Entity` (wrong format for `email`)
    - `500 Internal Server Error`
  - **Response body**: profile corresponding to `email` / Error message in case of error
  ```
  {
    "profileId": <profileId>,
    "email": <email>,
    "name": <name>,
    "surname": <surname>
  }
  ```
| Field       | Content         |
|-------------|-----------------|
| `profileId` | profile id      |
| `name`      | profile name    |
| `surname`   | profile surname |
| `email`     | profile email   |


- **METHOD** `GET` **URL**: `/API/manager/profiles/profileId/{profileId}`

  - **Description**: Get profile, if existing, with profileId corresponding to `profileId`
  - **Permissions allowed**: Manager
  - **Request path parameter**: `profileId` to retrieve the corresponding profile
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `403 Forbidden`
    - `404 Not Found` (profile with profileId `profileId` not existing)
    - `422 Unprocessable Entity` (wrong format for `profileId`)
    - `500 Internal Server Error`
  - **Response body**: profile corresponding to `profileId` / Error message in case of error
  ```
  {
    "profileId": <profileId>,
    "email": <email>,
    "name": <name>,
    "surname": <surname>
  }
  ```
| Field       | Content         |
|-------------|-----------------|
| `profileId` | profile id      |
| `name`      | profile name    |
| `surname`   | profile surname |
| `email`     | profile email   |

- **METHOD** `POST` **URL**: `/API/public/profiles`

  - **Description**: Create new profile, given its properties
  - **Permissions allowed**:  _Public_
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
      - `409 Conflict` (Profile with the given email already exists)
      - `422 Unprocessable Entity` (wrong format for request body)
      - `500 Internal Server Error`
    - **Response body**: _None_ / Error message in case of errors


| Field     | Content         |
|-----------|-----------------|
| `name`    | profile name    |
| `surname` | profile surname |
| `email`   | profile email   |


- **METHOD** `PUT` **URL**: `/API/manager/profiles/{email}`

  - **Description**: Change data of given profile
  - **Permissions allowed**: Manager
  - **Request path parameter**:`email` to retrieve the corresponding profile
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
    - `403 Forbidden`
    - `404 Not Found` (user associated with `email` not existing)
    - `409 Conflict` (Another profile with the given email already exists)
    - `422 Unprocessable Entity`  (wrong format for request body or email)
    - `500 Internal Server Error`
  - **Response body**: _None_ / Error message in case of error

| Field     | Content             |
|-----------|---------------------|
| `name`    | new profile name    |
| `surname` | new profile surname |
| `email`   | new profile email   |

- **METHOD** `PUT` **URL**: `/API/expert/profiles/{email}`

  - **Description**: Change data of given expert profile
  - **Permissions allowed**: Expert
  - **Request path parameter**:`email` to retrieve the corresponding profile
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
    - `403 Forbidden`
    - `404 Not Found` (user associated with `email` not existing)
    - `409 Conflict` (Another profile with the given email already exists)
    - `422 Unprocessable Entity`  (wrong format for request body or email)
    - `500 Internal Server Error`
  - **Response body**: _None_ / Error message in case of error

| Field     | Content             |
|-----------|---------------------|
| `name`    | new profile name    |
| `surname` | new profile surname |
| `email`   | new profile email   |

- **METHOD** `PUT` **URL**: `/API/client/profiles/{email}`

  - **Description**: Change data of given client profile
  - **Permissions allowed**: Client
  - **Request path parameter**:`email` to retrieve the corresponding profile
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
    - `403 Forbidden`
    - `404 Not Found` (user associated with `email` not existing)
    - `409 Conflict` (Another profile with the given email already exists)
    - `422 Unprocessable Entity`  (wrong format for request body or email)
    - `500 Internal Server Error`
  - **Response body**: _None_ / Error message in case of error

| Field     | Content             |
|-----------|---------------------|
| `name`    | new profile name    |
| `surname` | new profile surname |
| `email`   | new profile email   |

### Products
- **METHOD** `GET` **URL**: `/API/public/products/`

  - **Description**: Get all products in the DB
  - **Permissions allowed**:  _Public_
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
        "productId": <productId>,
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

- **METHOD** `GET` **URL**: `/API/public/products/{productId}`

  - **Description**: Get details of product with id `{productId}`
  - **Permissions allowed**:  _Public_
  - **Request path parameter**: `{productId}` to get corresponding product
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
    "productId": <productId>,
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
- **METHOD** `GET` **URL**: `/API/manager/ticketing/{ticketId}`

  - **Description**: Get ticket, if existing, with id corresponding to parameter `ticketId`
  - **Permissions allowed**: Manager
  - **Request path parameter**: `ticketId` to retrieve the corresponding ticket
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `403 Forbidden`
    - `404 Not Found` (ticket with ticketId `ticketId` not existing)
    - `422 Unprocessable Entity` (wrong format for `ticketId`)
    - `500 Internal Server Error`
  - **Response body**: ticket corresponding to ticketId / Error message in case of error
  ```
  {
    "ticketId": <ticketId>,
    "title": <title>,
    "description": <description>,
    "priority": <priority>,   
    "productId": <productId>, 
    "clientEmail": <clientEmail>,  
    "expertEmail": <expertEmail>, 
    "status": <status>, 
    "createdTimestamp": <createdTimestamp> 
  }
  ```
| Field              | Content                         |
|--------------------|---------------------------------|
| `ticketId`         | ticket id                       |
| `title`            | title of the ticket             |
| `description`      | description of the ticket       |
| `priority`         | priority of the ticket          |
| `productId`        | id of the product of the ticket |
| `clientEmail`    | client who created the ticket   |
| `expertEmail`      | expert assigned to the ticket   |
| `status`           | status of the ticket            |
| `createdTimestamp` | timestamp of ticket creation    |

- **METHOD** `GET` **URL**: `/API/expert/ticketing/{ticketId}`

  - **Description**: Get ticket associated to the expert, if existing, with id corresponding to parameter `ticketId`
  - **Permissions allowed**: Expert
  - **Request path parameter**: `ticketId` to retrieve the corresponding ticket
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `403 Forbidden`
    - `404 Not Found` (ticket with ticketId `ticketId` not existing)
    - `422 Unprocessable Entity` (wrong format for `ticketId`)
    - `500 Internal Server Error`
  - **Response body**: ticket corresponding to ticketId / Error message in case of error
  ```
  {
    "ticketId": <ticketId>,
    "title": <title>,
    "description": <description>,
    "priority": <priority>,   
    "productId": <productId>, 
    "clientEmail": <clientEmail>,  
    "expertEmail": <expertEmail>, 
    "status": <status>, 
    "createdTimestamp": <createdTimestamp> 
  }
  ```
| Field              | Content                         |
|--------------------|---------------------------------|
| `ticketId`         | ticket id                       |
| `title`            | title of the ticket             |
| `description`      | description of the ticket       |
| `priority`         | priority of the ticket          |
| `productId`        | id of the product of the ticket |
| `clientEmail`    | client who created the ticket |
| `expertEmail`      | expert assigned to the ticket   |
| `status`           | status of the ticket            |
| `createdTimestamp` | timestamp of ticket creation    |

- **METHOD** `GET` **URL**: `/API/client/ticketing/{ticketId}`

  - **Description**: Get ticket associated to the client, if existing, with id corresponding to parameter `ticketId`
  - **Permissions allowed**: Client
  - **Request path parameter**: `ticketId` to retrieve the corresponding ticket
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `403 Forbidden`
    - `404 Not Found` (ticket with ticketId `ticketId` not existing)
    - `422 Unprocessable Entity` (wrong format for `ticketId`)
    - `500 Internal Server Error`
  - **Response body**: ticket corresponding to ticketId / Error message in case of error
  ```
  {
    "ticketId": <ticketId>,
    "title": <title>,
    "description": <description>,
    "priority": <priority>,   
    "productId": <productId>, 
    "clientEmail": <clientEmail>,  
    "expertEmail": <expertEmail>, 
    "status": <status>, 
    "createdTimestamp": <createdTimestamp> 
  }
  ```
| Field              | Content                         |
|--------------------|---------------------------------|
| `ticketId`         | ticket id                       |
| `title`            | title of the ticket             |
| `description`      | description of the ticket       |
| `priority`         | priority of the ticket          |
| `productId`        | id of the product of the ticket |
| `clientEmail`    | client who created the ticket |
| `expertEmail`      | expert assigned to the ticket   |
| `status`           | status of the ticket            |
| `createdTimestamp` | timestamp of ticket creation    |

- **METHOD** `GET` **URL**: `/API/manager/ticketing/history/filter`

  - **Description**: Get all ticket history records satisfying the given filters
  - **Permissions allowed**: Manager
  - **Request query parameters**:
    - `ticketId` to retrieve the records associated to the corresponding ticket
    - `userEmail` to retrieve the  records associated to the corresponding user
    - `updatedAfter` to retrieve the records created after the given date
    - `updatedBefore` to retrieve the records created before the given date
    - `currentExpertId` to retrieve the records associated to the given expertId
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `404 Not Found` (ticket with ticketId `ticketId` not existing, expert associated to the given `currentExpertEmail` not existing, client associated to the given `userEmail` not existing)
    - `422 Unprocessable Entity` (wrong format for `request query parameters` or `updatedAfter` is after `updatedBefore`)
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
        "userEmail": <userEmail>,
        "currentExpertEmail": <expertEmail>,
        "updatedTimestamp": <timestamp>
      }
      ,
      {...}
    ]
  }
  ```

| Field                | Content                   |
|----------------------|---------------------------|
| `historyId`          | history event id          |
| `ticketId`           | ticket id of the event    |
| `oldState`           | old state of ticket       |
| `newState`           | new state of ticket       |
| `userEmail`          | user who updated ticket   |
| `currentExpertEmail` | expert assigned to ticket |
| `updatedTimestamp`   | timestamp of update       |


- **METHOD** `GET` **URL**: `/API/manager/ticketing/filter`

  - **Description**: Get all tickets satisfying the given filters
  - **Permissions allowed**: Manager
  - **Request query parameter**: Required filters (only needed ones to be specified)
    - `minPriority` specifies the minimum priority
    - `maxPriority` specifies the maximum priority
    - `productId` to retrieve the  records associated to the corresponding product
    - `clientEmail` to retrieve the  records associated to the corresponding customer
    - `updatedAfter` to retrieve the records created after the given date
    - `updatedBefore` to retrieve the records created before the given date
    - `expertEmail` to retrieve the records associated to the given expertId
    - `status`: list of the desired states of the records
  - **Request body**: _None_
 - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `403 Forbidden`
    - `404 Not Found` (ticket with ticketId `ticketId` not existing, expert associated to the given `expertId` not existing, customer associated to the given `customerId` not existing,  product associated to the given `productId` not existing)
    - `422 Unprocessable Entity` (wrong format for `request query parameters` or `updatedAfter` is after `updatedBefore`)
    - `500 Internal Server Error`
  - **Response body**: list of tickets satisfying the given filtering conditions / Error message in case of error
  ```
  {
    [
      <ticket_1>,
      <ticket_2>,
      ...
    ]
  }
  ```
| Field              | Content                         |
|--------------------|---------------------------------|
| `ticketId`         | ticket id                       |
| `title`            | title of the ticket             |
| `description`      | description of the ticket       |
| `priority`         | priority of the ticket          |
| `productId`        | id of the product of the ticket |
| `clientEmail`      | client who created the ticket   |
| `expertEmail`      | expert assigned to the ticket   |
| `status`           | status of the ticket            |
| `createdTimestamp` | timestamp of ticket creation    |

- **METHOD** `GET` **URL**: `/API/expert/ticketing/filter`

  - **Description**: Get all tickets, associated with the logged expert, satisfying the given filters
  - **Permissions allowed**: Expert
  - **Request query parameter**: Required filters (only needed ones to be specified)
    - `minPriority` specifies the minimum priority
    - `maxPriority` specifies the maximum priority
    - `productId` to retrieve the  records associated to the corresponding product
    - `clientEmail` to retrieve the  records associated to the corresponding customer
    - `updatedAfter` to retrieve the records created after the given date
    - `updatedBefore` to retrieve the records created before the given date
    - `expertEmail` to retrieve the records associated to the given expertId
    - `status`: list of the desired states of the records
  - **Request body**: _None_
- **Response**: `200 OK` (success)
- **Error responses**:
  - `400 Bad Request`
  - `403 Forbidden`
  - `404 Not Found` (ticket with ticketId `ticketId` not existing, expert associated to the given `expertId` not existing, customer associated to the given `customerId` not existing,  product associated to the given `productId` not existing)
  - `422 Unprocessable Entity` (wrong format for `request query parameters` or `updatedAfter` is after `updatedBefore`)
  - `500 Internal Server Error`
- **Response body**: list of tickets satisfying the given filtering conditions / Error message in case of error
  ```
  {
    [
      <ticket_1>,
      <ticket_2>,
      ...
    ]
  }
  ```
| Field              | Content                         |
|--------------------|---------------------------------|
| `ticketId`         | ticket id                       |
| `title`            | title of the ticket             |
| `description`      | description of the ticket       |
| `priority`         | priority of the ticket          |
| `productId`        | id of the product of the ticket |
| `clientEmail`      | client who created the ticket   |
| `expertEmail`      | expert assigned to the ticket   |
| `status`           | status of the ticket            |
| `createdTimestamp` | timestamp of ticket creation    |

- **METHOD** `GET` **URL**: `/API/client/ticketing/filter`

  - **Description**: Get all tickets, associated with the current client, satisfying the given filters
  - **Permissions allowed**: Manager
  - **Request query parameter**: Required filters (only needed ones to be specified)
    - `minPriority` specifies the minimum priority
    - `maxPriority` specifies the maximum priority
    - `productId` to retrieve the  records associated to the corresponding product
    - `clientEmail` to retrieve the  records associated to the corresponding customer
    - `updatedAfter` to retrieve the records created after the given date
    - `updatedBefore` to retrieve the records created before the given date
    - `expertEmail` to retrieve the records associated to the given expertId
    - `status`: list of the desired states of the records
  - **Request body**: _None_
- **Response**: `200 OK` (success)
- **Error responses**:
  - `400 Bad Request`
  - `403 Forbidden`
  - `404 Not Found` (ticket with ticketId `ticketId` not existing, expert associated to the given `expertId` not existing, customer associated to the given `customerId` not existing,  product associated to the given `productId` not existing)
  - `422 Unprocessable Entity` (wrong format for `request query parameters` or `updatedAfter` is after `updatedBefore`)
  - `500 Internal Server Error`
- **Response body**: list of tickets satisfying the given filtering conditions / Error message in case of error
  ```
  {
    [
      <ticket_1>,
      <ticket_2>,
      ...
    ]
  }
  ```
| Field              | Content                         |
|--------------------|---------------------------------|
| `ticketId`         | ticket id                       |
| `title`            | title of the ticket             |
| `description`      | description of the ticket       |
| `priority`         | priority of the ticket          |
| `productId`        | id of the product of the ticket |
| `clientEmail`      | client who created the ticket   |
| `expertEmail`      | expert assigned to the ticket   |
| `status`           | status of the ticket            |
| `createdTimestamp` | timestamp of ticket creation    |

- **METHOD** `POST` **URL**: `/API/client/ticketing/`

  - **Description**: Create new ticket, given its properties
  - **Permissions allowed**: Client
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
      - `403 Forbidden`
      - `404 Not Found` (product with id `productId` not existing, the customer that performs the request does not exist)
      - `422 Unprocessable Entity` (wrong format for request body)
      - `500 Internal Server Error`
    - **Response body**: id assigned to the created ticket / Error message in case of errors
    ```
      {
        "ticketId": <ticketId>
      }
    ```

| Field          | Content                                          |
|----------------|--------------------------------------------------|
| `title`        | ticket title (textual field)                     |
| `description`  | ticket description (textual field)               |
| `productId`    | product id of the product linked to the ticket   |



    
- **METHOD** `PUT` **URL**: `/API/manager/ticketing/assign`

  - **Description**: Assign ticket to expert
  - **Permissions allowed**: Manager
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
    - `403 Forbidden`
    - `404 Not Found` (ticket with ticketId `ticketId` not existing, expert associated to the given `expertEmail` not existing )
    - `422 Unprocessable Entity`  (wrong format for request body, the assigned profile is not an expert, the ticket cannot be assigned considering the actual status)
    - `500 Internal Server Error`
  - **Response body**: _None_ / Error message in case of error

| Field         | Content                                |
|---------------|----------------------------------------|
| `ticketId`    | ticket to assign                       |
| `expertEmail` | expert to which the ticket is assigned |
| `priority`    | priority assigned to the ticket        |


- **METHOD** `PUT` **URL**: `/API/manager/ticketing/update`

  - **Description**: Change state of given ticket
  - **Permissions allowed**: Manager
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
    - `403 Forbidden`
    - `404 Not Found` (ticket with ticketId `ticketId` not existing)
    - `422 Unprocessable Entity`  (wrong format for request body, the new state is invalid according to the current state)
    - `500 Internal Server Error`
  - **Response body**: _None_ / Error message in case of error

| Field      | Content                      |
|------------|------------------------------|
| `ticketId` | ticket to update             |
| `newState` | new state assigned to ticket |

- **METHOD** `PUT` **URL**: `/API/expert/ticketing/update`

  - **Description**: Change state of given ticket, associated with the logged expert
  - **Permissions allowed**: Expert
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
    - `403 Forbidden`
    - `404 Not Found` (ticket with ticketId `ticketId` not existing)
    - `422 Unprocessable Entity`  (wrong format for request body, the new state is invalid according to the current state)
    - `500 Internal Server Error`
  - **Response body**: _None_ / Error message in case of error

| Field      | Content                      |
|------------|------------------------------|
| `ticketId` | ticket to update             |
| `newState` | new state assigned to ticket |

- **METHOD** `PUT` **URL**: `/API/client/ticketing/update`

  - **Description**: Change state of given ticket, associated with the logged client
  - **Permissions allowed**: Client
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
    - `403 Forbidden`
    - `404 Not Found` (ticket with ticketId `ticketId` not existing)
    - `422 Unprocessable Entity`  (wrong format for request body, the new state is invalid according to the current state)
    - `500 Internal Server Error`
  - **Response body**: _None_ / Error message in case of error

| Field      | Content                      |
|------------|------------------------------|
| `ticketId` | ticket to update             |
| `newState` | new state assigned to ticket |


### Chat
- **METHOD** `GET` **URL**: `/API/chat/{ticketId}`

  - **Description**: Get messages of chat linked to `ticketId`, associated with the logged client, or expert
  - **Permissions allowed**: Expert, Client
  - **Request path parameter**: `ticketId` to retrieve the corresponding ticket
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `403 Forbidden`
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
        "senderEmail": <senderEmail>,
        "text": <text>,
        "sentTimestamp": <timestamp>,
        "attachments":[<attachment1>, <attachment2>...]
      },
      {...},
      ...
    ]
  }
  ```
| Field           | Content                           |
|-----------------|-----------------------------------|
| `messageId`     | id of the chat message            |
| `ticketId`      | id of the ticket                  |
| `senderEmail`   | email of the sender user          |
| `text`          | textual content of the message    |
| `sentTimestamp` | timestamp of the message          |
| `attachments`   | attachments linked to the message |

- **METHOD** `GET` **URL**: `/API/manager/chat/{ticketId}`

  - **Description**: Get messages of chat linked to `ticketId`
  - **Permissions allowed**: Manager
  - **Request path parameter**: `ticketId` to retrieve the corresponding ticket
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `403 Forbidden`
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
        "senderEmail": <senderEmail>,
        "text": <text>,
        "sentTimestamp": <timestamp>,
        "attachments":[<attachment1>, <attachment2>...]
      },
      {...},
      ...
    ]
  }
  ```
| Field           | Content                           |
|-----------------|-----------------------------------|
| `messageId`     | id of the chat message            |
| `ticketId`      | id of the ticket                  |
| `senderEmail`   | email of the sender user          |
| `text`          | textual content of the message    |
| `sentTimestamp` | timestamp of the message          |
| `attachments`   | attachments linked to the message |

- **METHOD** `POST` **URL**: `/API/chat/{ticketId}`

  - **Description**: Add message to chat linked to `ticketId`, associated with the logged client, or expert
  - **Permissions allowed**: Expert, Client
  - **Request path parameter**: `ticketId` to retrieve the corresponding ticket
  - **Request body**: Message to be added to chat
  ```
  {
     "messageId": <messageId>,
     "ticketId": <ticketId>,
     "senderEmail": <senderEmail>,
     "text": <text>,
     "sentTimestamp": <timestamp>,
     "attachments":[<attachment1>, <attachment2>...]
  }
  ```
  - **Response**: `201 CREATED` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `403 Forbidden`
    - `404 Not Found` (ticket with id `ticketId` not existing, sender associated to `senderId` not existing)
    - `409 Unauthorized` (the sender is not related to ticket)
    - `422 Unprocessable Entity` (wrong format for `ticketId` or request body)
    - `500 Internal Server Error`
  - **Response body**: _None_ / Error message in case of error
| Field           | Content                           |
|-----------------|-----------------------------------|
| `messageId`     | id of the chat message            |
| `ticketId`      | id of the ticket                  |
| `text`          | textual content of the message    |
| `sentTimestamp` | timestamp of the message          |
| `attachments`   | attachments linked to the message |

### Attachment
- **METHOD** `GET` **URL**: `/API/attachment/{attachmentId}`

  - **Description**: Get attachment linked to `attachmentId`, associated with the logged client, or expert
  - **Permissions allowed**: Expert, Client
  - **Request path parameter**: `attachmentId` to retrieve the corresponding attachment
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `403 Forbidden`
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

- **METHOD** `GET` **URL**: `/API/manager/attachment/{attachmentId}`

  - **Description**: Get attachment linked to `attachmentId`
  - **Permissions allowed**: Expert, Client
  - **Request path parameter**: `attachmentId` to retrieve the corresponding attachment
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `403 Forbidden`
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


### Authentication
- **METHOD** `POST` **URL**: `/API/login`

  - **Description**: login to the application
  - **Permissions allowed**:   _Public_
  - **Request path parameter**: _None_
  - **Request body**: Message to be added to chat
  ```
  {
    "username": <username>,
    "password": <password>
  }
  ```
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `401 Unauthorized`
  - **Response body**: token and associated data
  ```
  {
    "refreshToken" : <refreshToken>
    "token" : <token>,
    "expiresIn": <expiresIn>
  }
  ```
| Field          | Content                                      |
|----------------|----------------------------------------------|
| `refreshToken` | refreshToken associated to the login request |
| `token`        | token associated to the login request        |
| `expiresIn`    | time validity of the token                   |

- **METHOD** `POST` **URL**: `/API/refreshtoken`

  - **Description**: refresh the login token
  - **Permissions allowed**: _Public_
  - **Request path parameter**: _None_
  - **Request body**: Message to be added to chat
  ```
  {
    "username": <username>,
    "password": <password>
  }
  ```
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `401 Unauthorized`
  - **Response body**: token and associated data
  ```
  {
    "refreshToken" : <refreshToken>
    "token" : <token>,
    "expiresIn": <expiresIn>
  }
  ```
| Field          | Content                                      |
|----------------|----------------------------------------------|
| `refreshToken` | refreshToken associated to the login request |
| `token`        | token associated to the login request        |
| `expiresIn`    | time validity of the token                   |


### Accounting
- **METHOD** `POST` **URL**: `/API/signup`
  - **Description**: signup to the application
  - **Permissions allowed**:   _Public_
  - **Request path parameter**: _None_
  - **Request body**: requested user data
  ```
  {
    "username": <username>,
    "password": <password>,
    "email": <email>,
    "firstName": <firstName>,
    "lastName": <lastName>,
    "expertCategories": [<ProductCategory1>, <ProductCategory2>, ...],
    "address": {
       "country": <country>, "region": <region>, "city": <city>, "address": <address>
     },
    "role": <role>
  }
  ```
    - **Notes**:
      - _expertCategories_ is considered only if _role_ is "EXPERT"
      - _address_ is considered only if _role_ is "CLIENT"
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `409 Conflict` (user with the given email already exists)
    - `422 Unprocessable Entity` (wrong user format)
    - `500 Internal Server Error`
  - **Response body**:  _None_


- **METHOD** `POST` **URL**: `/API/createExpert`
  - **Description**: create an expert account
  - **Permissions allowed**:  Manager
  - **Request path parameter**: _None_
  - **Request body**: requested user data
  ```
  {
    "username": <username>,
    "password": <password>,
    "email": <email>,
    "firstName": <firstName>,
    "lastName": <lastName>,
    "expertCategories": [<ProductCategory1>, <ProductCategory2>, ...],
    "role": <role>
  }
  ```
  - **Response**: `200 OK` (success)
    - **Error responses**:
      - `400 Bad Request`
      - `409 Conflict` (user with the given email already exists)
      - `422 Unprocessable Entity` (wrong user format)
      - `500 Internal Server Error`
    - **Response body**:  _None_


- **METHOD** `POST` **URL**: `/API/createVendor`
  - **Description**: create a vendor account
  - **Permissions allowed**:  Manager
  - **Request path parameter**: _None_
  - **Request body**: requested user data
  ```
  {
    "username": <username>,
    "password": <password>,
    "email": <email>,
    "firstName": <firstName>,
    "lastName": <lastName>,
    "role": <role>
  }
  ```
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `409 Conflict` (user with the given email already exists)
    - `422 Unprocessable Entity` (wrong user format)
    - `500 Internal Server Error`
  - **Response body**:  _None_


- **METHOD** `PUT` **URL**: `/API/client/user/{email}`
  - **Description**: Change data of given client user
  - **Permissions allowed**: Client
  - **Request path parameter**:`email` to retrieve the corresponding user
  - **Request body**: Update information of client profile
    ```
    {
      "email": <email>
      "firstName": <firstName>,
      "lastName": <lastName>,
      "address": {
         "country": <country>, "region": <region>, "city": <city>, "address": <address>
       },
      "role: <role>
    }
    ```
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `403 Forbidden`
    - `404 Not Found` (user associated with `email` not existing)
    - `422 Unprocessable Entity`  (wrong format for request body or email)
    - `500 Internal Server Error`
  - **Response body**: _None_ / Error message in case of error


- **METHOD** `PUT` **URL**: `/API/expert/user/{email}`
  - **Description**: Change data of given expert user
  - **Permissions allowed**: Expert
  - **Request path parameter**:`email` to retrieve the corresponding user
  - **Request body**: Update information of expert profile
    ```
    {
      "email": <email>
      "firstName": <firstName>,
      "lastName": <lastName>,
      "expertCategories": [<ProductCategory1>, <ProductCategory2>, ...],
      "role": <role>
    }
    ```
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `403 Forbidden`
    - `404 Not Found` (user associated with `email` not existing)
    - `422 Unprocessable Entity`  (wrong format for request body or email)
    - `500 Internal Server Error`
  - **Response body**: _None_ / Error message in case of error


- **METHOD** `PUT` **URL**: `/API/manager/user/{email}`
  - **Description**: Change data of given manager user
  - **Permissions allowed**: Manager
  - **Request path parameter**:`email` to retrieve the corresponding user
  - **Request body**: Update information of manager profile
    ```
    {
      "email": <email>
      "firstName": <firstName>,
      "lastName": <lastName>,
      "role": <role>
    }
    ```
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `403 Forbidden`
    - `404 Not Found` (user associated with `email` not existing)
    - `422 Unprocessable Entity`  (wrong format for request body or email)
    - `500 Internal Server Error`
  - **Response body**: _None_ / Error message in case of error

### Category
- **METHOD** `GET` **URL**: `/API/public/categories/`
  - **Description**: Get all the available categories
  - **Permissions allowed**:  _Public_
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `500 Internal Server Error`
  - **Response body**: List of all available categories of products
  ```
  {
    [
       { "categoryName" : <categoryName1> },
       { "categoryName" : <categoryName2> },
       ...
     ]
  }
  ```

### Brand
- **METHOD** `GET` **URL**: `/API/public/brands/`
  - **Description**: Get all the available brands
  - **Permissions allowed**: _Public_
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `500 Internal Server Error`
  - **Response body**: List of all available brands
  ```
  {
    [
       { "name" : <brandName> },
       { "name" : <brandName> },
       ...
     ]
  }
  ```

- **METHOD** `POST` **URL**: `/API/manager/brand/`
  - **Description**: insert a new brand
  - **Permissions allowed**:  Manager
  - **Request path parameter**: _None_
  - **Request body**: object containing the brand name
  ```
  { "name": <brandName> }
  ```
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `422 Unprocessable Entity` (wrong brand format)
    - `500 Internal Server Error`
  - **Response body**: the created brand
   ```
  { "name": <brandName> }
  ```

### Item
- **METHOD** `GET` **URL**: `/API/public/products/{productId}/items/`
  - **Description**: Get all the available items associated to the given product
  - **Permissions allowed**: _Public_
  - **Request path parameter**: `productId` to retrieve the corresponding product
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `422 Unprocessable Entity` (wrong format)
    - `500 Internal Server Error`
  - **Response body**: List of all associated items
  ```
  {
    [
       { 
          "productId": <productId>,
          "serialNum": <serialNum>,
          "uuid": <uuid>,
          "clientEmail": <clientEmail>,
          "validFromTimestamp": <validFromTimestamp>,
          "durationMonths": <durationMonths>
        },
       ...
     ]
  }
  ```
| Field                | Content                                                  |
|----------------------|----------------------------------------------------------|
| `productId`          | id of the associated product                             |
| `serialNum`          | serial number                                            |
| `uuid`               | UUID associated to the item                              |
| `clientEmail`        | email of the client that owns the item                   |
| `validFromTimestamp` | timestamp that represents the start of the item validity |
| `durationMonths`     | duration of the item validity                            |


- **METHOD** `GET` **URL**: `/API/public/products/{productId}/items/{serialNum}`
  - **Description**: Get the item associated to the given product and serial number, if exists
  - **Permissions allowed**: _Public_
  - **Request path parameters**:
    - `productId` to retrieve the corresponding product
    - `serialNum` to retrieve the corresponding serial number
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `404 Not Found`
    - `422 Unprocessable Entity` (wrong format)
    - `500 Internal Server Error`
  - **Response body**: Associated item if exists
  ```
  { 
     "productId": <productId>,
     "serialNum": <serialNum>,
     "uuid": <uuid>,
     "clientEmail": <clientEmail>,
     "validFromTimestamp": <validFromTimestamp>,
     "durationMonths": <durationMonths>
  }
  ```
| Field                | Content                                                  |
|----------------------|----------------------------------------------------------|
| `productId`          | id of the associated product                             |
| `serialNum`          | serial number                                            |
| `uuid`               | UUID associated to the item                              |
| `clientEmail`        | email of the client that owns the item                   |
| `validFromTimestamp` | timestamp that represents the start of the item validity |
| `durationMonths`     | duration of the item validity                            |


- **METHOD** `POST` **URL**: `/API/vendor/products/items/`
  - **Description**: generate an UUID for the given item
  - **Permissions allowed**:  Vendor
  - **Request path parameter**: _None_
  - **Request body**: object containing the brand name
  ```
  { 
     "productId": <productId>,
     "serialNum": <serialNum>,
     "clientEmail": <clientEmail>
  }
  ```
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `422 Unprocessable Entity` (wrong format)
    - `500 Internal Server Error`
  - **Response body**: the updated item
  ```
  { 
     "productId": <productId>,
     "serialNum": <serialNum>,
     "uuid": <uuid>,
     "clientEmail": <clientEmail>,
     "validFromTimestamp": <validFromTimestamp>,
     "durationMonths": <durationMonths>
  }
  ```
| Field                | Content                                                  |
|----------------------|----------------------------------------------------------|
| `productId`          | id of the associated product                             |
| `serialNum`          | serial number                                            |
| `uuid`               | UUID associated to the item                              |
| `clientEmail`        | email of the client that owns the item                   |
| `validFromTimestamp` | timestamp that represents the start of the item validity |
| `durationMonths`     | duration of the item validity                            |

- **METHOD** `PUT` **URL**: `/API/client/products/items/register`
  - **Description**: register a new item
  - **Permissions allowed**:  Client
  - **Request path parameter**: _None_
  - **Request body**: the registered item
  ```
  { 
     "productId": <productId>,
     "serialNum": <serialNum>,
     "uuid": <uuid>,
     "clientEmail": <clientEmail>,
     "validFromTimestamp": <validFromTimestamp>,
     "durationMonths": <durationMonths>
  }
  ```
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `403 Forbidden`
    - `404 Not Found`
    - `422 Unprocessable Entity` (wrong format)
    - `500 Internal Server Error`
  - **Response body**: the created brand
  ```
  { 
     "productId": <productId>,
     "serialNum": <serialNum>,
     "uuid": <uuid>,
     "clientEmail": <clientEmail>,
     "validFromTimestamp": <validFromTimestamp>,
     "durationMonths": <durationMonths>
  }
  ```
| Field                | Content                                                  |
|----------------------|----------------------------------------------------------|
| `productId`          | id of the associated product                             |
| `serialNum`          | serial number                                            |
| `uuid`               | UUID associated to the item                              |
| `clientEmail`        | email of the client that owns the item                   |
| `validFromTimestamp` | timestamp that represents the start of the item validity |
| `durationMonths`     | duration of the item validity                            |
