### LargeProject

## @Autowired
L'annotazione @Autowired è usata in tutti i progetti vecchi che ho visto e viene inserita anche da Geppetto ogni volta che scrive una funzione. Non è estremamente necessaria ma fortemente consigliata.  
In pratica, quando in una classe (ad es. EntityService) etichetto il campo "private EntityRepository entityRepository" con @Autowired, sto diecendo a Spring che la classe "EntityService" dipende da EntityRepository e inizializza la classe "EntityRepository" e l'istanza "entityRepository" prima di "EntityService". Praticamente genera una specie di dipendenza a catena gestita da SpringBoot in cui ogni classe dipende da quella di livello inferiore.  
# Come usarlo
Il concetto è lo stesso spiegato, ma invece che etichettare in una classe direttamente i campi della classe "di livello inferiore", è consigliato scrivere il costruttore della classe in cui sono e etichettare quello con @Autowired, per vari motivi che non ho voglia di scrivere ma sono facilmente reperibili sul Veb.  

Scrivere quindi una cosa come sotto.  
```java
@Autowired  
public ReviewService(ReviewRepository reviewRepository) {  
    this.reviewRepository = reviewRepository;  
}
```

## Usare il seguente schema per organizzare i controlli che vengono fatti sugli input (cose passate nelle richieste HTTP sia come body che come parametri o path variable)
NEL CONTROLLER
* controlli sulla presenza dei dati (controllare che i dati obbligatori non siano null)
* controlli sul pattern degli input
* controlli sul ruolo degli utenti (controllare che un utente possa accedere all'API richiesta)

NEL SERVICE
* controlli sulla validità dell'id passato (ad es. controllare che non esista già un utente con l'username richiesto durante la creazione di un nuovo utente)
* controlli sull'integrità dei dati (ad es. controllare che esista l'utente con lo username richiesto, o la recensione con l'id passato come argomento)
* controlli sulla coerenza dei dati (ad es. controllare che la data di nascita sia passata)
* controlli sulla proprietà di una risorsa (controllare che l'utente che ha effettuato la richiesta, ovvero quello loggato, sia effettivamente il proprietario della risorsa che intende modificare/eliminare)

# Perché?
Secondo Geppetto: "Il controller dovrebbe occuparsi principalmente della gestione delle richieste HTTP e della serializzazione/deserializzazione dei dati. Aggiungere la logica di autorizzazione può appesantire il controller e violare il principio di responsabilità singola.".  

Sempre secondo Geppetto: "Il controller ha accesso diretto all'utente autenticato (tramite @Principal o @AuthenticationPrincipal) e all'ID della risorsa (@PathVariable). Questo rende la verifica relativamente semplice da implementare.". Quindi ricordiamoci di prenderci l'utente autentficato nel Controller e passarlo alle funzioni degli strati inferiori.  


## @PathVariable
Usare @PathVariable per passare in una richiesta HTTP il valore del campo che identifica univocamente una risorsa all'interno della sua collection (ad es. l'_id per un vino o una recensione o l'username per un utente). L'importante è separare questi campi dal resto dei campi che contengono le informazioni, che possono essere inserite tranquillamente nel body della richiesta HTTP.


## 'try-catch' syntax
Quando si scrivono le funzioni del Controller, usare uno schema 'try-catch': inserire all'interno del blocco 'try' sia i controlli da fare nel Controller che le chiamate alle funzioni del Service.  
Sia nel controller che nel Service, quando si fanno i controlli per scovare eventuali errori, lanciare le 'throw' con il tipo corretto di eccezione (lista sotto).
Scrivere i blocchi 'catch' appropriati per ogni eccezione che lanciamo, inserendo in ogni blocco il messaggio appropriato.


## ECCEZIONI da lanciare
* _BadRequestException_ (nel Controller) se i campi passati nelle richieste HTTP (sia come path variable, che come parametro, che nel body) non rispettano i pattern imposti
* _BadRequestException_ (nel Controller) se manca un campo (== null) necessario per andare avanti nella gestione della richiesta HTTP
* _IllegalArgumentException_ (nel Service) se i campi passati come argomenti delle funzioni non sono validi (ad es. la data di nascita è nel futuro o altri controlli)
* _ResourceNotFoundException_ se la risorsa con l'id richiesto non esiste
* _AccessDeniedException_ se l'utente non ha le autorizzazioni necessarie per accedere ad una certa API
* _AccessDeniedException_ se l'utente non possiede la risorsa con l'id richiesto (ovviamente per operazioni di modifica/eliminazione della risorsa)

Inserirne altre se le usate, ora non me ne vengono in mente altre.
