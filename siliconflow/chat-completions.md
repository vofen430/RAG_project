> ## Documentation Index
> Fetch the complete documentation index at: https://docs.siliconflow.com/llms.txt
> Use this file to discover all available pages before exploring further.

# Chat completions

> Creates a model response for the given chat conversation.



## OpenAPI

````yaml post /chat/completions
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
  /chat/completions:
    post:
      tags:
        - Chat Completions
      summary: Chat Completions
      description: Creates a model response for the given chat conversation.
      operationId: chat-completions
      requestBody:
        content:
          application/json:
            schema:
              oneOf:
                - $ref: '#/components/schemas/ChatCompletionRequest'
                - $ref: '#/components/schemas/ChatCompletionVLMRequest'
      responses:
        '200':
          description: '200'
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ChatCompletionResponse'
            text/event-stream:
              schema:
                $ref: '#/components/schemas/ChatCompletionStream'
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
    ChatCompletionRequest:
      title: LLM
      type: object
      required:
        - model
        - messages
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
          example: Qwen/QwQ-32B
          enum:
            - deepseek-ai/DeepSeek-R1
            - deepseek-ai/DeepSeek-R1-Distill-Qwen-14B
            - deepseek-ai/DeepSeek-R1-Distill-Qwen-32B
            - deepseek-ai/DeepSeek-V3
            - deepseek-ai/DeepSeek-V3.1
            - deepseek-ai/DeepSeek-V3.1-Terminus
            - deepseek-ai/DeepSeek-V3.2-Exp
            - deepseek-ai/DeepSeek-V3.2
            - deepseek-ai/deepseek-vl2
            - nex-agi/DeepSeek-V3.1-Nex-N1
            - baidu/ERNIE-4.5-300B-A47B
            - THUDM/GLM-4-32B-0414
            - THUDM/GLM-4-9B-0414
            - zai-org/GLM-4.5
            - zai-org/GLM-4.5-Air
            - zai-org/GLM-4.5V
            - zai-org/GLM-5
            - zai-org/GLM-4.7
            - zai-org/GLM-4.6
            - zai-org/GLM-4.6V
            - THUDM/GLM-Z1-32B-0414
            - THUDM/GLM-Z1-9B-0414
            - tencent/Hunyuan-A13B-Instruct
            - tencent/Hunyuan-MT-7B
            - moonshotai/Kimi-K2.5
            - moonshotai/Kimi-K2-Instruct
            - moonshotai/Kimi-K2-Instruct-0905
            - moonshotai/Kimi-K2-Thinking
            - inclusionAI/Ling-flash-2.0
            - inclusionAI/Ling-mini-2.0
            - inclusionAI/Ring-flash-2.0
            - meta-llama/Meta-Llama-3.1-8B-Instruct
            - MiniMaxAI/MiniMax-M2.5
            - MiniMaxAI/MiniMax-M2.1
            - Qwen/QwQ-32B
            - Qwen/Qwen2.5-14B-Instruct
            - Qwen/Qwen2.5-32B-Instruct
            - Qwen/Qwen2.5-72B-Instruct
            - Qwen/Qwen2.5-72B-Instruct-128K
            - Qwen/Qwen2.5-7B-Instruct
            - Qwen/Qwen2.5-Coder-32B-Instruct
            - Qwen/Qwen2.5-VL-32B-Instruct
            - Qwen/Qwen2.5-VL-72B-Instruct
            - Qwen/Qwen2.5-VL-7B-Instruct
            - Qwen/Qwen3-14B
            - Qwen/Qwen3-235B-A22B
            - Qwen/Qwen3-235B-A22B-Instruct-2507
            - Qwen/Qwen3-235B-A22B-Thinking-2507
            - Qwen/Qwen3-30B-A3B-Instruct-2507
            - Qwen/Qwen3-30B-A3B-Thinking-2507
            - Qwen/Qwen3-32B
            - Qwen/Qwen3-8B
            - Qwen/Qwen3-Coder-30B-A3B-Instruct
            - Qwen/Qwen3-Coder-480B-A35B-Instruct
            - Qwen/Qwen3-Next-80B-A3B-Instruct
            - Qwen/Qwen3-Next-80B-A3B-Thinking
            - Qwen/Qwen3-Omni-30B-A3B-Captioner
            - Qwen/Qwen3-Omni-30B-A3B-Instruct
            - Qwen/Qwen3-Omni-30B-A3B-Thinking
            - ByteDance-Seed/Seed-OSS-36B-Instruct
            - openai/gpt-oss-120b
            - openai/gpt-oss-20b
        messages:
          type: array
          description: A list of messages comprising the conversation so far.
          items:
            type: object
            properties:
              role:
                type: string
                description: >-
                  The role of the messages author. Choice between: system, user,
                  or assistant.
                example: user
                default: user
                enum:
                  - user
                  - assistant
                  - system
              content:
                oneOf:
                  - type: string
                    description: The contents of the message.
                    example: >-
                      What opportunities and challenges will the Chinese large
                      model industry face in 2025?
                    default: >-
                      What opportunities and challenges will the Chinese large
                      model industry face in 2025?
            required:
              - role
              - content
          minItems: 1
          maxItems: 10
        stream:
          type: boolean
          description: >-
            If set, tokens are returned as Server-Sent Events as they are made
            available. Stream terminates with `data: [DONE]`
          example: false
        max_tokens:
          type: integer
          description: >
            The maximum number of tokens to generate. Ensure that input tokens +
            max_tokens do not exceed the model’s context window. As some
            services are still being updated, avoid setting max_tokens to the
            window’s upper bound; reserve ~10k tokens as buffer for input and
            system overhead. See Models(https://cloud.siliconflow.cn/models) for
            details. 
          example: 4096
        enable_thinking:
          type: boolean
          description: >
            Switches between thinking and non-thinking modes. Default is True. 
            This field supports the following models: 

                - Qwen/Qwen3-8B
                - Qwen/Qwen3-14B
                - Qwen/Qwen3-32B
                - wen/Qwen3-30B-A3B
                - Qwen/Qwen3-235B-A22B
                - tencent/Hunyuan-A13B-Instruct
                - zai-org/GLM-4.6V
                - zai-org/GLM-4.5V
                - deepseek-ai/DeepSeek-V3.1
                - deepseek-ai/DeepSeek-V3.1-Terminus
                - deepseek-ai/DeepSeek-V3.2-Exp
                - deepseek-ai/DeepSeek-V3.2

            If you want to use the function call feature for
            deepseek-ai/DeepSeek-V3.1, you need to set enable_thinking to
            false. 
          example: false
        thinking_budget:
          type: integer
          description: >-
            Maximum number of tokens for chain-of-thought output. This field
            applies to all Reasoning models.
          example: 4096
          default: 4096
          minimum: 128
          maximum: 32768
        min_p:
          type: number
          description: >-
            Dynamic filtering threshold that adapts based on token
            probabilities.This field only applies to Qwen3.
          format: float
          example: 0.05
          minimum: 0
          maximum: 1
        stop:
          description: >
            Up to 4 sequences where the API will stop generating further tokens.
            The returned text will not contain the stop sequence.
          nullable: true
          oneOf:
            - type: string
              example: null
              nullable: true
            - type: array
              minItems: 1
              maxItems: 4
              items:
                type: string
                example: 'null'
        temperature:
          type: number
          description: Determines the degree of randomness in the response.
          format: float
          example: 0.7
        top_p:
          type: number
          description: >-
            The `top_p` (nucleus) parameter is used to dynamically adjust the
            number of choices for each predicted token based on the cumulative
            probabilities.
          format: float
          example: 0.7
          default: 0.7
        top_k:
          type: number
          format: float
          example: 50
        frequency_penalty:
          type: number
          format: float
          example: 0.5
        'n':
          type: integer
          description: Number of generations to return
          example: 1
        response_format:
          type: object
          description: An object specifying the format that the model must output.
          properties:
            type:
              type: string
              description: The type of the response format.
              example: text
        tools:
          type: array
          description: >
            A list of tools the model may call. Currently, only functions are
            supported as a tool. Use this to provide a list of functions the
            model may generate JSON inputs for. A max of 128 functions are
            supported.
          items:
            $ref: '#/components/schemas/ChatCompletionTool'
    ChatCompletionVLMRequest:
      title: VLM
      type: object
      required:
        - model
        - messages
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
          example: Qwen2.5-VL-32B-Instruct
          default: Qwen2.5-VL-32B-Instruct
          enum:
            - Qwen/Qwen3-VL-32B-Instruct
            - Qwen/Qwen3-VL-32B-Thinking
            - Qwen/Qwen3-VL-8B-Instruct
            - Qwen/Qwen3-VL-8B-Thinking
            - Qwen/Qwen3-VL-235B-A22B-Instruct
            - Qwen/Qwen3-VL-235B-A22B-Thinking
            - Qwen/Qwen3-VL-30B-A3B-Thinking
            - Qwen/Qwen3-VL-30B-A3B-Instruct
            - deepseek-ai/deepseek-vl2
            - zai-org/GLM-4.6V
            - zai-org/GLM-4.5V
            - Qwen/Qwen2.5-VL-32B-Instruct
            - Qwen/Qwen2.5-VL-72B-Instruct
            - Qwen/Qwen2.5-VL-7B-Instruct
            - Qwen/Qwen3-Omni-30B-A3B-Captioner
            - Qwen/Qwen3-Omni-30B-A3B-Instruct
            - Qwen/Qwen3-Omni-30B-A3B-Thinking
        messages:
          type: array
          description: A list of messages comprising the conversation so far.
          items:
            type: object
            properties:
              role:
                type: string
                description: >-
                  The role of the messages author. Choice between: system, user,
                  or assistant.
                example: user
                default: user
                enum:
                  - user
                  - assistant
                  - system
              content:
                oneOf:
                  - type: array
                    description: >-
                      An array of content parts with a defined type, each can be
                      of type `text` or `image_url` when passing in images. You
                      can pass multiple images by adding multiple `image_url`
                      content parts. The Qwen3-Omni series supports `video_url`
                      and `audio_url`, enabling the recognition of video and
                      audio content. The Qwen3-VL model also supports
                      `video_url`, allowing it to recognize video content.
                      Recommend videos and audio within 30 seconds.
                    items:
                      $ref: >-
                        #/components/schemas/ChatCompletionRequestUserMessageContentPart
                    minItems: 1
            required:
              - role
              - content
          minItems: 1
          maxItems: 10
        stream:
          type: boolean
          description: >-
            If set, tokens are returned as Server-Sent Events as they are made
            available. Stream terminates with `data: [DONE]`
          example: false
          default: false
        max_tokens:
          type: integer
          description: >
            The maximum number of tokens to generate. Ensure that input tokens +
            max_tokens do not exceed the model’s context window. As some
            services are still being updated, avoid setting max_tokens to the
            window’s upper bound; reserve ~10k tokens as buffer for input and
            system overhead. See Models(https://cloud.siliconflow.cn/models) for
            details. 
        stop:
          description: >
            Up to 4 sequences where the API will stop generating further tokens.
            The returned text will not contain the stop sequence.
          default: []
          nullable: true
          oneOf:
            - type: array
              minItems: 1
              maxItems: 4
              items:
                type: string
                example: 'null'
            - type: string
              default: <|endoftext|>
              example: |+

              nullable: true
            - type: string
              default: <|endoftext|>
              example: ''
              nullable: true
        temperature:
          type: number
          description: Determines the degree of randomness in the response.
          format: float
          example: 0.7
          default: 0.7
        top_p:
          type: number
          description: >-
            The `top_p` (nucleus) parameter is used to dynamically adjust the
            number of choices for each predicted token based on the cumulative
            probabilities.
          format: float
          example: 0.7
          default: 0.7
        top_k:
          type: number
          format: float
          example: 50
          default: 50
        frequency_penalty:
          type: number
          format: float
          example: 0.5
          default: 0.5
        'n':
          type: integer
          description: Number of generations to return
          example: 1
          default: 1
        response_format:
          type: object
          description: An object specifying the format that the model must output.
          properties:
            type:
              type: string
              description: The type of the response format.
              example: text
    ChatCompletionResponse:
      type: object
      properties:
        id:
          type: string
        choices:
          $ref: '#/components/schemas/ChatCompletionChoicesData'
        usage:
          $ref: '#/components/schemas/UsageData'
        created:
          type: integer
        model:
          type: string
        object:
          type: string
          enum:
            - chat.completion
    ChatCompletionStream:
      type: object
      properties:
        id:
          type: string
        choices:
          $ref: '#/components/schemas/ChatCompletionChoicesData'
        created:
          type: integer
        model:
          type: string
        object:
          type: string
          enum:
            - chat.completion.chunk
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
    ChatCompletionTool:
      type: object
      properties:
        type:
          type: string
          enum:
            - function
          description: The type of the tool. Currently, only `function` is supported.
        function:
          $ref: '#/components/schemas/FunctionObject'
      required:
        - type
        - function
    ChatCompletionRequestUserMessageContentPart:
      oneOf:
        - $ref: '#/components/schemas/ChatCompletionRequestMessageContentPartImage'
        - $ref: '#/components/schemas/ChatCompletionRequestMessageContentPartText'
      x-oaiExpandable: true
    ChatCompletionChoicesData:
      type: array
      items:
        type: object
        properties:
          message:
            type: object
            properties:
              role:
                type: string
                example: assistant
              content:
                type: string
              reasoning_content:
                description: >-
                  Only the deepseek-R1 series and Qwen/QwQ-32B models support
                  reasoning_content. This part returns the reasoning content,
                  which is at the same level as the content. In each round of
                  the conversation, the model outputs the reasoning chain
                  content (reasoning_content) and the final answer (content). In
                  the next round of the conversation, the reasoning chain
                  content from previous rounds will not be appended to the
                  context.
                type: string
              tool_calls:
                type: array
                description: The tool calls generated by the model, such as function calls.
                items:
                  $ref: '#/components/schemas/ChatCompletionMessageToolCall'
          finish_reason:
            $ref: '#/components/schemas/FinishReason'
    UsageData:
      type: object
      properties:
        prompt_tokens:
          type: integer
        completion_tokens:
          type: integer
        total_tokens:
          type: integer
    FunctionObject:
      type: object
      properties:
        description:
          type: string
          description: >-
            A description of what the function does, used by the model to choose
            when and how to call the function.
        name:
          type: string
          description: >-
            The name of the function to be called. Must be a-z, A-Z, 0-9, or
            contain underscores and dashes, with a maximum length of 64.
        parameters:
          $ref: '#/components/schemas/FunctionParameters'
        strict:
          type: boolean
          nullable: true
          default: false
          description: >-
            Whether to enable strict schema adherence when generating the
            function call. If set to true, the model will follow the exact
            schema defined in the `parameters` field. Only a subset of JSON
            Schema is supported when `strict` is `true`. Learn more about
            Structured Outputs in the [function calling
            guide](docs/guides/function-calling).
      required:
        - name
    ChatCompletionRequestMessageContentPartImage:
      type: object
      title: Image content part
      properties:
        type:
          type: string
          enum:
            - image_url
          description: The type of the content part.
          default: image_url
        image_url:
          type: object
          properties:
            url:
              type: string
              description: >-
                Either a URL of the image or the base64 encoded image data.
                TeleAI/TeleMM only support the base64 encoded image data.
              default: >-
                https://sf-maas.s3.us-east-1.amazonaws.com/images/recu6XreBFQ0st.png
              example: >-
                https://sf-maas.s3.us-east-1.amazonaws.com/images/recu6XreBFQ0st.png
            detail:
              type: string
              description: Specifies the detail level of the image.
              enum:
                - auto
                - low
                - high
              default: auto
          required:
            - url
      required:
        - type
        - image_url
    ChatCompletionRequestMessageContentPartText:
      type: object
      title: Text content part
      properties:
        type:
          type: string
          enum:
            - text
          description: The type of the content part.
          default: text
        text:
          type: string
          description: The text content.
          default: Describe this picture.
      required:
        - type
        - text
    ChatCompletionMessageToolCall:
      type: object
      properties:
        id:
          type: string
          description: The ID of the tool call.
        type:
          type: string
          enum:
            - function
          description: The type of the tool. Currently, only `function` is supported.
        function:
          type: object
          description: The function that the model called.
          properties:
            name:
              type: string
              description: The name of the function to call.
            arguments:
              type: string
              description: >-
                The arguments to call the function with, as generated by the
                model in JSON format. Note that the model does not always
                generate valid JSON, and may hallucinate parameters not defined
                by your function schema. Validate the arguments in your code
                before calling your function.
          required:
            - name
            - arguments
      required:
        - id
        - type
        - function
    FinishReason:
      type: string
      enum:
        - stop
        - eos
        - length
        - tool_calls
    FunctionParameters:
      type: object
      description: >-
        The parameters the functions accepts, described as a JSON Schema object.
        See the [guide](/guides/function_calling) for examples, and the [JSON
        Schema reference](https://json-schema.org/understanding-json-schema/)
        for documentation about the format. 


        Omitting `parameters` defines a function with an empty parameter list.
      additionalProperties: true
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