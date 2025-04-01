# LargeProject

Questo file contiene delle linee guida per la scrittura del codice del progetto, in modo da renderlo più uniforme possibile nelle parti scritte da persone diverse.  
Il contenuto di questo file dovrà essere eliminato prima della consegna.  

## ```@Autowired``` (ATTENZIONE, leggere tutto questo paragrafo)
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


## CONTROLLI: usare il seguente schema per organizzare i controlli che vengono fatti sugli input (cose passate nelle richieste HTTP sia come body che come parametri o path variable)
NEL CONTROLLER
* controlli sulla presenza dei dati (controllare che i dati obbligatori non siano null)
* controlli sul pattern degli input
* controlli sul ruolo degli utenti (controllare che un utente possa accedere all'API richiesta)

NEL SERVICE
* controlli sulla validità dell'id passato (ad es. controllare che non esista già un utente con l'username richiesto durante la creazione di un nuovo utente)
* controlli sull'integrità dei dati (ad es. controllare che esista l'utente con lo username richiesto, o la recensione con l'id passato come argomento)
* controlli sulla coerenza dei dati (ad es. controllare che la data di nascita sia passata)
* controlli sulla proprietà di una risorsa (controllare che l'utente che ha effettuato la richiesta, ovvero quello loggato, sia effettivamente il proprietario della risorsa che intende modificare/eliminare)

### Perché?
Secondo Geppetto: "Il controller dovrebbe occuparsi principalmente della gestione delle richieste HTTP e della serializzazione/deserializzazione dei dati. Aggiungere la logica di autorizzazione può appesantire il controller e violare il principio di responsabilità singola.".  

Sempre secondo Geppetto: "Il controller ha accesso diretto all'utente autenticato (tramite @Principal o @AuthenticationPrincipal) e all'ID della risorsa (@PathVariable). Questo rende la verifica relativamente semplice da implementare.". Quindi ricordiamoci di prenderci l'utente autentficato nel Controller e passarlo alle funzioni degli strati inferiori.  



## ```@PathVariable```
Usare ```@PathVariable``` per passare in una richiesta HTTP il valore del campo che identifica univocamente una risorsa all'interno della sua collection (ad es. l'_id per un vino o una recensione o l'username per un utente). L'importante è separare questi campi dal resto dei campi che contengono le informazioni, che possono essere inserite tranquillamente nel body della richiesta HTTP.



## 'try-catch' syntax
Quando si scrivono le funzioni del Controller, usare uno schema 'try-catch': inserire all'interno del blocco 'try' sia i controlli da fare nel Controller che le chiamate alle funzioni del Service.  
Sia nel controller che nel Service, quando si fanno i controlli per scovare eventuali errori, lanciare le 'throw' con il tipo corretto di eccezione (lista sotto).
Scrivere i blocchi 'catch' appropriati per ogni eccezione che lanciamo, inserendo in ogni blocco il messaggio appropriato.



## ECCEZIONI da lanciare
* ```BadRequestException``` (nel Controller) se i campi passati nelle richieste HTTP (sia come path variable, che come parametro, che nel body) non rispettano i pattern imposti.
* ```BadRequestException``` (nel Controller) se manca un campo (== null) necessario per andare avanti nella gestione della richiesta HTTP.
* ```IllegalArgumentException``` (nel Service) se i campi passati come argomenti delle funzioni non sono validi (ad es. la data di nascita è nel futuro o altri controlli).
* ```ResourceNotFoundException``` se la risorsa con l'id richiesto non esiste (creata da noi, attualmente nel package "exception").
* ```AccessDeniedException``` se l'utente non ha le autorizzazioni necessarie per accedere ad una certa API.
* ```AccessDeniedException``` se l'utente non possiede la risorsa con l'id richiesto (ovviamente per operazioni di modifica/eliminazione della risorsa).
* ```ConflictException``` se si tenta di creare una risorsa con un campo unique già presente nel database.

### !!!!!! ATTENZIONE !!!!!!
Se si usano le annotazioni ```@Pattern```, ```@Email```, ```@NotBlank```... del package _jakarta_ per la validazione degli input (consigliato per evitare codice boilerplate) bisogna intercettare nelle catch un'eccezione di tipo ```ConstraintViolationException``` (le annotazioni sollevano appunto un'eccezione di questo tipo).  
Nella ```ResponseEntity``` dobbiamo comunque inserire lo stato ```BAD_REQUEST```.  
  
Inserirne altre se le usate, ora non me ne vengono in mente altre.



## Formato scrittura risposte
Usare ```ResponseEntity.status(HttpStatus.STATO).body("Messaggio")``` come sintassi per il settaggio della risposta HTTP.  
NON le funzioni ```.ok()```, ```.created()```, ```.badRequest()```, ...



## Messaggi di errore
Scrivere i messaggi di errore all'interno delle 'throw' in inglese.
