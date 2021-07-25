# Guião de Demonstração

Grupo T04 - Bernardo Quinteiro 93692, Diogo Lopes 93700, Gonçalo Mateus 93713

## 1. Preparação do sistema

Para testar o sistema e todos os seus componentes, é necessário preparar um ambiente com dados para proceder à verificação dos testes.

### 1.1. Lançar o *registry*

Para lançar o *ZooKeeper*, ir à pasta `zookeeper/bin` e correr o comando  
`./zkServer.sh start` (Linux) ou `zkServer.cmd` (Windows).

É possível também lançar a consola de interação com o *ZooKeeper*, novamente na pasta `zookeeper/bin` e correr `./zkCli.sh` (Linux) ou `zkCli.cmd` (Windows).

### 1.2. Compilar o projeto

Primeiramente, é necessário compilar e instalar todos os módulos e suas dependências --  *rec*, *hub*, *app*, etc.
Para isso, basta ir à pasta *root* do projeto e correr o seguinte comando:

```sh
$ mvn clean install -DskipTests
```

### 1.3. Lançar e testar o *rec*

Para proceder aos testes, é preciso em primeiro lugar lançar o servidor *rec*.
Para isso basta ir à pasta *rec* e executar:

```sh
$ mvn compile exec:java -Dexec.args="8091 1"
```


Este comando vai colocar o *rec* no endereço *localhost* e na porta *8091*.

Neste caso vamos utilizar 3 *recs*, por isso falta inicializar mais 2, o que se pode fazer
da seguinte form:

```sh
$ mvn compile exec:java -Dexec.args="8092 2"
```

```sh
$ mvn compile exec:java -Dexec.args="8093 3"
```

E assim sucessivamente para mais réplicas, não esquecendo que os *portos* não se podem repetir e
a instância de cada réplica também tem que ser diferente para cada *rec*.

Assim temos 3 *recs* ativos no endereço *localhost*, nas portas *8091*, *8092* e *8093*, com as instâncias 1, 2 e 3, respetivamente.

Para confirmar o funcionamento do servidor com um *ping*, fazer:

```sh
$ cd rec-tester
$ mvn compile exec:java
```

Para executar toda a bateria de testes de integração, fazer:

```sh
$ mvn verify
```

Todos os testes devem ser executados sem erros.


### 1.4. Lançar e testar o *hub*

De seguida, é necessário lançar o servidor *hub*.
O servidor *hub* deve ser inicializado utilizando o argumento *initRec*, de modo a
que os servidores *rec* se populem com as informações mutáveis dos utilizadores e estações
presentes nos ficheiros *users.csv* e *stations.csv*, respetivamente, que se encontram dentro
da diretoria *demo*.

```sh
$ mvn compile exec:java -Dexec.args="8095 1 users.csv stations.csv initRec"
```


Este comando vai colocar o *hub* no endereço *localhost* e na porta *8095*.

Para confirmar o funcionamento do servidor com um *ping*, fazer:

```sh
$ cd hub-tester
$ mvn compile exec:java
```

Para executar toda a bateria de testes de integração, fazer:

```sh
$ mvn verify
```

Todos os testes devem ser executados sem erros.


### 1.5. *App*

Para se iniciar a aplicação é necessário estar dentro da diretoria
app/target/appassembler/bin. Aqui existe um ficheiro *app* que se pode inicializar
com o utilizador *alice* através do seguinte comando:

```sh
$ app localhost 2181 alice +35191102030 38.7380 -9.3000
```

**Nota:** Para poder correr o script *app* diretamente é necessário fazer `mvn install` e adicionar ao *PATH* ou utilizar diretamente os executáveis gerados na pasta `target/appassembler/bin/`.

Depois de lançar todos os componentes, tal como descrito acima, já temos o que é necessário para usar o sistema através dos comandos.

## 2. Teste dos comandos

Nesta secção vamos correr os comandos necessários para testar todas as operações do sistema.
Cada subsecção é respetiva a cada operação presente no *hub*. Vamos utilizar como base o
utilizador que iniciaria a app com o comando:

**Nota:** Por esta altura é necessário ter um hub (localhost 8095) e 3 recs (localhost 8091,
localhost 8092, localhost 8093).

```sh
$ app localhost 2181 alice +35191102030 38.7380 -9.3000
```

Para correr um ficheiro de input em vez de colocar os comandos individualmente, pode-se inicializar a app da seguinte forma:
```sh
$ app localhost 2181 alice +35191102030 38.7380 -9.3000 < ../demo/test1.txt
```

### 2.1. *balance*

O comando *balance* retorna o saldo disponível em BIC do utilizador:

```sh
> balance
alice 0 BIC
```

### 2.2 *top-up*

O comando *top-up* carrega um certo valor no saldo da aplicação do utilizador. O valor introduzido
é em euros e é convertido para BIC:

```sh
> top-up 10
alice 100 BIC
```

Caso o valor a depositar não seja um inteiro entre 1 e 20 é retornado um erro:

```sh
> top-up 25
ERRO saldo a submeter deve estar entre 1 e 20.
```

### 2.3. *scan*

O comando *scan* retorna informação sobre as *k* estações mais próximas do utilizador:

```sh
> scan 3
istt, lat 38.7372, -9.3023 long, 20 docas, 4 BIC premio, 12 bicicletas, a 242 metros.
stao, lat 38.6867, -9.3124 long, 30 docas, 3 BIC premio, 20 bicicletas, a 5832 metros.
jero, lat 38.6972, -9.2064 long, 30 docas, 3 BIC premio, 20 bicicletas, a 10252 metros.
```
Se o *k* introduzido for um valor menor ou igual a zero é retornado um erro:

```sh
> scan -1
ERRO valor de K invalido.
```

### 2.4. *tag*

O comando *tag* permite a criação de uma tag nas coordenadas introduzidas. Deste modo, permite
ao utilizador deslocar-se de forma mais simples:

```sh
> tag 38.7372 -9.3023 tagus
OK
```

### 2.5. *move*

O comando *move* permite ao utilizador se deslocar. Este comando pode ser introduzido com duas coordenadas:

```sh
> move 38.7372 -9.3023
alice em https://www.google.com/maps/place/38.7372,-9.3023
```

ou também pode ser utilizado para deslocar o utilizador para uma tag criada anteriormente:

```sh
> move tagus
alice em https://www.google.com/maps/place/38.7372,-9.3023
```

### 2.6. *at*

O comando *at* devolve a localização do utilizador num link do *Google Maps*:

```sh
> at
alice em https://www.google.com/maps/place/38.7380,-9.3000
```
### 2.7. *bike-up*

O comando *bike-up* permite ao utilizador alugar uma bicicleta na
estação escolhida. Isto tem o custo de 10 BIC:

```sh
> bike-up istt
OK
```

Se a estação introduzida não existir é retornado um erro:

```sh
> bike-up aaa
ERRO estacao nao existe.
```

Se o utilizador se encontrar a uma distância superior a 200 metros da estação
indicada é retornado um erro:

```sh
> bike-up ocea
ERRO fora de alcance.
```
Se o utilizador já possuir uma bicicleta ou não tiver no mínimo 10 BIC de saldo (custo de alugar uma
bicicleta) é retornado um erro:

```sh
> bike-up istt
ERRO impossivel alugar bike, saldo indisponivel ou ja possui atualmente uma bicicleta.
```


### 2.8. *info*

O comando *info* retorna informação sobre a estação introduzida:

```sh
> info istt
IST Taguspark, lat 38.7372, -9.3023 long, 20 docas, 4 BIC premio, 11 bicicletas, 1 levantamentos, 0 devolucoes, https://www.google.com/maps/place/38.7372,-9.3023
```

Caso a estação não exista é retornado um erro:

```sh
> info aaa
ERRO estacao nao existe.
```

### 2.9. *bike-down*

O comando *bike-down* permite ao utilizador devolver uma bicicleta na
estação escolhida. Se a devolução for executada, o utilizador receberá um prémio em BIC, que varia
de estação para estação:

```sh
> bike-down istt
OK
```

Se a estação introduzida não existir é retornado um erro:

```sh
> bike-down aaa
ERRO estacao nao existe.
```

Se o utilizador se encontrar a uma distância superior a 200 metros da estação
indicada é retornado um erro:

```sh
> bike-down ocea
ERRO fora de alcance.
```

Se o utilizador não possuir uma bicicleta é retornado um erro:

```sh
> bike-down istt
ERRO nao ha bicicleta para devolver.
```


### 2.10. *ping*

O comando *ping* retorna uma resposta do servidor:

```sh
> ping
Pong.
```

### 2.11. *sys_status*

O comando *sys_status* retorna o estado dos servidores que existem no *Zookeeper* :

```sh
> sys_status
Hub Server: localhost:8095 OK.
Rec Server: localhost:8091 OK.
Rec Server: localhost:8092 OK.
Rec Server: localhost:8093 OK.
```

### 2.12. *zzz*

O comando *zzz* faz um sleep de *k* milissegundos :

```sh
> zzz 3000
Sleep 3000 milissegundos.
```

### 2.13. *help*

O comando *help* retorna todos os comandos possíveis na app e uma breve explicação de como os executar:

```sh
> help
Estes sao os comandos disponiveis:
- balance       (retorna o balance do utilizador)
- top-up X      (acrescenta X em BIC na conta do utilizador)
- info X        (mostra informacao sobre a estacao X)
- scan X        (mostra as X estacoes mais proximas do utilizador)
- bike-up X     (levanta uma bicicleta da estacao X)
- bike-down X   (devolve uma bicileta na estacao X)
- at            (apresenta um link do google maps com as coordenadas do utilizador)
- tag X Y Z     (cria uma tag nas coordenadas (X,Y) com o nome Z)
- move X        (move o utilizador para a tag com nome X)
- move X Y      (move o utilizador para as coordenadas (X,Y))
- sys_status    (mostra os servidores que existem e se estao UP ou DOWN)
- ping          (retorna uma mensagem Pong. do servidor)
- quit          (fecha a aplicacao)
```

### 2.14. *quit*

O comando *quit* fecha a aplicação:

```sh
> quit
Exiting the app.
```

## 3. Replicação e Tolerância a Faltas

Na secção **1.3. Lançar e testar o *rec*** já foi explicado como inciar vários recs em simultâneo no endereço *localhost* 
e portas 8091, 8092 e 8093. 


De forma a testar a tolerância a faltas, enquanto um user está na *app*, pode-se fechar uma réplica através do
signal *Ctrl+C*. Por exemplo, se fecharmos a réplica do porto 8092 e depois executarmos na app o comando *sys_status* 
o output deve ser o seguinte:

```sh
> sys_status
Hub Server: localhost:8095 OK.
Rec Server: localhost:8091 OK.
Rec Server: localhost:8092 DOWN.
Rec Server: localhost:8093 OK.
```

A falta de uma réplica não interfere na resposta que chega ao cliente, pois é possível continuar a executar
comandos na *App* e o cliente obterá sempre a resposta mais atualizada, sem se aperceber da ausência do servidor,
daí a tolerância a **faltas silenciosas**.

----

## 4. Considerações Finais

Estes testes não cobrem tudo, pelo que devem ter sempre em conta os testes de integração e o código.