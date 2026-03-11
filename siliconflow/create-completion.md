> ## Documentation Index
> Fetch the complete documentation index at: https://docs.siliconflow.com/llms.txt
> Use this file to discover all available pages before exploring further.

# Create Completion

> Query a language, code, or image model.



## OpenAPI

````yaml post /completions
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
  /completions:
    post:
      tags:
        - Completion
      summary: Create completion
      description: Query a language, code, or image model.
      operationId: completions
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CompletionRequest'
      responses:
        '200':
          description: '200'
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CompletionResponse'
            text/event-stream:
              schema:
                $ref: '#/components/schemas/CompletionStream'
        '400':
          description: BadRequest
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorData'
        '401':
          description: Unauthorized
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorData'
        '404':
          description: NotFound
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorData'
        '429':
          description: RateLimit
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorData'
        '503':
          description: Overloaded
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorData'
        '504':
          description: Timeout
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorData'
      deprecated: false
components:
  schemas:
    CompletionRequest:
      type: object
      required:
        - model
        - prompt
      properties:
        prompt:
          type: string
          description: A string providing context for the model to complete.
          example: <s>[INST] What is the capital of Germany? [/INST]
        model:
          type: string
          description: >
            The name of the model to query [See all of SiliconFlow's chat
            models](https://docs.siliconflow.com/api-reference/models/get-model-list)
          example: deepseek-ai/DeepSeek-R1
          default: deepseek-ai/DeepSeek-R1
          enum:
            - tencent/Hunyuan-MT-7B
            - Qwen/Qwen3-Next-80B-A3B-Thinking
            - Qwen/Qwen3-Next-80B-A3B-Instruct
            - inclusionAI/Ring-flash-2.0
            - inclusionAI/Ling-mini-2.0
            - inclusionAI/Ling-flash-2.0
            - ByteDance-Seed/Seed-OSS-36B-Instruct
            - openai/gpt-oss-120b
            - openai/gpt-oss-20b
            - MiniMaxAI/MiniMax-M2.5
            - MiniMaxAI/MiniMax-M2.1
            - Qwen/QwQ-32B
            - Qwen/Qwen2.5-14B-Instruct
            - Qwen/Qwen2.5-32B-Instruct
            - Qwen/Qwen2.5-72B-Instruct
            - Qwen/Qwen2.5-72B-Instruct-128K
            - Qwen/Qwen2.5-7B-Instruct
            - Qwen/Qwen2.5-Coder-32B-Instruct
            - Qwen/Qwen3-14B
            - Qwen/Qwen3-235B-A22B
            - Qwen/Qwen3-235B-A22B-Instruct-2507
            - Qwen/Qwen3-235B-A22B-Thinking-2507
            - Qwen/Qwen3-30B-A3B-Instruct-2507
            - Qwen/Qwen3-30B-A3B-Thinking-2507
            - Qwen/Qwen3-32B
            - Qwen/Qwen3-8B
            - Qwen/Qwen3-Coder-30B-A3B-Instruct
            - THUDM/GLM-4-32B-0414
            - THUDM/GLM-4-9B-0414
            - THUDM/GLM-Z1-32B-0414
            - THUDM/GLM-Z1-9B-0414
            - baidu/ERNIE-4.5-300B-A47B
            - deepseek-ai/DeepSeek-R1
            - deepseek-ai/DeepSeek-R1-Distill-Qwen-14B
            - deepseek-ai/DeepSeek-R1-Distill-Qwen-32B
            - deepseek-ai/DeepSeek-V3.2-Exp
            - deepseek-ai/DeepSeek-V3.2
            - deepseek-ai/DeepSeek-V3.1-Terminus
            - deepseek-ai/DeepSeek-V3.1
            - deepseek-ai/DeepSeek-V3
            - nex-agi/DeepSeek-V3.1-Nex-N1
            - meta-llama/Meta-Llama-3.1-8B-Instruct
            - moonshotai/Kimi-K2-Instruct-0905
            - moonshotai/Kimi-K2-Instruct
            - moonshotai/Kimi-K2-Thinking
            - tencent/Hunyuan-A13B-Instruct
            - zai-org/GLM-5
            - zai-org/GLM-4.7
            - zai-org/GLM-4.6
            - zai-org/GLM-4.5
        max_tokens:
          type: integer
          description: The maximum number of tokens to generate.
        stop:
          type: array
          description: >-
            A list of string sequences that will truncate (stop) inference text
            output. For example, "</s>" will stop generation as soon as the
            model generates the given token.
          items:
            type: string
        temperature:
          type: number
          description: >-
            A decimal number from 0-1 that determines the degree of randomness
            in the response. A temperature less than 1 favors more correctness
            and is appropriate for question answering or summarization. A value
            closer to 1 introduces more randomness in the output.
          format: float
        top_p:
          type: number
          description: >-
            A percentage (also called the nucleus parameter) that's used to
            dynamically adjust the number of choices for each predicted token
            based on the cumulative probabilities. It specifies a probability
            threshold below which all less likely tokens are filtered out. This
            technique helps maintain diversity and generate more fluent and
            natural-sounding text.
          format: float
        top_k:
          type: integer
          description: >-
            An integer that's used to limit the number of choices for the next
            predicted word or token. It specifies the maximum number of tokens
            to consider at each step, based on their probability of occurrence.
            This technique helps to speed up the generation process and can
            improve the quality of the generated text by focusing on the most
            likely options.
          format: int32
        repetition_penalty:
          type: number
          description: >-
            A number that controls the diversity of generated text by reducing
            the likelihood of repeated sequences. Higher values decrease
            repetition.
          format: float
        stream:
          type: boolean
          description: >-
            If true, stream tokens as Server-Sent Events as the model generates
            them instead of waiting for the full model response. The stream
            terminates with `data: [DONE]`. If false, return a single JSON
            object containing the results.
        'n':
          type: integer
          description: The number of completions to generate for each prompt.
          minimum: 1
          maximum: 128
        presence_penalty:
          type: number
          description: >-
            A number between -2.0 and 2.0 where a positive value increases the
            likelihood of a model talking about new topics.
          format: float
        frequency_penalty:
          type: number
          description: >-
            A number between -2.0 and 2.0 where a positive value decreases the
            likelihood of repeating tokens that have already been mentioned.
          format: float
        logit_bias:
          type: object
          additionalProperties:
            type: number
            format: float
          description: >-
            Adjusts the likelihood of specific tokens appearing in the generated
            output.
          example:
            '105': 21.4
            '1024': -10.5
        seed:
          type: integer
          description: >-
            If specified, the system will make its best effort to perform
            deterministic sampling, so repeated requests with the same seed and
            parameters should return the same results. Determinism is not
            guaranteed to be implemented; the system_fingerprint response
            parameter should be referenced to monitor backend changes.
          example: 42
    CompletionResponse:
      type: object
      properties:
        id:
          type: string
        choices:
          $ref: '#/components/schemas/CompletionChoicesData'
        prompt:
          $ref: '#/components/schemas/PromptPart'
        usage:
          $ref: '#/components/schemas/UsageData'
        created:
          type: integer
        model:
          type: string
        object:
          type: string
          enum:
            - text_completion
      required:
        - id
        - choices
        - usage
        - created
        - model
        - object
    CompletionStream:
      oneOf:
        - $ref: '#/components/schemas/CompletionEvent'
    ErrorData:
      type: object
      required:
        - error
      properties:
        error:
          type: object
          properties:
            message:
              type: string
              nullable: false
            type:
              type: string
              nullable: false
            param:
              type: string
              nullable: true
              default: null
            code:
              type: string
              nullable: true
              default: null
          required:
            - type
            - message
            - param
            - code
    CompletionChoicesData:
      type: array
      items:
        type: object
        properties:
          text:
            type: string
          finish_reason:
            $ref: '#/components/schemas/FinishReason'
          logprobs:
            allOf:
              - $ref: '#/components/schemas/LogprobsPart'
              - nullable: true
    PromptPart:
      type: array
      items:
        type: object
        properties:
          text:
            type: string
            example: <s>[INST] What is the capital of France? [/INST]
            default: <s>[INST] What is the capital of France? [/INST]
          logprobs:
            $ref: '#/components/schemas/LogprobsPart'
    UsageData:
      type: object
      properties:
        prompt_tokens:
          type: integer
        completion_tokens:
          type: integer
        total_tokens:
          type: integer
    CompletionEvent:
      type: object
      required:
        - data
      properties:
        data:
          $ref: '#/components/schemas/CompletionChunk'
    FinishReason:
      type: string
      enum:
        - stop
        - eos
        - length
        - tool_calls
    LogprobsPart:
      type: object
      properties:
        tokens:
          type: array
          items:
            type: string
          description: List of token strings
        token_logprobs:
          type: array
          items:
            type: number
            format: float
          description: List of token log probabilities
    CompletionChunk:
      type: object
      required:
        - id
        - token
        - choices
        - usage
        - finish_reason
      properties:
        id:
          type: string
        token:
          $ref: '#/components/schemas/CompletionToken'
        choices:
          title: CompletionChoices
          type: array
          items:
            $ref: '#/components/schemas/CompletionChoice'
        tool_calls:
          type: array
          items:
            $ref: '#/components/schemas/ChatCompletionMessageToolCallChunk'
        usage:
          allOf:
            - $ref: '#/components/schemas/UsageData'
            - nullable: true
        finish_reason:
          allOf:
            - $ref: '#/components/schemas/FinishReason'
            - nullable: true
    CompletionToken:
      type: object
      required:
        - id
        - text
        - logprob
        - special
      properties:
        id:
          type: integer
        text:
          type: string
        logprob:
          type: number
          format: float
        special:
          type: boolean
    CompletionChoice:
      type: object
      required:
        - index
      properties:
        text:
          type: string
    ChatCompletionMessageToolCallChunk:
      type: object
      properties:
        index:
          type: integer
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
        - index
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