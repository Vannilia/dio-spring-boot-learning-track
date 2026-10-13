# API Inteligente com Spring Boot e Spring AI: assistente de orçamento por voz

Desafio de Projeto DIO. Projeto base: [dio-spring-boot-learning-track](https://github.com/digitalinnovationone/dio-spring-boot-learning-track/blob/main/05-spring-ai/README.md)

## O que o projeto faz
É uma API de orçamento financeiro que entende comandos de voz. A pessoa envia um arquivo de áudio (por exemplo, "gastei 30 reais no mercado"), e a aplicação:

1. transforma o áudio em texto (transcrição);
2. usa uma IA para entender o que a pessoa quer e escolher uma função da aplicação (Tool Calling);
3. executa essa função de verdade: registra uma transação ou consulta os gastos salvos no banco de dados;
4. responde em voz, devolvendo um áudio MP3 com o resultado.

As transações têm descrição, valor (guardado em centavos) e categoria (mercado, farmácia ou carro). A API também funciona por texto, sem IA, com os endereços `POST /transactions` (criar) e `GET /transactions/{categoria}` (listar). A IA e o REST usam os **mesmos casos de uso**, então as regras da aplicação ficam num lugar só.

## Tecnologias
- Java 25, Spring Boot 4, Spring AI 2.0.0-M4
- OpenAI: gpt-4o-mini (chat), whisper-1 (transcrição), gpt-4o-mini-tts (voz)
- MySQL 9.6 via Docker Compose, Spring Data JPA
- Gradle, JUnit 5

## Como executar
1. Instale o JDK 25 e o Docker Desktop (aberto).
2. Defina a chave da OpenAI como variável de ambiente (nunca no código):
   - PowerShell: `$env:OPENAI_API_KEY="sua_chave"`
3. Na pasta `05-spring-ai`: `./gradlew bootRun`

## Melhoria implementada
Criei uma nova ferramenta de Tool Calling, a `sum-expenses-by-category`, que soma os gastos de uma categoria. Com ela, dá para perguntar por voz "quanto gastei no mercado?" e a IA responde com o total.

- **Onde está:** classe `SumExpensesByCategoryUseCase`, na camada `application`.
- **Como funciona:** o método `execute` recebe uma categoria (`@ToolParam`), busca as transações dela no repositório e soma os valores. O resultado volta em centavos, e a descrição do `@Tool` avisa isso para a IA converter para reais ao responder.
- **Como a IA a enxerga:** registrei a classe no `TransactionController`, no `defaultTools(...)`, ao lado das duas ferramentas que já existiam.
- **Como verifiquei:** por voz, com "gastei 30 reais no mercado", "gastei 20 reais no mercado" e "quanto gastei no mercado?", a resposta foi "50 reais". O log da aplicação mostra a chamada de `sum-expenses-by-category`. Também escrevi dois testes automatizados, que não usam a OpenAI.

**Por que escolhi esta melhoria:** escolhi criar uma nova ferramenta porque ela repete, com o meu próprio código, o padrão central do projeto (um caso de uso com `@Tool` e `@ToolParam`, registrado no controller). Também dava para testar a maior parte sem gastar crédito da OpenAI.

## Como testar o fluxo principal
Enviei áudios `.m4a` ao endpoint `POST /transactions/ai` (campo `file`):

| O que falei | O que a IA respondeu |
|---|---|
| "gastei 30 reais no mercado" | A transação no mercado foi registrada com sucesso na categoria de compras  |
| "gastei 20 reais no mercado" | Confirmou que a transação de 20 reais no mercado foi registrada |
| "quanto gastei no mercado?" | "Você gastou 50 reais no mercado" |

Exemplo de requisição:
`curl.exe -X POST http://localhost:8080/transactions/ai -F "file=@gastei.m4a" -o resposta.mp3`

Conferir o que foi salvo: `curl.exe http://localhost:8080/transactions/GROCERIES`

Testes automatizados (sem usar a OpenAI):
`./gradlew test --tests "*SumExpensesByCategoryUseCaseTest"`

## O que aprendi
- **A IA e o REST usam o mesmo caso de uso.** No `TransactionController`, o mesmo `PersistTransactionUseCase` é chamado pelo endereço REST e entregue à IA em `defaultTools(...)`. Por isso a regra da aplicação fica num lugar só, e a IA não tem um caminho paralelo.
- **A descrição do `@ToolParam` orienta a IA.** O campo `amount` diz "valor do gasto (em centavos)", e foi por isso que "30 reais" foi salvo como 3000. Escrevi a descrição da minha ferramenta avisando que o total volta em centavos.
- **A IA pode errar e se corrigir.** Em uma chamada, ela não preencheu o valor da transação e a aplicação deu erro; logo depois ela chamou a ferramenta de novo e salvou certo. Isso mostra por que as descrições e a validação importam.
- **O motivo de um erro está no log.** O erro 500 que recebi na resposta não dizia nada; a causa (formato de áudio inválido) apareceu no terminal da aplicação.
- **O formato do áudio importa.** Um arquivo `.mp3` que na verdade era de outro formato foi recusado pela OpenAI. Funcionou com `.m4a` gravado no Gravador de Voz do Windows.
- **O Gradle só enxerga o que foi salvo.** Tive um BUILD SUCCESSFUL com código não salvo (o pontinho branco na aba avisa isso) e só depois de salvar o resultado valeu.
- **Preparar o ambiente também é parte do trabalho.** Precisei instalar o JDK 25 (eu tinha o 21) e o Docker Desktop, que exigiu ativar a Virtual Machine Platform e instalar o WSL.
- **Testar sem a IA é possível.** Escrevi dois testes com um repositório de mentira em memória, que conferem a soma sem chamar a OpenAI e sem gastar crédito.
- **Próxima melhoria que eu faria:** hoje nada impede salvar um valor zero ou negativo. Eu validaria isso antes de salvar a transação.