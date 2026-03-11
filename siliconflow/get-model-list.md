> ## Documentation Index
> Fetch the complete documentation index at: https://docs.siliconflow.com/llms.txt
> Use this file to discover all available pages before exploring further.

# List models

> Retrieve models information.



## OpenAPI

````yaml get /models
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
  /models:
    get:
      tags:
        - Models
      summary: Get Model List
      description: Retrieve models information.
      operationId: Retrieve a list of models.
      parameters:
        - name: type
          in: query
          description: The type of models
          required: false
          schema:
            type: string
            enum:
              - text
              - image
              - audio
              - video
        - name: sub_type
          in: query
          description: >-
            The sub type of models. You can use it to filter models individually
            without setting type.
          required: false
          schema:
            type: string
            enum:
              - chat
              - embedding
              - reranker
              - text-to-image
              - image-to-image
              - speech-to-text
              - text-to-video
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                type: object
                properties:
                  object:
                    type: string
                    example: list
                  data:
                    type: array
                    items:
                      type: object
                      properties:
                        id:
                          type: string
                          example: stabilityai/stable-diffusion-xl-base-1.0
                        object:
                          type: string
                          example: model
                        created:
                          type: integer
                          example: 0
                        owned_by:
                          type: string
                          example: ''
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