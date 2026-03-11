> ## Documentation Index
> Fetch the complete documentation index at: https://docs.siliconflow.com/llms.txt
> Use this file to discover all available pages before exploring further.

# Upload reference audio

> Upload user-provided voice style, which can be in base64 encoding or file format. Refer to (https://docs.siliconflow.com/capabilities/text-to-speech#2-2)



## OpenAPI

````yaml post /uploads/audio/voice
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
  /uploads/audio/voice:
    post:
      summary: Upload Voice
      description: >-
        Upload user-provided voice style, which can be in base64 encoding or
        file format. Refer to
        (https://docs.siliconflow.com/capabilities/text-to-speech#2-2)
      operationId: uploadAudioVoice
      parameters: []
      requestBody:
        content:
          multipart/form-data:
            schema:
              type: object
              properties:
                model:
                  type: string
                  example: FunAudioLLM/CosyVoice2-0.5B
                  enum:
                    - FunAudioLLM/CosyVoice2-0.5B
                  description: Predefined voice style model name
                customName:
                  type: string
                  example: your-voice-name
                  description: User-defined voice style name
                  default: Silicon flow voice style model
                text:
                  type: string
                  example: >-
                    In the midst of ignorance, a day in the dream comes to an
                    end, and a new cycle begins.
                  description: Corresponding text content for the audio
                  default: >-
                    In the midst of ignorance, a day in the dream comes to an
                    end, and a new cycle begins.
              required:
                - model
                - customName
                - text
              oneOf:
                - properties:
                    audio:
                      title: Base64 encoding of audio
                      type: string
                      example: data:audio/mpeg;base64,aGVsbG93b3JsZA==
                      description: >-
                        Audio file encoded in base64 with the header format of
                        `data:audio/mpeg;base64`
                - properties:
                    file:
                      title: File upload for audio
                      type: string
                      format: binary
                      example: /path/to/audio.mp3
                      description: File to upload
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                type: object
                properties:
                  uri:
                    type: string
                    example: speech:your-voice-name:xxx:xxx
        '400':
          description: BadRequest
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/BadRquestData'
                type: object
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
        '429':
          description: RateLimit
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RateLimitData'
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
    RateLimitData:
      type: object
      required:
        - message
        - data
      properties:
        message:
          type: string
          example: >-
            Request was rejected due to rate limiting. If you want more, please
            contact contact@siliconflow.com. Details:TPM limit reached.
        data:
          type: string
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