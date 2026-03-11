> ## Documentation Index
> Fetch the complete documentation index at: https://docs.siliconflow.com/llms.txt
> Use this file to discover all available pages before exploring further.

# Create transcription

> Creates an audio transcription.



## OpenAPI

````yaml post /audio/transcriptions
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
  /audio/transcriptions:
    post:
      tags:
        - Audio
      summary: Create Audio Transcriptions
      description: Creates an audio transcription.
      operationId: createAudioTranscriptions
      requestBody:
        content:
          multipart/form-data:
            schema:
              $ref: '#/components/schemas/AudioRequest'
      responses:
        '200':
          description: '200'
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AudioResponse'
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
    AudioRequest:
      type: object
      required:
        - model
        - file
      properties:
        file:
          type: string
          description: The audio file object (not file name) to transcribe
          example: /path/to/file/audio.mp3
          format: binary
        model:
          type: string
          description: >-
            Corresponding Model Name. To better enhance service quality, we will
            make periodic changes to the models provided by this service,
            including but not limited to model on/offlining and adjustments to
            model service capabilities. We will notify you of such changes
            through appropriate means such as announcements or message pushes
            where feasible.
          example: FunAudioLLM/SenseVoiceSmall
          enum:
            - FunAudioLLM/SenseVoiceSmall
            - TeleAI/TeleSpeechASR
    AudioResponse:
      type: object
      description: >-
        Represents a transcription response returned by model, based on the
        provided input.
      required:
        - text
      properties:
        text:
          description: The transcribed text.
          type: string
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