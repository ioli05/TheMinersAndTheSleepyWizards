							CommunicationChannel.java
							*************************
							
Am folosit 2 cozi de tip BlockingQueue, una in care vrajitorii scriu si minerii 
citesc (wizardchannel) si una in care minerii scriu si din care vrajitorii citesc 
(minerchannel).

putMessageMinerChannel(Message message)
=======================================
Am pus o instructiune de add in coada minerchannel intr-un bloc synchronized la 
instanta curenta de CommunicationChannel, pentru a ma asigura ca doar un miner 
poate scrie la un moment dat in coada (vreau sa ma asigur ca nu exista un caz de 
genul: 2 mineri scriu in acelasi timp si cumva un mesaj va fi suprascris de celalalt 
si astfel pierdut)

Message getMessageMinerChannel()
================================
Am pus o bucla care cicleaza in gol cat timp coada minerchannel este goala pentru 
ca altfel un pop pe acea coada ar returna null si mi-ar crea probleme (cu alte cuvinte 
pun threadurile Wizard care vor sa citeasca mesaje de la mineri, sa astepte pana cand 
exista cel putin un mesaj de citit). Apoi trebuie sa ma asigur ca la orice moment de timp, 
doar un vrajitor citeste un mesaj de pe canal si astfel pun codul de obtinere a unui 
mesaj din coada, intr-un bloc synchronized la instanta curenta de CommunicationChannel si 
apoi returnez mesajul.

putMessageWizardChannel(Message message)
========================================
Am pus o instructiune de add pe coada wizardchannel, pentru a ma asigura ca doar un thread 
de Wizard acceseaza acea coada.

Message getMessageWizardChannel()
=================================
Am pus o bucla de asteptare cat timp coada wizardchannel este goala si apoi ma asigur ca 
doar un miner ia la un moment dat un mesaj din aceasta coada (am pus totul intr-un bloc 
synchronized) si verific daca camera curenta este -1 (adica daca este o camera de start) si 
in caz afirmativ modific aceasta valoare la 0 pentru ca cu -1 mi-ar fi dat o exceptie de 
tip IndexOutOfBounds in Wizard.java si mi s-ar fi inchis acel thread.




									Miner.java
							*************************
Intr-o bucla care ruleaza la infinit si pe care o opresc doar in cazul in care primesc un mesaj 
de EXIT, pun lock pe instanta de CommunicationChannel si citesc primul mesaj de pe canal. Daca 
acest mesaj este de tip EXIT, atunci inchid bucla si astfel se termina executia threadului 
curent. Daca este de tip END, atunci nu mai citesc al doilea mesaj. Altfel, citesc al doilea 
mesaj si dau unlock pe instanta de CommunicationChannel. Prin lock si unlock, ma asigur ca doar 
o instanta (thread) de Miner va citi 2 mesaje, iar celelalte thread-uri vor astepta.
Dupa acel unlock si daca nu a intervenit o exceptie intre timp, calculez hash-ul si formez 
mesajul pe care apoi il pun in minerchannel.