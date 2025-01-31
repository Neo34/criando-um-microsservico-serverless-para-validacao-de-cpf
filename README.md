Este projeto tem como objetivo desenvolver um microsserviço eficiente, escalável e econômico para validação de CPFs, utilizando arquitetura serverless. A aplicação será construída com base em princípios modernos de computação em nuvem, garantindo alta disponibilidade, baixo custo operacional e facilidade de manutenção.

Explicação do Código:
Azure Function:

O ponto de entrada é configurado com o endpoint /api/validateCpf.
Espera uma requisição HTTP POST com um JSON no formato { "cpf": "123.456.789-09" }.
Validação do CPF:

Utiliza uma expressão regular para validar o formato.
Inclui lógica de checksum para verificar a validade matemática do CPF.
Retorno:

200 OK para CPFs válidos.
400 Bad Request para CPFs inválidos.
