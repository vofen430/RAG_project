> ## Documentation Index
> Fetch the complete documentation index at: https://docs.siliconflow.com/llms.txt
> Use this file to discover all available pages before exploring further.

# messages

> Creates a model response for the given chat conversation.



## OpenAPI

````yaml post /messages
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
  /messages:
    post:
      tags:
        - messages
      summary: Chat Completions
      description: Creates a model response for the given chat conversation.
      operationId: chat-completions
      requestBody:
        content:
          application/json:
            schema:
              oneOf:
                - $ref: '#/components/schemas/ChatMessagesRequest'
      responses:
        '200':
          description: '200'
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MessgesResponse'
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
      security:
        - bearerAuth: []
        - apiKey: []
components:
  schemas:
    ChatMessagesRequest:
      title: LLM
      type: object
      required:
        - model
        - messages
        - max_tokens
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
          example: moonshotai/Kimi-K2-Instruct
          default: null
          enum:
            - deepseek-ai/DeepSeek-V3.2
            - deepseek-ai/DeepSeek-V3.2-Exp
            - deepseek-ai/DeepSeek-V3.1-Terminus
            - deepseek-ai/DeepSeek-V3.1
            - deepseek-ai/DeepSeek-V3
            - nex-agi/DeepSeek-V3.1-Nex-N1
            - moonshotai/Kimi-K2.5
            - moonshotai/Kimi-K2-Instruct-0905
            - moonshotai/Kimi-K2-Instruct
            - moonshotai/Kimi-K2-Thinking
            - baidu/ERNIE-4.5-300B-A47B
        messages:
          type: array
          description: A list of messages comprising the conversation so far.
          items:
            type: object
            properties:
              role:
                type: string
                description: 'The role of the messages author. Choice between: system, user.'
                example: user
                enum:
                  - user
                  - system
                  - assistant
              content:
                oneOf:
                  - type: string
                    description: The contents of the message.
                    example: >-
                      What opportunities and challenges will the Chinese large
                      model industry face in 2025?
            required:
              - role
              - content
          minItems: 1
          maxItems: 10
        system:
          allOf:
            - anyOf:
                - type: string
                - items:
                    $ref: '#/components/schemas/RequestTextBlock'
                  type: array
              description: >-
                System prompt.


                A system prompt is a way of providing context and instructions
                to llm, such as specifying a particular goal or role. 
              title: System
        stop_sequences:
          allOf:
            - description: >-
                Custom text sequences that will cause the model to stop
                generating.


                Our models will normally stop when they have naturally completed
                their turn, which will result in a response `stop_reason` of
                `"end_turn"`.


                If you want the model to stop generating when it encounters
                custom strings of text, you can use the `stop_sequences`
                parameter. If the model encounters one of the custom sequences,
                the response `stop_reason` value will be `"stop_sequence"` and
                the response `stop_sequence` value will contain the matched stop
                sequence.
              items:
                type: string
              title: Stop Sequences
              type: array
        stream:
          type: boolean
          description: >-
            If set, tokens are returned as Server-Sent Events as they are made
            available. Stream terminates with `data: [DONE]`
          example: true
        max_tokens:
          type: integer
          description: >-
            The maximum number of tokens to generate before stopping.


            Note that our models may stop _before_ reaching this maximum. This
            parameter only specifies the absolute maximum number of tokens to
            generate.


            Different models have different maximum values for this parameter. 
            See
            [models](https://docs.siliconflow.com/cn/userguide/capabilities/text-generation)
            for details.
          example: 8192
        temperature:
          type: number
          description: Determines the degree of randomness in the response.
          format: float
          example: 0.7
          maximum: 2
          minimum: 0
        top_p:
          type: number
          description: >-
            The `top_p` (nucleus) parameter is used to dynamically adjust the
            number of choices for each predicted token based on the cumulative
            probabilities.
          format: float
          example: 0.7
          minimum: 0.1
          maximum: 1
        top_k:
          type: number
          format: float
          example: 50
          minimum: 0
          maximum: 50
        tools:
          type: array
          description: |
            Each tool definition includes:


              * `name`: Name of the tool.

              * `description`: Optional, but strongly-recommended
              description of the tool.

              * `input_schema`: [JSON
              schema](https://json-schema.org/draft/2020-12) for the
              tool `input` shape that the model will produce in
              `tool_use` output content blocks.
          items:
            $ref: '#/components/schemas/MessagesTool'
        tool_choice:
          allOf:
            - description: >-
                How the model should use the provided tools. The model can use a
                specific tool, any available tool, decide by itself, or not use
                tools at all.
              discriminator:
                mapping:
                  auto:
                    $ref: '#/components/schemas/ToolChoiceAuto'
                  none:
                    $ref: '#/components/schemas/ToolChoiceNone'
                  tool:
                    $ref: '#/components/schemas/ToolChoiceTool'
                propertyName: type
              oneOf:
                - $ref: '#/components/schemas/ToolChoiceAuto'
                - $ref: '#/components/schemas/ToolChoiceTool'
                - $ref: '#/components/schemas/ToolChoiceNone'
    MessgesResponse:
      type: object
      properties:
        id:
          type: string
        type:
          default: message
          description: |-
            Object type.

            For Messages, this is always `"message"`.
          enum:
            - message
          title: Type
          type: string
        role:
          default: assistant
          description: |-
            Conversational role of the generated message.

            This will always be `"assistant"`.
          enum:
            - assistant
          title: Role
          type: string
        content:
          description: >-
            Content generated by the model.


            This is an array of content blocks, each of which has a `type` that
            determines its shape.


            Example:


            ```json

            [{"type": "text", "text": "Hi"}]

            ```


            If the request input `messages` ended with an `assistant` turn, then
            the response `content` will continue directly from that last turn.
            You can use this to constrain the model's output.


            For example, if the input `messages` were:

            ```json

            [
              {"role": "user", "content": "What's the Greek name for Sun? (A) Sol (B) Helios (C) Sun"},
              {"role": "assistant", "content": "The best answer is ("}
            ]

            ```


            Then the response `content` might be:


            ```json

            [{"type": "text", "text": "B)"}]

            ```
          items:
            oneOf:
              - $ref: '#/components/schemas/ResponseToolUseBlock'
          title: Content
          type: array
        model:
          description: The model that handled the request.
          title: Model
          type: string
        stop_reason:
          anyOf:
            - enum:
                - end_turn
                - max_tokens
                - tool_use
                - refusal
              type: string
          description: >-
            The reason that we stopped.


            This may be one the following values:

            * `"end_turn"`: the model reached a natural stopping point or one of
            your provided custom `stop_sequences` was generated

            * `"max_tokens"`: we exceeded the requested `max_tokens` or the
            model's maximum

            * `"tool_use"`: the model invoked one or more tools

            * `"refusal"`: when streaming classifiers intervene to handle
            potential policy violations


            In non-streaming mode this value is always non-null. In streaming
            mode, it is null in the `message_start` event and non-null
            otherwise.
          title: Stop Reason
        stop_sequence:
          anyOf:
            - type: string
          default: null
          description: >-
            Which custom stop sequence was generated, if any.


            This value will be a non-null string if one of your custom stop
            sequences was generated.
          title: Stop Sequence
        usage:
          allOf:
            - $ref: '#/components/schemas/Usage'
              description: Billing and rate-limit usage.
              examples:
                - input_tokens: 2095
                  output_tokens: 503
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
    RequestTextBlock:
      additionalProperties: false
      properties:
        text:
          minLength: 1
          title: Text
          type: string
        type:
          enum:
            - text
          title: Type
          type: string
      required:
        - text
        - type
      title: Text
      type: object
    MessagesTool:
      type: object
      properties:
        name:
          description: >-
            Name of the tool.


            This is how the tool will be called by the model and in `tool_use`
            blocks.
          title: Name
          type: string
        input_schema:
          $ref: '#/components/schemas/InputSchema'
          description: >-
            [JSON schema](https://json-schema.org/draft/2020-12) for this tool's
            input.

            This defines the shape of the `input` that your tool accepts and
            that the model will produce.
      required:
        - name
        - input_schema
    ToolChoiceAuto:
      additionalProperties: false
      description: The model will automatically decide whether to use tools.
      properties:
        disable_parallel_tool_use:
          description: >-
            Whether to disable parallel tool use.


            Defaults to `false`. If set to `true`, the model will output at most
            one tool use.
          title: Disable Parallel Tool Use
          type: boolean
        type:
          enum:
            - auto
          title: Type
          type: string
      required:
        - type
      title: Auto
      type: object
    ToolChoiceNone:
      additionalProperties: false
      description: The model will not be allowed to use tools.
      properties:
        type:
          enum:
            - none
          title: Type
          type: string
      required:
        - type
      title: None
      type: object
    ToolChoiceTool:
      additionalProperties: false
      description: The model will use the specified tool with `tool_choice.name`.
      properties:
        disable_parallel_tool_use:
          description: >-
            Whether to disable parallel tool use.


            Defaults to `false`. If set to `true`, the model will output exactly
            one tool use.
          title: Disable Parallel Tool Use
          type: boolean
        name:
          description: The name of the tool to use.
          title: Name
          type: string
        type:
          enum:
            - tool
          title: Type
          type: string
      required:
        - name
        - type
      title: Tool
      type: object
    ResponseToolUseBlock:
      properties:
        id:
          title: Id
          type: string
        input:
          title: Input
          type: object
        name:
          minLength: 1
          title: Name
          type: string
        type:
          default: tool_use
          enum:
            - tool_use
          title: Type
          type: string
      required:
        - id
        - input
        - name
        - type
      title: Tool use
      type: object
    Usage:
      properties:
        input_tokens:
          description: The number of input tokens which were used.
          minimum: 0
          title: Input Tokens
          type: integer
        output_tokens:
          description: The number of output tokens which were used.
          minimum: 0
          title: Output Tokens
          type: integer
      required:
        - input_tokens
        - output_tokens
      title: Usage
      type: object
    InputSchema:
      type: object
      properties:
        properties:
          anyOf:
            - type: object
          title: Properties
        required:
          anyOf:
            - items:
                type: string
              type: array
          title: Required
        type:
          enum:
            - object
          title: Type
          type: string
      required:
        - type
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: your api key
      description: >-
        Use the following format for authentication: Bearer [<your api
        key>](https://cloud.siliconflow.com/account/ak)
    apiKey:
      type: apiKey
      in: header
      name: x-api-key
      description: >-
        Use the following format for authentication: [<your api
        key>](https://cloud.siliconflow.com/account/ak)

````

Built with [Mintlify](https://mintlify.com).