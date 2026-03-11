> ## Documentation Index
> Fetch the complete documentation index at: https://docs.siliconflow.com/llms.txt
> Use this file to discover all available pages before exploring further.

# Retrieve user info

> Get user information including balance and status



## OpenAPI

````yaml get /user/info
openapi: 3.0.0
info:
  title: SiliconFlow API
  description: The SiliconFlow REST API
  version: 1.0.0
  contact:
    name: SiliconFlow Support
    url: https://www.siliconflow.com/
  license:
    name: MIT
    url: https://github.com/siliconflow-inc/siliconflow-api/blob/main/LICENSE
servers:
  - url: https://api.siliconflow.com/v1
security:
  - bearerAuth: []
paths:
  /user/info:
    get:
      tags:
        - UserInfo
      summary: Get user information
      description: Get user information including balance and status
      operationId: user-info
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                type: object
                properties:
                  code:
                    type: integer
                    example: 20000
                  message:
                    type: string
                    example: OK
                  status:
                    type: boolean
                    example: true
                  data:
                    type: object
                    properties:
                      id:
                        type: string
                        example: userid
                      name:
                        type: string
                        example: username
                        description: >-
                          This field will no longer be returned after June 11th,
                          and a fixed empty string will be output instead.
                      image:
                        type: string
                        example: user_avatar_image_url
                        description: >-
                          This field will no longer be returned after June 11th,
                          and a fixed empty string will be output instead.
                      email:
                        type: string
                        example: user_email_address
                        description: >-
                          This field will no longer be returned after June 11th,
                          and a fixed empty string will be output instead.
                      isAdmin:
                        type: boolean
                        example: false
                      balance:
                        type: string
                        example: '0.88'
                      status:
                        type: string
                        example: normal
                      introduction:
                        type: string
                        example: ''
                      role:
                        type: string
                        example: ''
                      chargeBalance:
                        type: string
                        example: '88.00'
                      totalBalance:
                        type: string
                        example: '88.88'
        '400':
          description: BadRequest
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/StringData'
        '401':
          description: Unauthorized
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/StringData'
        '404':
          description: NotFound
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/StringData'
        '429':
          description: RateLimit
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/StringData'
        '503':
          description: Overloaded
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/StringData'
        '504':
          description: Timeout
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/StringData'
      deprecated: false
components:
  schemas:
    StringData:
      type: string
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: your api key
      description: >-
        Use the following format for authentication: Bearer [<your api
        key>](https://cloud.siliconflow.com/account/ak)

````

Built with [Mintlify](https://mintlify.com).