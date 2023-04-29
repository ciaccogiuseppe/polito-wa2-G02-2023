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
    "id": <id>,
    "name": <name>,
    "surname": <surname>,
    "email": <email>
  }
  ```
| Field     | Content         |
|-----------|-----------------|
| `id`      | profile id      |
| `name`    | profile name    |
| `surname` | profile surname |
| `email`   | profile email   |


- **METHOD** `POST` **URL**: `/API/profiles/`

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

| Field     | Content         |
|-----------|-----------------|
| `name`    | profile name    |
| `surname` | profile surname |
| `email`   | profile email   |

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
        "id": <id>,
        "name": <name>,
        "brand": <brand>
      }, {...}, ...
    ]
  }
  ```
| Field   | Content              |
|---------|----------------------|
| `id`    | product id           |
| `name`  | product name         |
| `brand` | brand of the product |

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
    "id": <id>,
    "name": <name>,
    "brand": <brand>
  }
  ```
| Field   | Content              |
|---------|----------------------|
| `id`    | product id           |
| `name`  | product name         |
| `brand` | brand of the product |

### Ticketing
- **METHOD** `GET` **URL**: `/API/ticketing/{ticket_id}`

  - **Description**: Get ticket, if existing, with id corresponding to parameter `ticket_id`
  - **Permissions allowed**:
  - **Request query parameter**: `ticket_id` to retrieve the corresponding ticket
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `404 Not Found` (profile with email `email` not existing)
    - `422 Unprocessable Entity` (wrong format for `email`)
    - `500 Internal Server Error`
  - **Response body**: ticket corresponding to ticket_id / Error message in case of error
  ```
  {
    "ticket_id": <ticket_id>,
    "title": <title>,
    "description": <description>,
    "priority": <priority>,       
    "product_id": <product_id>, 
    "customer_id": <customer_id>,
    "expert_id": <expert_id>,
    "status": <status>,
    "created_date": <created_date>
  }
  ```
| Field          | Content                         |
|----------------|---------------------------------|
| `ticket_id`    | ticket id                       |
| `title`        | title of the ticket             |
| `description`  | description of the ticket       |
| `priority`     | priority of the ticket          |
| `product_id`   | id of the product of the ticket |
| `customer_id`  | customer who created the ticket |
| `expert_id`    | expert assigned to the ticket   |
| `status`       | status of the ticket            |
| `created_date` | timestamp of ticket creation    |

- **METHOD** `GET` **URL**: `/API/ticketing/history/{ticket_id}`

  - **Description**: Get ticket history about ticket with id corresponding to parameter `ticket_id`, if existing
  - **Permissions allowed**:
  - **Request query parameter**: `ticket_id` to retrieve the corresponding ticket
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `404 Not Found` (ticket with id `ticket_id` not existing)
    - `422 Unprocessable Entity` (wrong format for `ticket_id`)
    - `500 Internal Server Error`
  - **Response body**: array of ticket history events, regarding ticket corresponding to ticket_id / Error message in case of error
  ```
  {
    [
      {
        "history_id": <history_id>,
        "ticket_id": <ticket_id>,
        "old_state": <old_state>,
        "new_state": <new_state>,
        "user_id": <user_id>,
        "expert_id": <expert_id>,
        "timestamp": <timestamp>
      }
      ,
      {...}
    ]
  }
  ```

| Field        | Content                   |
|--------------|---------------------------|
| `history_id` | history event id          |
| `ticket_id`  | ticket id of the event    |
| `old_state`  | old state of ticket       |
| `new_state`  | new state of ticket       |
| `user_id`    | user who updated ticket   |
| `expert_id`  | expert assigned to ticket |
| `timestamp`  | timestamp of update       |


- **METHOD** `GET` **URL**: `/API/ticketing/filter`

  - **Description**: Get all tickets satisfying the given filters
  - **Permissions allowed**:
  - **Request query parameter**: _None_
  - **Request body**: Required filters (only needed ones to be specified)
  ```
  {
    "customer_id": <customer_id>,
    "min_priority": <min_priority>,
    "max_priority": <max_priority>,
    "product_id": <product_id>,
    "created_after": <created_after>,
    "created_before": <created_before>,
    "expert_id": <expert_id>,
    "status": [<status1>, <status2>, <status3>...]    
  }
  ```
 - 
 - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `404 Not Found` (ticket with id `ticket_id` not existing)
    - `422 Unprocessable Entity` (wrong format for `ticket_id`)
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
| Field            | Content                                                 |
|------------------|---------------------------------------------------------|
| `customer_id`    | get tickets created by customer with `id = customer_id` |
| `min_priority`   | minimum priority of required tickets                    |
| `max_priority`   | maximum priority of required tickets                    |
| `product_id`     | get tickets regarding product `product_id`              |
| `created_after`  | get tickets with `created_time >= created_after`        |
| `created_before` | get tickets with `created_time <= created_before`       |
| `expert_id`      | get tickets assigned to expert `expert_id`              |
| `status`         | get tickets with status in `status` list                |

- **METHOD** `POST` **URL**: `/API/ticketing/`

  - **Description**: Create new ticket, given its properties
  - **Permissions allowed**:
  - **Request query parameter**: _None_
  - **Request body**: Ticket to be inserted

    ```
    {
      "title": <title>,
      "description": <description>,
      "product_id": <product_id>,
    }
    ```
    - **Response**: `201 Created` (success)
    - **Error responses**:
      - `400 Bad Request`
      - `404 Not Found` (product with id `product_id` not existing)
      - `422 Unprocessable Entity` (wrong format for request body)
      - `500 Internal Server Error`
    - **Response body**: id assigned to the created ticket / Error message in case of errors
    ```
      {
        "ticket_id": <ticket_id>
      }
    ```
    - **Note**: `user_id` is obtained from the session

| Field         | Content                                        |
|---------------|------------------------------------------------|
| `title`       | ticket title (textual field)                   |
| `description` | ticket description (textual field)             |
| `product_id`  | product id of the product linked to the ticket |



    
- **METHOD** `PUT` **URL**: `/API/ticketing/assign`

  - **Description**: Assign ticket to expert
  - **Permissions allowed**:
  - **Request query parameter**: _None_
  - **Request body**: Assignment information of ticket

    ```
    {
      "ticket_id": <ticket_id>,
      "expert_id": <expert_id>,
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
  - **Note**: `user_id` is obtained from the session

| Field       | Content                                |
|-------------|----------------------------------------|
| `ticket_id` | ticket to assign                       |
| `expert_id` | expert to which the ticket is assigned |
| `priority`  | priority assigned to the ticket        |


- **METHOD** `PUT` **URL**: `/API/ticketing/update`

  - **Description**: Change state of given ticket
  - **Permissions allowed**:
  - **Request query parameter**: _None_
  - **Request body**: Update information of ticket

    ```
    {
      "ticket_id": <ticket_id>,
      "new_state": <new_state>
    }
    ```

  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `404 Not Found` (non-existing fields in request body)
    - `422 Unprocessable Entity`  (wrong format for request body)
    - `500 Internal Server Error`
  - **Response body**: _None_ / Error message in case of error
  - **Note**: `user_id` is obtained from the session

| Field       | Content                      |
|-------------|------------------------------|
| `ticket_id` | ticket to update             |
| `new_state` | new state assigned to ticket |


### Chat
- **METHOD** `GET` **URL**: `/API/chat/{ticket_id}`

  - **Description**: Get messages of chat linked to `ticket_id`
  - **Permissions allowed**:
  - **Request query parameter**: `ticket_id` to retrieve the corresponding ticket
  - **Request body**: _None_
  - **Response**: `200 OK` (success)
  - **Error responses**:
    - `400 Bad Request`
    - `404 Not Found` (ticket with id `ticket_id` not existing)
    - `422 Unprocessable Entity` (wrong format for `ticket_id`)
    - `500 Internal Server Error`
  - **Response body**: list of messages of chat corresponding to ticket_id / Error message in case of error
  ```
  {
    [
      {
        "message_id": <message_id>,
        "ticket_id": <ticket_id>,
        "sender_id": <sender_id>,
        "text": <text>,
        "timestamp": <timestamp>,
        "attachments":[<attachment1>, <attachment2>...]
      },
      {...},
      ...
    ]
  }
  ```
| Field         | Content                           |
|---------------|-----------------------------------|
| `message_id`  | id of the chat message            |
| `ticket_id`   | id of the ticket                  |
| `sender_id`   | id of the sender user             |
| `text`        | textual content of the message    |
| `timestamp`   | timestamp of the message          |
| `attachments` | attachments linked to the message |


- **METHOD** `POST` **URL**: `/API/chat/{ticket_id}`

  - **Description**: Add message to chat linked to `ticket_id`
  - **Permissions allowed**:
  - **Request query parameter**: `ticket_id` to retrieve the corresponding ticket
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
    - `404 Not Found` (ticket with id `ticket_id` not existing)
    - `422 Unprocessable Entity` (wrong format for `ticket_id` or request body)
    - `500 Internal Server Error`
  - **Response body**: id of the added message / Error message in case of error
  - **Note**: `sender_id` is obtained from the session
  ```
  {
    "message_id": <message_id>
  }
  ```
| Field         | Content                           |
|---------------|-----------------------------------|
| `message_id`  | id of the chat message            |
| `ticket_id`   | id of the ticket                  |
| `text`        | textual content of the message    |
| `timestamp`   | timestamp of the message          |
| `attachments` | attachments linked to the message |