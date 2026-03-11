> ## Documentation Index
> Fetch the complete documentation index at: https://docs.siliconflow.com/llms.txt
> Use this file to discover all available pages before exploring further.

# Create rerank

> Creates a rerank request.



## OpenAPI

````yaml post /rerank
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
  /rerank:
    post:
      tags:
        - Rerank
      summary: Create Rerank
      description: Creates a rerank request.
      operationId: createRerank
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RerankRequest'
      responses:
        '200':
          description: '200'
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RerankResponse'
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
    RerankRequest:
      type: object
      required:
        - model
        - query
        - documents
      properties:
        model:
          type: string
          description: >-
            Corresponding Model Name. To better enhance service quality, we will
            make periodic changes to the models provided by this service,
            including but not limited to model on/offlining and adjustments to
            model service capabilities. We will notify you of such changes
            through appropriate means such as announcements or message pushes
            where feasible.
          example: Qwen/Qwen3-Reranker-8B
          default: Qwen/Qwen3-Reranker-8B
          enum:
            - Qwen/Qwen3-Reranker-8B
            - Qwen/Qwen3-Reranker-4B
            - Qwen/Qwen3-Reranker-0.6B
        query:
          type: string
          description: Required. The search query.
          example: Apple
        documents:
          type: array
          minItems: 1
          items:
            type: string
          description: >-
            Currently, only string lists are supported. Document objects will be
            supported in the future.
          example:
            - apple
            - banana
            - fruit
            - vegetable
          default:
            - apple
            - banana
            - fruit
            - vegetable
        top_n:
          type: integer
          example: 4
          description: Number of most relevant documents or indices to return.
        return_documents:
          type: boolean
          description: >-
            If false, the response does not include document text; if true, it
            includes the input document text.
        max_chunks_per_doc:
          type: integer
          description: >-
            Maximum number of chunks generated from within a document. Long
            documents are divided into multiple chunks for calculation, and the
            highest score among the chunks is taken as the document's score.
            only BAAI/bge-reranker-v2-m3, netease-youdao/bce-reranker-base_v1
            support this field.
        overlap_tokens:
          type: integer
          maximum: 80
          description: >-
            Number of token overlaps between adjacent chunks when documents are
            chunked. only BAAI/bge-reranker-v2-m3,
            netease-youdao/bce-reranker-base_v1 support this field.
    RerankResponse:
      type: object
      required:
        - id
        - results
        - tokens
      properties:
        id:
          type: string
        results:
          type: array
          items:
            type: object
            properties:
              document:
                type: object
                properties:
                  text:
                    type: string
                description: Original document content.
              index:
                type: integer
                description: >-
                  The index value of the position in the input candidate doc
                  array.
              relevance_score:
                type: number
                description: Similarity score.
        tokens:
          type: object
          properties:
            input_tokens:
              type: integer
            output_tokens:
              type: integer
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