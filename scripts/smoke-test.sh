#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# Roteiro de teste manual do CampusGigs.
# Requer a API no ar (docker compose up) e o comando curl.
#
#   ./scripts/smoke-test.sh
#
# Cobre: cadastro, login, publicacao, contratacao, regras de papel e erros.
# ---------------------------------------------------------------------------
set -uo pipefail

BASE="${BASE:-http://localhost:8080}"
SUFIXO="$(date +%s)"
EMAIL_ANA="ana.${SUFIXO}@campusgigs.com"
EMAIL_BRUNO="bruno.${SUFIXO}@campusgigs.com"
SENHA="senha12345"

verde()   { printf '\033[0;32m%s\033[0m\n' "$1"; }
vermelho(){ printf '\033[0;31m%s\033[0m\n' "$1"; }
titulo()  { printf '\n\033[1;36m== %s\033[0m\n' "$1"; }

# executa: descricao | status esperado | argumentos do curl
executa() {
  local descricao="$1"; local esperado="$2"; shift 2
  local resposta status corpo
  resposta=$(curl -s -w '\n%{http_code}' "$@")
  status=$(printf '%s' "$resposta" | tail -n1)
  corpo=$(printf '%s' "$resposta" | sed '$d')

  if [ "$status" = "$esperado" ]; then
    verde   "  [OK]    $descricao -> $status"
  else
    vermelho "  [FALHA] $descricao -> esperado $esperado, recebido $status"
    vermelho "          $corpo"
  fi
  ULTIMO_CORPO="$corpo"
}

extrai() { printf '%s' "$1" | grep -oE "\"$2\":\"?[^,\"}]*" | head -n1 | sed -E "s/\"$2\":\"?//"; }

titulo "1. Cadastro (CP2)"
executa "cadastrar Ana com CEP (dispara o ViaCEP)" 201 \
  -X POST "$BASE/auth/registrar" -H 'Content-Type: application/json' \
  -d "{\"nome\":\"Ana Souza\",\"email\":\"$EMAIL_ANA\",\"senha\":\"$SENHA\",\"cep\":\"01310-100\"}"
echo "          endereco resolvido: $(printf '%s' "$ULTIMO_CORPO" | tr -d ' \n' | sed -n 's/.*"endereco":{\(.*\)}.*/\1/p')"

executa "cadastrar Bruno" 201 \
  -X POST "$BASE/auth/registrar" -H 'Content-Type: application/json' \
  -d "{\"nome\":\"Bruno Lima\",\"email\":\"$EMAIL_BRUNO\",\"senha\":\"$SENHA\"}"

executa "cadastrar e-mail repetido deve falhar" 409 \
  -X POST "$BASE/auth/registrar" -H 'Content-Type: application/json' \
  -d "{\"nome\":\"Ana Clone\",\"email\":\"$EMAIL_ANA\",\"senha\":\"$SENHA\"}"

executa "cadastrar com senha curta deve falhar na validacao" 400 \
  -X POST "$BASE/auth/registrar" -H 'Content-Type: application/json' \
  -d '{"nome":"X","email":"x@campusgigs.com","senha":"123"}'

executa "cadastrar com CEP inexistente deve falhar (ViaCEP)" 400 \
  -X POST "$BASE/auth/registrar" -H 'Content-Type: application/json' \
  -d '{"nome":"Cep Ruim","email":"cep.ruim@campusgigs.com","senha":"senha12345","cep":"99999999"}'

titulo "2. Login e emissao do token (CP3)"
executa "login da Ana" 200 \
  -X POST "$BASE/auth/login" -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL_ANA\",\"senha\":\"$SENHA\"}"
TOKEN_ANA=$(extrai "$ULTIMO_CORPO" token)

executa "login do Bruno" 200 \
  -X POST "$BASE/auth/login" -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL_BRUNO\",\"senha\":\"$SENHA\"}"
TOKEN_BRUNO=$(extrai "$ULTIMO_CORPO" token)

executa "login do ADMIN (seed da migration V2)" 200 \
  -X POST "$BASE/auth/login" -H 'Content-Type: application/json' \
  -d '{"email":"admin@campusgigs.com","senha":"admin123"}'
TOKEN_ADMIN=$(extrai "$ULTIMO_CORPO" token)

executa "login com senha errada" 401 \
  -X POST "$BASE/auth/login" -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL_ANA\",\"senha\":\"senha-errada\"}"

titulo "3. Token obrigatorio nas operacoes de escrita (CP3)"
executa "publicar SEM token" 401 \
  -X POST "$BASE/servicos" -H 'Content-Type: application/json' \
  -d '{"titulo":"Teste","descricao":"Sem token","categoria":"Geral","preco":10}'

executa "publicar com token INVALIDO" 401 \
  -X POST "$BASE/servicos" -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer token.completamente.invalido' \
  -d '{"titulo":"Teste","descricao":"Token falso","categoria":"Geral","preco":10}'

titulo "4. Publicacao e listagem"
executa "Ana publica um freela" 201 \
  -X POST "$BASE/servicos" -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN_ANA" \
  -d '{"titulo":"Monitoria de Java","descricao":"Aulas de POO e Spring Boot","categoria":"Educacao","preco":80.00}'
SERVICO_ANA=$(extrai "$ULTIMO_CORPO" id)

executa "Bruno publica um freela" 201 \
  -X POST "$BASE/servicos" -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN_BRUNO" \
  -d '{"titulo":"Criacao de logo","descricao":"Identidade visual para projetos","categoria":"Design","preco":150.00}'
SERVICO_BRUNO=$(extrai "$ULTIMO_CORPO" id)

executa "listagem publica de servicos ativos" 200 "$BASE/servicos?situacao=ATIVO"

titulo "5. Autorizacao por titularidade (CP4)"
executa "Bruno tenta EDITAR o servico da Ana -> 403" 403 \
  -X PUT "$BASE/servicos/$SERVICO_ANA" -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN_BRUNO" \
  -d '{"titulo":"Invadido","descricao":"Nao deveria passar","categoria":"Educacao","preco":1.00}'

executa "Ana edita o PROPRIO servico -> 200" 200 \
  -X PUT "$BASE/servicos/$SERVICO_ANA" -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN_ANA" \
  -d '{"titulo":"Monitoria de Java e Spring","descricao":"Aulas de POO, JPA e Spring Boot","categoria":"Educacao","preco":95.00}'

titulo "6. ACESSO NEGADO POR PAPEL (CP4) <<< evidencia pedida na entrega"
executa "USER acessando area ADMIN -> 403" 403 \
  "$BASE/admin/usuarios" -H "Authorization: Bearer $TOKEN_ANA"
echo "          corpo: $ULTIMO_CORPO"

executa "ADMIN acessando a mesma rota -> 200" 200 \
  "$BASE/admin/usuarios" -H "Authorization: Bearer $TOKEN_ADMIN"

titulo "7. Contratacao e regras de negocio"
executa "Ana tenta contratar o PROPRIO servico -> 403" 403 \
  -X POST "$BASE/contratacoes" -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN_ANA" -d "{\"servicoId\":$SERVICO_ANA}"

executa "Bruno contrata o servico da Ana -> 201" 201 \
  -X POST "$BASE/contratacoes" -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN_BRUNO" -d "{\"servicoId\":$SERVICO_ANA}"
CONTRATACAO=$(extrai "$ULTIMO_CORPO" id)

executa "Bruno contrata o mesmo servico de novo -> 409" 409 \
  -X POST "$BASE/contratacoes" -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN_BRUNO" -d "{\"servicoId\":$SERVICO_ANA}"

executa "Bruno (contratante) tenta ACEITAR -> 403" 403 \
  -X PATCH "$BASE/contratacoes/$CONTRATACAO/aceitar" -H "Authorization: Bearer $TOKEN_BRUNO"

executa "Ana (prestadora) aceita -> 200" 200 \
  -X PATCH "$BASE/contratacoes/$CONTRATACAO/aceitar" -H "Authorization: Bearer $TOKEN_ANA"

executa "Ana conclui -> 200" 200 \
  -X PATCH "$BASE/contratacoes/$CONTRATACAO/concluir" -H "Authorization: Bearer $TOKEN_ANA"

executa "cancelar contratacao ja concluida -> 409" 409 \
  -X PATCH "$BASE/contratacoes/$CONTRATACAO/cancelar" -H "Authorization: Bearer $TOKEN_ANA"

titulo "8. Servico nao ativo nao pode ser contratado"
executa "Bruno pausa o proprio servico" 200 \
  -X PATCH "$BASE/servicos/$SERVICO_BRUNO/pausar" -H "Authorization: Bearer $TOKEN_BRUNO"

executa "Ana tenta contratar servico PAUSADO -> 409" 409 \
  -X POST "$BASE/contratacoes" -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN_ANA" -d "{\"servicoId\":$SERVICO_BRUNO}"

titulo "9. ADMIN encerra servico de terceiro (CP4)"
executa "Ana tenta ENCERRAR o servico do Bruno -> 403" 403 \
  -X PATCH "$BASE/servicos/$SERVICO_BRUNO/encerrar" -H "Authorization: Bearer $TOKEN_ANA"

executa "ADMIN encerra o servico do Bruno -> 200" 200 \
  -X PATCH "$BASE/servicos/$SERVICO_BRUNO/encerrar" -H "Authorization: Bearer $TOKEN_ADMIN"

titulo "10. Integracao ViaCEP (CP5)"
executa "Ana atualiza o CEP -> 200" 200 \
  -X PUT "$BASE/usuarios/me/cep" -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN_ANA" -d '{"cep":"20040-020"}'
echo "          endereco: $(printf '%s' "$ULTIMO_CORPO" | tr -d ' \n' | sed -n 's/.*"endereco":{\(.*\)}.*/\1/p')"

executa "CEP inexistente -> 400" 400 \
  -X PUT "$BASE/usuarios/me/cep" -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN_ANA" -d '{"cep":"99999999"}'

executa "recurso inexistente -> 404" 404 \
  "$BASE/servicos/99999999"

printf '\n'
verde "Roteiro concluido."
