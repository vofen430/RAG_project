> ## Documentation Index
> Fetch the complete documentation index at: https://docs.siliconflow.com/llms.txt
> Use this file to discover all available pages before exploring further.

# Retrieve video

> Get the user-generated video. The URL for the generated video is valid for one hour. Please make sure to download and store it promptly to avoid any issues due to URL expiration.



## OpenAPI

````yaml post /video/status
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
  /video/status:
    post:
      tags:
        - video
      summary: get video
      description: >-
        Get the user-generated video. The URL for the generated video is valid
        for one hour. Please make sure to download and store it promptly to
        avoid any issues due to URL expiration.
      requestBody:
        content:
          application/json:
            schema:
              oneOf:
                - $ref: '#/components/schemas/getVideosRequest'
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/getVideosResponse'
        '400':
          description: BadRequest
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/BadRquestData'
        '401':
          description: Unauthorized
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UnauthorizedData'
        '404':
          description: NotFound
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/NotFoundData'
        '503':
          description: Overloaded
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/OverloadedtData'
        '504':
          description: Timeout
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TimeoutData'
      deprecated: false
components:
  schemas:
    getVideosRequest:
      type: object
      required:
        - requestId
      properties:
        requestId:
          type: string
          description: The requestId returned by the interface submit.
    getVideosResponse:
      type: object
      properties:
        status:
          type: string
          description: >-
            Status of the operation. Possible values are
            'Succeed','InQueue','InProgress','Failed'.
          enum:
            - Succeed
            - InQueue
            - InProgress
            - Failed
        reason:
          type: string
          description: Reason for the operation
        results:
          type: object
          properties:
            videos:
              type: array
              items:
                type: object
                properties:
                  url:
                    description: >-
                      The URL for the generated image is valid for one hour.
                      Please make sure to download and store it promptly to
                      avoid any issues due to URL expiration.
                    type: string
            timings:
              type: object
              properties:
                inference:
                  type: number
                  format: double
                  description: Inference time
            seed:
              type: integer
              description: Seed value
    BadRquestData:
      type: object
      required:
        - message
        - data
        - code
      properties:
        code:
          type: integer
          nullable: true
          default: false
          example: 20012
        message:
          type: string
          nullable: false
        data:
          type: string
          nullable: false
    UnauthorizedData:
      type: string
      default: false
      example: Invalid token
    NotFoundData:
      type: string
      default: false
      example: 404 page not found
    OverloadedtData:
      type: object
      required:
        - code
        - message
        - data
      properties:
        code:
          type: integer
          example: 50505
        message:
          type: string
          example: Model service overloaded. Please try again later.
        data:
          type: string
          nullable: false
    TimeoutData:
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