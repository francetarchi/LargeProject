# LargeProject

Questo file contiene delle linee guida per la scrittura del codice del progetto, in modo da renderlo più uniforme possibile nelle parti scritte da persone diverse.  
Il contenuto di questo file dovrà essere eliminato prima della consegna.  



## ```@Autowired``` (!!!! ATTENZIONE !!!!, leggere tutto questo paragrafo)
L'annotazione ```@Autowired``` è usata in tutti i progetti vecchi che ho visto e viene inserita anche da Geppetto ogni volta che scrive una funzione. Non è estremamente necessaria ma fortemente consigliata.  
In pratica, quando in una classe (ad es. ```EntityService```) etichetto il campo ```private EntityRepository entityRepository``` con ```@Autowired```, sto dicendo a Spring che la classe ```EntityService``` dipende da ```EntityRepository```, in modo che Spring inizializzi automaticamente il campo ```entityRepository``` alla creazione di un'istanza della classe ```EntityService```.  
Praticamente genera una specie di dipendenza a catena gestita da SpringBoot in cui ogni classe dipende da quella di livello inferiore.  
### Come usarlo
Il concetto è lo stesso spiegato, ma invece che etichettare in una classe direttamente i campi della classe "di livello inferiore", è consigliato scrivere il costruttore della classe in cui sono e etichettare quello con ```@Autowired```, per vari motivi che non ho voglia di scrivere ma sono facilmente reperibili sul Veb.  

Scrivere quindi una cosa come sotto.  
```java
@Autowired  
public ReviewService(ReviewRepository reviewRepository) {  
    this.reviewRepository = reviewRepository;  
}
```

### Piccolo spoiler: possiamo NON utilizzarlo
Il fatto è che noi usiamo le annotazioni ```@RequiredArgsConstructor``` e ```@AllArgsConstructor```.  
* ```@RequiredArgsConstructor``` genera automaticamente per la classe annotata un costruttore che inizializza solamente i campi ```final``` e ```@NonNull```.  
* ```@AllArgsConstructor``` genera automaticamente per la classe annotata un costruttore che inizializza TUTTI i campi.  
L'utilizzo di queste due annotazioni in tutte le classi genera la catena discendente di dipendenze che dovremmo generare a mano scrivendo i costruttori e annotandoli con ```@Autowired```.  

Inoltre, esiste anche l'annotazione ```@NoArgsConstructor```: genera automaticamente per la classe annotata un costruttore che non inizializza NESSUN campo. Alle volte, viene usato in combinata con ```@AllArgsConstructor``` per generare il costruttore che non inizializza nulla e quello che inizializza tutto.  
  
In generale, credo sia meglio usare ```@RequiredArgsConstructor``` per le classi **Controller** e **Service**, in quanto necessario per l'iniezione delle dipendenze.  
Al contrario, credo sia meglio usare ```@NoArgsConstructor``` & ```@AllArgsConstructor``` per la classe **Model**, in quanto l'obiettivo per questa classe è avere classi semplici che rappresentino i dati e non abbiamo bisogno di iniettare dipendenze.  
La **Repository**, invece, non ha bisogno di niente perché è una _interface_ (le suddette annotazioni si possono applicare solo a _class_ e _enum_).  



## Formato scrittura risposte
Usare ```ResponseEntity.status(HttpStatus.STATO).body("Messaggio")``` come sintassi per il settaggio della risposta HTTP.  
NON le funzioni ```.ok()```, ```.created()```, ```.badRequest()```, ...



## ```@PathVariable```
Usare ```@PathVariable``` per passare in una richiesta HTTP il valore del campo che identifica univocamente una risorsa all'interno della sua collection (ad es. l'```_id``` per un vino o una recensione o l'```username``` per un utente).  
L'importante è separare questi campi da quelli che contengono informazioni: questi vanno al contrario inseriti nel body della richiesta HTTP (o come parametri annotati con ```@RequestParam```).



## CONTROLLI: usare il seguente schema per organizzare i controlli che vengono fatti sugli input (cose passate nelle richieste HTTP sia come body che come parametri o path variable)
**ATTENZIONE:** le seguenti regole sono corrette, ma leggere anche il paragrafo riguardo le annotazioni per i controlli sui parametri delle richieste HTTP (3/4 paragrafi più avanti) per sapere come fare certi controlli, al posto di metterli nel _controller_ o nel _service_.
NEL CONTROLLER:
* controlli sulla presenza dei dati (controllare che i dati obbligatori non siano null) **CON ANNOTAZIONI**
* controlli sul pattern degli input **CON ANNOTAZIONI**
* controlli sul ruolo degli utenti (controllare che un utente possa accedere all'API richiesta)

NEL SERVICE:
* controlli sulla validità dell'id passato (ad es. controllare che non esista già un utente con l'username richiesto durante la creazione di un nuovo utente)
* controlli sull'integrità dei dati (ad es. controllare che esista l'utente con lo username richiesto, o la recensione con l'id passato come argomento)
* controlli sulla coerenza dei dati (ad es. controllare che la data di nascita sia passata) **CON ANNOTAZIONI**
* controlli sulla proprietà di una risorsa (controllare che l'utente che ha effettuato la richiesta, ovvero quello loggato, sia effettivamente il proprietario della risorsa che intende modificare/eliminare)

### Perché?
Secondo Geppetto: "Il controller dovrebbe occuparsi principalmente della gestione delle richieste HTTP e della serializzazione/deserializzazione dei dati. Aggiungere la logica di autorizzazione può appesantire il controller e violare il principio di responsabilità singola.".  

Sempre secondo Geppetto: "Il controller ha accesso diretto all'utente autenticato (tramite ```@Principal``` o ```@AuthenticationPrincipal```) e all'ID della risorsa (```@PathVariable```). Questo rende la verifica relativamente semplice da implementare.". Quindi ricordiamoci di prenderci l'utente autentficato nel _controller_ e passarlo alle funzioni degli strati inferiori.  



## ```@RestControllerAdvice```
Per la gestione delle eccezioni usare la classe ```GlobalExceptionHandler``` etichettata ```@RestControllerAdvice```, che si trova nel package _exception_. In questa classe devono essere inseriti i metodi che devono essere eseguiti al sollevamento di una precisa eccezione: Spring si occuperà automaticamente di eseguire il metodo opportuno quando viene sollevata una certa eccezione.  
Ogni handler si occupa di generare una risposta HTTP con lo stato appropriato all'eccezione sollevata, prendendo il messaggio (o i messaggi) di errore "trasportati" dall'eccezione e inserendoli nel corpo della risposta HTTP.  
Come ultimo metodo ne è stato inserito uno che intercetta genericamente ```Exception```, quindi ogni eccezione sarà intercettata dal proprio handler o al più dall'handler globale, il quale, oltre a ritornare una risposta HTTP con codice di errore generico del server (500), stamperà in console il tipo di eccezione sollevata, consentendo di scrivere un handler, qualora si voglia, per tale eccezione.  

### (NON) utilizzo dei blocchi ```try-catch```
Con l'utilizzo del ```@RestControllerAdvice``` NON serve inserire il codice del _controller_ all'interno di blocchi ```try-catch``` permettendo di rendere il codice MOLTO più pulito e leggibile.  
**Nonostante ciò**, quando vogliamo sollevare un'eccezione manualmente (se riscontriamo un errore in qualsiasi package) basta lanciare l'eccezione che vogliamo con una ```throw```, inserendo un messaggio di errore appropriato. Questa verrà intercettata dall'apposito handler della classe ```GlobalExceptionHandler```.  
**ATTENZIONE:** scrivere _in inglese_ i messaggi di errore all'interno delle ```throw```.

### ```@Hidden```
Il ```@GlobalExceptionHandler```, oltre a essere etichettato con ```@RestcontrollerAdvice``` per indicare a Spring di cosa si tratta, va OBBLIGATORIAMENTE etichettato anche con ```@Hidden```: tale annotazione indica allo Swagger di non generare la documentazione API per la classe annotata.  
Il problema è che lo Swagger interpreta una classe annotata con ```@RestControllerAdvice``` come contenente endpoint di API: quando va ad analizzare il codice per cercare gli endpoint da mappare, però, non trova nulla e ciò porta ad un errore di caricamento delle API quando si apre lo Swagger dal browser, senza possibilità di testarle.  



## ECCEZIONI da lanciare
**N.B.:** le eccezioni etichettate con "---" sono quelle create da noi e memorizzate nel package _exception_.
* ```---BadRequestException``` se i campi passati nelle richieste HTTP (sia come path variable, che come parametro, che nel body) non rispettano i pattern imposti.
* ```---BadRequestException``` se manca un campo (== null) necessario per andare avanti nella gestione della richiesta HTTP.
* ```---ResourceNotFoundException``` se la risorsa (o le risorse) ricercata in base ad un qualsiasi campo non esiste.
* ```---ResourceAlreadyExistsException``` se la risorsa che sto creando fa conflitto con un'altra risorsa già esistente su un campo che deve essere unique nel database.
* ```---DebugException``` se abbiamo bisogno di lanciare un eccezione a scopi di debug in un qualsiasi punto del codice (non accetta il passaggio di messaggi: stampa in console un messaggio preimpostato).
* ```IllegalArgumentException``` da usare solamente nelle lambda function passate alla .map(): in queste funzioni non si può lanciare ```BadRequestException``` in quanto è una checked exception e non c'è modo di inserire ```throws BadRequestException``` nella firma della funzione (in quanto le lambda function banalmente non hanno una firma).
* ```AccessDeniedException``` se l'utente non ha le autorizzazioni necessarie per accedere ad una certa API.
* ```AccessDeniedException``` se l'utente non possiede la risorsa con l'id richiesto (ovviamente per operazioni di modifica/eliminazione della risorsa).

--> Inseritene altre se ne usate.


## Annotazioni per CONTROLLI sugli INPUT
Invece di fare un miliardo di ```if``` nel _controller_ e nel _service_, utilizzare le annotazioni per i controlli sui valori passati nel path o nel body delle richieste HTTP che arrivano al server.  
Le annotazioni da usare fanno parte del package _jakarta_ e vanno inserito prima della dichiarazione di una variabile.  
Possono essere usate sia sui campi di una classe sia direttamente sugli argomenti dei metodi del _controller_.  
Ogni annotazione di controllo dei parametri supporta l'inserimento di un messaggio di errore con la sintassi ```@Annotazione(message = "errorMessage")```. Questo messaggio è poi raggiungibile nei metodi del ```GlobalExceptionHandler``` con il metodo ```.getDefaultMessage()```. Purtroppo il metodo in questione non è direttamente raggiungibile da tutti i tipi di eccezione, ma in generale tutte lo ereditano; quindi, per alcune, bisogna prima passare da un altro metodo che ritorna un qualcosa su cui ```.getDefaultMessage()``` è richiamabile. Per sapere come fare per ogni tipo di eccezione, consultare la documentazione di ogni eccezione, e cercare un modo per arrivare a ```.getDefaultMessage()``` o a ```.getAllErrors()``` (quest'ultimo metodo dovrebbe ritornare una lista di oggetti, ognuno contenente un errore riscontrato dal server e su ognuno dei quali è sempre richiamabile il metodo ```.getDefaultMessage()```).  
Per capire meglio, controllare il codice del metodo ```handleHandlerMethodValidationException``` del ```GlobalExceptionHandler```, in cui viene intercettata l'eccezione di tipo ```HandlerMethodValidationException```).

### Eccezioni sollevate
Le seguenti annotazioni sollevano delle eccezioni preimpostate.  
In teoria, ho già inserito nel ```GlobalExceptionHandler``` dei metodi che dovrebbero intercettare tutte le eccezioni che possono essere sollevate da tali annotazioni, con una corretta gestione del messaggio di default inserito nell'annotazione stessa.  
Alle brutte, dovesse essere sollevata un'eccezione mai vista prima, verrà utilizzato l'handler per le eccezioni generiche, che ritornerà al client un messaggio di errore preimpostato (non quello inserito da noi nell'annotazione) e stamperà in console il tipo di eccezione sollevata.

### STATO delle risposte HTTP
A prescindere dall'annotazione e dall'eccezioni sollevata, ha comunque inviare al client una risposta HTTP con stato ```BAD_REQUEST```.  

### ```@Valid```
L'annotazione ```@Valid``` VA INSERITA OBBLIGATORIAMENTE su un parametro (nella firma di un metodo, tendenzialmente del _controller_) per attivare il controlli su tale parametro (altrimenti, anche se li inserisco, non verranno eseguiti da Spring).  
Tale annotazione, va anche inserita nella definizione di una classe per la **validazione nidificata** (validazione dei sottocampi di un campo che ne contiene altri).

### PRESENZA DI UN DATO
* ```@NotNull``` per verificare che un dato non sia ```null```.
* ```@NotBlank``` per verificare che un dato non sia ```null```, non sia ```""``` e non sia ```"     "``` (solo spazi bianchi).
**ATTENZIONE:** l'annotazione ```@NotBlank``` può essere utilizzata solamente sulle variabili di tipo ```String```.
**ATTENZIONE:** le suddette annotazioni sono le uniche due che vengono eseguite anche senza l'annotazione ```@Valid```.

### STRINGHE
* ```@Pattern(regex = "regexPattern")``` sue un campo di tipo ```String``` per verificare se rispetta un certo pattern.
* ```@Email``` su un campo di ```String``` per verificare in automatico che la stringa sia un indirizzo email valido.

### DATE
* ```@Past``` su un campo di tipo ```LocalDateTime``` (o simili) verifica che la data sia valida e sia precedente a quella odierna.
* ```@PastOrPresent``` ammette anche la data odierna, ma non date future.
* Esistono anche altre combinazioni: se vi servono, usate Google.



## DTO
Creare le classi DTO da utilizzare per prendere i parametri o il body delle richieste HTTP.  
NON utilizzare le classi del _model_ (quelle servono logicamente per "parlare" con il db).  
Inserire le annotazioni per i controlli (```@NotNull```, ```@NotBlank```, ```@Pattern```...) nelle classi del package _DTO_, **NON** nelle classi del _model_ (quelle, ripeto, servono logicamente per "parlare" con il db ed è logicamente sbagliato mettergli dei controlli sui parametri: devono essere lasciate "libere" per permettere al programmatore una gestione più libera del db).  
Inoltre, usando le classi DTO, possiamo creare delle classi "personalizzate" per ogni endpoint, in modo che prendano solamente i campi necessari in ogni parametro.  


