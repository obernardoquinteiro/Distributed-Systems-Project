# Relatório do projeto *Bicloin*

Sistemas Distribuídos 2020-2021, segundo semestre

## Autores
 
**Grupo T04**

![Bernardo Quinteiro](https://imgur.com/9WMds62.png) ![Diogo Lopes](https://imgur.com/euXpRys.png) ![Gonçalo Mateus](https://imgur.com/HX6ulbs.png)


| Número | Nome              | Utilizador                                   | Correio eletrónico                  |
| -------|-------------------|----------------------------------------------| ------------------------------------|
| 93692  | Bernardo Quinteiro| <https://git.rnl.tecnico.ulisboa.pt/ist193692> | <mailto:bernardo.quinteiro@tecnico.ulisboa.pt>   |
| 93700  | Diogo Lopes       | <https://git.rnl.tecnico.ulisboa.pt/ist193700> | <mailto:diogo.andre.fulgencio.lopes@tecnico.ulisboa.pt>     |
| 93713  | Gonçalo Mateus    | <https://git.rnl.tecnico.ulisboa.pt/ist193713> | <mailto:goncalo.filipe.mateus@tecnico.ulisboa.pt> |


## Melhorias da primeira parte

- [Atualizações à App](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/T04-Bicloin/commit/0e2fd446c2ad6a0b52422bc74d282092a3bd1e01)
  (Foram removidas algumas chamadas a funções que serviam para verificar o estado dos hubs na primeira entrega
   do projeto, contudo deixou de vir a ser necessário)
  

- [Sys_Status e Sincronização](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/T04-Bicloin/commit/a3d38dd5e2252bb75c29b02b61ba1a70c7a39dbf)
  (Foi implementado o comando sys_status no hub e adicionadas as sincronizações, ambos em falta na primeira entrega)
  

- [Inicialização do Hub e Rec simplificada](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/T04-Bicloin/commit/c194e24b6f25dad0a6b7be53d1e4240c346984ec)
  (Reduzido o número de argumentos necessários para as inicializações dos servidores)
  

- [Correção da leitura do input na App](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/T04-Bicloin/commit/7ec72ded2fd139aa1fc14bc1f97f722262b5056e)
  (Após o comando *zzz* a prompt ">" não aparecia)
  

- [Correção de testes do Rec](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/T04-Bicloin/commit/4508493906b643c7a79ebfa0de50e0a11e8e5426)
  (Correção e otimização de alguns testes que falhavam no RecTester)

## Modelo de faltas

Uma **falta** é um acontecimento que altera o padrão normal de funcionamento de
uma dada componente do sistema e é a causadora de um **erro**. Este **erro** pode ser processado, permitindo assim
a continuação do serviço, ou então pode ocorrer uma **falha**, o que torna impossível fornecer o serviço correto.

Assim sendo, podemos classificar a falta de ausência de um servidor *rec* como tendo uma **origem interna**, uma vez
que é uma falta num programa. Relativamente à duração, esta pode ser permanente se o servidor for desligado, pois
este so voltaria a fornecer serviços quando ligado novamente, ou então uma falta temporária, pois pode ocorrer
um pequeno erro que acaba por ser reparado, sem necessária intervenção. No que diz respeito ao determinismo,
seriam faltas não-determinísticas, uma vez que dependem de fatores não previsíveis, como threads ou
ordem de entrega de mensagens.

No modelo de faltas é necessário identificar quais as faltas expectáveis e, em seguida, decidir quais as **faltas** 
que devem ser **toleradas** ou **não toleradas** (à relação entre estas dá-se o nome de **taxa de cobertura**). Assim,
podemos ter dois tipos de faltas: 
- as **faltas bizantinas** são o pior caso possível e envolvem por exemplo a resposta
do servidor mesmo quando não há estímulos ou com conteúdo corrompido, sendo neste caso faltas que **não são 
toleradas**; 
  

- as **faltas silenciosas** são as que acontecem com maior probabilidade, por exemplo uma mensagem 
não ser entregue. Estas **são toleradas**.
  

## Solução

![Photo](https://imgur.com/2sSvnG8.png)

Em primeiro lugar, o cliente envia um pedido. Este vai ser enviado para todos os servidores **rec** que existem 
no *Zookeeper*. Um **Response Collector** vai coletar as respostas até ser atingido um *quórum* de respostas 
((numero de servidores / 2) + 1), sendo de seguida retornada a resposta mais atualizada para o cliente. 

Esta solução permite que as **faltas silenciosas** sejam toleradas, uma vez que, se um **rec** estiver offline, ou
demorar demasiado tempo a responder, a sua resposta não vai ser necessária, pois as respostas de um **quórum** de
outros servidores são suficientes para garantir que chega ao cliente a informação mais atualizada, sem que este se 
aperceba da ocorrência de alguma falta.

## Protocolo de replicação

A **replicação** traz vários benefícios, como uma melhor disponibilidade, isto é, o sistema mantém-se pronto a prestar 
serviço mesmo quando algum servidor falha ou se torna inacessível (tolerância a faltas), para além do facto de proporcionar
um melhor desempenho e escalabilidade, ou seja, os clientes podem aceder às réplicas mais próximas de si e o facto de se 
adicionar mais utilizadores não degrada muito o desempenho, uma vez que a carga é distribuída.

No entanto, aquando a utilização de réplicas tem que se ter em atenção alguns aspetos, como a **transparência de replicação**, 
ou seja, o utilizador deve ter a ilusão de estar a aceder a um único objeto lógico, bem como a importância da coerência, de
modo a que a resposta ao utilizador tenha sempre a informação mais atualizada.

Através da informação descrita acima, decidimos optar por um **protocolo de registo distribuído coerente completo (sem 
writeback)**. Para que isto fosse possível, a cada registo foram atribuídos um **seq** (versão do registo) e um **cid** 
(no caso do projeto é sempre 1, pois existe um único hub). Estes são utilizados para se decidir qual a resposta a 
retornar ao cliente. Existem 2 processos:

- **read:** quando o **Response Collector** já contém um quórum de respostas, são comparados os **seqs** de cada registo 
e retornado ao cliente o que possuir o valor mais elevado, ou seja, o que representa a versão mais atualizada do registo. Em
caso de empate do valor do **seq** é utilizado o **cid** para desempatar.
  

- **write:** é executado um **read** para se obter o registo mais atualizado e é calculado o novo valor. De seguida, 
de forma a se manter a **coerência** entre todas as réplicas, é realizado um **write** em todos os servidores com o novo 
valor atualizado e o valor de **seq** associado ao registo é incrementado. Ou seja, após cada **write** todas as réplicas 
disponíveis passam a estar atualizadas em relação ao registo em questão, o que permite manter a coerência e, em caso de falta de 
um ou mais servidores, permite com que um **quórum** de respostas seja suficiente para garantir que o cliente recebe a resposta 
mais atualizada.

## Medições de desempenho

**Antes de Otimizações**

|initRec|balance|info station|top-up|
|:----:|:----:|:----:|:----:|
|11.60783|0.11878|0.12375|0.24385|
|11.42504|0.11386|0.12219|0.24460|
|11.41326|0.12393|0.12419|0.25633|


**Depois de Otimizações**

|initRec|balance|info station|top-up|
|:----:|:----:|:----:|:----:|
|1.15967|0.00595|0.00644|0.00958|
|1.09550|0.00490|0.00793|0.01041|
|1.12531|0.00545|0.00652|0.00985|


Ambas as tabelas representam a execução dos mesmos comandos, contudo a primeira são valores obtidos com uma versão muito 
simplificada e a segunda é após algumas otimizações.

A escolha dos comandos a analisar não foi aleatória, passamos a explicar o porquê de cada coluna:

- **initRec:** representa o tempo que demora a inicializar os valores em todos os servidores **rec** 
  (foram utilizados 4 réplicas para cada tabela)
  

- **balance:** este comando foi selecionado, uma vez que representa a execução da mais simples instrução **read**


- **info station:** tal como o **balance**, este comando também é apenas exemplificativo da instrução **read**, contudo 
neste caso são efetuados múltiplos **reads**, uma vez que é necessário saber o *número de bicicletas, levantamentos e 
devoluções* da estação em questão, daí ter um tempo de execução superior ao da instrução **balance**
  

- **top-up:** o comando top-up foi selecionado, pois representa a execução do comando **write**. Tal como já foi visto 
anteriormente, quando se faz um **write**, primeiramente é realizado um **read**, de forma a se obter o registo mais 
atualizado e, de seguida, é enviado um **write** com o novo valor para todas as réplicas de modo a se manter uma coerência 
entre servidores

## Opções de implementação

Para um projeto como este seriam várias as otimizações que se poderiam efetuar. Em primeiro lugar, na versão mais 
simplificada, existiam ciclos com declarações/inicializações de variáveis, como *arrays*, o que tornava a execução do
programa bastante mais lento. Esta foi a principal causa da diferença entre as medições apresentadas no ponto anterior, 
onde se pode verificar uma diferença de cerca de 10 segundos aquando a inicialização das réplicas (**initRec**). Para além disto,
na versão mais simples do projeto, não era utilizado o **quórum**, ou seja, as respostas vindas dos servidores **rec** eram 
todas analisadas e era retornada a mais atualizada para o cliente, enquanto que na versão mais recente, a partir do 
momento que o **Response Collector** possui um **quórum** de respostas, estas podem ser logo analisadas e retornada a mais 
atualizada, não sendo necessário ficar à espera das respostas das restantes réplicas. No caso de poucas réplicas, esta 
implementação não se mostra significativa nas medições dos tempos de execução, contudo se estivéssemos a falar de um conjunto
de 100 servidores, faz, de facto, diferença esperar pela resposta dos 100 ou apenas de 51 (**quórum**), sabendo que estes 
seriam suficientes para retornar ao cliente a informação mais atualizada.


Para além das otimizações referidas acima, ainda seria possível implementar **pesos**. Isto é, dar uma certa prioridade a 
determinadas instruções ou réplicas de modo a tornar a execução dos comandos ainda mais rápida, contudo a tentativa desta implementação 
estava a causar uns quantos problemas e, visto que estamos apertados de tempo para esta entrega, decidimos não a incluir 
na solução para evitar problemas inesperados durante a execução do programa. Esta implementação é bastante importante quando 
aplicada a projetos de grande escala, uma vez que permite ao cliente se conectar a servidores mais próximos de si, ou que 
possuam menos tráfego, o que torna a resposta ao cliente mais rápida e garante escalabilidade.

## Notas finais

Infelizmente não conseguimos otimizar o projeto ao máximo, devido a incompatibilidades de tempo, mas estudámos as diferentes 
otimizações possíveis e foram explicadas ao longo do relatório.


Ao contrário da ideia assimilada na última entrega, nós adotámos uma metodologia, para a resolução
do 2ª enunciado, em que todas as tarefas foram realizadas, em conjunto, pelos 3 membros do grupo,
pelo que, perante esta situação, não achámos objetivo que se possa atribuir tarefas, em específico,
a um elemento do grupo, em particular. Portanto, nesta entrega, não fizemos nenhuma identificação,
para cada diretoria, de um *lead developer* e contribuintes, dado o facto de todos terem tido uma
participação equivalente no desenvolvimento de cada uma das pastas criadas/editadas, para esta
entrega.